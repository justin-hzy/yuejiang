package weaver.interfaces.tx.util;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.StaticObj;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.*;
import weaver.system.SysRemindWorkflow;
import weaver.system.SysWFLMonitor;
import weaver.systeminfo.SysMaintenanceLog;
import weaver.workflow.monitor.Monitor;
import weaver.workflow.msg.PoppupRemindInfoUtil;
import weaver.workflow.webservices.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * 流程处理管理类
 * @author Administrator
 *
 */
public class WorkflowUtil extends BaseBean {
	
	
	/**
	 * 触发系统流程
	 * @param createrid 创建人id
	 * @param workflowid	流程id
	 * @param requestname	流程标题
	 * @param maintable		主表信息 
	 * 	     Map	key: 字段名  value: 值
	 * @return
	 */
	public int creatRequest(String createrid, String workflowid, String requestname, Map maintable){
		
		int requestid = 0;
		
		if(maintable==null || "0".equals(createrid) || "0".equals(workflowid)){
			return 0;
		}
		
		WorkflowServiceImpl impl = new WorkflowServiceImpl();
		
		WorkflowRequestTableField[] wrti = new WorkflowRequestTableField[maintable.size()]; //主表字段信息
		int i = 0;
		Set keys = maintable.keySet();
		Iterator iter = keys.iterator();
		while(iter.hasNext()){
			String key = Util.null2String((String)iter.next());
			String keyValue = Util.null2String((String)maintable.get(key));
			
			wrti[i] = new WorkflowRequestTableField();
			if(key.indexOf("http:")==0){
				key = key.split(":")[1];
				
				String type = keyValue.split(";")[0];
				keyValue = keyValue.split(";")[1];
				
				writeLog("Key:"+key+"  type:"+type+"  Value:"+keyValue);
				wrti[i].setFieldName(key);//字段名
				wrti[i].setFieldType(type); //http:开头代表该字段为附件字段
				wrti[i].setFieldValue(keyValue);//附件地址
				
			}else{
				wrti[i].setFieldName(key);//字段名
				wrti[i].setFieldValue(keyValue);//字段的值，
			}
			wrti[i].setView(true);//字段是否可见
			wrti[i].setEdit(true);//字段是否可编辑
			i++;
		}
		
		WorkflowRequestTableRecord[] wrtri = new WorkflowRequestTableRecord[1];//主字段只有一行数据
		wrtri[0] = new WorkflowRequestTableRecord();
		wrtri[0].setWorkflowRequestTableFields(wrti);	
		
		WorkflowMainTableInfo wmi = new WorkflowMainTableInfo(); //主表信息
		wmi.setRequestRecords(wrtri);  
		
		WorkflowBaseInfo wbi = new WorkflowBaseInfo();//流程id
		wbi.setWorkflowId(workflowid);//workflowid 
		
	    WorkflowRequestInfo wri = new WorkflowRequestInfo();//流程基本信息
		wri.setCreatorId(createrid);//创建人id
		wri.setRequestLevel("0");//0 正常，1重要，2紧急
		wri.setRequestName(requestname);//流程标题
	    wri.setWorkflowMainTableInfo(wmi);//添加主字段数据
		wri.setWorkflowBaseInfo(wbi);
		
		
		requestid =  Util.getIntValue(impl.doCreateWorkflowRequest(wri, Util.getIntValue(createrid)),0);
		
		return requestid;
	}
	
	/**
	 * 触发系统流程
	 * @param createrid 创建人id
	 * @param workflowid	流程id
	 * @param requestname	流程标题
	 * @param maintable		主表信息 
	 * 	     Map	key: 字段名  value: 值
	 * @return
	 */
	public int creatRequest(String createrid, String workflowid, String IsNextFlow, String requestname, Map maintable){
		
		int requestid = 0;
		
		if(maintable==null || "0".equals(createrid) || "0".equals(workflowid)){
			return 0;
		}
		
		WorkflowServiceImpl impl = new WorkflowServiceImpl();
		
		WorkflowRequestTableField[] wrti = new WorkflowRequestTableField[maintable.size()]; //主表字段信息
		int i = 0;
		Set keys = maintable.keySet();
		Iterator iter = keys.iterator();
		while(iter.hasNext()){
			String key = Util.null2String((String)iter.next());
			String keyValue = Util.null2String((String)maintable.get(key));
			
			wrti[i] = new WorkflowRequestTableField();
			if(key.indexOf("http:")==0){
				key = key.split(":")[1];
				
				String type = keyValue.split(";")[0];
				keyValue = keyValue.split(";")[1];
				
				writeLog("Key:"+key+"  type:"+type+"  Value:"+keyValue);
				wrti[i].setFieldName(key);//字段名
				wrti[i].setFieldType(type); //http:开头代表该字段为附件字段
				wrti[i].setFieldValue(keyValue);//附件地址
				
			}else{
				wrti[i].setFieldName(key);//字段名
				wrti[i].setFieldValue(keyValue);//字段的值，
			}
			wrti[i].setView(true);//字段是否可见
			wrti[i].setEdit(true);//字段是否可编辑
			i++;
		}
		
		WorkflowRequestTableRecord[] wrtri = new WorkflowRequestTableRecord[1];//主字段只有一行数据
		wrtri[0] = new WorkflowRequestTableRecord();
		wrtri[0].setWorkflowRequestTableFields(wrti);	
		
		WorkflowMainTableInfo wmi = new WorkflowMainTableInfo(); //主表信息
		wmi.setRequestRecords(wrtri);  
		
		WorkflowBaseInfo wbi = new WorkflowBaseInfo();//流程id
		wbi.setWorkflowId(workflowid);//workflowid 
		
	    WorkflowRequestInfo wri = new WorkflowRequestInfo();//流程基本信息
		wri.setCreatorId(createrid);//创建人id
		wri.setRequestLevel("0");//0 正常，1重要，2紧急
		wri.setRequestName(requestname);//流程标题
	    wri.setWorkflowMainTableInfo(wmi);//添加主字段数据
		wri.setWorkflowBaseInfo(wbi);
		if ("0".equals(IsNextFlow)) {
			wri.setIsnextflow(IsNextFlow);
		}
		
		requestid =  Util.getIntValue(impl.doCreateWorkflowRequest(wri, Util.getIntValue(createrid)),0);
		
		return requestid;
	}
	
	/**
	 * 触发系统流程
	 * @param createrid 创建人id
	 * @param workflowid	流程id
	 * @param requestname	流程标题
	 * @param maintable		主表信息 
	 * 	     Map	key: 字段名  value: 值
	 * @return
	 */
	public int creatRequest(String createrid, String workflowid, String requestname, Map maintable, List list){
		
		int requestid = 0;
		
		if(maintable==null || "0".equals(createrid) || "0".equals(workflowid)){
			return 0;
		}
		
		WorkflowServiceImpl impl = new WorkflowServiceImpl();
		
		WorkflowRequestTableField[] wrti = new WorkflowRequestTableField[maintable.size()]; //主表字段信息
		int i = 0;
		Set keys = maintable.keySet();
		Iterator iter = keys.iterator();
		while(iter.hasNext()){
			String key = Util.null2String((String)iter.next());
			String keyValue = Util.null2String((String)maintable.get(key));
			
			wrti[i] = new WorkflowRequestTableField();
			if(key.indexOf("http:")==0){
				key = key.split(":")[1];
				
				String type = keyValue.split(";")[0];
				keyValue = keyValue.split(";")[1];
				
				writeLog("Key:"+key+"  type:"+type+"  Value:"+keyValue);
				wrti[i].setFieldName(key);//字段名
				wrti[i].setFieldType(type); //http:开头代表该字段为附件字段
				wrti[i].setFieldValue(keyValue);//附件地址
				
			}else{
				wrti[i].setFieldName(key);//字段名
				wrti[i].setFieldValue(keyValue);//字段的值，
			}
			wrti[i].setView(true);//字段是否可见
			wrti[i].setEdit(true);//字段是否可编辑
			i++;
		}
		
		WorkflowRequestTableRecord[] wrtri = new WorkflowRequestTableRecord[1];//主字段只有一行数据
		wrtri[0] = new WorkflowRequestTableRecord();
		wrtri[0].setWorkflowRequestTableFields(wrti);	
		
		WorkflowMainTableInfo wmi = new WorkflowMainTableInfo(); //主表信息
		wmi.setRequestRecords(wrtri);  
		
		int uu = list.size();
		WorkflowRequestTableRecord[] Dwrtri = new WorkflowRequestTableRecord[uu];//行数据
		for (int j = 0; j < Dwrtri.length; j++) {
			
			Map map = (Map) list.get(j);
			
			WorkflowRequestTableField[] dwrti = new WorkflowRequestTableField[map.size()];
			int ii = 0;
			Set dkeys = map.keySet();
			Iterator diter = dkeys.iterator();
			while(diter.hasNext()){
				String key = Util.null2String((String)diter.next());
				String keyValue = Util.null2String((String)map.get(key));
				
				dwrti[ii] = new WorkflowRequestTableField();
				dwrti[ii].setFieldName(key);//字段名
				dwrti[ii].setFieldValue(keyValue);//字段的值，
				dwrti[ii].setView(true);//字段是否可见
				dwrti[ii].setEdit(true);//字段是否可编辑
				ii++;
			}
			
			WorkflowRequestTableRecord record = new WorkflowRequestTableRecord();
			record.setWorkflowRequestTableFields(dwrti);
			Dwrtri[j] = record;
		}
		
		WorkflowDetailTableInfo[] wdtis = new WorkflowDetailTableInfo[1];//明细信息
		wdtis[0] = new WorkflowDetailTableInfo();
		wdtis[0].setWorkflowRequestTableRecords(Dwrtri);
		wdtis[0].setTableDBName("formtable_main_99_dt1");

		WorkflowBaseInfo wbi = new WorkflowBaseInfo();//流程id
		wbi.setWorkflowId(workflowid);//workflowid 
		
	    WorkflowRequestInfo wri = new WorkflowRequestInfo();//流程基本信息
		wri.setCreatorId(createrid);//创建人id
		wri.setRequestLevel("0");//0 正常，1重要，2紧急
		wri.setRequestName(requestname);//流程标题
	    wri.setWorkflowMainTableInfo(wmi);//添加主字段数据
	    wri.setWorkflowDetailTableInfos(wdtis);//添加明细字段
		wri.setWorkflowBaseInfo(wbi);
		
		
		requestid =  Util.getIntValue(impl.doCreateWorkflowRequest(wri, Util.getIntValue(createrid)),0);
		
		return requestid;
	}
	
	/**
	 * 触发系统流程 停留在创建节点
	 * @param createrid 创建人id
	 * @param workflowid	流程id
	 * @param requestname	流程标题
	 * @param maintable		主表信息 
	 * 	     Map	key: 字段名  value: 值
	 * @param IsNextFlow 是否流转到下一节点
	 * @return
	 */
	public int creatRequest(String createrid, String workflowid, String requestname, Map maintable, List list, String IsNextFlow){
		
		int requestid = 0;
		
		if(maintable==null || "0".equals(createrid) || "0".equals(workflowid)){
			return 0;
		}
		
		WorkflowServiceImpl impl = new WorkflowServiceImpl();
		
		WorkflowRequestTableField[] wrti = new WorkflowRequestTableField[maintable.size()]; //主表字段信息
		int i = 0;
		Set keys = maintable.keySet();
		Iterator iter = keys.iterator();
		while(iter.hasNext()){
			String key = Util.null2String((String)iter.next());
			String keyValue = Util.null2String((String)maintable.get(key));
			
			wrti[i] = new WorkflowRequestTableField();
			if(key.indexOf("http:")==0){
				key = key.split(":")[1];
				
				String type = keyValue.split(";")[0];
				keyValue = keyValue.split(";")[1];
				
				writeLog("Key:"+key+"  type:"+type+"  Value:"+keyValue);
				wrti[i].setFieldName(key);//字段名
				wrti[i].setFieldType(type); //http:开头代表该字段为附件字段
				wrti[i].setFieldValue(keyValue);//附件地址
				
			}else{
				wrti[i].setFieldName(key);//字段名
				wrti[i].setFieldValue(keyValue);//字段的值，
			}
			wrti[i].setView(true);//字段是否可见
			wrti[i].setEdit(true);//字段是否可编辑
			i++;
		}
		
		WorkflowRequestTableRecord[] wrtri = new WorkflowRequestTableRecord[1];//主字段只有一行数据
		wrtri[0] = new WorkflowRequestTableRecord();
		wrtri[0].setWorkflowRequestTableFields(wrti);	
		
		WorkflowMainTableInfo wmi = new WorkflowMainTableInfo(); //主表信息
		wmi.setRequestRecords(wrtri);  
		
		int uu = list.size();
		WorkflowRequestTableRecord[] Dwrtri = new WorkflowRequestTableRecord[uu];//行数据
		for (int j = 0; j < Dwrtri.length; j++) {
			
			Map map = (Map) list.get(j);
			
			WorkflowRequestTableField[] dwrti = new WorkflowRequestTableField[map.size()];
			int ii = 0;
			Set dkeys = map.keySet();
			Iterator diter = dkeys.iterator();
			while(diter.hasNext()){
				String key = Util.null2String((String)diter.next());
				String keyValue = Util.null2String((String)map.get(key));
				
				dwrti[ii] = new WorkflowRequestTableField();
				dwrti[ii].setFieldName(key);//字段名
				dwrti[ii].setFieldValue(keyValue);//字段的值，
				dwrti[ii].setView(true);//字段是否可见
				dwrti[ii].setEdit(true);//字段是否可编辑
				ii++;
			}
			
			WorkflowRequestTableRecord record = new WorkflowRequestTableRecord();
			record.setWorkflowRequestTableFields(dwrti);
			Dwrtri[j] = record;
		}
		
		WorkflowDetailTableInfo[] wdtis = new WorkflowDetailTableInfo[1];//明细信息
		wdtis[0] = new WorkflowDetailTableInfo();
		wdtis[0].setWorkflowRequestTableRecords(Dwrtri);
		wdtis[0].setTableDBName("formtable_main_99_dt1");

		WorkflowBaseInfo wbi = new WorkflowBaseInfo();//流程id
		wbi.setWorkflowId(workflowid);//workflowid 
		
	    WorkflowRequestInfo wri = new WorkflowRequestInfo();//流程基本信息
		wri.setCreatorId(createrid);//创建人id
		wri.setRequestLevel("0");//0 正常，1重要，2紧急
		wri.setRequestName(requestname);//流程标题
	    wri.setWorkflowMainTableInfo(wmi);//添加主字段数据
	    wri.setWorkflowDetailTableInfos(wdtis);//添加明细字段
		wri.setWorkflowBaseInfo(wbi);
		if("0".equals(IsNextFlow)){//是否提交下一节点
			wri.setIsnextflow("0");
		}
		
		requestid =  Util.getIntValue(impl.doCreateWorkflowRequest(wri, Util.getIntValue(createrid)),0);
		
		return requestid;
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
	public int creatRequest(String createrid, String workflowid, String requestname, Map<String, String> maintable, Map<String, List<Map<String, String>>> detail, String IsNextFlow){

		int requestid = 0;

		if(maintable==null || "0".equals(createrid) || "0".equals(workflowid)){
			return 0;
		}

		WorkflowServiceImpl impl = new WorkflowServiceImpl();

		WorkflowRequestTableField[] wrti = new WorkflowRequestTableField[maintable.size()]; //主表字段信息
		int i = 0;
		Set keys = maintable.keySet();
		Iterator iter = keys.iterator();
		while(iter.hasNext()){
			String key = Util.null2String((String)iter.next());
			String keyValue = Util.null2String((String)maintable.get(key));
			/*writeLog("key="+key);
			writeLog("keyValue="+keyValue);*/

			wrti[i] = new WorkflowRequestTableField();
			if(key.indexOf("http:")==0){
				key = key.split(":")[1];

				String type = keyValue.split(";")[0];
				keyValue = keyValue.split(";")[1];

				wrti[i].setFieldName(key);//字段名
				wrti[i].setFieldType(type); //http:开头代表该字段为附件字段
				wrti[i].setFieldValue(keyValue);//附件地址

			}else{
				wrti[i].setFieldName(key);//字段名
				wrti[i].setFieldValue(keyValue);//字段的值，
			}
			wrti[i].setView(true);//字段是否可见
			wrti[i].setEdit(true);//字段是否可编辑
			i++;
		}

		WorkflowRequestTableRecord[] wrtri = new WorkflowRequestTableRecord[1];//主字段只有一行数据
		wrtri[0] = new WorkflowRequestTableRecord();
		wrtri[0].setWorkflowRequestTableFields(wrti);

		WorkflowMainTableInfo wmi = new WorkflowMainTableInfo(); //主表信息
		wmi.setRequestRecords(wrtri);

		WorkflowDetailTableInfo[] wdtis = new WorkflowDetailTableInfo[detail.size()];//明细信息
		for (Map.Entry<String, List<Map<String, String>>> entry : detail.entrySet()) {
			int dt = Integer.valueOf(entry.getKey())-1; //明细表序号
			writeLog("dt="+dt);
			List<Map<String, String>> list = entry.getValue(); //获取明细数据
			writeLog("list="+list);
			if(list.size() <= 0){
				continue;
			}
			int uu = list.size();
			writeLog("uu="+uu);
			WorkflowRequestTableRecord[] Dwrtri = new WorkflowRequestTableRecord[uu];//行数据
			for (int j = 0; j < Dwrtri.length; j++) {

				Map map = (Map) list.get(j);
				//writeLog("map="+map.toString());

				WorkflowRequestTableField[] dwrti = new WorkflowRequestTableField[map.size()];
				int ii = 0;
				Set dkeys = map.keySet();
				Iterator diter = dkeys.iterator();
				while (diter.hasNext()) {
					String key = Util.null2String((String) diter.next());
					String keyValue = Util.null2String((String) map.get(key));

					dwrti[ii] = new WorkflowRequestTableField();
					dwrti[ii].setFieldName(key);//字段名
					dwrti[ii].setFieldValue(keyValue);//字段的值，
					dwrti[ii].setView(true);//字段是否可见
					dwrti[ii].setEdit(true);//字段是否可编辑
					ii++;
				}

				WorkflowRequestTableRecord record = new WorkflowRequestTableRecord();
				record.setWorkflowRequestTableFields(dwrti);
				Dwrtri[j] = record;
			}

			WorkflowDetailTableInfo detailTableInfo  = new WorkflowDetailTableInfo();
			detailTableInfo.setWorkflowRequestTableRecords(Dwrtri);
			wdtis[dt] = detailTableInfo;

		}
		WorkflowBaseInfo wbi = new WorkflowBaseInfo();//流程id
		wbi.setWorkflowId(workflowid);//workflowid

		WorkflowRequestInfo wri = new WorkflowRequestInfo();//流程基本信息
		wri.setCreatorId(createrid);//创建人id
		wri.setRequestLevel("0");//0 正常，1重要，2紧急
		wri.setRequestName(requestname);//流程标题
		wri.setWorkflowMainTableInfo(wmi);//添加主字段数据
		wri.setWorkflowDetailTableInfos(wdtis);//添加明细字段
		wri.setWorkflowBaseInfo(wbi);
		if("0".equals(IsNextFlow)){//是否提交下一节点
			wri.setIsnextflow("0");
		}

		requestid =  Util.getIntValue(impl.doCreateWorkflowRequest(wri, Util.getIntValue(createrid)),0);

		return requestid;
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
	public String creatRequest(String createrid , String workflowid , String requestname , String IsNextFlow, Map<String, String> maintable, Map<String, List<Map<String, String>>> detail){
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
			 writeLog("【主表】Key="+entry.getKey()+"    Value="+entry.getValue());
		}
		mainTableInfo.setProperty(propertyArray);
		requestInfo.setMainTableInfo(mainTableInfo);
		
		//明细信息
		DetailTableInfo detailTableInfo = new DetailTableInfo();
		
		DetailTable[] detailTables = new DetailTable[detail.size()];
		int ds = 0;
		for (Entry<String, List<Map<String, String>>> entry : detail.entrySet()) {
			
			int u= 0;
			DetailTable detailTable = new DetailTable();
			List<Map<String, String>> list = entry.getValue();
			for (Map<String, String> map : list) {
				
				Row row = new Row();
				for (Entry<String, String> rentry : map.entrySet()) {
					Cell cell = new Cell();
					cell.setName(""+rentry.getKey());
					cell.setValue(""+rentry.getValue());
					row.addCell(cell);
					writeLog("【明细表】Key:"+entry.getKey()+"    Value:"+entry.getValue());
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
		    writeLog("[creatRequest requestid]"+requestid);
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			writeLog("[creatRequest]"+e);
		}
		
		return requestid;
	}
	
	/**
	 * 流程请求删除
	 * @param requestid
	 * @param isAcc 是否删除相关附件 （流程签字意见相关附件、以及流程表单关联文档）
	 */
	public void delRequest(int requestid,boolean isAcc){

		if(requestid<=0){
			return;
		}
		RecordSet rs = new RecordSet();
		PoppupRemindInfoUtil remindInfoUtil = new PoppupRemindInfoUtil();
		SysWFLMonitor wflMonitor = new SysWFLMonitor();
		
	
        String candeleteflag = "0";//是否可以删除该类流程
        rs.executeSql("select isdelete from workflow_monitor_bound a, workflow_requestbase b where a.workflowid=b.workflowid and a.monitorhrmid=1 and a.isdelete='1' and b.requestid="+requestid);
        if(rs.next()) candeleteflag = rs.getString("isdelete");
        if(candeleteflag.equals("0")){ 
        	SysRemindWorkflow("该流程不能被删除【requestid:"+requestid+"】", "1", "该流程不能被删除【requestid:"+requestid+"】请给系统管理员相应权限");
        	return ;
        }
        
        //删除流程提醒信息
        remindInfoUtil.deletePoppupRemindInfo(requestid, 0);
		remindInfoUtil.deletePoppupRemindInfo(requestid,1);
        remindInfoUtil.deletePoppupRemindInfo(requestid,10);
	        
	        
		//记录删除日志
        SysMaintenanceLog sysMaintenanceLog = new SysMaintenanceLog();
    	rs.executeSql("select requestname from workflow_requestbase where requestid="+requestid);
    	rs.next();
    	String temprequestname = rs.getString("requestname");
    	
    	sysMaintenanceLog.resetParameter();
    	sysMaintenanceLog.setRelatedId(Util.getIntValue(requestid+""));
    	sysMaintenanceLog.setRelatedName(temprequestname);
    	sysMaintenanceLog.setOperateType("3");
    	sysMaintenanceLog.setOperateDesc("delete workflow_currentoperator where requestid="+requestid+
										";delete workflow_form where requestid="+requestid+
										";delete workflow_formdetail where requestid="+requestid+
										";delete workflow_requestLog where requestid="+requestid+
										";delete workflow_requestViewLog where id="+requestid+
										";delete workflow_requestbase where requestid="+requestid);
    	sysMaintenanceLog.setOperateItem("85");
    	sysMaintenanceLog.setOperateUserid(1);
    	sysMaintenanceLog.setClientAddress("127.0.0.1");
		try {
			sysMaintenanceLog.setSysLogInfo();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			writeLog("[delRequest]"+e.toString());
		}
	    
	    Calendar today = Calendar.getInstance();
	    String formatdate = Util.add0(today.get(Calendar.YEAR), 4) + "-" + Util.add0(today.get(Calendar.MONTH)+1,2) + "-" + Util.add0(today.get(Calendar.DAY_OF_MONTH), 2);
	    String formattime = Util.add0(today.get(Calendar.HOUR_OF_DAY), 2) + ":" + Util.add0(today.get(Calendar.MINUTE), 2) + ":" + Util.add0(today.get(Calendar.SECOND), 2);
		rs.executeSql("insert into WorkFlow_Monitor_Bound(monitorhrmid,workflowid,operatordate,operatortime,isview) values('"+1+"','"+requestid+"','"+formatdate+"','"+formattime+"',0)");
	    
		if(isAcc){//删除流程相关附件
			
			Monitor monitor = new Monitor();
			monitor.delWfAcc(requestid+"");
		}
		
        /*删除流程，如果是资产申请流程，则在删除流程后解冻资产*/
        rs.executeSql(" select r.requestid,r.workflowid,r.currentnodetype from workflow_requestbase r,workflow_base b where requestid in ( " + requestid + " ) and r.workflowid=b.id and b.formid=19 and b.isbill=1" ) ;
        RecordSet RecordSet2=new RecordSet();
        while(rs.next()){
        	String workflowid=rs.getString("workflowid"); //流程id
        	String requestid2=rs.getString("requestid");   //流程请求id
        	//String workflowtype = WorkflowComInfo.getWorkflowtype(workflowid);   //工作流种类
        	String currentnodetype=rs.getString("currentnodetype"); //当前节点状态
        	//流程为资产申请流程且不为 3归档节点 0 创建节点
        	if(!"0".equals(currentnodetype)&&!"3".equals(currentnodetype)){
        		String sql=" select b.* from workflow_form w,bill_CptFetchDetail b where w.requestid ="+requestid+" and w.billid=b.cptfetchid";
        		RecordSet2.executeSql(sql) ;
        		RecordSet RecordSet3=new RecordSet();
        		while(RecordSet2.next()){
	        		String capitalid=RecordSet2.getString("capitalid");
	        		float old_number_n = 0.0f;
					float old_frozennum = 0.0f;
					float new_frozennum = 0.0f;
					RecordSet3.executeSql("select number_n as old_number_n from bill_CptFetchDetail where cptfetchid = (select id from bill_CptFetchMain where requestid="+requestid2+") and capitalid="+capitalid);
					if(RecordSet3.next())	old_number_n = RecordSet3.getFloat("old_number_n");
					RecordSet3.executeSql("select frozennum as old_frozennum from CptCapital where id="+capitalid);
					if(RecordSet3.next()) old_frozennum = RecordSet3.getFloat("old_frozennum");
					 new_frozennum = old_frozennum - old_number_n;
					 RecordSet3.executeSql("update CptCapital set frozennum="+new_frozennum+" where id="+capitalid);
        		}
        	}
        }
        

        wflMonitor.WorkflowDel(requestid+"");
        rs.executeSql(" delete workflow_currentoperator where requestid in ( " + requestid + " ) " ) ;
        rs.executeSql(" delete workflow_form where requestid in ( " + requestid + " ) " ) ;
        rs.executeSql(" delete workflow_formdetail where requestid in ( " + requestid + " ) " ) ;
        rs.executeSql(" delete workflow_requestLog where requestid in ( " + requestid + " ) " ) ;
        rs.executeSql(" delete workflow_requestViewLog where id in ( " + requestid + " ) " ) ;
        rs.executeSql(" delete workflow_requestbase where requestid in ( " + requestid + " ) " ) ;
        rs.executeSql(" delete FnaExpenseInfo where requestid in ( " + requestid + " ) " ) ;//删除财务相关的数据
        	        
	}

	/**
	 * 流程请求删除
	 * @param requestid
	 * @param isAcc 是否删除相关附件 （流程签字意见相关附件、以及流程表单关联文档）
	 * @param isdelete 判断是否有删除权限
	 */
	public void delRequest(int requestid,boolean isAcc,boolean isdelete){

		if(requestid<=0){
			return;
		}
		RecordSet rs = new RecordSet();
		PoppupRemindInfoUtil remindInfoUtil = new PoppupRemindInfoUtil();
		SysWFLMonitor wflMonitor = new SysWFLMonitor();


		if(isdelete) {
			String candeleteflag = "0";//是否可以删除该类流程
			rs.executeSql("select isdelete from workflow_monitor_bound a, workflow_requestbase b where a.workflowid=b.workflowid and a.monitorhrmid=1 and a.isdelete='1' and b.requestid=" + requestid);
			if (rs.next()) candeleteflag = rs.getString("isdelete");
			if (candeleteflag.equals("0")) {
				SysRemindWorkflow("该流程不能被删除【requestid:" + requestid + "】", "1", "该流程不能被删除【requestid:" + requestid + "】请给系统管理员相应权限");
				return;
			}
		}

		//删除流程提醒信息
		remindInfoUtil.deletePoppupRemindInfo(requestid, 0);
		remindInfoUtil.deletePoppupRemindInfo(requestid,1);
		remindInfoUtil.deletePoppupRemindInfo(requestid,10);


		//记录删除日志
		SysMaintenanceLog sysMaintenanceLog = new SysMaintenanceLog();
		rs.executeSql("select requestname from workflow_requestbase where requestid="+requestid);
		rs.next();
		String temprequestname = rs.getString("requestname");

		sysMaintenanceLog.resetParameter();
		sysMaintenanceLog.setRelatedId(Util.getIntValue(requestid+""));
		sysMaintenanceLog.setRelatedName(temprequestname);
		sysMaintenanceLog.setOperateType("3");
		sysMaintenanceLog.setOperateDesc("delete workflow_currentoperator where requestid="+requestid+
				";delete workflow_form where requestid="+requestid+
				";delete workflow_formdetail where requestid="+requestid+
				";delete workflow_requestLog where requestid="+requestid+
				";delete workflow_requestViewLog where id="+requestid+
				";delete workflow_requestbase where requestid="+requestid);
		sysMaintenanceLog.setOperateItem("85");
		sysMaintenanceLog.setOperateUserid(1);
		sysMaintenanceLog.setClientAddress("127.0.0.1");
		try {
			sysMaintenanceLog.setSysLogInfo();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			writeLog("[delRequest]"+e.toString());
		}

		Calendar today = Calendar.getInstance();
		String formatdate = Util.add0(today.get(Calendar.YEAR), 4) + "-" + Util.add0(today.get(Calendar.MONTH)+1,2) + "-" + Util.add0(today.get(Calendar.DAY_OF_MONTH), 2);
		String formattime = Util.add0(today.get(Calendar.HOUR_OF_DAY), 2) + ":" + Util.add0(today.get(Calendar.MINUTE), 2) + ":" + Util.add0(today.get(Calendar.SECOND), 2);
		rs.executeSql("insert into WorkFlow_Monitor_Bound(monitorhrmid,workflowid,operatordate,operatortime,isview) values('"+1+"','"+requestid+"','"+formatdate+"','"+formattime+"',0)");

		if(isAcc){//删除流程相关附件

			Monitor monitor = new Monitor();
			monitor.delWfAcc(requestid+"");
		}

		/*删除流程，如果是资产申请流程，则在删除流程后解冻资产*/
		rs.executeSql(" select r.requestid,r.workflowid,r.currentnodetype from workflow_requestbase r,workflow_base b where requestid in ( " + requestid + " ) and r.workflowid=b.id and b.formid=19 and b.isbill=1" ) ;
		RecordSet RecordSet2=new RecordSet();
		while(rs.next()){
			String workflowid=rs.getString("workflowid"); //流程id
			String requestid2=rs.getString("requestid");   //流程请求id
			//String workflowtype = WorkflowComInfo.getWorkflowtype(workflowid);   //工作流种类
			String currentnodetype=rs.getString("currentnodetype"); //当前节点状态
			//流程为资产申请流程且不为 3归档节点 0 创建节点
			if(!"0".equals(currentnodetype)&&!"3".equals(currentnodetype)){
				String sql=" select b.* from workflow_form w,bill_CptFetchDetail b where w.requestid ="+requestid+" and w.billid=b.cptfetchid";
				RecordSet2.executeSql(sql) ;
				RecordSet RecordSet3=new RecordSet();
				while(RecordSet2.next()){
					String capitalid=RecordSet2.getString("capitalid");
					float old_number_n = 0.0f;
					float old_frozennum = 0.0f;
					float new_frozennum = 0.0f;
					RecordSet3.executeSql("select number_n as old_number_n from bill_CptFetchDetail where cptfetchid = (select id from bill_CptFetchMain where requestid="+requestid2+") and capitalid="+capitalid);
					if(RecordSet3.next())	old_number_n = RecordSet3.getFloat("old_number_n");
					RecordSet3.executeSql("select frozennum as old_frozennum from CptCapital where id="+capitalid);
					if(RecordSet3.next()) old_frozennum = RecordSet3.getFloat("old_frozennum");
					new_frozennum = old_frozennum - old_number_n;
					RecordSet3.executeSql("update CptCapital set frozennum="+new_frozennum+" where id="+capitalid);
				}
			}
		}


		wflMonitor.WorkflowDel(requestid+"");
		rs.executeSql(" delete workflow_currentoperator where requestid in ( " + requestid + " ) " ) ;
		rs.executeSql(" delete workflow_form where requestid in ( " + requestid + " ) " ) ;
		rs.executeSql(" delete workflow_formdetail where requestid in ( " + requestid + " ) " ) ;
		rs.executeSql(" delete workflow_requestLog where requestid in ( " + requestid + " ) " ) ;
		rs.executeSql(" delete workflow_requestViewLog where id in ( " + requestid + " ) " ) ;
		rs.executeSql(" delete workflow_requestbase where requestid in ( " + requestid + " ) " ) ;
		rs.executeSql(" delete FnaExpenseInfo where requestid in ( " + requestid + " ) " ) ;//删除财务相关的数据

	}
	
	/**
	 * 流程请求删除
	 * @param requestid
	 */
	public void delRequest(int requestid){
		
		delRequest(requestid,false);
		
	}
	
	/**
	 * 流程请求删除
	 * @param requestid
	 * @param isAcc 是否删除相关附件 （流程签字意见相关附件、以及流程表单关联文档）
	 * @param isSRWf  是否提醒（系统默认提醒流程）
	 * @param operators 提醒人
	 * @return
	 */
	public void delRequest(int requestid, boolean isAcc, boolean isSRWf, String operators){
		
		delRequest(requestid,isAcc);
		if(isSRWf){
			RecordSet rs  = new RecordSet();
			rs.executeSql("select requestname from workflow_requestbase where requestid="+requestid);
	    	rs.next();
	    	String temprequestname = rs.getString("requestname");
	    	
			
			String requestname = "提醒"+temprequestname+" 已被删除";
			//String operators = "";
			String remark = ""+temprequestname+" 流程已被删除";
			SysRemindWorkflow(requestname, operators, remark);
		}
	}
	
	/**
	 * 当前流程停留的节点
	 * @param requestid
	 * @return
	 */
	public int getReqeustNode(int requestid){
		
		int node  = 0;
		
		RecordSet rs = new RecordSet();
		
		String sql = "select currentnodeid from workflow_requestbase where requestid = "+requestid;
		rs.executeSql(""+sql);
		if(rs.next()){
			node = rs.getInt("currentnodeid");
		}
		return node;
	}
	
	/**
	 * 当前请求的 workflowid
	 * @param requestid
	 * @return
	 */
	public static int getWorkflowid(int requestid){
		
		int node  = 0;
		
		RecordSet rs = new RecordSet();
		
		String sql = "select workflowid from workflow_requestbase where requestid = "+requestid;
		rs.executeSql(""+sql);
		if(rs.next()){
			node = rs.getInt("workflowid");
		}
		return node;
	}
	
	/**
	 * 获取请求当前操作者
	 * @param requestid
	 * @return
	 */
	public String getCurroperators(int requestid){
		
		String operators = "";
		RecordSet rs = new RecordSet();
		
		String sql = "select userid from workflow_currentoperator where isremark=0 and requestid = "+requestid;
		rs.executeSql(""+sql);
		while(rs.next()){
			
			operators += ","+rs.getInt("userid");
		}
		if(operators.indexOf(",")==0){
			operators = operators.substring(1);
		}
		
		return operators;
	}
	
	/**
	 * 获取请求当前操作者
	 * @param requestid
	 * @return
	 */
	public String getCurroperator(int requestid){
		
		String operators = "";
		RecordSet rs = new RecordSet();
		
		String sql = "select userid from workflow_currentoperator where isremark=0 and requestid = "+requestid;
		rs.executeSql(""+sql);
		if(rs.next()){
			
			operators = rs.getInt("userid")+"";
		}
		
		writeLog("getCurroperator["+requestid+"]"+operators);
		
		return operators;
	}
	
	/**
	 * 流程提交
	 * @param requestid 请求id
	 * @param userId	提交人
	 * @param remark	提交意见
	 * @return
	 */
	public  boolean nextNodeBySubmit(int requestid, int userId, String remark) {
		writeLog("【流程提交】");
		boolean oaflag = false;
		
		RequestService rss = new RequestService();
		RequestInfo info = rss.getRequest(requestid);
		
		oaflag = rss.nextNodeBySubmit(info, requestid, userId, remark); //提交流程
		
		return oaflag;
	}
	
	/**
	 * 流程退回
	 * @param requestid 请求id
	 * @param userId	提交人
	 * @param remark	提交意见
	 * @return
	 */
	public boolean nextNodeByReject(int requestid, int userId, String remark) {
		
		boolean oaflag = false;

		RequestService rss = new RequestService();

		oaflag = rss.nextNodeByReject(requestid, userId, remark);//流程退回
		
		return oaflag;
	}
	
	/**
	 * 触发系统默认提醒流程
	 * @param requestname 流程标题
	 * @param operators  接收人  多人用  , 隔开
	 * @param remark  提醒信息
	 */
	public void SysRemindWorkflow(String requestname, String operators, String remark){
		
		SysRemindWorkflow workflow = new SysRemindWorkflow();
		
		try {
			workflow.setSysRemindInfo(requestname, 0, 0, 0, 0, 1, operators, remark);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 触发系统默认提醒流程
	 * @param requestname 流程标题
	 * @param operators  接收人  多人用  , 隔开
	 * @param remark  提醒信息
	 * @param creater 创建人
	 */
	public void SysRemindWorkflow(String requestname, String operators, String remark, int creater){
		
		SysRemindWorkflow workflow = new SysRemindWorkflow();
		
		try {
			workflow.setSysRemindInfo(requestname, 0, 0, 0, 0, creater, operators, remark);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * 提交某流程的action 此方法（慎用）不要乱用 
	 * @param requestid  请求id
	 * @param customervalue  Action名称
	 */
	public void executeAction(int requestid, String customervalue){
		
		 //String customervalue = "";
		 Action action= (Action) StaticObj.getServiceByFullname(customervalue, Action.class);
         RequestService requestService=new RequestService();
         String msg=action.execute(requestService.getRequest(requestid));
	}

	/**
	 * 强制归档
	 * @param requestid 请求id
	 * @param rs
	 */
	public void mandatoryFiling(String requestid,int userId,RecordSet rs){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String now = sdf.format(new Date());

		SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
		String lastoperatetime = sdf1.format(new Date());

		int wfid = -1;
		Map<String,String> zf = new HashMap<String, String>();

		//修改流程基本信息
		rs.executeProc("workflow_Requestbase_SByID", requestid+"");
		if (rs.next()){
			int lastnodeid = rs.getInt("currentnodeid");
			int lastnodetype = Util.getIntValue(rs.getString("currentnodetype"), 0);
			wfid = rs.getInt("workflowid");
			zf.put("requestname","【终止】"+rs.getString("requestname"));
			zf.put("requestnamenew","【终止】"+rs.getString("requestname"));
			zf.put("lastnodeid",lastnodeid+"");
			zf.put("lastnodetype",lastnodetype+"");
			zf.put("currentnodetype","3");
			zf.put("currentnodetype","3");
			zf.put("status","终止");
			zf.put("passedgroups","0");
			zf.put("totalgroups","1");
			zf.put("lastoperator",userId+"");
			zf.put("lastoperatedate",now);
			zf.put("lastoperatetime",lastoperatetime);
			zf.put("lastoperatortype","1");
			zf.put("nodepasstime","0");
			zf.put("nodelefttime","0");
		}

		int currentnodeid = -1;
		rs.execute("select nodeid from workflow_flownode where workflowid = " + wfid + " and nodetype = 3");
		if (rs.next()){
			currentnodeid = Util.getIntValue(rs.getString("nodeid"));
		}
		zf.put("currentnodeid",currentnodeid+"");
		rs.execute(ToolsFunction.UpSql("workflow_requestbase",zf," where requestid="+requestid));

		rs.execute("delete from workflow_nownode where requestid="+requestid);
		rs.execute("insert into workflow_nownode(requestid,nownodeid,nownodetype,nownodeattribute) values("+requestid+","+currentnodeid+",3,0)");
		rs.execute("update workflow_currentoperator set isremark = '2'  where requestid = "+requestid+" and nodeid = "+currentnodeid+" and isremark in ('0','8','9','7')");
		rs.execute("update  workflow_currentoperator  set isremark='4'  where isremark='0' and requestid = "+requestid);
		rs.execute("update  workflow_currentoperator  set iscomplete=1  where requestid = "+requestid);
	}

	/**
	 * 强制退回上一个节点
	 * @param requestid 请求id
	 * @param rs
	 */
	public void mandatoryReject(String requestid,int userId,RecordSet rs,String remark){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String now = sdf.format(new Date());

		SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
		String lastoperatetime = sdf1.format(new Date());

		Map<String,String> zf = new HashMap<String, String>();
		Map<String,String> log = new HashMap<String, String>();
		Map<String,String> ctrl = new HashMap<String, String>();
		int currentnodeid = -1;
		int nownodetype = 0;
		int workflowid = -1;

		//修改流程基本信息
		rs.executeProc("workflow_Requestbase_SByID", requestid+"");
		if (rs.next()){
			int lastnodeid = rs.getInt("currentnodeid");
			int lastnodetype = Util.getIntValue(rs.getString("currentnodetype"), 0);
			currentnodeid = Util.getIntValue(rs.getString("lastnodeid"), 0);
			nownodetype = Util.getIntValue(rs.getString("lastnodetype"), 0);
			zf.put("lastnodeid",lastnodeid+"");
			zf.put("lastnodetype",lastnodetype+"");
			zf.put("currentnodetype",nownodetype+"");
			zf.put("currentnodeid",currentnodeid+"");
			zf.put("status","Revoke");
			zf.put("passedgroups","0");
			zf.put("totalgroups","1");
			zf.put("lastoperator",userId+"");
			zf.put("lastoperatedate",now);
			zf.put("lastoperatetime",lastoperatetime);
			zf.put("lastoperatortype","1");
			zf.put("nodepasstime","0");
			zf.put("nodelefttime","0");

			log.put("requestid",requestid);
			workflowid = rs.getInt("workflowid");
			log.put("workflowid",workflowid+"");
			log.put("nodeid",lastnodeid+"");
			log.put("logtype","3");
			log.put("operatedate",now);
			log.put("operatetime",lastoperatetime);
			log.put("operator",userId+"");
			log.put("remark",remark);
			log.put("operatortype","1");
			log.put("showorder","0");
			log.put("agentorbyagentid","-1");
			log.put("agenttype","0");
			log.put("requestLogId","0");
			log.put("destnodeid",lastnodeid+"");

			ctrl.put("requestid",requestid);
			ctrl.put("workflowid",workflowid+"");
			ctrl.put("isremark","0");
			ctrl.put("usertype","1");
			ctrl.put("nodeid",currentnodeid+"");
			ctrl.put("agentorbyagentid","-1");
			ctrl.put("agenttype","0");
			ctrl.put("showorder","0");
			ctrl.put("receivedate",now);
			ctrl.put("receivetime",lastoperatetime);
			ctrl.put("viewtype","0");
			ctrl.put("iscomplete","0");
		}

		boolean flag = rs.execute(ToolsFunction.UpSql("workflow_requestbase", zf, " where requestid=" + requestid));
		rs.execute("select receivedPersons,receivedpersonids from workflow_requestlog where requestid="+requestid+" and destnodeid="+log.get("destnodeid")+" order by LOGID");
		if (rs.next()){
			log.put("receivedPersons",rs.getString("receivedPersons"));
			log.put("receivedpersonids",rs.getString("receivedpersonids"));
		}
		rs.execute("select departmentid from Hrmresource where id="+userId);
		if (rs.next()){
			log.put("operatorDept",rs.getString("departmentid"));
		}
		rs.execute(ToolsFunction.InserSql("workflow_requestlog",log));
		rs.execute("delete from workflow_nownode where requestid="+requestid);
		rs.execute("insert into workflow_nownode(requestid,nownodeid,nownodetype,nownodeattribute) values("+requestid+","+currentnodeid+","+nownodetype+",0)");
		rs.execute("update workflow_currentoperator set isremark = '2'  where requestid = "+requestid+" and nodeid = "+currentnodeid+" and isremark in ('0','8','9','7') and userid="+userId);

		int groupid = 0;
		rs.execute("select max(groupid) groupid from workflow_currentoperator where requestid= "+requestid+" and nodeid="+currentnodeid);
		if (rs.next()){
			groupid = rs.getInt("groupid");
		}

		rs.execute("select workflowtype from workflow_base where id="+workflowid);
		if (rs.next()){
			ctrl.put("workflowtype",rs.getString("workflowtype"));
		}
		RecordSet rs1 = new RecordSet();
		rs.execute("select userid from workflow_currentoperator where requestid="+requestid+" and nodeid="+currentnodeid+" and islasttimes = 1 and isremark in (0,2)");
		while (rs.next()){
			ctrl.put("userid",rs.getString("userid"));
			groupid++;
			ctrl.put("groupid",groupid+"");
			rs1.execute("update workflow_currentoperator set islasttimes=0 where userid="+rs.getString("userid")+" and requestid="+requestid+" and nodeid="+currentnodeid+" and islasttimes = 1");
			rs1.execute(ToolsFunction.InserSql("workflow_currentoperator",ctrl));
		}
	}

}
