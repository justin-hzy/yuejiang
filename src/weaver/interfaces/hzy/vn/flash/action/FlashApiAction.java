package weaver.interfaces.hzy.vn.flash.action;

import com.icbc.api.internal.apache.http.impl.cookie.S;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.hzy.common.service.CommonService;
import weaver.interfaces.hzy.vn.flash.util.FlashApiUtil;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlashApiAction extends BaseBean implements Action {

    private String apiId;

    private String flashIp = getPropValue("flash_api_config","flashIp");

    private String putFlashOutOrderUrl = getPropValue("flash_api_config","putFlashOutOrderUrl");

    private String putFlashInOrderUrl = getPropValue("flash_api_config","putFlashInOrderUrl");

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("开始执行FlashApiAction");
        CommonService commonService = new CommonService();
        RequestManager requestManager = requestInfo.getRequestManager();
        String requestId = requestInfo.getRequestid();

        FlashApiUtil apiUtil = new FlashApiUtil();

        //获取flash配置表信息
        Map<String,String> apiConfig = apiUtil.getApiConfig(apiId);

        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        mainData.put("requestid",requestId);

        List<Map<String, String>> detailData = new ArrayList<>();
        int mxindex = Util.getIntValue(apiConfig.get("mxbxh"));
        writeLog("mxindex="+mxindex);
        if(mxindex != -1){
            detailData = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, mxindex);
            //detailData = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);
        }

        writeLog("mainData="+mainData.toString());

        writeLog("detailData="+detailData.toString());

        //构建接口入参
        String param = apiUtil.getParams(apiId, mainData, detailData);

        if("1".equals(apiId)){
            String respJsonStr = commonService.doK3Action(param,flashIp,putFlashOutOrderUrl);
            writeLog("respJsonStr="+respJsonStr);
        }else if("2".equals(apiId)){
            String respJsonStr = commonService.doK3Action(param,flashIp,putFlashInOrderUrl);
            writeLog("respJsonStr="+respJsonStr);
        }else if("3".equals(apiId)){
            String respJsonStr = commonService.doK3Action(param,flashIp,putFlashOutOrderUrl);
            writeLog("respJsonStr="+respJsonStr);
        }else if("4".equals(apiId)){
            String respJsonStr = commonService.doK3Action(param,flashIp,putFlashInOrderUrl);
            writeLog("respJsonStr="+respJsonStr);
        }

        return SUCCESS;
    }

}
