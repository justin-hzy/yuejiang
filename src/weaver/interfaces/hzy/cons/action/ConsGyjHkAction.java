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

public class ConsGyjHkAction extends BaseBean implements Action {
    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("开始执行ConsGyjHkAction");

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        List<Map<String, String>> detailDatas6 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 6);

        writeLog("月结寄售明细6数据="+detailDatas6.toString());

        if (detailDatas6.size()>0){
            int id = requestInfo.getRequestManager().getBillid();

            writeLog("id="+id);

            RecordSet dt6Rs = new RecordSet();

            String querySql = "select sku ,quantity from formtable_main_238_dt6 where mainid = ?";

            dt6Rs.executeQuery(querySql,id);

            List<Map<String, String>> dt6MapList = new ArrayList<>();

            while (dt6Rs.next()){

                Map<String, String> dtSum = new HashMap<>();
                String sku = dt6Rs.getString("sku");
                String quantity = dt6Rs.getString("quantity");

                dtSum.put("tm", sku);
                dtSum.put("sl", quantity);

                dt6MapList.add(dtSum);
            }

            writeLog("dt6MapList="+dt6MapList.toString());

            if(CollUtil.isNotEmpty(dt6MapList)){
                Map<String,String> mainTableData = new HashMap<>();

                Map<String, List<Map<String, String>>> hkDetail = new HashMap<>();

                String kh = mainData.get("kh");
                String bb  = "PRE007";
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

                mainTableData.put("lcbh","GYJHK_"+lcbh);

                mainTableData.put("zlclj",lclj);


                mainTableData.put("bb",bb);

                mainTableData.put("fhdc",fhdc);

                mainTableData.put("zlcqqid",requestid);

                mainTableData.put("ydh",lcbh);

                writeLog("mainTableData="+mainTableData.toString());

                hkDetail.put("1",dt6MapList);

                writeLog("twDetail="+hkDetail.toString());

                WorkflowUtil workflowUtil = new WorkflowUtil();
                int result = workflowUtil.creatRequest("1","165","广悦进_香港_寄售发货_金蝶"+"（子流程）",mainTableData,hkDetail,"1");
                writeLog("触发成功的子流程请求id：" + result);
            }
            return SUCCESS;
        }else {
            return FAILURE_AND_CONTINUE;
        }
    }
}
