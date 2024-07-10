package weaver.interfaces.tx.bojun.action;

import com.alibaba.fastjson.JSONObject;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.tx.bojun.util.BoJunApiUtil;
import weaver.interfaces.tx.bojun.util.TicketUtil;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * FileName: BoJunApiAction.java
 * 调用伯俊接口Action
 *
 * @Author tx
 * @Date 2023/7/16
 * @Version 1.00
 **/
public class BoJunApiAction extends BaseBean implements Action {

    private String apiId;


    @Override
    public String execute(RequestInfo requestInfo) {
        writeLog("开始执行BoJunApiAction！");

        RequestManager requestManager = requestInfo.getRequestManager();
        String requestid = requestInfo.getRequestid();
        BoJunApiUtil apiUtil = new BoJunApiUtil();

        //获取伯俊配置表信息
        Map<String,String> apiConfig = apiUtil.getApiConfig(apiId);

        //获取流程主表，明细表数据
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        mainData.put("requestid",requestid);//将流程请求id存入集合中

        List<Map<String, String>> detailData = new ArrayList<>();
        int mxindex = Util.getIntValue(apiConfig.get("mxbxh"));
        if(mxindex != -1){
            detailData = WorkflowToolMethods.getDetailTableInfo(requestInfo, mxindex);
        }

        //构建接口入参
        String params = apiUtil.getParams(apiId, mainData, detailData);
        //调用伯俊接口
        JSONObject apiRes = apiUtil.doAction(apiId, params);

        //处理返回信息
        String result = apiRes.getString("result");
        String message = apiRes.getString("message");
        if(!result.equals("true")){
            //身份验证出错，则需要重新调用认证接口获取身份认证令牌
            if(message.equals("身份验证出错!")){
                apiUtil.refreshTicket(); //刷新身份认证令牌
                apiRes = apiUtil.doAction(apiId, mainData, detailData);
                if(!apiRes.getString("result").equals("true")) {
                    apiUtil.errlogMessage(apiId, requestid, params, apiRes.toJSONString());
                    requestManager.setMessageid("1000");
                    requestManager.setMessagecontent("请求失败，请联系系统管理员，失败原因：" + message);
                    return FAILURE_AND_CONTINUE;
                }
            }else {
                apiUtil.errlogMessage(apiId, requestid, params, apiRes.toJSONString());
                requestManager.setMessageid("1000");
                requestManager.setMessagecontent("请求失败，请联系系统管理员，失败原因：" + message);
                return FAILURE_AND_CONTINUE;
            }
        }
        return Action.SUCCESS;
    }
}
