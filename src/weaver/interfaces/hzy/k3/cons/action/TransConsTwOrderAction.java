package weaver.interfaces.hzy.k3.cons.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.cons.service.TransConsHkOrderService;
import weaver.interfaces.hzy.k3.cons.service.TransConsTwOrderService;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class TransConsTwOrderAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("��ʼִ��TransConsTwOrderAction");


        TransConsTwOrderService service = new TransConsTwOrderService();


        String requestid = requestInfo.getRequestid();

        //��ȡ����������ϸ������
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        writeLog("mainData="+mainData);
        String lcbh = mainData.get("lcbh");
        if(lcbh != null){
            service.putConsTwSale(requestid);
        }

        return SUCCESS;
    }
}
