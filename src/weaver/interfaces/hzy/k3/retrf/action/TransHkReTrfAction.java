package weaver.interfaces.hzy.k3.retrf.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.trf.service.TransHkTrfService;
import weaver.interfaces.hzy.k3.trf.service.TransTwTrfService;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class TransHkReTrfAction extends BaseBean implements Action {
    @Override
    public String execute(RequestInfo requestInfo) {
        writeLog("开始执行TransHkReTrfAction");

        TransHkTrfService service = new TransHkTrfService();

        String requestid = requestInfo.getRequestid();

        //获取流程主表，明细表数据
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        writeLog("mainData="+mainData);
        String lcbh = mainData.get("lcbh");
        if (lcbh != null){
            service.putTrf(requestid,mainData);
        }
        return SUCCESS;
    }
}
