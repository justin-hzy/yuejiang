package weaver.interfaces.hzy.tha.k3.refund.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.tha.k3.refund.service.PutSaleThaRefundService;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class PutSaleThaRefundAction extends BaseBean implements Action {
    @Override
    public String execute(RequestInfo requestInfo) {
        PutSaleThaRefundService service = new PutSaleThaRefundService();
        writeLog("开始执行PutSaleThaRefundAction");
        String requestid = requestInfo.getRequestid();

        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        writeLog("mainData="+mainData);

        //流程编号
        String processCode = mainData.get("lcbh");

        if(processCode != null){
            service.putRefund(requestid);
        }
        return SUCCESS;
    }
}
