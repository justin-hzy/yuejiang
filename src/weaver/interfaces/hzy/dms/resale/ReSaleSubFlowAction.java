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

    private String mainField;//�����������ֶ�

    private String mainFieldChild;//�����������ֶ�

    private String dtField;//��������ϸ�ֶ�

    private String dtIndex;//��������ϸ�����

    private String dtFieldChild;//��������ϸ�ֶ�

    private String writeField;//��д�ֶ� ���ǻ�д����ϸ�����ʶһ����ϸ����� ����ϸ��1��xxx_1


    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("ִ��ReSaleSubFlowAction");

        String requestid = requestInfo.getRequestid();

        RequestManager requestManager = requestInfo.getRequestManager();

        String tableName = requestManager.getBillTableName();

        Map<String,String> mainTableData = getMainTableData(requestid);

        writeLog("�������ݣ�"+mainTableData);

        WorkflowUtil workflowUtil = new WorkflowUtil();

        int billId = requestManager.getBillid();//������id

        List<String> requestids = new ArrayList<>();//������������

        //�� to do
        Map<String,List<Map<String,String>>> classificationMap = getDetailMap(billId);

        writeLog("classificationMap="+classificationMap.toString());

        Map<String, List<Map<String, String>>> detail = new HashMap<>();

        for(String key : classificationMap.keySet()){
            List<Map<String,String>> mapValue = classificationMap.get(key);
            mainTableData.put("lcbh",key);
            detail.put("1",mapValue);
            int result = workflowUtil.creatRequest("1","173","HK_�����˻��������̣�",mainTableData,mapValue,"1");
            requestids.add(String.valueOf(result));
            writeLog("�����ɹ�������������id��" + result);
        }
        writeBackField(tableName,billId,requestids);

        return SUCCESS;
    }



    public Map<String,String> getMainTableData(String requestId){
        Map<String,String> mainTableData = new HashMap<>();//��������

        RecordSet rs=new RecordSet();

        String selectSql = "select " + mainField + " from formtable_main_227 where requestid =  "+ requestId;

        rs.execute(selectSql);

        String[] mainFieldChildArr = mainFieldChild.split(",");

        while (rs.next()){
            String[] name = rs.getColumnName();//�ֶ���
            for (int i=0;i<name.length;i++){
                String value = Util.null2String(rs.getString(name[i]));//�ֶζ�Ӧֵ
                mainTableData.put(mainFieldChildArr[i],value);//������������������
            }
        }

        return mainTableData;
    }

    public Map<String,List<Map<String,String>>> getDetailMap(int billId){

        Map<String,List<Map<String,String>>> classificationMap = new HashMap<>();

        //Map<String,List<Map<String,String>>> classificationMap = new HashMap<>();
        RecordSet rs = new RecordSet();

        String selectDtSql=" select " + dtField +" from "  + "formtable_main_227_dt" + dtIndex  +"  where mainid =  "+ billId;

        writeLog("������������ϸ������sql��"+selectDtSql);

        rs.execute(selectDtSql);

        String[] dtFieldChildArr = dtFieldChild.split(",");

        List<Map<String,String>> list=new ArrayList<>();
        while (rs.next()){
            String[] columnNames = dtField.split(",");//�ֶ���

            Map<String,String> map = new HashMap<>();
            for (int i=0;i<columnNames.length;i++){
                String value = Util.null2String(rs.getString(columnNames[i]));//�ֶζ�Ӧֵ
                /*writeLog("columnName="+columnNames[i]);
                writeLog("value="+value);*/
                map.put(dtFieldChildArr[i],value);//������������������
            }
            list.add(map);
        }

        writeLog("list="+list.toString());

        //��
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


    //��������������requestid���浽��������
    public void writeBackField(String tableName, int billId, List<String> requestids){
        writeLog("��������������requestid���浽��������");
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
