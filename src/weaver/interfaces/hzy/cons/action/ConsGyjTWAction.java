package weaver.interfaces.hzy.cons.action;

import cn.hutool.core.collection.CollUtil;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.tx.util.WorkflowUtil;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsGyjTWAction extends BaseBean implements Action {


    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("开始执行 ConsGyjTWAction");

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        List<Map<String, String>> detailDatas5 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 5);

        writeLog("月结寄售明细5数据="+detailDatas5.toString());

        if (detailDatas5.size()>0){
            int id = requestInfo.getRequestManager().getBillid();

            writeLog("id="+id);

            RecordSet dt5Rs = new RecordSet();

            String querySql = "select sku ,quantity from formtable_main_238_dt5 where mainid = ?";

            dt5Rs.executeQuery(querySql,id);

            List<Map<String, String>> dt5MapList = new ArrayList<>();


            while (dt5Rs.next()){

                Map<String, String> dtSum = new HashMap<>();
                String sku = dt5Rs.getString("sku");
                String quantity = dt5Rs.getString("quantity");

                dtSum.put("tm", sku);
                dtSum.put("sl", quantity);

                dt5MapList.add(dtSum);
            }

            writeLog("dt5MapList="+dt5MapList.toString());


            if(CollUtil.isNotEmpty(dt5MapList)){
                Map<String,String> mainTableData = new HashMap<>();

                Map<String, List<Map<String, String>>> twDetail = new HashMap<>();

                String kh = mainData.get("kh");
                String bb  = "PRE005";
                String fhdc = mainData.get("fhdcxs");
                String shdc = mainData.get("shdcxs");
                String lcbh = mainData.get("lcbh");
                String lclj = mainData.get("lclj");

                String ddrq = mainData.get("ddrq");

                String requestid = requestInfo.getRequestid();

                mainTableData.put("kh",kh);
                //单据日期为入库日期
                String fhrq = ddrq;

                mainTableData.put("chrq",fhrq);

                mainTableData.put("djrq",ddrq);

                mainTableData.put("shdc",shdc);

                mainTableData.put("lcbh","GYJ_TW_"+lcbh);

                mainTableData.put("zlclj",lclj);


                mainTableData.put("bb",bb);

                mainTableData.put("fhdc",fhdc);

                mainTableData.put("zlcqqid",requestid);

                mainTableData.put("ydh",lcbh);

                writeLog("mainTableData="+mainTableData.toString());

                twDetail.put("1",dt5MapList);

                writeLog("twDetail="+twDetail.toString());

                WorkflowUtil workflowUtil = new WorkflowUtil();
                int result = workflowUtil.creatRequest("1","165","广悦进_台湾_寄售发货_金蝶"+"（子流程）",mainTableData,twDetail,"1");
                writeLog("触发成功的子流程请求id：" + result);
            }
            return SUCCESS;
        }else {
            return FAILURE_AND_CONTINUE;
        }

    }
}
