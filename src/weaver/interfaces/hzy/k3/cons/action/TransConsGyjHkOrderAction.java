package weaver.interfaces.hzy.k3.cons.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.cons.service.TransConsGyjHkOrderService;
import weaver.interfaces.hzy.k3.service.K3Service;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class TransConsGyjHkOrderAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("��ʼִ��TransConsGyjHkOrderAction");

        TransConsGyjHkOrderService service = new TransConsGyjHkOrderService();

        String requestid = requestInfo.getRequestid();

        //��ȡ����������ϸ������
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        writeLog("mainData="+mainData);
        String lcbh = mainData.get("lcbh");

        if(lcbh != null){
            service.putGyjHKConsSale(requestid);
        }

        return SUCCESS;
    }

}
