package weaver.interfaces.hzy.queue;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class DmsQueueAction extends BaseBean implements Action {

    private String type;

    @Override
    public String execute(RequestInfo requestInfo) {

        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        String processId = mainData.get("lcbh");

        int requestId = requestInfo.getRequestManager().getBillid();

        writeLog("开始执行DmsQueueAction");

        String insertSql = "insert into dms_queue (process_id,request_id,type,is_finish) values (?,?,?,?)";

        RecordSet insertRs = new RecordSet();

        insertRs.executeUpdate(insertSql,processId,requestId,type,0);

        writeLog("DmsQueueAction执行完毕");

        return SUCCESS;
    }

}
