package weaver.interfaces.hzy.k3.resale.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.resale.service.TransReSaleGyjTwRePurService;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class TransReSaleGyjTwRePurAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {
        writeLog("��ʼִ��TransReSaleGyjTwRePurAction");

        String requestid = requestInfo.getRequestid();

        TransReSaleGyjTwRePurService service = new TransReSaleGyjTwRePurService();


        //��ȡ����������ϸ������
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        writeLog("mainData="+mainData);
        String lcbh = mainData.get("lcbh");

        if(lcbh != null){
            service.putGYJTwRePur(requestid);
        }

        return SUCCESS;
    }
}
