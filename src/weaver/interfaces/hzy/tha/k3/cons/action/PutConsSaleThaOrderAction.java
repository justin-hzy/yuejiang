package weaver.interfaces.hzy.tha.k3.cons.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.tha.k3.cons.service.PutConsSaleThaOrderService;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class PutConsSaleThaOrderAction extends BaseBean implements Action {
    @Override
    public String execute(RequestInfo requestInfo) {
        PutConsSaleThaOrderService service = new PutConsSaleThaOrderService();
        writeLog("��ʼִ��PutConsSaleThaOrderAction");
        String requestid = requestInfo.getRequestid();

        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        writeLog("mainData="+mainData);

        //���̱��
        String processCode = mainData.get("lcbh");

        if(processCode != null){
            //�������ڼ����½�
            service.putSale(requestid);
        }

        writeLog("PutConsSaleThaOrderActionִ�н���");
        return SUCCESS;
    }
}
