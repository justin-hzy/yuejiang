package weaver.interfaces.hzy.k3.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.service.K3Service;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class K3Action extends BaseBean implements Action {

    private String apiId;

    /*业务类型*/
    private String type;

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("开始执行hzy-K3Action！");

        K3Service k3Service = new K3Service();

        String requestid = requestInfo.getRequestid();

        //获取流程主表，明细表数据
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        writeLog("mainData="+mainData);
        String lcbh = mainData.get("lcbh");
        if("sale".equals(type)){
            if(lcbh != null){
                if(lcbh.contains("HK_")){
                    String code = k3Service.putSale(requestid,"HK");
                    writeLog("code="+code);
                    if("200".equals(code)){
                        code = k3Service.putPur(requestid,"TW");
                        writeLog("code="+code);
                        writeLog("流程号"+lcbh+"香港销售出库单据同步成功");
                        if("200".equals(code)){
                            // todo 调用ME接口实现提交节点功能
                            writeLog("流程号"+lcbh+"台湾采购单据同步成功");
                        }else {
                            writeLog("流程号"+lcbh+"台湾采购单据同步失败");
                            return FAILURE_AND_CONTINUE;
                        }
                    }else {
                        writeLog("流程号"+lcbh+"香港销售出库单据同步失败");
                        return FAILURE_AND_CONTINUE;
                    }
                }else if(lcbh.contains("TW_")) {
                    k3Service.putSale(requestid,"TW");
                }
            }
        }else if("trf".equals(type)){
            if(lcbh != null){
                if(lcbh.contains("HK_")){
                    k3Service.putTrf(requestid,mainData,"HK");
                }else if (lcbh.contains("TW_")){
                    k3Service.putTrf(requestid,mainData,"TW");
                }
            }
        }else if("cons".equals(type)){
            if(lcbh != null){
                if(lcbh.contains("HK_")){
                    String code = k3Service.putConsSale(requestid,"HK");
                    if("200".equals(code)){
                        k3Service.putConsPur(requestid,"TW");
                    }
                    //k3Service.putConsPur(requestid,"TW");
                }else if (lcbh.contains("TW_")){
                    k3Service.putConsSale(requestid,"TW");
                }
            }
        }else if("resale".equals(type)){
            if(lcbh != null){
                if(lcbh.contains("HK_")){
                    String code = k3Service.putReSale(requestid,"HK");
                    if("200".equals(code)){

                    }
                }else if(lcbh.contains("TW_")){
                    k3Service.putReSale(requestid,"TW");
                }

            }
        }else if("recons".equals(type)){
            if(lcbh != null){
                if(lcbh.contains("HK_")){
                    String code = k3Service.putRePur(requestid,"HK");
                    if("200".equals(code)){
                        k3Service.putReSale(requestid,"HK");
                    }
                }else if(lcbh.contains("TW_")){
                    k3Service.putReSale(requestid,"TW");
                }
            }
        }
        return SUCCESS;
    }

}
