package weaver.interfaces.hzy.k3.pur.action;

import com.weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.pur.service.PurService;
import weaver.interfaces.hzy.k3.service.K3Service;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DailyNecAction extends BaseBean implements Action {

    private String apiId;

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("执行 DailyNecAction");

        PurService purService = new PurService();

        K3Service k3Service = new K3Service();

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        List<Map<String, String>> detailDatas1 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);

        writeLog("mainData=" + mainData.toString());

        writeLog("detailDatas1=" + detailDatas1.toString());

        //流程编码
        String lcbh = mainData.get("lcbh");

        //供应商
        String gys = mainData.get("gys");

        //发货仓
        String fhdc1 = mainData.get("fhdc1");

        //出库仓库,后续用系统字段替代
        String ckck = fhdc1;

        //入库仓库
        String rkck = mainData.get("rkck");

        //币别
        String bb = mainData.get("bb");
        //香港-台湾币别
        String hkBb = mainData.get("hk_bb");

        //子流程单据日期，暂时取预计进仓日
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String pushDate = today.format(formatter);


        if("1".equals(apiId)){
            String code = purService.tranDailyNecCnSale(lcbh,gys,pushDate,ckck,bb,detailDatas1,k3Service,"0");
            if("200".equals(code)){
                k3Service.addLog(lcbh,"200");
                writeLog("同步金蝶-日用品-销售出库单成功");
                return SUCCESS;
            }else {
                k3Service.addLog(lcbh,"500");
                writeLog("同步金蝶-日用品-销售出库单失败");
                return FAILURE_AND_CONTINUE;
            }
        }else if("2".equals(apiId)){
            String code = purService.tranHkPur_2(lcbh,gys,pushDate,rkck,bb,detailDatas1,k3Service,"0");
            if("200".equals(code)){
                k3Service.addLog(lcbh,"200");
                writeLog("同步金蝶-香港-采购单成功");
                return SUCCESS;
            }else {
                k3Service.addLog(lcbh,"500");
                writeLog("同步金蝶-香港-采购单库单失败");
                return FAILURE_AND_CONTINUE;
            }
        }else if("3".equals(apiId)){
            String code = purService.tranHkSale_2(lcbh,gys,pushDate,rkck,hkBb,detailDatas1,k3Service,"0");
            if("200".equals(code)){
                k3Service.addLog(lcbh,"200");
                writeLog("同步金蝶-香港-销售出库单成功");
                return SUCCESS;
            }else {
                k3Service.addLog(lcbh,"500");
                writeLog("同步金蝶-香港-销售出库单失败");
                return FAILURE_AND_CONTINUE;
            }
        }
        else {
            return SUCCESS;
        }
    }

}
