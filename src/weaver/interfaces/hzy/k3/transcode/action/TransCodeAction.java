package weaver.interfaces.hzy.k3.transcode.action;

import cn.hutool.core.collection.CollUtil;
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


    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    private String putHkAssemblyUrl = getPropValue("k3_api_config","putHkAssemblyUrl");

    private String putTwAssemblyUrl = getPropValue("k3_api_config","putTwAssemblyUrl");

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("开始执行TransCodeAction");

        String requestid  = requestInfo.getRequestid();

        K3Service k3Service = new K3Service();

        RequestManager requestManager = requestInfo.getRequestManager();

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);


        List<Map<String, String>> detailDatas3 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 3);

        List<Map<String, String>> detailDatas4 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 4);

        //入库日期
        String inWareDate = mainData.get("rkrq");
        //流程编号
        String processCode = mainData.get("lcbh");

        int id = requestManager.getBillid();

        if(CollUtil.isNotEmpty(detailDatas3)){
            writeLog("detailDatas3="+detailDatas3.toString());

            JSONObject jsonObject = new JSONObject();

            String sql1 = "select dt3.ori_warehouse,dt3.fin_warehouse from formtable_main_244_dt3 dt3 where mainid = ? limit 0,1";

            RecordSet recordSet1 = new RecordSet();
            recordSet1.executeQuery(sql1,id);
            String oriWareHouse = "";
            String finWarehouse = "";

            while (recordSet1.next()){
                oriWareHouse = recordSet1.getString("ori_warehouse");
                finWarehouse = recordSet1.getString("fin_warehouse");
            }

            jsonObject.put("fillno",processCode);
            jsonObject.put("faffairtype","Dassembly");
            jsonObject.put("fdate",inWareDate);

            String sql2 = "select dt3.fin_sku,dt3.ori_sku,dt3.qty from formtable_main_244_dt3 dt3 where mainid = ?";

            RecordSet recordSet2 = new RecordSet();
            recordSet2.executeQuery(sql2,id);

            JSONArray assyFEntities = new JSONArray();

            while (recordSet2.next()){
                //货品编码
                String oriSku = recordSet2.getString("ori_sku");
                //货品变更好编码
                String finSku = recordSet2.getString("fin_sku");
                //实际变更数量
                String qty = recordSet2.getString("qty");
                JSONObject assyFEntitiy = new JSONObject();
                assyFEntitiy.put("fmaterialid",oriSku);
                assyFEntitiy.put("fqty",qty);
                assyFEntitiy.put("fstockid",oriWareHouse);
                assyFEntitiy.put("frefbomid","");

                JSONArray assyFSubEntities = new JSONArray();

                JSONObject assyFSubEntitiy = new JSONObject();

                assyFSubEntitiy.put("fmaterialdsety",finSku);

                assyFSubEntitiy.put("fqtysety",qty);

                assyFSubEntitiy.put("fstockidsety",finWarehouse);

                assyFSubEntities.add(assyFSubEntitiy);

                assyFEntitiy.put("assyFSubEntities",assyFSubEntities);

                assyFEntities.add(assyFEntitiy);

                jsonObject.put("assyFEntities",assyFEntities);
            }
            jsonObject.put("fstockorgid","ZT021");
            String param = jsonObject.toJSONString();
            writeLog("param="+jsonObject);
            String resStr = k3Service.doK3Action(param,k3Ip,putHkAssemblyUrl);
            JSONObject resJson = JSONObject.parseObject(resStr);
            writeLog("resJson="+resJson);
        }


        if(CollUtil.isNotEmpty(detailDatas4)){
            writeLog("detailDatas4="+detailDatas4.toString());
            JSONObject jsonObject = new JSONObject();
            String sql1 = "select dt4.ori_warehouse,dt4.fin_warehouse from formtable_main_244_dt4 dt4 where mainid = ? limit 0,1";

            RecordSet recordSet1 = new RecordSet();
            recordSet1.executeQuery(sql1,id);
            String oriWareHouse = "";
            String finWarehouse = "";

            while (recordSet1.next()){
                oriWareHouse = recordSet1.getString("ori_warehouse");
                finWarehouse = recordSet1.getString("fin_warehouse");
            }

            jsonObject.put("fillno",processCode);
            jsonObject.put("faffairtype","Dassembly");
            jsonObject.put("fdate",inWareDate);

            String sql2 = "select dt4.fin_sku,dt4.ori_sku,dt4.qty from formtable_main_244_dt4 dt4 where mainid = ?";

            RecordSet recordSet2 = new RecordSet();
            recordSet2.executeQuery(sql2,id);

            JSONArray assyFEntities = new JSONArray();

            while (recordSet2.next()){
                //货品编码
                String oriSku = recordSet2.getString("ori_sku");
                //货品变更好编码
                String finSku = recordSet2.getString("fin_sku");
                //实际变更数量
                String qty = recordSet2.getString("qty");
                JSONObject assyFEntitiy = new JSONObject();
                assyFEntitiy.put("fmaterialid",oriSku);
                assyFEntitiy.put("fqty",qty);
                assyFEntitiy.put("fstockid",oriWareHouse);
                assyFEntitiy.put("frefbomid","");

                JSONArray assyFSubEntities = new JSONArray();

                JSONObject assyFSubEntitiy = new JSONObject();

                assyFSubEntitiy.put("fmaterialdsety",finSku);

                assyFSubEntitiy.put("fqtysety",qty);

                assyFSubEntitiy.put("fstockidsety",finWarehouse);

                assyFSubEntities.add(assyFSubEntitiy);

                assyFEntitiy.put("assyFSubEntities",assyFSubEntities);

                assyFEntities.add(assyFEntitiy);

                jsonObject.put("assyFEntities",assyFEntities);
            }
            jsonObject.put("fstockorgid","ZT026");
            String param = jsonObject.toJSONString();
            writeLog("param="+jsonObject);
            String resStr = k3Service.doK3Action(param,k3Ip,putTwAssemblyUrl);
            JSONObject resJson = JSONObject.parseObject(resStr);
            writeLog("resJson="+resJson);
        }

        /*
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



        if("ZT026".equals(szzt)){
            jsonObject.put("fstockorgid","ZT026");
            String param = jsonObject.toJSONString();
            writeLog("param="+jsonObject);
            String resStr = k3Service.doK3Action(param,k3Ip,putTwAssemblyUrl);
            JSONObject resJson = JSONObject.parseObject(resStr);
            writeLog("resJson="+resJson);
        }else if ("ZT021".equals(szzt)){
            jsonObject.put("fstockorgid","ZT021");
            String param = jsonObject.toJSONString();
            writeLog("param="+jsonObject);
            String resStr = k3Service.doK3Action(param,k3Ip,putHkAssemblyUrl);
            JSONObject resJson = JSONObject.parseObject(resStr);
            writeLog("resJson="+resJson);
        }*/

        return SUCCESS;

    }
}
