package weaver.interfaces.hzy.k3.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.service.assy.AssyService;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class AssyAction extends BaseBean implements Action {


    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("Ö´ÐÐ AssyAction");

        String requestid  = requestInfo.getRequestid();

        AssyService assyService = new AssyService();

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);








        return null;
    }
}
