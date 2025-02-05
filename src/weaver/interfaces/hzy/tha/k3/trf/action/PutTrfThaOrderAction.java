package weaver.interfaces.hzy.tha.k3.trf.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.tha.k3.trf.service.PutTrfThaOrderService;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class PutTrfThaOrderAction extends BaseBean implements Action {
    @Override
    public String execute(RequestInfo requestInfo) {
        PutTrfThaOrderService service = new PutTrfThaOrderService();
        writeLog("开始执行PutTrfThaOrderAction");
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
