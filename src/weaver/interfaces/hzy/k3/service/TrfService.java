package weaver.interfaces.hzy.k3.service;

import com.icbc.api.internal.apache.http.M;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.tx.util.WorkflowUtil;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;
import static weaver.interfaces.workflow.action.Action.SUCCESS;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrfService extends BaseBean {

    public String trf(RequestInfo requestInfo){


        RequestManager requestManager = requestInfo.getRequestManager();

        String requestid = requestInfo.getRequestid();

        /*获取主流程主表数据*/
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        InventoryService inventoryService = new InventoryService();

        String lcbh = mainData.get("lcbh");

        String dbxz1 = mainData.get("dbxz1");

        String getCkRqSql = "select dt2.fckrq from formtable_main_228 main inner join formtable_main_228_dt2 dt2 on main.id = dt2.mainid where main.requestid = ? limit 0,1";

        RecordSet rs = new RecordSet();

        rs.executeQuery(getCkRqSql,requestid);

        WorkflowUtil workflowUtil = new WorkflowUtil();

        String fckrq = "";

        Map<String,String> mainTableData = new HashMap<>();

        if(rs.next()){
            //发货日期
            fckrq = rs.getString("fckrq");
        }

        mainTableData.put("chrq",fckrq);
        mainTableData.put("zlclj","6");
        //原单号
        mainTableData.put("ydh",lcbh);

        if("5".equals(dbxz1)){
            //寄售调拨出库
            String fhdc3 = mainData.get("fhdc3");
            String shdc3 = mainData.get("shdc3");

            mainTableData.put("fhdc",fhdc3);
            mainTableData.put("shdc",shdc3);

            List<Map<String,String>> dtMapList = getTrfDt2(requestid);

            Map<String,List<Map<String,String>>> resMap = inventoryService.getTrfInventory(dtMapList,fhdc3,lcbh);

            writeLog("resMap="+resMap);

            if(resMap.containsKey("tw")){
                mainTableData.put("lcbh","TW_"+lcbh);
                Map<String, List<Map<String, String>>> detail = new HashMap<>();

                List<Map<String,String>> mapList = resMap.get("tw");

                if(mapList.size()>0){
                    detail.put("1",mapList);

                    int result = workflowUtil.creatRequest("1","165","HK_寄售出库_金蝶"+"（子流程）",mainTableData,detail,"1");//创建子流程
                    writeLog("触发成功的子流程请求id：" + result);
                }
            }

            if(resMap.containsKey("hk")){
                mainTableData.put("lcbh","HK_"+lcbh);
                Map<String, List<Map<String, String>>> detail = new HashMap<>();

                List<Map<String,String>> mapList = resMap.get("hk");

                if(mapList.size()>0){
                    detail.put("1",mapList);

                    int result = workflowUtil.creatRequest("1","165","HK_寄售出库_金蝶"+"（子流程）",mainTableData,detail,"1");//创建子流程
                    writeLog("触发成功的子流程请求id：" + result);
                }
            }
        }else if("3".equals(dbxz1)){
            //仓内调拨
            String fhdc2 = mainData.get("fhdc2");
            String shdc2 = mainData.get("shdc2");

            mainTableData.put("fhdc",fhdc2);
            mainTableData.put("shdc",shdc2);

            List<Map<String,String>> dtMapList = getTrfDt2(requestid);

            Map<String,List<Map<String,String>>> resMap = inventoryService.getTrfInventory(dtMapList,fhdc2,lcbh);

            writeLog("resMap="+resMap);

            if(resMap.containsKey("tw")){
                mainTableData.put("lcbh","TW_"+lcbh);
                Map<String, List<Map<String, String>>> detail = new HashMap<>();

                List<Map<String,String>> mapList = resMap.get("tw");

                if(mapList.size()>0){
                    detail.put("1",mapList);

                    int result = workflowUtil.creatRequest("1","165","TW_寄售出库_金蝶"+"（子流程）",mainTableData,detail,"1");//创建子流程
                    writeLog("触发成功的子流程请求id：" + result);
                }
            }

            if(resMap.containsKey("hk")){
                mainTableData.put("lcbh","HK_"+lcbh);
                Map<String, List<Map<String, String>>> detail = new HashMap<>();

                List<Map<String,String>> mapList = resMap.get("hk");

                if(mapList.size()>0){
                    detail.put("1",mapList);
                    int result = workflowUtil.creatRequest("1","165","TW_寄售出库_金蝶"+"（子流程）",mainTableData,detail,"1");//创建子流程
                    writeLog("触发成功的子流程请求id：" + result);
                }
            }

        }else if("7".equals(dbxz1)){
            //获取出库日期
            String chrqSql = "select chrq from formtable_main_228 where requestid = ?";

            RecordSet chrqRs = new RecordSet();

            chrqRs.executeQuery(chrqSql,requestid);

            String chrq = "";

            if(chrqRs.next()){
                chrq = chrqRs.getString("chrq");
            }

            mainTableData.put("chrq",chrq);

            //仓内调拨
            String fhdc2 = mainData.get("fhdc2");
            String shdc2 = mainData.get("shdc2");

            mainTableData.put("fhdc",fhdc2);
            mainTableData.put("shdc",shdc2);

            List<Map<String,String>> dtMapList = getTrfDt1(requestid);

            Map<String,List<Map<String,String>>> resMap = inventoryService.getTrfInventory(dtMapList,fhdc2,lcbh);

            writeLog("resMap="+resMap);

            if(resMap.containsKey("tw")){
                mainTableData.put("lcbh","TW_"+lcbh);
                Map<String, List<Map<String, String>>> detail = new HashMap<>();

                List<Map<String,String>> mapList = resMap.get("tw");

                if(mapList.size()>0){
                    detail.put("1",mapList);

                    int result = workflowUtil.creatRequest("1","165","TW_效期品移仓出库_金蝶"+"（子流程）",mainTableData,detail,"1");//创建子流程
                    writeLog("触发成功的子流程请求id：" + result);
                }
            }

            if(resMap.containsKey("hk")){
                mainTableData.put("lcbh","HK_"+lcbh);
                Map<String, List<Map<String, String>>> detail = new HashMap<>();

                List<Map<String,String>> mapList = resMap.get("hk");

                if(mapList.size()>0){
                    detail.put("1",mapList);
                    int result = workflowUtil.creatRequest("1","165","HK_效期品移仓_金蝶"+"（子流程）",mainTableData,detail,"1");//创建子流程
                    writeLog("触发成功的子流程请求id：" + result);
                }
            }
        }else if("6".equals(dbxz1)){
            //寄售调拨退货
            String fhdc3 = mainData.get("fhdc3");
            String shdc3 = mainData.get("shdc3");

            mainTableData.put("fhdc",fhdc3);
            mainTableData.put("shdc",shdc3);

            List<Map<String,String>> dtMapList = getTrfDt2(requestid);

            writeLog("dtMapList="+dtMapList.toString());

            Map<String,List<Map<String,String>>> resMap = matchTrfOrg(dtMapList);

            if(resMap.containsKey("tw")){
                mainTableData.put("lcbh","TW_"+lcbh);
                Map<String, List<Map<String, String>>> detail = new HashMap<>();

                List<Map<String,String>> mapList = resMap.get("tw");

                if(mapList.size()>0){
                    detail.put("1",mapList);

                    writeLog("mainTableData="+mainTableData.toString());
                    writeLog("detail="+detail.toString());
                    int result = workflowUtil.creatRequest("1","165","TW_寄售出库_金蝶"+"（子流程）",mainTableData,detail,"1");//创建子流程
                    writeLog("触发成功的子流程请求id：" + result);
                }
            }

            if(resMap.containsKey("hk")){
                mainTableData.put("lcbh","HK_"+lcbh);
                Map<String, List<Map<String, String>>> detail = new HashMap<>();

                List<Map<String,String>> mapList = resMap.get("hk");

                if(mapList.size()>0){
                    detail.put("1",mapList);
                    writeLog("mainTableData="+mainTableData.toString());
                    writeLog("detail="+detail.toString());
                    int result = workflowUtil.creatRequest("1","165","HK_寄售出库_金蝶"+"（子流程）",mainTableData,detail,"1");//创建子流程
                    writeLog("触发成功的子流程请求id：" + result);
                }
            }
        }

        return SUCCESS;
    }

    public void getTrfMainData(String sql,String requestid,String cNumber){

        writeLog("执行getTrfMainData");
        Map<String,String> mainTableData = new HashMap<>();

        RecordSet rs = new RecordSet();

        rs.executeQuery(sql,requestid);

        if(rs.next()){
            String fhdc = rs.getString("fhdc");
            String shdc = rs.getString("shdc");
            String fckrq = rs.getString("fckrq");

            mainTableData.put("fhdc",fhdc);
            mainTableData.put("shdc",shdc);
            mainTableData.put("fckrq",fckrq);
        }

        mainTableData.put("lcbh",cNumber);
    }

    public List<Map<String,String>> getTrfDt2(String requestid){

        RecordSet dtRs = new RecordSet();

        String sql = "select hpbh,sum(cksl) sl from formtable_main_228 main inner join formtable_main_228_dt2 dt2 on main.id = dt2.mainid where main.requestid = ? group by hpbh";

        writeLog("dt1Sql="+sql);

        dtRs.executeQuery(sql,requestid);

        List<Map<String,String>> trfDts = new ArrayList<>();

        while (dtRs.next()){
            Map<String,String> map = new HashMap<>();
            String hpbh = dtRs.getString("hpbh");
            String sl = dtRs.getString("sl");

            map.put("hpbh",hpbh);
            map.put("sl",sl);

            trfDts.add(map);
        }
        return trfDts;
    }


    public List<Map<String,String>> getTrfDt1(String requestid){
        RecordSet dt1Rs = new RecordSet();

        String sql = "select dt1.hpbh,sum(dt1.dbsl) sl from formtable_main_228 main inner join formtable_main_228_dt1 dt1 on main.id = dt1.mainid where main.requestid = ? group by hpbh";

        dt1Rs.executeQuery(sql,requestid);

        List<Map<String,String>> trfDts = new ArrayList<>();

        while (dt1Rs.next()){
            Map<String,String> map = new HashMap<>();
            String hpbh = dt1Rs.getString("hpbh");
            String sl = dt1Rs.getString("sl");

            map.put("hpbh",hpbh);
            map.put("sl",sl);

            trfDts.add(map);
        }
        return trfDts;
    }

    public Map<String,List<Map<String,String>>> matchTrfOrg(List<Map<String,String>> dtMapList){

        Map<String,List<Map<String,String>>> resMap = new HashMap<>();

        InventoryService inventoryService = new InventoryService();


        List<Map<String,String>> hkMapList = new ArrayList<>();

        List<Map<String,String>> twMapList = new ArrayList<>();

        for (Map<String,String> dtMap : dtMapList){

            String hpbh = dtMap.get("hpbh");
            String sl = dtMap.get("sl");
            String szzt = inventoryService.getOrg(hpbh);
            if("ZT021".equals(szzt)){

                HashMap<String,String> hkMap = new HashMap<>();
                hkMap.put("tm",hpbh);
                hkMap.put("sl",sl);
                hkMapList.add(hkMap);

            }else if("ZT026".equals(szzt)){
                HashMap<String,String> twMap = new HashMap<>();
                twMap.put("tm",hpbh);
                twMap.put("sl",sl);
                twMapList.add(twMap);
            }
        }

        resMap.put("tw",twMapList);

        resMap.put("hk",hkMapList);

        return resMap;
    }


}
