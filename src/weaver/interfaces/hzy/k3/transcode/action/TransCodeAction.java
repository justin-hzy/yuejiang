package weaver.interfaces.hzy.k3.transcode.action;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.weaver.general.BaseBean;
import weaver.conn.RecordSet;
import weaver.interfaces.hzy.k3.assy.service.AssyService;
import weaver.interfaces.hzy.k3.service.K3Service;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;

import java.util.List;
import java.util.Map;

public class TransCodeAction extends BaseBean implements Action {


    private String meIp = getPropValue("fulun_api_config","meIp");

    private String putAssemblyUrl = getPropValue("k3_api_config","putAssemblyUrl");

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("执行 TransCodeAction");

        String requestid  = requestInfo.getRequestid();

        K3Service k3Service = new K3Service();

        RequestManager requestManager = requestInfo.getRequestManager();

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);


        List<Map<String, String>> detailDatas = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);

        int id = requestManager.getBillid();

        JSONObject jsonObject = new JSONObject();


        String sql1 = "select dt1.dqcw1,dt1.bghcw1 from formtable_main_244_dt1 dt1 where mainid = ? limit 0,1";


        RecordSet recordSet1 = new RecordSet();
        recordSet1.executeQuery(sql1,id);
        String dqcw1 = "";
        String bghcw1 = "";

        while (recordSet1.next()){
            dqcw1 = recordSet1.getString("dqcw1");
            bghcw1 = recordSet1.getString("bghcw1");

        }

        String rkrq = mainData.get("rkrq");
        String szzt = mainData.get("szzt");

        if("ZT026".equals(szzt)){
            jsonObject.put("fstockorgid","ZT026");
        }else if ("ZT021".equals(szzt)){
            jsonObject.put("fstockorgid","ZT021");
        }
        String lcbh = mainData.get("lcbh");
        jsonObject.put("fillno",lcbh);
        jsonObject.put("faffairtype","Dassembly");
        jsonObject.put("fdate",rkrq);

        String sql2 = "select dt1.hpbmbghtw,dt1.hpbmtw,dt1.sjbgsl from formtable_main_244_dt1 dt1 where mainid = ?";

        RecordSet recordSet2 = new RecordSet();
        recordSet2.executeQuery(sql2,id);

        JSONArray assyFEntities = new JSONArray();

        while (recordSet2.next()){
            //货品编码
            String hpbmtw = recordSet2.getString("hpbmtw");
            //货品变更好编码
            String hpbmbghtw = recordSet2.getString("hpbmbghtw");
            //实际变更数量
            String sjbgsl = recordSet2.getString("sjbgsl");

            JSONObject assyFEntitiy = new JSONObject();

            assyFEntitiy.put("fmaterialid",hpbmtw);
            assyFEntitiy.put("fqty",sjbgsl);
            assyFEntitiy.put("fstockid",dqcw1);
            assyFEntitiy.put("frefbomid","");

            JSONArray assyFSubEntities = new JSONArray();

            JSONObject assyFSubEntitiy = new JSONObject();

            assyFSubEntitiy.put("fmaterialdsety",hpbmbghtw);

            assyFSubEntitiy.put("fqtysety",sjbgsl);

            assyFSubEntitiy.put("fstockidsety",bghcw1);

            assyFSubEntities.add(assyFSubEntitiy);

            assyFEntitiy.put("assyFSubEntities",assyFSubEntities);

            assyFEntities.add(assyFEntitiy);

            jsonObject.put("assyFEntities",assyFEntities);
        }

        String param = jsonObject.toJSONString();
        writeLog("param="+jsonObject);


        String resStr = k3Service.doK3Action(param,meIp,putAssemblyUrl);
        JSONObject resJson = JSONObject.parseObject(resStr);
        //String code = resJson.getString("code");
        return SUCCESS;

    }
}
