package weaver.interfaces.hzy.k3.action.pur;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.service.K3Service;
import weaver.interfaces.hzy.k3.service.PurService;
import weaver.interfaces.tx.util.WorkflowUtil;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurAction extends BaseBean implements Action {

    private String meIp = getPropValue("fulun_api_config","meIp");

    private String putPurUrl = getPropValue("k3_api_config","putPurUrl");


    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("执行 PurAction");

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
        String rkrq = mainData.get("rkrq");





        List<Map<String,String>> twList = new ArrayList<>();

        Map<String,List<Map<String,String>>> twDtl = new HashMap<>();

        K3Service k3Service = new K3Service();

        if("3".equals(cglx)){
            //材料购买
            purService.tranTwPur_0(lcbh,gys,rkrq,rkck,bb,detailDatas1,k3Service,"5","");
        }else if("1".equals(cglx)){
            //大陆直发
            /*String code = purService.tranHkPur_1(lcbh,gys,yjjcr,rkck,bb,detailDatas1,k3Service,"0");
            if("200".equals(code)){
                code = purService.tranHkSale_1(lcbh,gys,yjjcr,rkck,"PRE005",detailDatas1,k3Service,"0");
                if("200".equals(code)){
                    //进口税
                    String jks = mainData.get("jks");
                    purService.tranTwPur_1(lcbh,gys,yjjcr,rkck,"PRE005",detailDatas1,k3Service,"0",jks);
                }
            }*/
            //进口税
            String jks = mainData.get("jks");
            purService.tranTwPur_1(lcbh,gys,rkrq,rkck,"PRE005",detailDatas1,k3Service,"0",jks);
        }else if("0".equals(cglx)){
            //台湾资生堂
            purService.tranHkPur_1(lcbh,gys,rkrq,rkck,bb,detailDatas1,k3Service,"0");
        }else if("2".equals(cglx)){
            /*String code = purService.tranHkPur_1(lcbh,gys,yjjcr,rkck,bb,detailDatas1,k3Service,"0");
            if("200".equals(code)){
                code = purService.tranHkSale_1(lcbh,gys,yjjcr,rkck,"PRE005",detailDatas1,k3Service,"0");
                if("200".equals(code)){
                    //进口税
                    String jks = mainData.get("jks");
                    purService.tranTwPur_1(lcbh,gys,yjjcr,rkck,"PRE005",detailDatas1,k3Service,"0",jks);
                }
            }*/
            //进口税
            String jks = mainData.get("jks");
            purService.tranTwPur_1(lcbh,gys,rkrq,rkck,"PRE005",detailDatas1,k3Service,"0",jks);
        }

        return SUCCESS;
    }


    //材料购买-台湾采购入库





}
