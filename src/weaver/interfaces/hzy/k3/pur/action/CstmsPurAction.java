package weaver.interfaces.hzy.k3.pur.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.service.K3Service;
import weaver.interfaces.hzy.k3.pur.service.PurService;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CstmsPurAction extends BaseBean implements Action {

    private String meIp = getPropValue("fulun_api_config","meIp");

    private String putPurUrl = getPropValue("k3_api_config","putPurUrl");

    /*采购海关生成香港采购,香港销出 */
    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("执行CstmsPurAction");

        PurService purService = new PurService();

        String requestid = requestInfo.getRequestid();

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        List<Map<String, String>> detailDatas1 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);

        writeLog("mainData=" + mainData.toString());

        writeLog("detailDatas1=" + detailDatas1.toString());

        //流程编码
        String lcbh = mainData.get("lcbh");

        //采购类型
        String cglx = mainData.get("cglx");

        //供应商
        String gys = mainData.get("gys");

        //入库仓库
        String rkck = mainData.get("rkck");

        //币别
        String bb = mainData.get("bb");

        //子流程单据日期，暂时取预计进仓日
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String pushDate = today.format(formatter);

        List<Map<String,String>> twList = new ArrayList<>();

        Map<String,List<Map<String,String>>> twDtl = new HashMap<>();

        K3Service k3Service = new K3Service();

        if("1".equals(cglx) || "2".equals(cglx)){
            //大陆出口-永青/贝泰妮-工厂直发 & 大陆出口-总部发货
            String code = purService.tranHkPur_1(lcbh,gys,pushDate,rkck,bb,detailDatas1,k3Service,"0");
            /*if("200".equals(code)){
                code = purService.tranHkSale_1(lcbh,gys,pushDate,rkck,"PRE005",detailDatas1,k3Service,"0");
            }*/
        }
        return SUCCESS;
    }
}
