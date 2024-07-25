package weaver.interfaces.hzy.k3.assy.action;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.service.K3Service;
import weaver.interfaces.hzy.k3.assy.service.AssyService;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;

import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AssyAction extends BaseBean implements Action {

    private String meIp = getPropValue("fulun_api_config","meIp");

    private String putAssemblyUrl = getPropValue("k3_api_config","putAssemblyUrl");

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("执行 AssyAction");

        K3Service k3Service = new K3Service();

        String requestid  = requestInfo.getRequestid();

        RequestManager requestManager = requestInfo.getRequestManager();

        AssyService assyService = new AssyService();

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);


        List<Map<String, String>> detailDatas1 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);

        int id = requestManager.getBillid();

        String sql = "select bombm,fxwlbm,sjztfxsl,spbm,sjztsl,xq,pch from formtable_main_242_dt1 where mainid = ?";

        RecordSet recordSet = new RecordSet();

        recordSet.executeQuery(sql,String.valueOf(id));

        JSONObject jsonObject = new JSONObject();

        String lcbh = mainData.get("lcbh");

        String lx = mainData.get("lx");

        jsonObject.put("fillno",lcbh);

        String  szzt = mainData.get("szzt");

        String fhdc1 = mainData.get("fhdc1");

        String shdc1 = mainData.get("shdc1");

        //实际完成时间
        String sjwcsj = mainData.get("sjwcsj");

        if("ZT026".equals(szzt)){
            jsonObject.put("fstockorgid","ZT026");
        }else if ("ZT021".equals(szzt)){
            jsonObject.put("fstockorgid","ZT021");
        }


        if(lx.equals("0")){
            jsonObject.put("faffairtype","Assembly");
        }else if(lx.equals("1")){
            jsonObject.put("faffairtype","Dassembly");
        }


        jsonObject.put("fdate",sjwcsj);

        JSONArray assyFEntities = new JSONArray();


        List<String> list = new ArrayList<>();

        while (recordSet.next()){

            String fxwlbm = recordSet.getString("fxwlbm");
            String bombm = recordSet.getString("bombm");
            String sjztfxsl = recordSet.getString("sjztfxsl");

//            writeLog("fxwlbm="+fxwlbm);
//            writeLog("bombm="+bombm);
//            writeLog("sjztfxsl="+sjztfxsl);

            if(!list.contains(fxwlbm)){
                list.add(fxwlbm);
                JSONObject assyFEntitiy = new JSONObject();
                assyFEntitiy.put("fmaterialid",fxwlbm);
                assyFEntitiy.put("fqty",sjztfxsl);
                assyFEntitiy.put("fstockid",shdc1);
                assyFEntitiy.put("frefbomid",bombm);

                JSONArray assyFSubEntities = new JSONArray();

                JSONObject assyFSubEntitiy = new JSONObject();

                String spbm = recordSet.getString("spbm");

                String sjztsl = recordSet.getString("sjztsl");

                assyFSubEntitiy.put("fmaterialdsety",spbm);

                assyFSubEntitiy.put("fqtysety",sjztsl);

                assyFSubEntitiy.put("fstockidsety",fhdc1);

                assyFSubEntities.add(assyFSubEntitiy);

                assyFEntitiy.put("assyFSubEntities",assyFSubEntities);

                assyFEntities.add(assyFEntitiy);

                jsonObject.put("assyFEntities",assyFEntities);
            }
            else {
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

                       assyFSubEntitiy.put("fstockidsety",fhdc1);

                       assyFSubEntities.add(assyFSubEntitiy);
                   }
                }
            }
        }
        list.clear();
        String param = jsonObject.toJSONString();
        writeLog("param="+jsonObject);


        String resStr = k3Service.doK3Action(param,meIp,putAssemblyUrl);
        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");
        return SUCCESS;
    }
}
