package weaver.interfaces.tx.util;

import weaver.conn.RecordSet;
import weaver.formmode.virtualform.VirtualFormHandler;
import weaver.general.Util;
import weaver.soa.workflow.request.*;
import weaver.system.SysRemindWorkflow;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class WorkflowToolMethods<fori> {
    public WorkflowToolMethods() {}

    public static Map<String,String> getMainTableInfo(RequestInfo request){
        //获取主表信息
        Map<String,String>  mainMap=new HashMap<String,String>();
        Property[] props=request.getMainTableInfo().getProperty();//获取主表信息 name:数据库字段名 value:表中的值
        for(int i=0;i<props.length;i++){
            String key=Util.null2String(props[i].getName().toLowerCase());
            String value=Util.null2String(props[i].getValue());
            mainMap.put(key,value);
        }
        return mainMap;
    }

    public static List<Map<String, String>> getDetailTableInfo(RequestInfo request, int index1) {
        int index = index1 - 1;

        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        DetailTable[] detailtables = request.getDetailTableInfo().getDetailTable();// 获取明细表数据
        if (detailtables.length > index) {
            DetailTable dt = detailtables[index];// 获取明细表 0代表明细表1
            Row[] rows = dt.getRow();// 获取明细表中所有行的信息
            for (int i = 0; i < rows.length; i++) {
                Row row = rows[i];// 获取具体行信息
                Cell[] cells = row.getCell();// 获取具体行所有列的信息
                Map<String, String> map = new HashMap<String, String>();
                for (int j = 0; j < cells.length; j++) {
                    Cell cell = cells[j];
                    String name = cell.getName().toLowerCase();// 获取字段名
                    String value = cell.getValue();// 获取具体的值
                    map.put(name, value);
                }
                list.add(map);
            }
        }

        return list;
    }

    /**
     * 触发系统默认提醒流程
     * @param requestname 流程标题
     * @param operators  接收人  多人用  , 隔开
     * @param remark  提醒信息
     */
    public static void SysRemindWorkflow(String requestname,String operators,String remark){

        SysRemindWorkflow workflow = new SysRemindWorkflow();

        try {
            workflow.setSysRemindInfo(requestname, 0, 0, 0, 0, 1, operators, remark);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 创建系统流程
     * @param createrid  创建人id
     * @param workflowid 流程id
     * @param requestname 流程标题
     * @param IsNextFlow
     * @param maintable		主表信息
     * 	     Map	key: 字段名  value: 值
     *
     * @param detail  明细信息
     *       Map	key:明细表序号   value：  List {Map  key: 字段名  value: 值}
     * @return
     */
    public static String creatRequest(String createrid ,String workflowid ,String requestname ,String IsNextFlow,Map<String,String> maintable,Map<String,List<Map<String,String>>> detail){
        String requestid = "0";

        if(maintable==null || "0".equals(createrid) || "0".equals(workflowid)){
            return "0";
        }

        //请求基本信息
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setCreatorid(createrid);//创建人
        requestInfo.setWorkflowid(workflowid);//工作流id
        requestInfo.setDescription(requestname);//标题

        if("0".equals(IsNextFlow)){//是否提交下一节点
            requestInfo.setIsNextFlow("0");
        }

        //主表信息
        MainTableInfo mainTableInfo = new MainTableInfo();
        Property[] propertyArray = new Property[maintable.size()];
        int p = 0;

        for (Entry<String, String> entry : maintable.entrySet()) {
            propertyArray[p] = new Property();
            propertyArray[p].setName((String) entry.getKey());
            propertyArray[p].setValue((String) entry.getValue());
            p++;
            //writeLog("Key="+entry.getKey()+"    Value="+entry.getValue());
        }
        mainTableInfo.setProperty(propertyArray);
        requestInfo.setMainTableInfo(mainTableInfo);

        //明细信息
        DetailTableInfo detailTableInfo = new DetailTableInfo();

        DetailTable[] detailTables = new DetailTable[detail.size()];
        int ds = 0;
        for (Entry<String, List<Map<String,String>>> entry : detail.entrySet()) {

            int u= 0;
            DetailTable detailTable = new DetailTable();
            List<Map<String,String>> list = entry.getValue();
            for (Map<String,String> map : list) {

                Row row = new Row();
                for (Entry<String,String> rentry : map.entrySet()) {
                    Cell cell = new Cell();
                    cell.setName(""+rentry.getKey());
                    cell.setValue(""+rentry.getValue());
                    row.addCell(cell);
                    //writeLog("Key:"+entry.getKey()+"    Value:"+entry.getValue());
                }
                detailTable.addRow(row);
                u++;
            }
            detailTables[ds] = detailTable;
            ds++;
        }

        detailTableInfo.setDetailTable(detailTables);
        requestInfo.setDetailTableInfo(detailTableInfo);//明细表

        RequestService service = new RequestService();
        try {
            requestid = service.createRequest(requestInfo);//创建请求id
            //String userId = requestInfo.getLastoperator();//请求最后的操作者

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return requestid;
    }

    /**
     * 流程退回
     * @param requestid 请求id
     * @param userId	提交人
     * @param remark	提交意见
     * @return
     */
    public static boolean nextNodeByReject(int requestid,int userId,String remark) {

        boolean oaflag = false;

        RequestService rss = new RequestService();
        //RequestInfo info = new RequestInfo();
        //info = null;

        oaflag = rss.nextNodeByReject(requestid, userId, remark);//流程退回
        return oaflag;
    }

    /**
     * 流程提交
     * @param requestid 请求id
     * @param userId	提交人
     * @param remark	提交意见
     * @return
     */
    public static boolean nextNodeBySubmit(int requestid, int userId, String remark) {

        boolean oaflag = false;
        RequestService rss = new RequestService();
        RequestInfo info = new RequestInfo();
        //info = null;
        info = rss.getRequest(requestid);
        oaflag = rss.nextNodeBySubmit(info, requestid, userId, remark); //提交流程

        return oaflag;
    }

    /**
     * @description 通过流程信息获取流程表名
     * @param requestInfo 流程信息
     * @return tablename 流程表名
     */
    public static String getTablename(RequestInfo requestInfo){
        String workflowid = requestInfo.getWorkflowid();
        String tablename = "";
        String sql="select tablename from workflow_base base,workflow_bill bill where base.formid = bill.id and base.id ="+workflowid;
        RecordSet rs = new RecordSet();
        rs.execute(sql);
        if(rs.next()){
            tablename = rs.getString("tablename");
        }
        return VirtualFormHandler.getRealFromName(Util.null2String(tablename));
    }


    /**
     * 判断时间是否在时间段内
     *
     * @param nowTime
     * @param beginTime
     * @param endTime
     * @return
     */
    public static  String belongCalendar(Date nowTime, Date beginTime, Date endTime) {
        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);
        Calendar begin = Calendar.getInstance();
        begin.setTime(beginTime);
        Calendar end = Calendar.getInstance();
        end.setTime(endTime);
        if (date.after(begin) && date.before(end)) {
            return "0";
        } else if (nowTime.compareTo(beginTime) == 0 || nowTime.compareTo(endTime) == 0) {
            return "0";
        } else {
            return "1";
        }
    }
    // 字符串 转 日期
    public  static Date strToDate(String str) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(str);
        } catch (ParseException e) {
        }
        return date;
    }




}
