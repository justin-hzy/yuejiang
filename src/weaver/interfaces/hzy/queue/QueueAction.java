package weaver.interfaces.hzy.queue;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class QueueAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {

        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        String processId = mainData.get("lcbh");

        writeLog("开始执行QueueAction");

        String insertSql = "insert into queue (process_id, is_finish) values (?,?)";

        RecordSet insertRs = new RecordSet();

        insertRs.executeUpdate(insertSql,processId,0);

        return SUCCESS;
    }

}
