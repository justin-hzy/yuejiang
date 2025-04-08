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

        writeLog("��ʼִ��hzy-MeApiAction��");

        RequestManager requestManager = requestInfo.getRequestManager();
        String requestid = requestInfo.getRequestid();
        MeApiUtil apiUtil = new MeApiUtil();

        //��ȡME���ñ���Ϣ
        Map<String,String> apiConfig = apiUtil.getApiConfig(apiId);

        //��ȡ����������ϸ������
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        mainData.put("requestid",requestid);//����������id���뼯����

        List<Map<String, String>> detailData = new ArrayList<>();
        int mxindex = Util.getIntValue(apiConfig.get("mxbxh"));
        if(mxindex != -1){
            detailData = WorkflowToolMethods.getDetailTableInfo(requestInfo, mxindex);
        }

        //�����ӿ����
        List<String> params = apiUtil.getParams(requestid,apiId,dataTableName);

        if (params.size()>0){
            for (String param : params){

                //����ME�ӿ�
                String resulString = apiUtil.doMeAction(param,apiId);

                //��������Ϣ
                JSONObject resultJson = JSONObject.parseObject(resulString);

                String code = resultJson.getString("code");

                if("500".equals(code)){
                    writeLog("����ʧ�ܣ�����ϵϵͳ����Ա");
                    /*apiUtil.errlogMessage(apiId, requestid, params, resulString);
                    requestManager.setMessageid("1000");
                    requestManager.setMessagecontent("����ʧ�ܣ�����ϵϵͳ����Ա��ʧ��ԭ��" + message);
                    return FAILURE_AND_CONTINUE;*/
                }

            /*if(!result.equals("true")){
            //�����֤��������Ҫ���µ�����֤�ӿڻ�ȡ�����֤����
            if(message.equals("�����֤����!")){
                apiUtil.refreshTicket(); //ˢ�������֤����
                apiRes = apiUtil.doAction(apiId, mainData, detailData);
                if(!apiRes.getString("result").equals("true")) {
                    apiUtil.errlogMessage(apiId, requestid, params, apiRes.toJSONString());
                    requestManager.setMessageid("1000");
                    requestManager.setMessagecontent("����ʧ�ܣ�����ϵϵͳ����Ա��ʧ��ԭ��" + message);
                    return FAILURE_AND_CONTINUE;
                }
            }else {
                apiUtil.errlogMessage(apiId, requestid, params, apiRes.toJSONString());
                requestManager.setMessageid("1000");
                requestManager.setMessagecontent("����ʧ�ܣ�����ϵϵͳ����Ա��ʧ��ԭ��" + message);
                return FAILURE_AND_CONTINUE;
            }
            }*/
            }
        }else {
            writeLog("params����Ϊ�գ�������ME�ӿ�");
        }
        return Action.SUCCESS;
    }
}
