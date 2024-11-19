package weaver.interfaces.hzy.trf.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.trf.service.TransTrfTwReSaleService;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

    public class TransTrfTwReSaleAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {
        writeLog("��ʼִ��TransTrfTwReSaleAction");
        TransTrfTwReSaleService service = new TransTrfTwReSaleService();

        service.putGyjTwReSale(requestInfo);
        return SUCCESS;
    }

}
