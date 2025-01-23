package weaver.interfaces.hzy.tha.k3.order.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.tha.k3.order.service.PutSaleThaOrderService;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class PutSaleThaOrderAction extends BaseBean implements Action {
    @Override
    public String execute(RequestInfo requestInfo) {
        PutSaleThaOrderService service = new PutSaleThaOrderService();
        writeLog("开始执行PutSaleThaOrderAction");
        String requestid = requestInfo.getRequestid();

        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        writeLog("mainData="+mainData);

        //流程编号
        String processCode = mainData.get("lcbh");

        if(processCode != null){
            service.putSale(requestid);
        }

        return SUCCESS;
    }
}
