package weaver.interfaces.hzy.retrf.service;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.tx.util.WorkflowUtil;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReTrfHkProcessService extends BaseBean {


    public void createReTrfHkProcess(RequestInfo requestInfo){
        String requestid = requestInfo.getRequestid();

        /*获取主流程主表数据*/
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        //流程编号
        String lcbh = mainData.get("lcbh");
        //入库日期
        String rkrq = mainData.get("rkrq");

        WorkflowUtil workflowUtil = new WorkflowUtil();

        Map<String,String> mainTableData = new HashMap<>();

        mainTableData.put("chrq",rkrq);
        //主流程路径为,台湾调拨寄售退=10
        mainTableData.put("zlclj","10");

        //原单号
        mainTableData.put("ydh",lcbh);

        //收货店仓
        String fhdc = mainData.get("fhdc");
        //发货店仓
        String shdc = mainData.get("shdc");

        mainTableData.put("fhdc",fhdc);
        mainTableData.put("shdc",shdc);

        List<Map<String,String>> dtMapList = getTrfDt3(requestid);

        writeLog("dtMapList="+dtMapList.toString());


        mainTableData.put("lcbh","HK_"+lcbh);
        Map<String, List<Map<String, String>>> detail = new HashMap<>();
        detail.put("1",dtMapList);
        writeLog("mainTableData="+mainTableData.toString());
        writeLog("detail="+detail.toString());
        int result = workflowUtil.creatRequest("1","165","HK_调拨寄售退_金蝶"+"（子流程）",mainTableData,detail,"1");//创建子流程
        writeLog("触发成功的子流程请求id：" + result);

    }

    public List<Map<String,String>> getTrfDt3(String requestid){

        RecordSet dtRs = new RecordSet();

        String sql = "select sku hpbh,sum(retrf_qty) sl from formtable_main_263 main inner join formtable_main_263_dt3 dt3 on main.id = dt3.mainid where main.requestid = ? group by sku";

        writeLog("dt3Sql="+sql);

        dtRs.executeQuery(sql,requestid);

        List<Map<String,String>> trfDts = new ArrayList<>();

        while (dtRs.next()){
            Map<String,String> map = new HashMap<>();
            String hpbh = dtRs.getString("hpbh");
            String sl = dtRs.getString("sl");

            map.put("tm",hpbh);
            map.put("sl",sl);

            trfDts.add(map);
        }
        return trfDts;
    }
}
