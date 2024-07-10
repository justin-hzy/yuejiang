package weaver.interfaces.hzy.k3.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.service.HkProSaleService;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;


public class HKSaleSubflowAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {


        writeLog("¿ªÊ¼Ö´ÐÐHKSubflowAction£¡");

        HkProSaleService hkProSaleService = new HkProSaleService();

        hkProSaleService.getHkSaleDt(requestInfo);







        return SUCCESS;
    }



}
