package weaver.interfaces.hzy.inventory.action;

import cn.hutool.core.collection.CollUtil;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.service.InventoryService;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaleHkInvCheckAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("开始执行SaleHkInvCheckAction");

        String requestid = requestInfo.getRequestid();

        InventoryService inventoryService = new InventoryService();

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        String warehouseCode = mainData.get("fhdc");

        //香港出库数据
        List<Map<String, String>> detailDatas3 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 3);

        List<Map<String, String>> dtSums = new ArrayList<>();

        List<String> skus = new ArrayList<>();

        if(CollUtil.isNotEmpty(detailDatas3)){
            for (Map<String, String> dtl : detailDatas3){
                Map<String, String> dtSum = new HashMap<>();
                String sku  = dtl.get("tm");
                String qty = dtl.get("sl");

                dtSum.put("sku", sku);
                dtSum.put("qty", qty);
                dtSums.add(dtSum);

                skus.add(sku);
            }
        }
        writeLog("skus="+skus.toString());

        //批量查询香港库存
        String respStr = inventoryService.getBatchHkInventory(skus,warehouseCode);


        List<Map<String, String>> k3InvList = inventoryService.anlysBatIn(respStr,skus);

        writeLog("k3InvList=" + k3InvList);

        Map<String,List<Map<String,String>>> respMap = compareInv(k3InvList, dtSums, inventoryService, warehouseCode);


        if(respMap.containsKey("hkNotEnoughList")){
            List<Map<String,String>> hkNotEnoughList = respMap.get("hkNotEnoughList");
            if(CollUtil.isNotEmpty(hkNotEnoughList)){
                String errorMessage = "";
                writeLog("hkNotEnoughList=" + hkNotEnoughList.toString());
                for (Map<String,String> hkNotEnough : hkNotEnoughList){
                    String sku = hkNotEnough.get("sku");
                    String qty = hkNotEnough.get("qty");

                    errorMessage = errorMessage+"sku = "+sku+",qty= -"+qty+";";
                }
                String updateSql = "update formtable_main_272 set hk_status  = ?,is_hk_enough = ?,error_message = ? where requestid = ? ";
                writeLog("updateSql="+updateSql);
                RecordSet rs  = new RecordSet();
                rs.executeUpdate(updateSql,"0","1",errorMessage,requestid);
            }
        }else {
            String updateSql = "update formtable_main_272 set hk_status  = ?,is_hk_enough = ?,error_message = ? where requestid = ? ";
            writeLog("updateSql="+updateSql);
            RecordSet rs  = new RecordSet();
            rs.executeUpdate(updateSql,"0","0",null,requestid);
        }

        return SUCCESS;
    }

    public Map<String,List<Map<String,String>>> compareInv(List<Map<String,String>> k3InvList,List<Map<String,String>> dtSums, InventoryService inventoryService,String lcbh){
        Map<String,List<Map<String,String>>> respMap = new HashMap<>();
        List<Map<String,String>> hkNotEnoughList = new ArrayList<>();

        writeLog("dtSums="+dtSums.toString());

        for (Map<String,String> dtSum : dtSums) {
            String sku = dtSum.get("sku");

            String qty = dtSum.get("qty");

            for (Map<String,String> k3Inv : k3InvList){
                String k3Sku = k3Inv.get("sku");
                if(k3Sku.equals(sku)){
                    Map<String,String> hkNotEnough = new HashMap<>();

                    String fBaseQty = k3Inv.get("fBaseQty");

                    if(Integer.compare(Integer.valueOf(fBaseQty),Integer.valueOf(qty)) < 0){
                        hkNotEnough.put("sku",sku);
                        hkNotEnough.put("qty",String.valueOf(Integer.valueOf(fBaseQty) - Integer.valueOf(qty)));
                        hkNotEnoughList.add(hkNotEnough);
                    }
                }
            }
        }

        if(CollUtil.isNotEmpty(hkNotEnoughList)){
            respMap.put("hkNotEnoughList",hkNotEnoughList);
        }

        return respMap;
    }
}