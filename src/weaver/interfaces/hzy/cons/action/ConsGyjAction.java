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

public class ConsGyjAction extends BaseBean implements Action {


    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("开始执行ConsGyjAction");

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        writeLog("mainData="+mainData.toString());

        List<Map<String, String>> detailDatas1 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);

        writeLog("月结寄售明细1数据="+detailDatas1.toString());

        if (detailDatas1.size()>0){

            int id = requestInfo.getRequestManager().getBillid();

            writeLog("id="+id);

            RecordSet dt1Rs = new RecordSet();

            String querySql = "select wlbm tm ,xssl sl from formtable_main_238_dt1 where mainid = ?";

            dt1Rs.executeQuery(querySql,id);

            List<Map<String, String>> dt1MapList = new ArrayList<>();

            while (dt1Rs.next()){

                Map<String, String> dtSum = new HashMap<>();
                String tm = dt1Rs.getString("tm");
                String sl = dt1Rs.getString("sl");

                dtSum.put("tm", tm);
                dtSum.put("sl", sl);

                dt1MapList.add(dtSum);
            }

            writeLog("dt1MapList="+dt1MapList.toString());

            if(CollUtil.isNotEmpty(dt1MapList)){
                writeLog("生成广悦进寄售出库单");

                Map<String,String> mainTableData = new HashMap<>();

                Map<String, List<Map<String, String>>> gyjDetail = new HashMap<>();

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

                mainTableData.put("lcbh","GYJ_"+lcbh);

                mainTableData.put("zlclj",lclj);


                mainTableData.put("bb",bb);

                mainTableData.put("fhdc",fhdc);

                mainTableData.put("zlcqqid",requestid);

                mainTableData.put("ydh",lcbh);

                writeLog("mainTableData="+mainTableData.toString());

                gyjDetail.put("1",dt1MapList);

                writeLog("twDetail="+gyjDetail.toString());

                WorkflowUtil workflowUtil = new WorkflowUtil();
                int result = workflowUtil.creatRequest("1","165","广悦进_寄售发货_金蝶"+"（子流程）",mainTableData,gyjDetail,"1");
                writeLog("触发成功的子流程请求id：" + result);
            }
            return SUCCESS;
        }else {
            return FAILURE_AND_CONTINUE;
        }
    }
}
