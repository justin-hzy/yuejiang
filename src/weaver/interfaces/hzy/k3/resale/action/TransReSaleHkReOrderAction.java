package weaver.interfaces.hzy.k3.resale.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.resale.service.TransReSaleHkReOrderService;
import weaver.interfaces.hzy.k3.resale.service.TransReSaleTwReOrderService;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class TransReSaleHkReOrderAction extends BaseBean implements Action {
    @Override
    public String execute(RequestInfo requestInfo) {
        writeLog("��ʼִ��TransReSaleHkReOrderAction");

        TransReSaleHkReOrderService service = new TransReSaleHkReOrderService();

        String requestid = requestInfo.getRequestid();

        //��ȡ����������ϸ������
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        writeLog("mainData="+mainData);
        String lcbh = mainData.get("lcbh");

        if(lcbh != null){
            service.putHkReSale(requestid);
        }

        return SUCCESS;
    }
}
