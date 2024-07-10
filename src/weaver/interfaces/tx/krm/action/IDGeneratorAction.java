package weaver.interfaces.tx.krm.action;

import com.engine.interfaces.tx.field.biz.FieldSignBiz;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;

/**
 * FileName: IDGeneratorAction.java
 * 流程生成编号
 *
 * @Author tx
 * @Date 2023/6/19
 * @Version 1.00
 **/
public class IDGeneratorAction extends BaseBean implements Action {

    private String custOrPos;//判断是生成客户还是生成店铺编号 0客户 1店铺
    private String reversedField;//将生成的编号保存至流程字段

    @Override
    public String execute(RequestInfo requestInfo) {
        FieldSignBiz biz = new FieldSignBiz();
        String number = "";
        //生成编号
        if(custOrPos.equals("0")){
            number = biz.generateCUSTNumber();
        }else if(custOrPos.equals("1")){
            number = biz.generatePOSNumber();
        }
        //保存编号
        String tableName = requestInfo.getRequestManager().getBillTableName();
        String requestid = requestInfo.getRequestid();
        RequestManager requestManager = requestInfo.getRequestManager();
        RecordSet rs = new RecordSet();
        boolean sqlStatus = rs.executeUpdate("update " + tableName + " set " + reversedField +" = ? where requestid = ?",number,requestid);
        if(number.equals("")){
            requestManager.setMessageid("1000");
            requestManager.setMessagecontent("生成编号错误，请联系系统管理员！");
            return FAILURE_AND_CONTINUE;
        }
        if(!sqlStatus){
            requestManager.setMessageid("1001");
            requestManager.setMessagecontent("保存编号错误，请联系系统管理员！");
            return FAILURE_AND_CONTINUE;
        }
        return SUCCESS;
    }
}
