package weaver.interfaces.hzy.sale.action;

import cn.hutool.core.collection.CollUtil;
import com.icbc.api.internal.apache.http.impl.cookie.S;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.service.InventoryService;
import weaver.interfaces.tx.util.WorkflowUtil;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.text.SimpleDateFormat;
import java.util.*;


public class HKAction extends BaseBean implements Action {



    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("开始执行HKAction");

        WorkflowUtil workflowUtil = new WorkflowUtil();

        String requestid = requestInfo.getRequestid();

        InventoryService inventoryService = new InventoryService();

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        List<Map<String, String>> detailDatas1 = new ArrayList<>();

        detailDatas1 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);

        writeLog("mainData=" + mainData.toString());

        writeLog("detailDatas=" + detailDatas1.toString());

        Map<String,List<Map<String,String>>> respMap = new HashMap<>();

        List<Map<String, String>> hkSales1 = new ArrayList<>();

        List<Map<String,String>> twNotEnoughList = new ArrayList<>();

        String fhdc = mainData.get("fhdc");
        String lcbh = mainData.get("lcbh");

        if (detailDatas1.size() > 0) {

            List<String> skus = new ArrayList<>();

            //正品 寄售销售 查询字段为 sku、销售数量、含税单价
            //String sql1 = "select main.id,dt1.hptxm tm,dt1.ddje xsj,sum(dt1.fhl) sl from formtable_main_226 main inner join formtable_main_226_dt1 dt1 on main.id = dt1.mainid where dt1.fhl is not null and main.requestId = ? group by main.id, dt1.hptxm, dt1.ddje";


            String sql1 = "select main.id,dt1.tm tm,sum(dt1.fhl) sl from formtable_main_272 main inner join formtable_main_272_dt1 dt1 on main.id = dt1.mainid where dt1.fhl is not null and main.requestId = ? group by main.id, dt1.tm";

            RecordSet dtRs = new RecordSet();

            dtRs.executeQuery(sql1, requestid);

            List<Map<String, String>> dtSums = new ArrayList<>();

            while (dtRs.next()) {
                Map<String, String> dtSum = new HashMap<>();
                String id  = dtRs.getString("id");
                String tm = dtRs.getString("tm");
                String sl = dtRs.getString("sl");
                //含税单价
                //String xsj = dtRs.getString("xsj");

                dtSum.put("id",id);
                dtSum.put("tm", tm);
                dtSum.put("sl", sl);
                //dtSum.put("xsj", xsj);

                dtSums.add(dtSum);
            }

            //收集正品sku
            if (detailDatas1.size() > 0) {
                for (Map<String, String> detailData : detailDatas1) {
                    String hptxm = detailData.get("tm");
                    writeLog("hptxm="+hptxm);
                    skus.add(hptxm);
                }
            }
            writeLog("skus="+skus.toString());
            //收集样品sku

            String respStr = inventoryService.getBatchTwInventory(skus, fhdc);

            writeLog("respStr=" + respStr);


            List<Map<String, String>> k3InvList = inventoryService.anlysBatIn(respStr,skus);

            writeLog("k3InvList=" + k3InvList);

            respMap = compareInv(k3InvList, dtSums, inventoryService, lcbh);




            //hkSales1 = compareInv(k3InvList, dtSums, inventoryService, lcbh);


        }


        if (respMap.containsKey("twNotEnoughList")){
            twNotEnoughList = respMap.get("twNotEnoughList");
            if (twNotEnoughList.size()>0){
                String errorMessage = "";
                writeLog("twNotEnoughList=" + twNotEnoughList.toString());
                for (Map<String,String> twNotEnough : twNotEnoughList){
                    String sku = twNotEnough.get("tm");
                    String qty = twNotEnough.get("qty");

                    errorMessage = errorMessage+"sku = "+sku+",qty= -"+qty+";";
                }



                String updateSql = "update formtable_main_272 set hk_status  = ?,is_tw_enough = ?,error_message = ? where requestid = ? ";
                writeLog("updateSql="+updateSql);
                RecordSet rs  = new RecordSet();
                rs.executeUpdate(updateSql,null,"1",errorMessage,requestid);
            }
        }else {
            if (respMap.containsKey("hkSales")){
                hkSales1 = respMap.get("hkSales");

                writeLog("hkSales1=" + hkSales1.toString());

                String updateSql = "update formtable_main_272 set hk_status  = ?,is_tw_enough = ? where requestid = ? ";

                RecordSet rs  = new RecordSet();

                if(hkSales1.size()>0){
                    rs.executeUpdate(updateSql,"0","0",requestid);

                    String id = getSaleId(requestid);

                    String deleteSql = "DELETE FROM formtable_main_272_dt3 where mainid = ?";
                    RecordSet deleteRs = new RecordSet();
                    deleteRs.executeUpdate(deleteSql,id);

                    for (Map<String,String> hkSale: hkSales1){

                        String tm = hkSale.get("tm");
                        String sl = hkSale.get("sl");

                        String insertSql = "insert into formtable_main_272_dt3 (mainid,tm,sl) values ('"+id+"','"+tm+"','"+sl+"')";
                        RecordSet insertRs = new RecordSet();
                        insertRs.executeUpdate(insertSql);
                    }
                }
            }else if(hkSales1.size()==0){
                String updateSql = "update formtable_main_272 set hk_status  = ?,is_tw_enough = ? where requestid = ? ";
                RecordSet rs  = new RecordSet();
                rs.executeUpdate(updateSql,"1","0",requestid);
            }
        }
        return SUCCESS;
    }

    public Map<String,List<Map<String,String>>> compareInv(List<Map<String,String>> k3InvList,List<Map<String,String>> dtSums, InventoryService inventoryService,String lcbh){

        Map<String,List<Map<String,String>>> respMap = new HashMap<>();

        List<Map<String,String>> hkSales = new ArrayList<>();

        List<Map<String,String>> twNotEnoughList = new ArrayList<>();


        writeLog("dtSums="+dtSums.toString());

        for (Map<String,String> dtSum : dtSums) {

            String wlbm = dtSum.get("tm");

            String xssl = dtSum.get("sl");


            for (Map<String,String> k3Inv : k3InvList){
                String sku = k3Inv.get("sku");
                //writeLog("sku="+sku);

                if(sku.equals(wlbm)){

                    /*writeLog("金蝶-sku="+sku);
                    writeLog("dms-wlbm="+wlbm);*/


                    Map<String,String> hkSale = new HashMap<>();
                    Map<String,String> twNotEnough = new HashMap<>();
                    String fBaseQty = k3Inv.get("fBaseQty");
//                    writeLog("fBaseQty="+fBaseQty);


                    /*writeLog("fBaseQty="+fBaseQty);
                    writeLog("xssl="+xssl);*/

                    if(Integer.compare(Integer.valueOf(fBaseQty),Integer.valueOf(xssl)) < 0){
                        writeLog("生成香港数据");

                        String szzt = inventoryService.getOrg(wlbm);


                        if("ZT026".equals(szzt)){
                            writeLog("发现库存不足");

                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String now = dateFormat.format(new Date());

                            StringTokenizer tokenizer = new StringTokenizer(now, " ");

                            String dateString = tokenizer.nextToken();
                            String timeString = tokenizer.nextToken();

                            Integer qty = Integer.valueOf(xssl) - Integer.valueOf(fBaseQty);

                            twNotEnough.put("tm",sku);
                            twNotEnough.put("qty",String.valueOf(qty));

                            twNotEnoughList.add(twNotEnough);

                            sku = sku+"库存不足";

                            String insertError = "insert into dms_k3_error_log (billNo,message,createTime,date,time) values ('" +lcbh+"','"+sku+"','"+now+"','"+dateString+"','"+timeString+"')";

                            RecordSet insertRs = new RecordSet();

                            insertRs.executeUpdate(insertError);

                        }else {
                            if(Integer.valueOf(fBaseQty)<0){
                                writeLog(wlbm+"的台湾即时库存为负数");
                                Integer hkNumber = Integer.valueOf(xssl);
                                hkSale.put("tm",wlbm);
                                hkSale.put("sl",String.valueOf(hkNumber));
                                hkSales.add(hkSale);
                            }else {
                                Integer hkNumber = Integer.valueOf(xssl) - Integer.valueOf(fBaseQty) ;
                                hkSale.put("tm",wlbm);
                                hkSale.put("sl",String.valueOf(hkNumber));
                                hkSales.add(hkSale);
                            }
                        }
                    }
                }
            }
        }

        if(CollUtil.isNotEmpty(hkSales)){
            respMap.put("hkSales",hkSales);
        }

        if(CollUtil.isNotEmpty(twNotEnoughList)){
            respMap.put("twNotEnoughList",twNotEnoughList);
        }
        return  respMap;
    }


    public String getSaleId(String requestid){
        String sql = "select id from formtable_main_272 where requestid = ?";
        RecordSet recordSet = new RecordSet();
        recordSet.executeQuery(sql,requestid);
        String id = "";
        if (recordSet.next()){
            id = recordSet.getString("id");
        }
        return id;
    }



}
