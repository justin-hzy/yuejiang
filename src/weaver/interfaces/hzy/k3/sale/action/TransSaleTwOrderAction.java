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
        writeLog("��ʼִ��TransSaleTwOrderAction");

        TransSaleTwOrderService transSaleTwOrderService = new TransSaleTwOrderService();

        String requestid = requestInfo.getRequestid();

        Integer id = requestInfo.getRequestManager().getBillid();


        //��ȡ����������ϸ������
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        writeLog("mainData="+mainData);
        String lcbh = mainData.get("lcbh");

        if(lcbh != null){
            transSaleTwOrderService.putSale(requestid,id);
        }

        return SUCCESS;
    }
}
