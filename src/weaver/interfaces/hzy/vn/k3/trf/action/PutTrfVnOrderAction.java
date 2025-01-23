package weaver.interfaces.hzy.vn.k3.trf.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.vn.k3.refund.service.PutRefundVnOrderService;
import weaver.interfaces.hzy.vn.k3.trf.service.PutTrfVnOrderService;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class PutTrfVnOrderAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {

        PutTrfVnOrderService service = new PutTrfVnOrderService();
        writeLog("开始执行PutTrfVnOrderAction");
        String requestId = requestInfo.getRequestid();

        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        writeLog("mainData="+mainData);

        //流程编号
        String processCode = mainData.get("lcbh");

        if(processCode != null){
            service.putTrf(requestId,mainData);
        }

        return SUCCESS;
    }
}
