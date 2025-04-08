package weaver.interfaces.hzy.me.action;

import com.alibaba.fastjson.JSONObject;
import com.icbc.api.internal.apache.http.impl.cookie.S;
import weaver.general.BaseBean;
import weaver.general.Util;

import weaver.interfaces.hzy.me.util.MeApiUtil;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MeApiAction extends BaseBean implements Action {

    private String apiId;

    private String dataTableName;

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("开始执行hzy-MeApiAction！");

        RequestManager requestManager = requestInfo.getRequestManager();
        String requestid = requestInfo.getRequestid();
        MeApiUtil apiUtil = new MeApiUtil();

        //获取ME配置表信息
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
        List<String> params = apiUtil.getParams(requestid,apiId,dataTableName);

        if (params.size()>0){
            for (String param : params){

                //调用ME接口
                String resulString = apiUtil.doMeAction(param,apiId);

                //处理返回信息
                JSONObject resultJson = JSONObject.parseObject(resulString);

                String code = resultJson.getString("code");

                if("500".equals(code)){
                    writeLog("请求失败，请联系系统管理员");
                    /*apiUtil.errlogMessage(apiId, requestid, params, resulString);
                    requestManager.setMessageid("1000");
                    requestManager.setMessagecontent("请求失败，请联系系统管理员，失败原因：" + message);
                    return FAILURE_AND_CONTINUE;*/
                }

            /*if(!result.equals("true")){
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
            }*/
            }
        }else {
            writeLog("params参数为空，不请求ME接口");
        }
        return Action.SUCCESS;
    }
}
