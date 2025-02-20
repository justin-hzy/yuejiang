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

        writeLog("执行DmsQueueAction");

        /*获取主流程主表数据*/
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        String requestId = requestInfo.getRequestid();

        //流程编号
        String processCode = mainData.get("lcbh");

        //流程路径
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
            //0:未占用锁 1:已占用锁2:占用锁失败3:已释放锁
            String isLock = "0";
            //0:未同步,1:同步中,2:同步成功,3:同步失败
            String isFinish = "0";

            if(StrUtil.isNotEmpty(existRequestId)){
                writeLog("requestId="+requestId+",请求编号="+processCode+",已入栈,把记录唤醒为未同步&未占用锁");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String updateTime = dateFormat.format(new Date());

                String updateQueue = "update dms_tw_queue set is_lock = ?,is_finish = ?,update_time = ? where request_id = ?";
                RecordSet updateRs = new RecordSet();
                updateRs.executeUpdate(updateQueue,isLock,isFinish,updateTime,requestId);
                writeLog("更新队列表");

            }else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String now = dateFormat.format(new Date());


                writeLog("requestId="+requestId+",请求编号="+processCode+",从未入栈,插入数据准备入栈");

                String insertQueue = "insert into dms_tw_queue (request_id,process_code,type,is_lock,is_finish,create_time,update_time) " +
                        "values ('" +requestId+"','"+processCode+"','"+type+"','"+isLock+"','"+isFinish+"','"+now+"','"+now+"')";

                RecordSet insertRs = new RecordSet();
                insertRs.executeUpdate(insertQueue);
                writeLog("进入队列表");
            }
        }else {
            writeLog("当前流程类型不确定通过，不进行入栈");
        }

        writeLog("DmsQueueAction执行完毕");

        return SUCCESS;
    }
}
