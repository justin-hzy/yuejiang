package weaver.interfaces.hzy.k3.action.lock;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.service.lock.LockService;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;

public class LockAction extends BaseBean implements Action {

    private String type;

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("执行LockAction");



        LockService lockService = new LockService();

        RequestManager requestManager = requestInfo.getRequestManager();

        Integer billId = requestManager.getBillid();

        writeLog("billId="+billId);



        /*String lcbh = queryBillNo(type,billId);

        writeLog("流程号:"+lcbh);*/
        /*
        boolean result = lockService.getLock(lcbh);

        writeLog("result="+result);

        if(result == true){
            return SUCCESS;
        }else {
            return FAILURE_AND_CONTINUE;
        }*/
        //return SUCCESS;
        writeLog("执行LockAction结束");
        return FAILURE_AND_CONTINUE;
    }

    public String queryBillNo(String type,Integer billId){
        String saleSql = "select lcbh from formtable_main_226 where id = ?";

        RecordSet recordSet = new RecordSet();

        recordSet.executeQuery(saleSql,billId);

        String lcbh = "";
        if(recordSet.next()){
            lcbh = recordSet.getString("lcbh");
        }
        return lcbh;
    }
}
