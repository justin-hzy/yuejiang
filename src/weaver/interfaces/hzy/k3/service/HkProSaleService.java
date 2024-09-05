package weaver.interfaces.hzy.k3.service;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.tx.util.WorkflowUtil;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.text.SimpleDateFormat;
import java.util.*;

public class HkProSaleService extends BaseBean {

    public void getHkSaleDt(RequestInfo requestInfo){

        writeLog("开始执行getHkSaleDt");

        WorkflowUtil workflowUtil = new WorkflowUtil();

        String requestid = requestInfo.getRequestid();

        InventoryService inventoryService = new InventoryService();

        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        List<Map<String, String>> detailDatas = new ArrayList<>();

        List<Map<String, String>> detailDatas2 = new ArrayList<>();

        detailDatas = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);

        detailDatas2 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 2);

        writeLog("mainData="+mainData.toString());

        writeLog("detailData="+detailDatas.toString());

        writeLog("detailDatas2="+detailDatas2.toString());


        if(detailDatas.size()>0 || detailDatas2.size()>0){
            String fhdc = mainData.get("fhdc");
            String lcbh = mainData.get("lcbh");

            List<String> skus = new ArrayList<>();

            //寄售销售 查询字段为 sku、销售数量、含税单价
            String sql = "select dt3.tm tm,dt3.sl sl from formtable_main_272 main inner join formtable_main_272_dt3 dt3 on main.id = dt3.mainid where main.requestId = ?";

            RecordSet dtRs = new RecordSet();

            dtRs.executeQuery(sql,requestid);

            List<Map<String,String>> dtSums = new ArrayList<>();

            while (dtRs.next()){
                Map<String,String> dtSum = new HashMap<>();
                String tm = dtRs.getString("tm");
                String sl = dtRs.getString("sl");
                //含税单价
                //String xsj = dtRs.getString("xsj");

                dtSum.put("tm",tm);
                dtSum.put("sl",sl);
                //dtSum.put("xsj",xsj);

                skus.add(tm);
                dtSums.add(dtSum);
                //skus.add(tm);
            }

            /*String respStr = inventoryService.getBatchInventory(skus,fhdc);

            writeLog("respStr="+respStr);


            List<Map<String,String>> k3InvList = inventoryService.anlysBatIn(respStr);

            writeLog("k3InvList="+k3InvList);

            List<Map<String,String>> hkSales = compareInv(k3InvList,dtSums,inventoryService,lcbh);


            writeLog("hkSales="+hkSales.toString());*/

            if (dtSums.size()>0){
                writeLog("生成香港销售出库单");
                Map<String,String> mainTableData = new HashMap<>();

                Map<String, List<Map<String, String>>> twDetail = new HashMap<>();

                String kh = mainData.get("kh");
                String bb  = mainData.get("bb");

                mainTableData.put("kh",kh);
                //单据日期为入库日期
                String fhrq = "2026-06-12";
                if(detailDatas.size()>0){
                    fhrq = detailDatas.get(detailDatas.size()-1).get("fhrq");
                }else if(detailDatas2.size()>0){
                    fhrq = detailDatas2.get(detailDatas2.size()-1).get("fhrq");
                }

                mainTableData.put("chrq",fhrq);

                mainTableData.put("shdc",kh);

                mainTableData.put("lcbh","HK_"+lcbh);

                mainTableData.put("zlclj","3");

                mainTableData.put("bb",bb);

                mainTableData.put("fhdc",fhdc);

                mainTableData.put("zlcqqid",requestid);

                mainTableData.put("ydh",lcbh);

                writeLog("mainTableData="+mainTableData.toString());

                twDetail.put("1",dtSums);

                writeLog("twDetail="+twDetail.toString());


                int result = workflowUtil.creatRequest("1","165","HK_销售发货_金蝶"+"（子流程）",mainTableData,twDetail,"1");
                writeLog("触发成功的子流程请求id：" + result);

                /*lcbh = "HK_"+lcbh;

                String id = getId(requestid);

                String insertSql = "insert formtable_main_272_dt6 (mainid,jdzlcid,jdzlcbh,jdzlcsfgd) values ('"+id+"','"+result+"','"+lcbh+"','"+1+"')";

                RecordSet rs1 = new RecordSet();
                rs1.executeUpdate(insertSql);*/
            }
        }



    }

    public List<Map<String,String>> compareInv(List<Map<String,String>> k3InvList,List<Map<String,String>> dtSums, InventoryService inventoryService,String lcbh){

        List<Map<String,String>> hkSales = new ArrayList<>();

        for (Map<String,String> dtSum : dtSums) {

            String wlbm = dtSum.get("tm");

            String xssl = dtSum.get("sl");

            for (Map<String,String> k3Inv : k3InvList){
                String sku = k3Inv.get("sku");
                //writeLog("sku="+sku);

                if(sku.equals(wlbm)){

                    Map<String,String> hkSale = new HashMap<>();
                    String fBaseQty = k3Inv.get("fBaseQty");
//                    writeLog("fBaseQty="+fBaseQty);

                    if(Integer.compare(Integer.valueOf(fBaseQty),Integer.valueOf(xssl)) < 0){
                        writeLog("生成香港数据");

                        String szzt = inventoryService.getOrg(wlbm);


                        if("ZT026".equals(szzt)){
                            writeLog("发现库存不足");

                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String now = dateFormat.format(new Date());

                            now = "'"+now+"'";

                            StringTokenizer tokenizer = new StringTokenizer(now, " ");

                            String dateString = tokenizer.nextToken();
                            String timeString = tokenizer.nextToken();

                            dateString= "'"+dateString+"'";

                            timeString= "'"+timeString+"'";
                            lcbh = "'"+lcbh+"'";

                            sku = "'"+sku+"库存不足"+"'";


                            /*String insertError = "insert into dms_k3_error_log (billNo,message,createTime,time,date) values ("
                                    +lcbh+","+sku+","+now+","+timeString+","+dateString+")";*/

                            String insertError = "insert into dms_k3_error_log (billNo,message,createTime) values (" +lcbh+","+sku+","+now+")";

                            RecordSet insertRs = new RecordSet();

                            insertRs.executeUpdate(insertError);
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
        return  hkSales;
    }


    public String getId(String requestid){
        String getIdSql = "select id from formtable_main_272 where requestid = ?";

        RecordSet rs1 = new RecordSet();

        rs1.executeQuery(getIdSql,requestid);
        String id = "";
        if(rs1.next()){
            id = rs1.getString("id");
        }
        return id;
    }
}
