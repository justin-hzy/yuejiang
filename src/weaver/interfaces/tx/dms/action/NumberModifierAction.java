package weaver.interfaces.tx.dms.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.tx.util.ToolsFunction;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

/**
 * FileName: NumberModifierAction.java
 * 修改下单子流程编号
 * 屈臣氏下单子流程编号修改为po号，
 * 其他下单子流程编号修改为原销售订单加两位流水号
 *
 * @Author tx
 * @Date 2023/12/08
 * @Version 1.00
 **/
public class NumberModifierAction extends BaseBean implements Action {

    private String orderType; //屈臣氏下单0 其他下单1


    @Override
    public String execute(RequestInfo requestInfo) {
        writeLog("开始执行NumberModifierAction！");
        Map<String, String> mainData = ToolsFunction.getMainData(requestInfo);
        RecordSet rs = new RecordSet();
        String requestid = requestInfo.getRequestid();
        String wfTable = requestInfo.getRequestManager().getBillTableName();
        //获取编号
        String num = "";
        if( "0".equals(orderType)){
            num = mainData.get("po");
        }else{
            if("".equals(mainData.get("lcbh")) || mainData.get("lcbh").contains("DMS99")){
                num = generateNumber(rs, mainData.get("ydh"), wfTable);
            }else{
                return SUCCESS;
            }
        }
        rs.executeUpdate("update " + wfTable + " set lcbh= ? where requestid = ?", num, requestid);
        return SUCCESS;
    }

    //生成子流程编号
    public  String generateNumber(RecordSet rs, String ydh, String wfTable){
        int processNum = 0;
        rs.executeQuery("select REPLACE(lcbh,concat(ydh,'-'),'') num from " + wfTable + "  where ydh=? order by REPLACE( lcbh,concat(ydh,'-'),'')*1 DESC LIMIT 1 ", ydh);
        while (rs.next()){
            processNum = Util.getIntValue(rs.getString("num"),0);
        }
        processNum++;
        String format = "%s%02d";
        String number = String.format(format, ydh + "-", processNum);
        return number;
    }





}
