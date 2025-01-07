package weaver.interfaces.hzy.inventory.action;

import cn.hutool.core.collection.CollUtil;
import com.icbc.api.internal.apache.http.impl.cookie.S;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.inventory.service.VirtualInventoryCheckService;
import weaver.interfaces.hzy.k3.service.InventoryService;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

    public class VirtualInventoryCheckAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {

        InventoryService inventoryService = new InventoryService();

        VirtualInventoryCheckService virtualInventoryCheckService = new VirtualInventoryCheckService();

        writeLog("开始执行VirtualInventoryCheckAction");

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        String Delivery = mainData.get("fhdc");

        String processId = mainData.get("lcbh");

        String requestid = requestInfo.getRequestid();

        List<Map<String, String>> detailDatas1 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);

        writeLog("detailDatas1="+detailDatas1.toString());

        List<Map<String, String>> hkReTrfList = new ArrayList<>();

        List<Map<String,String>> twReTrfList = new ArrayList<>();

        List<Map<String,String>> twNotEnoughList = new ArrayList<>();

        if (detailDatas1.size() > 0) {

            List<String> skus = new ArrayList<>();

            String sql1 = "select main.id,dt1.hptxm tm,sum(dt1.fhl) sl from formtable_main_263 main inner join formtable_main_263_dt1 dt1 on main.id = dt1.mainid " +
                    "where dt1.fhl is not null and dt1.fhl > 0 and main.requestId = ? group by main.id, dt1.hptxm";

            RecordSet dtRs = new RecordSet();

            dtRs.executeQuery(sql1, requestid);

            List<Map<String, String>> dtSums = new ArrayList<>();

            while (dtRs.next()) {
                Map<String, String> dtSum = new HashMap<>();
                String id  = dtRs.getString("id");
                String tm = dtRs.getString("tm");
                String sl = dtRs.getString("sl");
                //含税单价
                //String xsj = dtRs.getString("xsj");

                dtSum.put("id",id);
                dtSum.put("tm", tm);
                dtSum.put("sl", sl);
                //dtSum.put("xsj", xsj);

                dtSums.add(dtSum);
            }


            for (Map<String, String> detailData : detailDatas1) {
                String sku = detailData.get("hptxm");
                if (!skus.contains(sku)){
                    skus.add(sku);
                }
            }

            String respStr = inventoryService.getBatchTwInventory(skus, Delivery);

            writeLog("respStr="+respStr);

            List<Map<String, String>> k3InvList = inventoryService.anlysBatIn(respStr,skus);
            writeLog("k3InvList=" + k3InvList);

            Map<String,List<Map<String,String>>> respMap = virtualInventoryCheckService.compareInv(k3InvList, dtSums, inventoryService, processId);


            if (respMap.containsKey("twNotEnoughList")){
                twNotEnoughList = respMap.get("twNotEnoughList");
                writeLog("twNotEnoughList="+twNotEnoughList.toString());
            }

            if(respMap.containsKey("hkReTrfList")){
                hkReTrfList = respMap.get("hkReTrfList");
                writeLog("hkReTrfList="+hkReTrfList.toString());
            }

            if(respMap.containsKey("twReTrfList")){
                twReTrfList = respMap.get("twReTrfList");
                writeLog("twReTrfList="+twReTrfList.toString());
            }

            RecordSet rs  = new RecordSet();
            String updateHkStatusSql = "update formtable_main_263 set retrf_hk_status  = ? ,is_tw_enough = ? where requestid = ? ";

            if(CollUtil.isNotEmpty(twNotEnoughList)){
                String updateTwStatusSql = "update formtable_main_263 set is_tw_enough  = ? where requestid = ? ";
                rs.executeUpdate(updateTwStatusSql,1,requestid);
            }else {
                //默认 reTrfHkStatus = 1 不需要香港出单
                int reTrfHkStatus = 1;
                if (CollUtil.isNotEmpty(hkReTrfList) && CollUtil.isEmpty(twNotEnoughList)) {
                    reTrfHkStatus = 0;
                    rs.executeUpdate(updateHkStatusSql, reTrfHkStatus, 0, requestid);

                    String id = getSaleId(requestid);
                    String deleteSql = "DELETE FROM formtable_main_263_dt3 where mainid = ?";
                    RecordSet deleteRs = new RecordSet();
                    deleteRs.executeUpdate(deleteSql, id);

                    for (Map<String, String> hkTrf : hkReTrfList) {

                        String tm = hkTrf.get("tm");
                        String reTrfQty = hkTrf.get("sl");

                        String insertSql = "insert into formtable_main_263_dt3 (mainid,sku,retrf_qty) values ('" + id + "','" + tm + "','" + reTrfQty + "')";
                        RecordSet insertRs = new RecordSet();
                        insertRs.executeUpdate(insertSql);
                    }
                }

                if (CollUtil.isNotEmpty(twReTrfList) && CollUtil.isEmpty(twNotEnoughList)){

                    rs.executeUpdate(updateHkStatusSql, reTrfHkStatus, 0, requestid);

                    String id = getSaleId(requestid);
                    String deleteSql = "DELETE FROM formtable_main_263_dt4 where mainid = ?";
                    RecordSet deleteRs = new RecordSet();
                    deleteRs.executeUpdate(deleteSql, id);

                    for (Map<String, String> twTrf : twReTrfList) {

                        String tm = twTrf.get("tm");
                        String reTrfQty = twTrf.get("sl");

                        String insertSql = "insert into formtable_main_263_dt4 (mainid,sku,retrf_qty) values ('" + id + "','" + tm + "','" + reTrfQty + "')";
                        RecordSet insertRs = new RecordSet();
                        insertRs.executeUpdate(insertSql);
                    }
                }

//                if (CollUtil.isEmpty(hkReTrfList)) {
//                    rs.executeUpdate(updateHkStatusSql, 1, 0, requestid);
//                }

            }
        }

        return SUCCESS;
    }

        public String getSaleId(String requestid){
            String sql = "select id from formtable_main_263 where requestid = ?";
            RecordSet recordSet = new RecordSet();
            recordSet.executeQuery(sql,requestid);
            String id = "";
            if (recordSet.next()){
                id = recordSet.getString("id");
            }
            return id;
        }
}
