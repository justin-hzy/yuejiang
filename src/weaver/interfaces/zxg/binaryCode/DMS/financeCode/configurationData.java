package weaver.interfaces.zxg.binaryCode.DMS.financeCode;

import com.api.integration.Base;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class configurationData {

    RecordSet rs = new RecordSet();
    BaseBean baseBean = new BaseBean();
    private String  configwfTable = "uf_tyzxjk"; //配置表名
    //配置字段名 主表
    private String  wfidcolumn = "wfidcolumn"; //流程ID
    private String  modetablename = "modetablename"; //建模表名
    private  String startnownode = "startnownode";//流程发起节点ID
    //配置字段名 明细
    private String  totalamountcolumn_dj = "totalamountcolumn_dj"; //发起冻结金额
    private String  totalamountcolumn_sf = "totalamountcolumn_sf"; //归档释放金额

    private String  frozenamountcolumn = "frozenamountcolumn"; //冻结金额
    private String  usedamountcolumn = "usedamountcolumn"; //已使用金额
    private String  unusedasmountcolumn = "unusedasmountcolumn"; //未使用金额
    private String  conditioncolumn1 = "conditioncolumn1"; //条件字段1
    /**
     * 朱兴光
     * 获取配置表数据
     */
    public  String getDataZb(String wfId,String jmbm) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        String v_sql = " select "+modetablename+","+startnownode+" from "+configwfTable+" where "+wfidcolumn+" = '"+wfId+"' and "+modetablename+" = '"+jmbm+"' ";
        rs.execute(v_sql);
        while (rs.next()){
            jsonObject.put("modetablename",rs.getString("modetablename"));
            jsonObject.put("startnownode",rs.getString("startnownode"));
        }
        return jsonObject.toString();
    }
    public  String getDataPzsjzd(String wfId,String detailed,String jmbm) throws JSONException {

        JSONArray jsonArray = new JSONArray();
        //获取明细表数据
        String table = configwfTable+"_dt"+detailed;
        String v_sql = " select b.* from "+configwfTable+" a,"+table+" b where  a.id = b.mainid and "+wfidcolumn+" = '"+wfId+"' and "+modetablename+" = '"+jmbm+"' ";
        new BaseBean().writeLog("获取配置SQL"+v_sql);
        rs.execute(v_sql);
        while (rs.next()){
            JSONObject jsonObject = new JSONObject();
            Map<String,Object> mapData = new HashMap<String, Object>();
            mapData.put("wfdata",rs.getString("lczd"));
            mapData.put("modedata",rs.getString("jmzd"));
            jsonObject.put(rs.getString("bs"),mapData);
            jsonArray.put(jsonObject.toString());
        }
        new BaseBean().writeLog("ListData"+jsonArray.toString());
        return jsonArray.toString();
    }
}
