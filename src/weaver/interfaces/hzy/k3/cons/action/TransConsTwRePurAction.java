package weaver.interfaces.hzy.k3.cons.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.cons.service.TransConsTwRePurService;
import weaver.interfaces.hzy.k3.service.K3Service;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class TransConsTwRePurAction extends BaseBean implements Action {
    @Override
    public String execute(RequestInfo requestInfo) {


        writeLog("��ʼִ��TransConsHkRePurAction");

        K3Service k3Service = new K3Service();

        TransConsTwRePurService service = new TransConsTwRePurService();

        String requestid = requestInfo.getRequestid();

        //��ȡ����������ϸ������
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        writeLog("mainData="+mainData);
        String lcbh = mainData.get("lcbh");
        if(lcbh != null){
            service.putHkRePur(requestid);
        }

        return SUCCESS;
    }
}
