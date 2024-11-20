package weaver.interfaces.hzy.k3.resale.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.resale.service.TransReSaleGyjRePurService;
import weaver.interfaces.hzy.k3.resale.service.TransReSaleGyjTwReOrderService;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class TransReSaleGyjTwReOrderAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {
        writeLog("��ʼִ��TransReSaleGyjTwReOrderAction");

        String requestid = requestInfo.getRequestid();

        TransReSaleGyjTwReOrderService service = new TransReSaleGyjTwReOrderService();

        //��ȡ����������ϸ������
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        writeLog("mainData="+mainData);
        String lcbh = mainData.get("lcbh");

        if(lcbh != null){
            service.putGyjTwReSale(requestid);
        }

        return SUCCESS;
    }
}
