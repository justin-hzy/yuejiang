package weaver.interfaces.hzy.k3.cons.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.cons.service.TransConsGyjHkOrderService;
import weaver.interfaces.hzy.k3.cons.service.TransConsGyjTwPurService;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class TransConsGyjTwPurAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {
        writeLog("开始执行TransConsGyjTwPurAction");

        TransConsGyjTwPurService service = new TransConsGyjTwPurService();

        String requestid = requestInfo.getRequestid();

        //获取流程主表，明细表数据
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        writeLog("mainData="+mainData);
        String lcbh = mainData.get("lcbh");

        if(lcbh != null){
            service.putGyjTwConsPur(requestid);
        }

        return SUCCESS;
    }
}
