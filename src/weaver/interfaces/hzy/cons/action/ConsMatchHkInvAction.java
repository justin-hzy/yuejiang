package weaver.interfaces.hzy.cons.action;

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

public class ConsMatchHkInvAction extends BaseBean implements Action {

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    private String getBatchHkInventoryUrl = getPropValue("k3_api_config","getBatchHkInventoryUrl");


    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("开始执行 ConsMatchHkInvAction");

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        List<Map<String, String>> detailDatas6 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 6);

        writeLog("月结寄售明细6数据="+detailDatas6.toString());

        if (detailDatas6.size()>0){
            int id = requestInfo.getRequestManager().getBillid();

            writeLog("id="+id);

            RecordSet dt6Rs = new RecordSet();

            String querySql = "select sku,quantity from formtable_main_238_dt6 where mainid = ?";

            dt6Rs.executeQuery(querySql,id);

            JSONArray skuArr = new JSONArray();

            List<String> skus = new ArrayList<>();

            List<Map<String, String>> dt6MapList = new ArrayList<>();

            while (dt6Rs.next()){

                Map<String, String> dtSum = new HashMap<>();
                String sku = dt6Rs.getString("sku");
                String quantity = dt6Rs.getString("quantity");

                dtSum.put("sku", sku);
                dtSum.put("quantity", quantity);

                skus.add(sku);

                skuArr.add(sku);
                dt6MapList.add(dtSum);
            }

            writeLog("dt6MapList="+dt6MapList);

            JSONObject reqJson = new JSONObject();

            reqJson.put("skus",skuArr);
            reqJson.put("stockNumber","S1");
            K3Service k3Service = new K3Service();
            String params = reqJson.toJSONString();
            String respStr = k3Service.doK3Action(params,k3Ip,getBatchHkInventoryUrl);

            InventoryService inventoryService = new InventoryService();

            List<Map<String, String>> k3InvList = inventoryService.anlysBatIn(respStr,skus);

            writeLog("k3InvList=" + k3InvList);


            String lcbh = mainData.get("lcbh");

            compareInv(k3InvList, dt6MapList, lcbh);

        }
        return SUCCESS;
    }


    public void compareInv(List<Map<String,String>> k3InvList,List<Map<String,String>> dt6MapList,String lcbh){

        List<Map<String,String>> hkSales = new ArrayList<>();

        writeLog("dt6MapList="+dt6MapList.toString());

        for (Map<String,String> dt6Map : dt6MapList) {

            String sku = dt6Map.get("sku");

            String hkQuantity = dt6Map.get("quantity");


            for (Map<String,String> k3Inv : k3InvList){
                String invSku = k3Inv.get("sku");
                //writeLog("sku="+sku);

                if(sku.equals(invSku)){

                    /*writeLog("金蝶-sku="+sku);
                    writeLog("dms-wlbm="+wlbm);*/


                    Map<String,String> hkSale = new HashMap<>();
                    String fBaseQty = k3Inv.get("fBaseQty");
//                    writeLog("fBaseQty="+fBaseQty);

                    /*writeLog("fBaseQty="+fBaseQty);
                    writeLog("xssl="+xssl);*/

                    if(Integer.valueOf(fBaseQty)<0){
                        String updateSql = "update formtable_main_238 set match_inv_fail = ? where lcbh = ?";
                        RecordSet rs = new RecordSet();
                        rs.executeUpdate(updateSql,0,lcbh);
                    }else {
                        writeLog("fBaseQty"+Integer.valueOf(fBaseQty));
                        writeLog("hkQuantity"+Integer.valueOf(hkQuantity));
                        if(Integer.compare(Integer.valueOf(fBaseQty),Integer.valueOf(hkQuantity)) < 0){
                                writeLog("香港库存不足,匹配香港库存失败");
                            String updateSql = "update formtable_main_238 set match_inv_fail = ? ,is_gyj_hk = ? where lcbh = ?";
                            RecordSet rs = new RecordSet();
                            rs.executeUpdate(updateSql,0,1,lcbh);
                        }else {
                            writeLog("香港库存足够,匹配香港库存成功");
                            String updateSql = "update formtable_main_238 set is_gyj_hk = ? where lcbh = ?";
                            RecordSet rs = new RecordSet();
                            rs.executeUpdate(updateSql,0,lcbh);
                        }
                    }
                }
            }
        }
    }

}
