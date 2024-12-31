package weaver.interfaces.hzy.mabang.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.mabang.service.TransMaBangSaleOrderService;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

public class TransMaBangSaleOrderAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {

        TransMaBangSaleOrderService service = new TransMaBangSaleOrderService();
        writeLog("Ö´ÐÐTransMaBangSaleOrderAction");
        String requestid = requestInfo.getRequestid();

        service.putMaBangSaleOrder(requestid);


        return SUCCESS;
    }

}
