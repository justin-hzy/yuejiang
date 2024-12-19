package weaver.interfaces.hzy.k3.action;


import weaver.general.BaseBean;
import weaver.interfaces.hzy.cons.service.ConsService;
import weaver.interfaces.hzy.k3.service.ReSaleService;
import weaver.interfaces.hzy.k3.service.SaleService;
import weaver.interfaces.hzy.k3.service.TrfService;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;


public class K3SubflowAction extends BaseBean implements Action {

    private String type;


    private String meIp = getPropValue("fulun_api_config","meIp");

    private String getInventoryUrl = getPropValue("k3_api_config","getInventoryUrl");



    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("¿ªÊ¼Ö´ÐÐK3SubflowAction£¡");

        SaleService saleService = new SaleService();

        ReSaleService reSaleService = new ReSaleService();

        ConsService consService = new ConsService();

        TrfService trfService = new TrfService();

        if("sale".equals(type)){
            saleService.sale(requestInfo);
        }else if("resale".equals(type)){
            reSaleService.reSale(requestInfo);
        }else if("trf".equals(type)){
            trfService.trf(requestInfo);
        }else if("cons".equals(type)){
            consService.cons(requestInfo);
        }
        return SUCCESS;

    }


}
