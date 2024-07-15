package weaver.interfaces.hzy.k3.action.assy;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icbc.api.internal.apache.http.impl.cookie.S;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.service.assy.AssyService;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AssyAction extends BaseBean implements Action {


    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("Ö´ÐÐ AssyAction");

        String requestid  = requestInfo.getRequestid();

        RequestManager requestManager = requestInfo.getRequestManager();

        AssyService assyService = new AssyService();

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);


        List<Map<String, String>> detailDatas1 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);

        int id = requestManager.getBillid();

        String sql = "select bombm,fxwlbm,sjztfxsl,spbm,sjztsl,xq,pch from formtable_main_242_dt1 where mainid = ?";

        RecordSet recordSet = new RecordSet();

        recordSet.execute(sql,String.valueOf(id));

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("fillno","");
        jsonObject.put("fstockorgid","");
        jsonObject.put("faffairtype","");
        jsonObject.put("fdate","");

        JSONArray assyFEntities = new JSONArray();


        List<String> list = new ArrayList<>();

        while (recordSet.next()){

            String fxwlbm = recordSet.getString("fxwlbm");
            String bombm = recordSet.getString("bombm");
            String sjztfxsl = recordSet.getString("sjztfxsl");

            if(!list.contains(fxwlbm)){
                JSONObject assyFEntitiy = new JSONObject();
                assyFEntitiy.put("fmaterialid",fxwlbm);
                assyFEntitiy.put("fqty",sjztfxsl);
                assyFEntitiy.put("fstockid","");
                assyFEntitiy.put("frefbomid",bombm);

                JSONArray assyFSubEntities = new JSONArray();

                JSONObject assyFSubEntitiy = new JSONObject();

                String spbm = recordSet.getString("spbm");

                String sjztsl = recordSet.getString("sjztsl");

                assyFSubEntitiy.put("fmaterialdsety",spbm);

                assyFSubEntitiy.put("fqtysety",sjztsl);

                assyFSubEntitiy.put("fstockidsety","");

                assyFSubEntities.add(assyFSubEntitiy);

                assyFEntitiy.put("assyFSubEntities",assyFSubEntities);

                assyFEntities.add(assyFEntitiy);

                jsonObject.put("assyFEntities",assyFEntities);

            }else {
                JSONArray assyFEntities_1 = jsonObject.getJSONArray("assyFEntities");
                for (int i = 0;i<assyFEntities_1.size();i++){
                   JSONObject assyFEntitiy = assyFEntities_1.getJSONObject(i);

                   String frSku = assyFEntitiy.getString("fmaterialid");

                   if(frSku.equals(fxwlbm)){
                       JSONArray assyFSubEntities = assyFEntitiy.getJSONArray("assyFSubEntities");

                       JSONObject assyFSubEntitiy = new JSONObject();

                       String spbm = recordSet.getString("spbm");

                       String sjztsl = recordSet.getString("sjztsl");

                       assyFSubEntitiy.put("fmaterialdsety",spbm);

                       assyFSubEntitiy.put("fqtysety",sjztsl);

                       assyFSubEntitiy.put("fstockidsety","");

                       assyFSubEntities.add(assyFSubEntitiy);
                   }
                }
            }




        }








        return null;
    }
}
