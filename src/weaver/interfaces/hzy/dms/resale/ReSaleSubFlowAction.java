package weaver.interfaces.hzy.dms.resale;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.tx.util.WorkflowUtil;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReSaleSubFlowAction extends BaseBean implements Action {

    private String mainField;//主流程主表字段

    private String mainFieldChild;//子流程主表字段

    private String dtField;//主流程明细字段

    private String dtIndex;//主流程明细表序号

    private String dtFieldChild;//子流程明细字段

    private String writeField;//回写字段 若是回写至明细表需标识一下明细表序号 如明细表1则xxx_1


    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("执行ReSaleSubFlowAction");

        String requestid = requestInfo.getRequestid();

        RequestManager requestManager = requestInfo.getRequestManager();

        String tableName = requestManager.getBillTableName();

        Map<String,String> mainTableData = getMainTableData(requestid);

        writeLog("主表数据："+mainTableData);

        WorkflowUtil workflowUtil = new WorkflowUtil();

        int billId = requestManager.getBillid();//表单数据id

        List<String> requestids = new ArrayList<>();//触发的子流程

        //拆单 to do
        Map<String,List<Map<String,String>>> classificationMap = getDetailMap(billId);

        writeLog("classificationMap="+classificationMap.toString());

        Map<String, List<Map<String, String>>> detail = new HashMap<>();

        for(String key : classificationMap.keySet()){
            List<Map<String,String>> mapValue = classificationMap.get(key);
            mainTableData.put("lcbh",key);
            detail.put("1",mapValue);
            int result = workflowUtil.creatRequest("1","173","HK_销售退货（子流程）",mainTableData,mapValue,"1");
            requestids.add(String.valueOf(result));
            writeLog("触发成功的子流程请求id：" + result);
        }
        writeBackField(tableName,billId,requestids);

        return SUCCESS;
    }



    public Map<String,String> getMainTableData(String requestId){
        Map<String,String> mainTableData = new HashMap<>();//主表数据

        RecordSet rs=new RecordSet();

        String selectSql = "select " + mainField + " from formtable_main_227 where requestid =  "+ requestId;

        rs.execute(selectSql);

        String[] mainFieldChildArr = mainFieldChild.split(",");

        while (rs.next()){
            String[] name = rs.getColumnName();//字段名
            for (int i=0;i<name.length;i++){
                String value = Util.null2String(rs.getString(name[i]));//字段对应值
                mainTableData.put(mainFieldChildArr[i],value);//存入子流程主表数据
            }
        }

        return mainTableData;
    }

    public Map<String,List<Map<String,String>>> getDetailMap(int billId){

        Map<String,List<Map<String,String>>> classificationMap = new HashMap<>();

        //Map<String,List<Map<String,String>>> classificationMap = new HashMap<>();
        RecordSet rs = new RecordSet();

        String selectDtSql=" select " + dtField +" from "  + "formtable_main_227_dt" + dtIndex  +"  where mainid =  "+ billId;

        writeLog("搜索主流程明细表数据sql："+selectDtSql);

        rs.execute(selectDtSql);

        String[] dtFieldChildArr = dtFieldChild.split(",");

        List<Map<String,String>> list=new ArrayList<>();
        while (rs.next()){
            String[] columnNames = dtField.split(",");//字段名

            Map<String,String> map = new HashMap<>();
            for (int i=0;i<columnNames.length;i++){
                String value = Util.null2String(rs.getString(columnNames[i]));//字段对应值
                /*writeLog("columnName="+columnNames[i]);
                writeLog("value="+value);*/
                map.put(dtFieldChildArr[i],value);//存入子流程主表数据
            }
            list.add(map);
        }

        writeLog("list="+list.toString());

        //拆单
        for (Map<String,String> map : list){
            String khddh = map.get("khddh");

            if(classificationMap.containsKey(khddh)){
                map.remove(khddh);
                classificationMap.get(khddh).add(map);
            }else {
                List<Map<String,String>> dtlList = new ArrayList<>();
                map.remove(khddh);
                dtlList.add(map);
                classificationMap.put(khddh,dtlList);
            }
        }
        return classificationMap;
    }


    //将触发的子流程requestid保存到主流程中
    public void writeBackField(String tableName, int billId, List<String> requestids){
        writeLog("将触发的子流程requestid保存到主流程中");
        RecordSet rs = new RecordSet();
        List<String> field = Util.TokenizerString(writeField, "_");
        writeLog("field="+field.toString());
        String sql = "";
        if (field.size()>1&&!Util.null2String(field.get(1)).equals("")) {
            tableName = tableName + "_dt" + field.get(1);
            for (String requestid : requestids){
                sql = "insert into "+ tableName  + " (mainid," + field.get(0) + ") values (" + billId + "," + requestid + ")" ;
                rs.executeUpdate(sql);
            }
        }else {
            String requestidStr = String.join(",",requestids);
            sql = "update "+ tableName  + " set " + field.get(0) + "=? where id = ?"  ;
            rs.executeUpdate(sql, requestidStr, billId);
        }
    }
}
