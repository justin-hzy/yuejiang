package weaver.interfaces.hzy.k3.retrf.service;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.service.InventoryService;
import weaver.interfaces.tx.util.WorkflowUtil;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.workflow.request.RequestManager;
import weaver.soa.workflow.request.RequestInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static weaver.interfaces.workflow.action.Action.SUCCESS;


public class ReTrfService extends BaseBean {

    public String tranReTrf(RequestInfo requestInfo){
        RequestManager requestManager = requestInfo.getRequestManager();

        String requestid = requestInfo.getRequestid();

        /*获取主流程主表数据*/
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        InventoryService inventoryService = new InventoryService();
        //流程编号
        String lcbh = mainData.get("lcbh");
        //入库日期
        String rkrq = mainData.get("rkrq");

        WorkflowUtil workflowUtil = new WorkflowUtil();

        Map<String,String> mainTableData = new HashMap<>();

        mainTableData.put("chrq",rkrq);
        //主流程路径为,台湾寄售=6
        mainTableData.put("zlclj","6");

        //原单号
        mainTableData.put("ydh",lcbh);


        //收货店仓
        String fhdc = mainData.get("fhdc");
        //发货店仓
        String shdc = mainData.get("shdc");

        mainTableData.put("fhdc",fhdc);
        mainTableData.put("shdc",shdc);

        List<Map<String,String>> dtMapList = getTrfDt2(requestid);
        writeLog("dtMapList="+dtMapList.toString());

        Map<String,List<Map<String,String>>> resMap = matchTrfOrg(dtMapList);

        writeLog("resMap="+resMap.toString());

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

        return SUCCESS;
    }


    public List<Map<String,String>> getTrfDt2(String requestid){

        RecordSet dtRs = new RecordSet();

        String sql = "select hptxm hpbh,sum(rksl) sl from formtable_main_263 main inner join formtable_main_263_dt2 dt2 on main.id = dt2.mainid where main.requestid = ? group by hptxm";

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
