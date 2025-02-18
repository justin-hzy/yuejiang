package weaver.interfaces.hzy.lock.action;

import com.alibaba.fastjson.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.common.service.CommonService;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class UnLockAction extends BaseBean implements Action {

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    private String unTwLockUrl = getPropValue("k3_api_config","unTwLockUrl");

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("开始执行UnLockAction");

        CommonService commonService = new CommonService();
        JSONObject reqJson = new JSONObject();
        String requestId = requestInfo.getRequestid();
        reqJson.put("requestId",requestId);
        String param = reqJson.toJSONString();
        String respJsonStr = commonService.doK3Action(param,k3Ip,unTwLockUrl);

        JSONObject respJson = JSONObject.parseObject(respJsonStr);

        String message = respJson.getString("message");

        String updateSql = "update formtable_main_272 set k3_result = ? where requestid = ?";
        RecordSet updateRs = new RecordSet();
        updateRs.executeUpdate(updateSql,message,requestId);

        writeLog("UnLockAction执行完毕");
        return SUCCESS;
    }
}
