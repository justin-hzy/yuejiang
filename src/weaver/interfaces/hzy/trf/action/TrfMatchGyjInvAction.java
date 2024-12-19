package weaver.interfaces.hzy.trf.action;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.service.InventoryService;
import weaver.interfaces.hzy.k3.service.K3Service;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrfMatchGyjInvAction extends BaseBean implements Action {

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    private String getBatchGyjInventoryUrl = getPropValue("k3_api_config","getBatchGyjInventoryUrl");

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("开始执行TrfMatchGyjInvAction");

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        List<Map<String, String>> detailDatas1 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);

        writeLog("调拨信息="+detailDatas1.toString());

        if (detailDatas1.size()>0){
            int id = requestInfo.getRequestManager().getBillid();

            writeLog("id="+id);

            RecordSet dt1Rs = new RecordSet();

            String querySql = "select hptxm,dbsl from formtable_main_228_dt1 where mainid = ?";

            dt1Rs.executeQuery(querySql,id);

            JSONArray skuJsonArr = new JSONArray();
            List<String> skus = new ArrayList<>();

            List<Map<String, String>> dtMapList = new ArrayList<>();

            while (dt1Rs.next()){

                Map<String, String> dtSum = new HashMap<>();
                String trfSku = dt1Rs.getString("hptxm");
                String trfQty = dt1Rs.getString("dbsl");

                dtSum.put("trfSku", trfSku);
                dtSum.put("trfQty", trfQty);

                skuJsonArr.add(trfSku);
                skus.add(trfSku);
                dtMapList.add(dtSum);

            }

            writeLog("dtMapList="+dtMapList);

            JSONObject reqJson = new JSONObject();

            reqJson.put("skus",skuJsonArr);
            reqJson.put("stockNumber","S1");
            K3Service k3Service = new K3Service();
            String params = reqJson.toJSONString();
            String respStr = k3Service.doK3Action(params,k3Ip,getBatchGyjInventoryUrl);

            InventoryService inventoryService = new InventoryService();

            List<Map<String, String>> k3InvList = inventoryService.anlysBatIn(respStr,skus);

            writeLog("k3InvList=" + k3InvList);

            String lcbh = mainData.get("lcbh");

            List<Map<String,String>> twReSaleList = compareInv(k3InvList, dtMapList, inventoryService, lcbh);

            writeLog("twReSaleList="+twReSaleList.toString());

            if(CollUtil.isNotEmpty(twReSaleList)){

                String updateIsTwReSale = "update formtable_main_228 set is_tw_resale = ? where id = ?";
                RecordSet updateRs = new RecordSet();
                updateRs.executeUpdate(updateIsTwReSale,0,id);

                String deleteSql = "DELETE FROM formtable_main_228_dt3 where mainid = ?";
                RecordSet deleteRs = new RecordSet();
                deleteRs.executeUpdate(deleteSql,id);

                for (Map<String,String> twReSale: twReSaleList){

                    String trfSku = twReSale.get("reSaleSku");
                    String reSaleQty = twReSale.get("reSaleQty");

                    String insertSql = "insert into formtable_main_228_dt3 (mainid,sku, resale_qty) values ('"+id+"','"+trfSku+"','"+reSaleQty+"')";
                    RecordSet insertRs = new RecordSet();
                    insertRs.executeUpdate(insertSql);
                }
            }else {
                String updateIsTwReSale = "update formtable_main_228 set is_tw_resale = ? where id = ?";
                RecordSet updateRs = new RecordSet();
                updateRs.executeUpdate(updateIsTwReSale,1,id);

                String deleteSql = "DELETE FROM formtable_main_228_dt3 where mainid = ?";
                RecordSet deleteRs = new RecordSet();
                deleteRs.executeUpdate(deleteSql,id);
            }
        }
        return SUCCESS;
    }


    public List<Map<String,String>> compareInv(List<Map<String,String>> k3InvList,List<Map<String,String>> dtMapList, InventoryService inventoryService,String lcbh){

        List<Map<String,String>> twReSales = new ArrayList<>();

        writeLog("dtMapList="+dtMapList.toString());

        for (Map<String,String> dtSum : dtMapList) {

            String trfSku = dtSum.get("trfSku");

            String trfQty = dtSum.get("trfQty");

            for (Map<String,String> k3Inv : k3InvList){
                String sku = k3Inv.get("sku");
                Map<String,String> twReSale = new HashMap<>();

                if(sku.equals(trfSku)){
                    String fBaseQty = k3Inv.get("fBaseQty");
                    //当广悦进库存<调拨单数量，广悦进库存数量全部退回到台湾，故reSaleQty = fBaseQty
                    if(Integer.compare(Integer.valueOf(fBaseQty),Integer.valueOf(trfQty)) < 0){
                        String reSaleQty = fBaseQty;
                        twReSale.put("reSaleSku",trfSku);
                        twReSale.put("reSaleQty",reSaleQty);
                        twReSales.add(twReSale);

                    }else {
                        String reSaleQty = trfQty;
                        ////当广悦进库存>调拨单数量，
                        twReSale.put("reSaleSku",trfSku);
                        twReSale.put("reSaleQty",reSaleQty);
                        twReSales.add(twReSale);
                    }
                }
            }
        }
        return twReSales;
    }

}
