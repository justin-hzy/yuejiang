package weaver.interfaces.hzy.vn.k3.refund.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.vn.k3.refund.service.PutRefundVnOrderService;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class PutRefundVnOrderAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {
        PutRefundVnOrderService service = new PutRefundVnOrderService();
        writeLog("开始执行PutRefundVnOrderAction");
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
