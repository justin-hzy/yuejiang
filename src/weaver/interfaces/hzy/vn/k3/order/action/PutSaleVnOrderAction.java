package weaver.interfaces.hzy.vn.k3.order.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.vn.k3.order.service.PutSaleVnOrderService;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class PutSaleVnOrderAction extends BaseBean implements Action {
    @Override
    public String execute(RequestInfo requestInfo) {
        PutSaleVnOrderService service = new PutSaleVnOrderService();
        writeLog("开始执行PutSaleVnOrderAction");
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
