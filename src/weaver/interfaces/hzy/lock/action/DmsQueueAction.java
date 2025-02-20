package weaver.interfaces.hzy.lock.action;

import cn.hutool.core.util.StrUtil;
import com.icbc.api.internal.apache.http.impl.cookie.S;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class DmsQueueAction extends BaseBean implements Action {
    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("ִ��DmsQueueAction");

        /*��ȡ��������������*/
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        String requestId = requestInfo.getRequestid();

        //���̱��
        String processCode = mainData.get("lcbh");

        //����·��
        String processPath = mainData.get("lclj");

        String type = "";
        if ("3".equals(processPath)){
            type = "4";
        }

        if (StrUtil.isNotEmpty(type)){
            String selectQueue = "select request_id from dms_tw_queue where request_id = ?";
            RecordSet selectRs = new RecordSet();
            selectRs.executeQuery(selectQueue,requestId);
            String existRequestId = "";
            while(selectRs.next()){
                existRequestId  = selectRs.getString("request_id");
            }
            //0:δռ���� 1:��ռ����2:ռ����ʧ��3:���ͷ���
            String isLock = "0";
            //0:δͬ��,1:ͬ����,2:ͬ���ɹ�,3:ͬ��ʧ��
            String isFinish = "0";

            if(StrUtil.isNotEmpty(existRequestId)){
                writeLog("requestId="+requestId+",������="+processCode+",����ջ,�Ѽ�¼����Ϊδͬ��&δռ����");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String updateTime = dateFormat.format(new Date());

                String updateQueue = "update dms_tw_queue set is_lock = ?,is_finish = ?,update_time = ? where request_id = ?";
                RecordSet updateRs = new RecordSet();
                updateRs.executeUpdate(updateQueue,isLock,isFinish,updateTime,requestId);
                writeLog("���¶��б�");

            }else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String now = dateFormat.format(new Date());


                writeLog("requestId="+requestId+",������="+processCode+",��δ��ջ,��������׼����ջ");

                String insertQueue = "insert into dms_tw_queue (request_id,process_code,type,is_lock,is_finish,create_time,update_time) " +
                        "values ('" +requestId+"','"+processCode+"','"+type+"','"+isLock+"','"+isFinish+"','"+now+"','"+now+"')";

                RecordSet insertRs = new RecordSet();
                insertRs.executeUpdate(insertQueue);
                writeLog("������б�");
            }
        }else {
            writeLog("��ǰ�������Ͳ�ȷ��ͨ������������ջ");
        }

        writeLog("DmsQueueActionִ�����");

        return SUCCESS;
    }
}
