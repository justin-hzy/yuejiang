package weaver.interfaces.hzy.tha.k3.cons.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.tha.k3.cons.service.PutConsRefundThaOrderService;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class PutConsRefundThaOrderAction extends BaseBean implements Action {
    @Override
    public String execute(RequestInfo requestInfo) {
        PutConsRefundThaOrderService service = new PutConsRefundThaOrderService();
        writeLog("��ʼִ��PutConsRefundThaOrderAction");

        String requestid = requestInfo.getRequestid();

        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        writeLog("mainData="+mainData);

        //���̱��
        String processCode = mainData.get("lcbh");

        if(processCode != null){
            //�������ڼ����½�
            service.putRefund(requestid);
        }

        writeLog("PutConsRefundThaOrderActionִ�н���");
        return SUCCESS;
    }
}
