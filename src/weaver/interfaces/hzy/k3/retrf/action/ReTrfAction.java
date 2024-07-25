package weaver.interfaces.hzy.k3.retrf.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.retrf.service.ReTrfService;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

public class ReTrfAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("Ö´ÐÐTrfAction");

        ReTrfService reTrfService = new ReTrfService();

        String result = reTrfService.tranReTrf(requestInfo);

        return result;
    }
}
