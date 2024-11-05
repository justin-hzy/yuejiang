package weaver.interfaces.hzy.k3.resale.action;

import org.docx4j.wml.R;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.resale.service.TransReSaleTwReOrderService;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class TransReSaleTwReOrderAction extends BaseBean implements Action {
    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("开始执行TransReSaleTwReOrderAction");

        TransReSaleTwReOrderService transReSaleTwReOrderService = new TransReSaleTwReOrderService();

        String requestid = requestInfo.getRequestid();

        //获取流程主表，明细表数据
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        writeLog("mainData="+mainData);
        String lcbh = mainData.get("lcbh");

        if(lcbh != null){
            transReSaleTwReOrderService.putTwReSale(requestid);
        }

        return SUCCESS;
    }
}
