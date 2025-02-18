package weaver.interfaces.hzy.k3.sale.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.sale.service.TransSaleTwOrderService;
import weaver.interfaces.hzy.k3.service.K3Service;
import weaver.interfaces.hzy.sale.service.SaleService;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class TransSaleTwOrderAction extends BaseBean implements Action {
    @Override
    public String execute(RequestInfo requestInfo) {
        writeLog("开始执行TransSaleTwOrderAction");

        TransSaleTwOrderService transSaleTwOrderService = new TransSaleTwOrderService();

        String requestid = requestInfo.getRequestid();

        Integer id = requestInfo.getRequestManager().getBillid();


        //获取流程主表，明细表数据
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        writeLog("mainData="+mainData);
        String lcbh = mainData.get("lcbh");

        if(lcbh != null){
            transSaleTwOrderService.putSale(requestid,id);
        }

        return SUCCESS;
    }
}
