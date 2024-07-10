package weaver.interfaces.tx.dms.action;

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


/**
 * FileName: TriggerSubflowAction.java
 * 根据区分字段触发子流程（主流程主表对应子流程主表，主流程明细对应子流程明细）
 *
 * @Author tx
 * @Date 2022/8/13
 * @Version 1.00
 **/
public class TriggerSubflowAction extends BaseBean implements Action  {

    private String workflowid;//子流程id
    private String createrid;//子流程流程创建人 默认流程操作者创建
    private String isNextFlow;//子流程是否提交到下一个节点0否 1是
    private String mainField;//主流程主表字段
    private String dtIndex;//主流程明细表序号
    private String dtField;//主流程明细字段
    private String mainFieldChild;//子流程主表字段
    private String dtIndexChild;//子流程明细表序号
    private String dtFieldChild;//子流程明细字段
    private String classification;//区分字段 多字段用逗号“,”隔开
    private String writeField;//回写字段 若是回写至明细表需标识一下明细表序号 如明细表1则xxx_1

    @Override
    public String execute(RequestInfo requestInfo) {
        writeLog("开始执行TriggerSubflowAction！");

        RequestManager requestManager = requestInfo.getRequestManager();
        WorkflowUtil workflowUtil = new WorkflowUtil();
        String requestId = requestInfo.getRequestid();//获取流程请求id
        String tableName = requestManager.getBillTableName();//获取表单名称
        int billId = requestManager.getBillid();//表单数据id
        String requestName = requestManager.getRequestname();//请求标题
        List<String> requestids = new ArrayList<>();//触发的子流程
        if(Util.null2String(createrid).equals("")){
            createrid = requestInfo.getCreatorid();
        }
        RecordSet rs=new RecordSet();

        Map<String,String> mainTableData = getMainTableData(tableName, requestId);
        writeLog("主表数据："+mainTableData);


        Map<String, List<Map<String, String>>> classificationMap = classificationMap = getDetailMap(tableName, billId);
        writeLog("区分后的明细数据：" + classificationMap);


        //创建子流程
        int index = Integer.valueOf(dtIndexChild);
        Map<String, List<Map<String, String>>> detail = new HashMap<>();
        for (int i = 1; i < index; i++) {
            detail.put(String.valueOf(i),new ArrayList<>());
        }
        for (Map.Entry<String, List<Map<String,String>>> entry : classificationMap.entrySet()) {//触发子流程
            String mapKey = entry.getKey();
            List<Map<String,String>> mapValue = entry.getValue();
            detail.put(dtIndexChild,mapValue);
            int requestid = workflowUtil.creatRequest(createrid,workflowid,requestName+"（子流程）",mainTableData,detail,isNextFlow);//创建子流程
            requestids.add(String.valueOf(requestid));
            if(requestid == 0){
                requestManager.setMessageid("1000");
                requestManager.setMessagecontent("触发子流程失败，请联系系统管理员！");
                return FAILURE_AND_CONTINUE;
            }
            writeLog("触发成功的子流程请求id：" + requestid);
        }
        writeBackField(tableName, billId, requestids);
        return SUCCESS;
    }


    //主表数据
    public Map<String,String> getMainTableData(String tableName, String requestId){
        Map<String,String> mainTableData = new HashMap<>();//主表数据
        RecordSet rs=new RecordSet();
        //取主流程主表字段
        String selectSql = "select " + mainField + " from "+ tableName +" where requestid =  "+ requestId;
        writeLog("搜索主流程数据sql：" + selectSql);
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

    //明细表数据
    public Map<String,List<Map<String,String>>> getDetailMap(String tableName, int billId){
        Map<String,List<Map<String,String>>> classificationMap = new HashMap<>();
        RecordSet rs = new RecordSet();
        //取主流程明细表字段
        String selectDtSql=" select " + dtField + "," + classification  + " from " + tableName + "_dt" + dtIndex  +"  where mainid =  "+ billId;
        writeLog("搜索主流程明细表数据sql："+selectDtSql);
        rs.execute(selectDtSql);
        String[] dtFieldChildArr = dtFieldChild.split(",");
        while (rs.next()){
            String[] columnNames = dtField.split(",");//字段名
            List<Map<String,String>> list=new ArrayList<>();
            Map<String,String> map = new HashMap<>();
            for (int i=0;i<columnNames.length;i++){
                String value = Util.null2String(rs.getString(columnNames[i]));//字段对应值
                map.put(dtFieldChildArr[i],value);//存入子流程主表数据
            }
            list.add(map);
            String[] fields = classification.split(",");
            String field = "key";
            for (String item : fields ) {
                field = field + "_" + Util.null2String(rs.getString(item));
            }
            if(!field.equals("")&&classificationMap.containsKey(field)){
                classificationMap.get(field).add(map);
            }else{
                if(!field.equals("")){
                    classificationMap.put(field,list);
                }
            }
        }
        return classificationMap;
    }



    //将触发的子流程requestid保存到主流程中
    public void writeBackField(String tableName, int billId, List<String> requestids){
        RecordSet rs = new RecordSet();
        List<String> field = Util.TokenizerString(writeField, "_");
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

