package weaver.interfaces.hzy.k3.cons.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.cons.service.TransConsHkReOrderService;
import weaver.interfaces.hzy.k3.cons.service.TransConsTwRePurService;
import weaver.interfaces.hzy.k3.service.K3Service;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class TransConsHkReOrderAction extends BaseBean implements Action {
    @Override
    public String execute(RequestInfo requestInfo) {
        writeLog("开始执行TransConsHkReOrderAction");

        K3Service k3Service = new K3Service();

        TransConsHkReOrderService service = new TransConsHkReOrderService();

        String requestid = requestInfo.getRequestid();

        //获取流程主表，明细表数据
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        writeLog("mainData="+mainData);
        String lcbh = mainData.get("lcbh");
        if(lcbh != null){
            service.putHkReSale(requestid);
        }

        return SUCCESS;
    }
}
