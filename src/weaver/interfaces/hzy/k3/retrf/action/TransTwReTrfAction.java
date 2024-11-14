package weaver.interfaces.hzy.k3.retrf.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.service.K3Service;
import weaver.interfaces.hzy.k3.trf.service.TransTwTrfService;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class TransTwReTrfAction extends BaseBean implements Action {
    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("��ʼִ��TransTwReTrfAction");

        TransTwTrfService service = new TransTwTrfService();


        String requestid = requestInfo.getRequestid();

        //��ȡ����������ϸ������
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        writeLog("mainData="+mainData);
        String lcbh = mainData.get("lcbh");
        if (lcbh != null){
            service.putTrf(requestid,mainData);
        }
        return SUCCESS;
    }

}
