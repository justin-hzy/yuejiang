package weaver.interfaces.hzy.th.sf.action;

import com.alibaba.fastjson.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.hzy.common.service.CommonService;
import weaver.interfaces.hzy.th.sf.util.SfApiUtil;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SfApiAction extends BaseBean implements Action {

    private String apiId;

    private String sfIp = getPropValue("sf_api_config","sfIp");


    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("开始执行SfApiAction");

        CommonService commonService = new CommonService();
        RequestManager requestManager = requestInfo.getRequestManager();
        String requestId = requestInfo.getRequestid();

        SfApiUtil apiUtil = new SfApiUtil();

        //获取顺丰国际配置表信息
        Map<String,String> apiConfig = apiUtil.getApiConfig(apiId);

        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        mainData.put("requestid",requestId);

        List<Map<String, String>> detailData = new ArrayList<>();
        int mxindex = Util.getIntValue(apiConfig.get("mxbxh"));
        String url = apiConfig.get("fjk");
        writeLog("mxindex="+mxindex);
        if(mxindex != -1){
            detailData = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, mxindex);
            //detailData = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);
        }

        writeLog("mainData="+mainData.toString());

        writeLog("detailData="+detailData.toString());

        //构建接口入参
        String param = apiUtil.getParams(apiId, mainData, detailData);


        String respJsonStr = commonService.doK3Action(param,sfIp,url);
        writeLog("respJsonStr="+respJsonStr);

        JSONObject respJson = JSONObject.parseObject(respJsonStr);
        JSONObject dataJson = respJson.getJSONObject("data");

        String note = dataJson.getString("note");
        String isSf = dataJson.getString("isSf");

        //更新每个流程的状态状态
        if("1".equals(apiId)){
            //更新添加销售出库流程表状态
            RecordSet rs = new RecordSet();
            String updateSql = "update formtable_main_347 set is_sf = ? , sf_request_result = ? where requestid = ?";
            rs.executeUpdate(updateSql,isSf,note,requestId);
        }else if("2".equals(apiId)){
            //更新添加销售出库流程表状态
            RecordSet rs = new RecordSet();
            String updateSql = "update formtable_main_348 set is_sf = ? , sf_request_result = ? where requestid = ?";
            rs.executeUpdate(updateSql,isSf,note,requestId);
        }
        return SUCCESS;
    }
}
