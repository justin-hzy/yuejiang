package weaver.interfaces.hzy.k3.action;

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

        List<Map<String, String>> detailDatas2 = new ArrayList<>();

        detailDatas1 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);

        detailDatas2 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 2);

        writeLog("mainData=" + mainData.toString());

        writeLog("detailDatas=" + detailDatas1.toString());

        List<Map<String, String>> hkSales1 = new ArrayList<>();

        List<Map<String, String>> hkSales2 = new ArrayList<>();

        String fhdc = mainData.get("fhdc");
        String lcbh = mainData.get("lcbh");

        if (detailDatas1.size() > 0) {

            List<String> skus = new ArrayList<>();

            //正品 寄售销售 查询字段为 sku、销售数量、含税单价
            //String sql1 = "select main.id,dt1.hptxm tm,dt1.ddje xsj,sum(dt1.fhl) sl from formtable_main_226 main inner join formtable_main_226_dt1 dt1 on main.id = dt1.mainid where dt1.fhl is not null and main.requestId = ? group by main.id, dt1.hptxm, dt1.ddje";


            String sql1 = "select main.id,dt1.hptxm tm,sum(dt1.fhl) sl from formtable_main_272 main inner join formtable_main_272_dt1 dt1 on main.id = dt1.mainid where dt1.fhl is not null and main.requestId = ? group by main.id, dt1.hptxm";

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

                skus.add(tm);
                dtSums.add(dtSum);
            }

            //收集正品sku
            if (detailDatas1.size() > 0) {
                for (Map<String, String> detailData : detailDatas1) {
                    String hptxm = detailData.get("hptxm");
                    skus.add(hptxm);
                }
            }

            //收集样品sku

            String respStr = inventoryService.getBatchInventory(skus, fhdc);

            writeLog("respStr=" + respStr);


            List<Map<String, String>> k3InvList = inventoryService.anlysBatIn(respStr);

            writeLog("k3InvList=" + k3InvList);

            hkSales1 = compareInv(k3InvList, dtSums, inventoryService, lcbh);

        }

        //样品品 寄售销售 查询字段为 sku、销售数量、含税单价

        if(detailDatas2.size()>0){
            List<String> skus = new ArrayList<>();

            //正品 寄售销售 查询字段为 sku、销售数量、含税单价
            String sql1 = "select main.id,dt2.hptxm tm,sum(dt2.fhsl )sl from formtable_main_272 main inner join formtable_main_272_dt2 dt2 on main.id = dt2.mainid where main.requestId = ? and fhsl is not null group by main.id,dt2.hptxm";

            RecordSet dtRs = new RecordSet();

            dtRs.executeQuery(sql1, requestid);

            List<Map<String, String>> dtSums = new ArrayList<>();

            while (dtRs.next()) {
                Map<String, String> dtSum = new HashMap<>();
                String id  = dtRs.getString("id");
                String tm = dtRs.getString("tm");
                String sl = dtRs.getString("sl");
                //含税单价
                String xsj = dtRs.getString("xsj");

                dtSum.put("id",id);
                dtSum.put("tm", tm);
                dtSum.put("sl", sl);
                dtSum.put("xsj", xsj);

                skus.add(tm);
                dtSums.add(dtSum);
            }

            //收集正品sku
            if (detailDatas1.size() > 0) {
                for (Map<String, String> detailData : detailDatas1) {
                    String hptxm = detailData.get("hptxm");
                    skus.add(hptxm);
                }
            }

            //收集样品sku

            String respStr = inventoryService.getBatchInventory(skus, fhdc);

            writeLog("respStr=" + respStr);


            List<Map<String, String>> k3InvList = inventoryService.anlysBatIn(respStr);

            writeLog("k3InvList=" + k3InvList);

            hkSales2 = compareInv(k3InvList, dtSums, inventoryService, lcbh);

            //遍历hkSales2,hkSales2元素加入 hkSales1里面

            for (Map<String,String> hkSales : hkSales2){
                hkSales1.add(hkSales);
            }
        }


        writeLog("hkSales1=" + hkSales1.toString());

        String updateSql = "update formtable_main_272 set hk_status  = ? where requestid = ? ";

        RecordSet rs  = new RecordSet();



        if(hkSales1.size()>0){

            rs.executeUpdate(updateSql,"0",requestid);

            String id = getSaleId(requestid);

            String deleteSql = "DELETE FROM formtable_main_272_dt5 where mainid = ?";
            RecordSet deleteRs = new RecordSet();
            deleteRs.executeUpdate(deleteSql,id);

            for (Map<String,String> hkSale: hkSales1){

                String tm = hkSale.get("tm");
                String sl = hkSale.get("sl");

                String insertSql = "insert into formtable_main_272_dt5 (mainid,tm,sl) values ('"+id+"','"+tm+"','"+sl+"')";
                RecordSet insertRs = new RecordSet();
                insertRs.executeUpdate(insertSql);
            }
        }else if(hkSales1.size()==0){
            rs.executeUpdate(updateSql,"1",requestid);
        }
        return SUCCESS;
    }

    public List<Map<String,String>> compareInv(List<Map<String,String>> k3InvList,List<Map<String,String>> dtSums, InventoryService inventoryService,String lcbh){

        List<Map<String,String>> hkSales = new ArrayList<>();

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
        return  hkSales;
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
