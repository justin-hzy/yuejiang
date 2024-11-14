package weaver.interfaces.hzy.retrf.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.retrf.service.ReTrfService;
import weaver.interfaces.hzy.retrf.service.ReTrfTwProcessService;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

public class ReTrfTwProcessAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("¿ªÊ¼Ö´ÐÐReTrfTwProcessAction");

        ReTrfTwProcessService reTrfService = new ReTrfTwProcessService();

        reTrfService.createReTrfTwProcess(requestInfo);

        return SUCCESS;
    }

}
