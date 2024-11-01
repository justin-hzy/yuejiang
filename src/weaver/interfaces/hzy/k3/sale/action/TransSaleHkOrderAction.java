package weaver.interfaces.hzy.k3.sale.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.sale.service.TransSaleHkOrderService;
import weaver.interfaces.hzy.k3.service.K3Service;
import weaver.interfaces.hzy.sale.service.SaleService;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class TransSaleHkOrderAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {

        TransSaleHkOrderService transSaleHkOrderService = new TransSaleHkOrderService();

        writeLog("开始执行TransSaleHkOrderAction");

        String requestid = requestInfo.getRequestid();

        //获取流程主表，明细表数据
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        writeLog("mainData="+mainData);
        String lcbh = mainData.get("lcbh");

        if(lcbh != null){
            transSaleHkOrderService.putSale(requestid);
        }

        return SUCCESS;
    }

}
