package weaver.interfaces.hzy.cons.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.service.HKConsSubflowService;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

public class HKConsSubflowAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {
        writeLog("¿ªÊ¼Ö´ÐÐHKConsSubflowAction!");

        HKConsSubflowService service = new HKConsSubflowService();
        service.getHkConsSaleDt(requestInfo);

        return SUCCESS;
    }
}
