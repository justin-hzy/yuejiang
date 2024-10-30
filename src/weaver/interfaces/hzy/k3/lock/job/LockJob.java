package weaver.interfaces.hzy.k3.lock.job;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.lock.service.LockService;
import weaver.interfaces.schedule.BaseCronJob;
import weaver.general.BaseBean;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;

public class LockJob extends BaseCronJob {

    private String type;

    @Override
    public void execute() {
        new BaseBean().writeLog("Ö´ÐÐLockAction");

        



    }

    public String queryBillNo(String type,Integer billId){
        String saleSql = "select lcbh from formtable_main_272 where id = ?";

        RecordSet recordSet = new RecordSet();

        recordSet.executeQuery(saleSql,billId);

        String lcbh = "";
        if(recordSet.next()){
            lcbh = recordSet.getString("lcbh");
        }
        return lcbh;
    }
}
