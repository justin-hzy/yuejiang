package weaver.interfaces.zxg.binaryCode.DMS.financeCode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class Wf_launchAction implements Action {
    /**
     * 流程发起及归档
     */
    //当前金额
    private String  totalamountcolumn_wf_dj;
    private String  totalamountcolumn_wf_sf;
    private String  totalamountcolumn_mode;
    //冻结金额
    private String  frozenamountcolumn_wf ;
    private String  frozenamountcolumn_mode;
    //已使用金额
    private String  usedamountcolumn_wf;
    private String  usedamountcolumn_mode;
    //未使用金额
    private String  unusedamountcolumn_wf;
    private String  unusedamountcolumn_mode;
    //条件字段1
    private String  conditioncolumn1_wf;
    private String  conditioncolumn1_mode;
    private String detailed;//明细标识
    private String jmbm;//建模表名


    @Override
    public String execute(RequestInfo requestInfo) {
        RecordSet rs = new RecordSet();
        String requestId = requestInfo.getRequestid();
        String tablename = requestInfo.getRequestManager().getBillTableName();
        String workflowid = requestInfo.getWorkflowid();
        new BaseBean().writeLog("当前流程ID"+workflowid+"流程表名"+tablename+"流程requestId"+requestId);
        //获取配置信息
        configurationData configurationData = new configurationData();
        try {
            JSONObject jsonObjectZb = new JSONObject(configurationData.getDataZb(workflowid,jmbm));
            String jsonMx =  configurationData.getDataPzsjzd(workflowid,detailed,jmbm);
            new BaseBean().writeLog("jsonZb"+jsonObjectZb.toString());
            new BaseBean().writeLog("jsonMx"+jsonMx);
            JSONArray datas = new JSONArray(jsonMx.replace("\\","").replace("\"{","{").replace("}}\"","}}"));
            for (int i = 0; i < datas.length(); i++) {
                try {
                    JSONObject data = datas.getJSONObject(i);

                    if(String.valueOf(data.names()).contains("totalamountcolumn_dj")){
                        String str_json = String.valueOf(data.get("totalamountcolumn_dj"));
                        JSONObject jsonObject = new JSONObject(str_json);
                        if(!"".equals(jsonObject.get("modedata"))) {
                            totalamountcolumn_mode = (String) jsonObject.get("modedata");
                        }
                        if(!"".equals(jsonObject.get("wfdata"))) {
                            totalamountcolumn_wf_dj = (String) jsonObject.get("wfdata");
                        }
                    }else if(String.valueOf(data.names()).contains("totalamountcolumn_sf")){
                        String str_json = String.valueOf(data.get("totalamountcolumn_sf"));
                        JSONObject jsonObject = new JSONObject(str_json);
                        if(!"".equals(jsonObject.get("wfdata"))) {
                            totalamountcolumn_wf_sf = (String) jsonObject.get("wfdata");
                        }
                    }else if(String.valueOf(data.names()).contains("frozenamountcolumn")){
                        String str_json = String.valueOf(data.get("frozenamountcolumn"));
                        JSONObject jsonObject = new JSONObject(str_json);
                        if(!"".equals(jsonObject.get("modedata"))) {
                            frozenamountcolumn_mode = (String) jsonObject.get("modedata");
                        }
                        if(!"".equals(jsonObject.get("wfdata"))) {
                            frozenamountcolumn_wf = (String) jsonObject.get("wfdata");
                        }
                    }else if(String.valueOf(data.names()).contains("usedamountcolumn")){
                        String str_json = String.valueOf(data.get("usedamountcolumn"));
                        JSONObject jsonObject = new JSONObject(str_json);
                        if(!"".equals(jsonObject.get("modedata"))) {
                            usedamountcolumn_mode = (String) jsonObject.get("modedata");
                        }
                        if(!"".equals(jsonObject.get("wfdata"))) {
                            usedamountcolumn_wf = (String) jsonObject.get("wfdata");
                        }
                    }else if(String.valueOf(data.names()).contains("unusedasmountcolumn")){
                        String str_json = String.valueOf(data.get("unusedasmountcolumn"));
                        JSONObject jsonObject = new JSONObject(str_json);
                        if(!"".equals(jsonObject.get("modedata"))) {
                            unusedamountcolumn_mode = (String) jsonObject.get("modedata");
                        }
                        if(!"".equals(jsonObject.get("wfdata"))) {
                            unusedamountcolumn_wf = (String) jsonObject.get("wfdata");
                        }
                    }else if(String.valueOf(data.names()).contains("conditioncolumn1")){
                        String str_json = String.valueOf(data.get("conditioncolumn1"));
                        JSONObject jsonObject = new JSONObject(str_json);
                        if(!"".equals(jsonObject.get("wfdata"))) {
                            conditioncolumn1_wf = (String) jsonObject.get("wfdata");
                        }
                        if (!"".equals(jsonObject.get("modedata"))) {
                            conditioncolumn1_mode = (String) jsonObject.get("modedata");
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //获取主表MAP
            Map<String, String> mainTableInfo = WorkflowToolMethods.getMainTableInfo(requestInfo);
            //判断是否为创建节点
            String v_sql = "select b.nownodeid from " + tablename + " a,workflow_nownode b where a.requestid = b.requestid and a.requestid = '" + requestId + "'  ";
            rs.execute(v_sql);
            rs.next();
            if (jsonObjectZb.get("startnownode").equals(rs.getString(1))) {
                String v1_sql = "select "+totalamountcolumn_wf_dj+","+conditioncolumn1_wf+"  from  " + tablename + " a where  a.requestid = '" + requestId + "'";
                rs.execute(v1_sql);
                new BaseBean().writeLog("冻结金额SQL" + v1_sql);
                while (rs.next()) {
                    //更新冻结金额 未使用金额
                    String v2_sql = "update " +jsonObjectZb.get("modetablename")+ " set "+frozenamountcolumn_mode+" = ifnull(replace("+frozenamountcolumn_mode+",',',''),'0')+" + rs.getString(1) + "," +unusedamountcolumn_mode+" = ifnull(replace("+totalamountcolumn_mode+",',',''),'0')- ifnull(replace("+frozenamountcolumn_mode+",',',''),'0')- ifnull(replace("+usedamountcolumn_mode+",',',''),'0') where "+conditioncolumn1_mode+" = '" + rs.getString(2)+"' ";
                    new BaseBean().writeLog("是插入冻结金额SQL" + v2_sql);
                    rs.execute(v2_sql);
                }
            }else{
                new BaseBean().writeLog("归档");
                //释放冻结金额，更新已使用金额
                String v3_sql = "select "+totalamountcolumn_wf_dj+","+conditioncolumn1_wf+","+totalamountcolumn_wf_sf+" from  " + tablename + " a  where  a.requestid = '" + requestId + "'";
                rs.execute(v3_sql);
                while (rs.next()) {
                    double djje = rs.getDouble(1);
                    double sfje = rs.getDouble(3);
                    String v4_sql = "";
                    if(djje==sfje){
                        v4_sql = "update " +jsonObjectZb.get("modetablename")+ " set "+frozenamountcolumn_mode+" = ifnull(replace("+frozenamountcolumn_mode+",',',''),'0') -" + rs.getString(1) + ","+usedamountcolumn_mode+" = ifnull(replace("+usedamountcolumn_mode+",',',''),'0')+" + rs.getString(1) + "  where "+conditioncolumn1_mode+" = '" + rs.getString(2)+"'  ";
                    }else {
                        double ksyje = djje - sfje;
                        v4_sql = "update " +jsonObjectZb.get("modetablename")+ " set "+frozenamountcolumn_mode+" = ifnull(replace("+frozenamountcolumn_mode+",',',''),'0') -" + rs.getString(1) + ","+usedamountcolumn_mode+" = ifnull(replace("+usedamountcolumn_mode+",',',''),'0')+" + rs.getString(3) + ", "+unusedamountcolumn_mode+" = ifnull(replace("+unusedamountcolumn_mode+",',',''),'0')+" +ksyje+" where "+conditioncolumn1_mode+" = '" + rs.getString(2)+"'  ";
                    }
                    new BaseBean().writeLog("释放金额SQL" + v4_sql);
                    rs.execute(v4_sql);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "1";
    }

    public String getDetailed() {
        return detailed;
    }

    public void setDetailed(String detailed) {
        this.detailed = detailed;
    }
    public String getJmbm() {
        return jmbm;
    }

    public void setJmbm(String jmbm) {
        this.jmbm = jmbm;
    }
}
