package weaver.interfaces.hzy.retrf.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.retrf.service.ReTrfHkProcessService;
import weaver.interfaces.hzy.retrf.service.ReTrfTwProcessService;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

public class ReTrfHkProcessAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("¿ªÊ¼Ö´ÐÐReTrfHkProcessAction");

        ReTrfHkProcessService reTrfService = new ReTrfHkProcessService();

        reTrfService.createReTrfHkProcess(requestInfo);


        return SUCCESS;
    }

}
