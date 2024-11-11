package weaver.interfaces.hzy.cons.action;

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

public class ConsMatchTwInvAction  extends BaseBean implements Action {

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    private String getTwBatchInventoryUrl = getPropValue("k3_api_config","getTwBatchInventoryUrl");

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("开始执行ConsMatchTwInvAction");

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        List<Map<String, String>> detailDatas5 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 5);

        writeLog("月结寄售-台湾明细5数据="+detailDatas5.toString());

        if (detailDatas5.size()>0){

            int id = requestInfo.getRequestManager().getBillid();

            writeLog("id="+id);

            RecordSet dt5Rs = new RecordSet();

            String querySql = "select sku,quantity from formtable_main_238_dt5 where mainid = ?";

            dt5Rs.executeQuery(querySql,id);

            JSONArray skuArr = new JSONArray();

            List<Map<String, String>> dt5MapList = new ArrayList<>();

            while (dt5Rs.next()){

                Map<String, String> dtSum = new HashMap<>();
                String sku = dt5Rs.getString("sku");
                String quantity = dt5Rs.getString("quantity");

                dtSum.put("sku", sku);
                dtSum.put("quantity", quantity);

                skuArr.add(sku);
                dt5MapList.add(dtSum);
            }

            writeLog("dt5MapList="+dt5MapList);


            JSONObject reqJson = new JSONObject();

            reqJson.put("skus",skuArr);
            reqJson.put("stockNumber","S1");
            K3Service k3Service = new K3Service();
            String params = reqJson.toJSONString();
            String respStr = k3Service.doK3Action(params,k3Ip,getTwBatchInventoryUrl);

            InventoryService inventoryService = new InventoryService();

            List<Map<String, String>> k3InvList = inventoryService.anlysBatIn(respStr);

            writeLog("k3InvList=" + k3InvList);


            String lcbh = mainData.get("lcbh");

            List<Map<String, String>> hkSales = new ArrayList<>();

            hkSales = compareInv(k3InvList, dt5MapList, lcbh);

            if (CollUtil.isNotEmpty(hkSales)){


                for (Map<String, String> hkSale : hkSales){

                    String insertSql = "insert into formtable_main_238_dt6 (mainid,sku,quantity) values (?,?,?)";

                    RecordSet insertRs = new RecordSet();

                    String sku = hkSale.get("sku");

                    String quantity = hkSale.get("quantity");

                    insertRs.executeUpdate(insertSql,id,sku,quantity);
                }

                String updateSql = "update formtable_main_238 set is_gyj_tw = ? where lcbh = ?";
                RecordSet rs = new RecordSet();
                rs.executeUpdate(updateSql,1,lcbh);


                writeLog("台湾货品不够，需要生成香港单据");
                return SUCCESS;
            }else {
                writeLog("台湾货品足够，无需生成香港单据");
                return SUCCESS;
            }
        }
        return FAILURE_AND_CONTINUE;
    }


    public List<Map<String,String>> compareInv(List<Map<String,String>> k3InvList,List<Map<String,String>> dt5MapList,String lcbh){

        List<Map<String,String>> hkSales = new ArrayList<>();

        writeLog("dt5MapList="+dt5MapList.toString());

        for (Map<String,String> dt5Map : dt5MapList) {

            String sku = dt5Map.get("sku");

            String twQuantity = dt5Map.get("quantity");


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
                        if(Integer.compare(Integer.valueOf(fBaseQty),Integer.valueOf(twQuantity)) < 0){
                            writeLog("生成香港数据");
                            Integer hkNumber = Integer.valueOf(twQuantity) - Integer.valueOf(fBaseQty) ;
                            hkSale.put("sku",sku);
                            hkSale.put("quantity",String.valueOf(hkNumber));
                            hkSales.add(hkSale);
                        }else {
                            String updateSql = "update formtable_main_238 set is_gyj_tw = ? where lcbh = ?";
                            RecordSet rs = new RecordSet();
                            rs.executeUpdate(updateSql,0,lcbh);
                        }
                    }
                }
            }
        }
        return hkSales;
    }
}
