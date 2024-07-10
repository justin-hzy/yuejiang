package weaver.interfaces.tx.util;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import weaver.conn.RecordSet;
import weaver.formmode.data.ModeDataIdUpdate;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.soa.workflow.request.*;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * 接口辅助工具类
 */
public class ToolsFunction extends BaseBean {

	/**
	 * SQL 插入拼接
	 * @param table 表名
	 * @param map key 字段名 value 值
	 * @return
	 */
	public static String InserSql(String table, Map<String, String> map){
		
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("insert into "+table+"(");
		if(map==null){
			return "";
		}
		String colNames = "";
		String valNames = "";
		for (Map.Entry<String, String> tempEntry : map.entrySet()) {
			String key =  tempEntry.getKey();
			String value =  tempEntry.getValue();
			colNames  = colNames +key+",";
			valNames = valNames + "'"+value +"',";
		}
		
		colNames = colNames.substring(0, colNames.length()-1);
		valNames = valNames.substring(0, valNames.length()-1);
		
		buffer.append(colNames+")");
		buffer.append(" values(");
		buffer.append(valNames+")");
		
		//writeLog("InserSql:"+buffer.toString());
		//System.out.println("InserSql:"+buffer.toString());
		return buffer.toString();
	}
	
	/**
	 * SQL update 拼接
	 * @param table 表名
	 * @param map key 字段名 value 值
	 * @param wheres  where 条件
	 * @return
	 */

	public static String UpSql(String table, Map<String, String> map, String wheres){
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("update "+table+" set ");
		if(map==null){
			return "";
		}
		String colNames = "";
		for (Map.Entry<String, String> tempEntry : map.entrySet()) {
			String key =  tempEntry.getKey();
			String value =  tempEntry.getValue();
			colNames  = colNames +key+" = "+"'"+value+"'," ;
		}
		
		colNames = colNames.substring(0, colNames.length()-1);
		
		buffer.append(colNames+" "+wheres);

		//writeLog("UpSql:"+buffer.toString());
		//System.out.println("UpSql:"+buffer.toString());
		return buffer.toString();
	}
	
	/**
	 * select语句拼接
	 */
	public static String SelSql(String table, Map<String, String> map, String wheres) {
		int count = 0;
		StringBuffer buffer = new StringBuffer();
		buffer.append("select ");
		for (String col:map.keySet()) {
			buffer.append(col+",");
			count++;
		}
		if (count>0) {
			buffer.delete(buffer.length()-1, buffer.length());
		}
		
		buffer.append(" from "+table+" "+wheres);
		return buffer.toString();
	}
	
	/**
	 * 获取主表
	 * @param request 请求内容
	 * @return mainTableDataMap 主表内容
	 */
	public static Map<String, String> getMainData(RequestInfo request) {
		Map<String, String> mainTableDataMap = new HashMap<String, String>();
		Property[] properties = request.getMainTableInfo().getProperty();

		for (int i = 0; i < properties.length; i++) {
			String name = properties[i].getName().toLowerCase();
			String value = Util.null2String(properties[i].getValue());
			mainTableDataMap.put(name, value);
		}
		return mainTableDataMap;
	}
 
	/**
	 * 获取明细表
	 * @param request 请求内容
	 * @param index 明细表编号
	 * @return list 明细表内容列表
	 */
	public static List<Map<String, String>> getDetail1Data(RequestInfo request, int index) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();

		DetailTable[] detailtable = request.getDetailTableInfo().getDetailTable();
		if (detailtable.length > index) {
			DetailTable dt = detailtable[index];
			Row[] s = dt.getRow();

			for (int j = 0; j < s.length; j++) {
				Row r = s[j];
				Cell[] c = r.getCell();
				Map<String, String> detailTableDataMap = new HashMap<String, String>();
				for (int k = 0; k < c.length; k++) {
					Cell c1 = c[k];
					String name = c1.getName().toLowerCase();
					String value = Util.null2String(c1.getValue());
					detailTableDataMap.put(name, value);
				}
				list.add(detailTableDataMap);
			}
		}
		return list;
	}
	
	/**
	 * 通过requestid获取主表表单名
	 * @param requestid 请求id
	 * @param rs 
	 * @return tableName 流程表名
	 */
	public static String getTablename(String requestid, RecordSet rs){
        rs.executeSql("select abs(a.formid) as formid from workflow_base a ,workflow_requestbase b where a.id=b.workflowid and b.requestid="+requestid);
        rs.next();
        String formid= Util.null2String(rs.getString("formid"));
        String tableName="formtable_main_"+formid;
        return  tableName;
    }
	
	/**
	 * 模块数据插入操作
	 * @param modeid 模块id
	 * @param tablename 模块表名
	 * @param map 数据详细内容
	 * @param userid 模块创建人
	 * @param  idUpdate ModeDataIdUpdate 表单建模标识
	 * @param  modeRightInfo ModeRightInfo 更新表单建模 权限
	 * @param rs RecordSet
	 * @return flag 执行结果标志 
	 */
	public static int insertMode(int modeid, String tablename, Map<String, String> map, int userid, ModeDataIdUpdate idUpdate, ModeRightInfo modeRightInfo, RecordSet rs) {
		boolean flag = false;
		int billid = -1;//数据id

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");//获取当前日期
		SimpleDateFormat dateFormat2 = new SimpleDateFormat("HH:mm:ss");//获取当前时间
		String currentdate = dateFormat.format(new Date());//给当前日期赋值
		String currenttime = dateFormat2.format(new Date()); //给当前时间赋值

		//新数据插入
		billid = idUpdate.getModeDataNewId(tablename, modeid, userid, 1, currentdate, currenttime);
		
		//详细字段更新
		String wheres = "where id="+billid;
		String upSql = UpSql(tablename, map, wheres);
		flag = rs.executeSql(upSql);

		
		//表单权限更新（权限重构）
		modeRightInfo.editModeDataShare(1, modeid, billid);
		
		return billid;
		
	}
	
	/**
	 * md5加密方法
	 * @param password
	 * @return
	 */
	public String md5Change(String password) {
		try {  
            MessageDigest md = MessageDigest.getInstance("MD5");  // 创建一个md5算法对象
            byte[] messageByte = password.getBytes("UTF-8");  
            byte[] md5Byte = md.digest(messageByte);              // 获得MD5字节数组,16*8=128位  
            password = bytesToHex(md5Byte);                            // 转换为16进制字符串  
        } catch (Exception e) {
            e.printStackTrace();  
        }
		return password;
	}

	
	// 二进制转十六进制  
	private String bytesToHex(byte[] bytes) {
        StringBuffer hexStr = new StringBuffer();
        int num;  
        for (int i = 0; i < bytes.length; i++) {  
            num = bytes[i];  
             if(num < 0) {  
                 num += 256;  
            }  
            if(num < 16){  
                hexStr.append("0");  
            }  
            hexStr.append(Integer.toHexString(num));
        }  
        return hexStr.toString().toUpperCase();  
    }

	/**
	 * 获取下拉框的值对应的显示值
	 * @param requestid 请求id
	 * @param rs RecordSet
	 * @param fieldName 下拉框字段名
	 * @param selectvalue 下拉框值
	 */
	public static String getSelectName(String requestid, RecordSet rs, String fieldName, String selectvalue) {
		String selectName = "";

		StringBuilder sql = new StringBuilder();
		sql.append("select selectname from workflow_selectitem where fieldid in")
				.append("(select id from workflow_billfield where billid in (")
				.append("select a.formid from workflow_base a ,workflow_requestbase b where a.id=b.workflowid and b.requestid=").append(requestid).append(") and fieldname='")
				.append(fieldName).append("')")
				.append(" and selectvalue in (").append(selectvalue).append(")");
		rs.execute(sql.toString());
		while (rs.next()) {
			selectName = Util.null2String(rs.getString("selectname"));
		}
		return selectName;
	}

	/**
	 * 获取显示值对应的下拉框的值
	 * @param billid 表单id
	 * @param rs
	 * @param fieldName 字段名
	 * @param selectName 显示值
	 * @return 选择值
	 */
	public static String getSelectValue(String billid, RecordSet rs, String fieldName, String selectName) {
		String getSelectValue = null;

		StringBuilder sql = new StringBuilder();
		sql.append("select selectvalue from workflow_selectitem where fieldid in (select id from workflow_billfield where billid=").append(billid)
				.append(" and fieldname='").append(fieldName).append("') and selectname='").append(selectName).append("'");
		rs.execute(sql.toString());
		while (rs.next()) {
			getSelectValue = Util.null2String(rs.getString("getSelectValue"));
		}
		return getSelectValue;
	}

	/**
	 * 获取复选下拉框的值对应的显示值
	 * @param requestid 请求id
	 * @param rs RecordSet
	 * @param fieldName 下拉框字段名
	 * @param selectvalue 下拉框值
	 */
	public static String getSelectNames(String requestid, RecordSet rs, String fieldName, String selectvalue) {
		StringBuilder selectName = new StringBuilder();

		StringBuilder sql = new StringBuilder();
		sql.append("select selectname from workflow_selectitem where fieldid in")
				.append("(select id from workflow_billfield where billid in (")
				.append("select a.formid from workflow_base a ,workflow_requestbase b where a.id=b.workflowid and b.requestid=").append(requestid).append(") and fieldname='")
				.append(fieldName).append("')")
				.append(" and selectvalue in (").append(selectvalue).append(")");
		rs.execute(sql.toString());
		while (rs.next()) {
			if (selectName.length()>0){
				selectName.append(",");
			}
			selectName.append(Util.null2String(rs.getString("selectname")));
		}
		return selectName.toString();
	}

	/**
	 * json特殊字符转义
	 */
	public static String jsonChange(String jsonStr) {
		jsonStr = jsonStr.replaceAll("\"", "\\\"");
		jsonStr = jsonStr.replaceAll(":", "\\:");
		jsonStr = jsonStr.replaceAll("\\{", "\\{");
		jsonStr = jsonStr.replaceAll("\\}", "\\}");
		jsonStr = jsonStr.replaceAll("\\[", "\\[");
		jsonStr = jsonStr.replaceAll("\\]", "\\]");

		return jsonStr;
	}

	/**
	 * 将json转换成map
	 */
	public static Map jsonToMap(String json){
		JSONObject jsonObject = JSONObject.fromObject(json);
		Map map = (Map)jsonObject;
		return  map;
	}

	/**
	 * 将jsonArray转换成List-Map
	 */
	public static List<HashMap> jsonToList(JSONArray jsonArray){
		List<HashMap> mapListJson = JSONArray.toList(jsonArray,new HashMap(),new JsonConfig());
		return mapListJson;
	}

}