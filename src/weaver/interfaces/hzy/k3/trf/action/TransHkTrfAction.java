package weaver.interfaces.hzy.k3.trf.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.trf.service.TransHkTrfService;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class TransHkTrfAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("开始执行TransHkTrfAction  ");

        String requestid = requestInfo.getRequestid();

        TransHkTrfService transHkTrfService = new TransHkTrfService();

        //获取流程主表，明细表数据
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        writeLog("mainData="+mainData);
        String lcbh = mainData.get("lcbh");

        if(lcbh != null){
            transHkTrfService.putTrf(requestid,mainData);
        }

        return SUCCESS;

    }

}
