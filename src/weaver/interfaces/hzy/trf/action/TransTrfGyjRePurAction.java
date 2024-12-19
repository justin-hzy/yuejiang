package weaver.interfaces.hzy.trf.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.trf.service.TransTrfGyjRePurService;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

public class TransTrfGyjRePurAction extends BaseBean implements Action {
    @Override
    public String execute(RequestInfo requestInfo) {
        writeLog("¿ªÊ¼Ö´ÐÐTransTrfGyjRePurAction");
        TransTrfGyjRePurService service = new TransTrfGyjRePurService();
        service.putGyjRePur(requestInfo);

        return SUCCESS;
    }
}
