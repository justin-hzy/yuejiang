package weaver.interfaces.hzy.cons.action;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icbc.api.internal.apache.http.impl.cookie.S;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.service.InventoryService;
import weaver.interfaces.hzy.k3.service.K3Service;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.text.SimpleDateFormat;
import java.util.*;

public class ConsMatchGyjInvAction extends BaseBean implements Action {

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    private String getBatchGyjInventoryUrl = getPropValue("k3_api_config","getBatchGyjInventoryUrl");

    private InventoryService inventoryService;

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("开始执行ConsMatchGyjInvAction");

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        List<Map<String, String>> detailDatas1 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);

        writeLog("月结寄售明细1数据="+detailDatas1.toString());

        if (detailDatas1.size()>0){

            int id = requestInfo.getRequestManager().getBillid();

            writeLog("id="+id);

            RecordSet dt1Rs = new RecordSet();

            String querySql = "select wlbm tm ,sum(xssl) sl  from formtable_main_238_dt1 where mainid = ? group by tm";

            dt1Rs.executeQuery(querySql,id);

            JSONArray skuJsonArr = new JSONArray();

            List<String> skus = new ArrayList<>();

            List<Map<String, String>> dtMapList = new ArrayList<>();

            while (dt1Rs.next()){
                Map<String, String> dtSum = new HashMap<>();
                String tm = dt1Rs.getString("tm");
                String sl = dt1Rs.getString("sl");

                dtSum.put("tm", tm);
                dtSum.put("sl", sl);

                skuJsonArr.add(tm);

                skus.add(tm);
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

            List<Map<String, String>> twSales = new ArrayList<>();

            //判断负库存
            List<Map<String,String>> negInvList = inventoryService.CheckOutNegInv(k3InvList);
            writeLog("negInvList="+negInvList.toString());
            if(CollUtil.isNotEmpty(negInvList)){

                String message = "负库存信息, ";
                for (Map<String,String> negInvMap : negInvList){
                    String sku = negInvMap.get("sku");
                    String fBaseQty = negInvMap.get("fBaseQty");
                    message = message + sku + "为" + fBaseQty + ";";
                }

                String updateSql = "update formtable_main_238 set match_inv_fail = ? , inv_error_msg = ? where lcbh = ?";
                RecordSet rs = new RecordSet();
                rs.executeUpdate(updateSql,0,message,lcbh);

                return SUCCESS;
            }else {
                twSales = compareInv(k3InvList, dtMapList, inventoryService, lcbh);

                if (CollUtil.isNotEmpty(twSales)){

                    String deleteSql = "DELETE FROM formtable_main_238_dt5 where mainid = ?";
                    RecordSet deleteRs = new RecordSet();
                    deleteRs.executeUpdate(deleteSql,id);

                    for (Map<String, String> twSale : twSales){

                        String insertSql = "insert into formtable_main_238_dt5 (mainid,sku,quantity) values (?,?,?)";

                        RecordSet insertRs = new RecordSet();

                        String sku = twSale.get("sku");

                        String quantity = twSale.get("quantity");

                        insertRs.executeUpdate(insertSql,id,sku,quantity);
                    }

                    String updateSql = "update formtable_main_238 set is_gyj = ? where lcbh = ?";
                    RecordSet rs = new RecordSet();
                    /*is_gyj = 1 代表不够货*/
                    rs.executeUpdate(updateSql,1,lcbh);

                    writeLog("广悦进货品不够，需要生成香港单据");
                    return SUCCESS;
                }else {
                    writeLog("广悦进货品组够，无需生成香港单据");
                    return SUCCESS;
                }
            }
        }else {
            return FAILURE_AND_CONTINUE;
        }
    }



    public List<Map<String,String>> compareInv(List<Map<String,String>> k3InvList,List<Map<String,String>> dtMapList, InventoryService inventoryService,String lcbh){

        List<Map<String,String>> twSales = new ArrayList<>();

        writeLog("dtMapList="+dtMapList.toString());

        for (Map<String,String> dtSum : dtMapList) {

            String wlbm = dtSum.get("tm");

            String xssl = dtSum.get("sl");


            for (Map<String,String> k3Inv : k3InvList){
                String sku = k3Inv.get("sku");
                //writeLog("sku="+sku);

                if(sku.equals(wlbm)){

                    /*writeLog("金蝶-sku="+sku);
                    writeLog("dms-wlbm="+wlbm);*/


                    Map<String,String> twSale = new HashMap<>();
                    String fBaseQty = k3Inv.get("fBaseQty");


                    if(Integer.compare(Integer.valueOf(fBaseQty),Integer.valueOf(xssl)) < 0){
                        writeLog("生成台湾数据");
                        Integer twNumber = Integer.valueOf(xssl) - Integer.valueOf(fBaseQty) ;
                        twSale.put("sku",wlbm);
                        twSale.put("quantity",String.valueOf(twNumber));
                        twSales.add(twSale);
                    }else {
                        String updateSql = "update formtable_main_238 set is_gyj = ? where lcbh = ?";
                        RecordSet rs = new RecordSet();
                        rs.executeUpdate(updateSql,0,lcbh);
                    }


//                    writeLog("fBaseQty="+fBaseQty);

                    /*writeLog("fBaseQty="+fBaseQty);
                    writeLog("xssl="+xssl);*/

                    /*if(Integer.valueOf(fBaseQty)<0){
                        String updateSql = "update formtable_main_238 set match_inv_fail = ? where lcbh = ?";
                        RecordSet rs = new RecordSet();
                        rs.executeUpdate(updateSql,0,lcbh);
                    }else {
                        if(Integer.compare(Integer.valueOf(fBaseQty),Integer.valueOf(xssl)) < 0){
                            writeLog("生成台湾数据");
                            Integer twNumber = Integer.valueOf(xssl) - Integer.valueOf(fBaseQty) ;
                            twSale.put("sku",wlbm);
                            twSale.put("quantity",String.valueOf(twNumber));
                            twSales.add(twSale);
                        }else {
                            String updateSql = "update formtable_main_238 set is_gyj = ? where lcbh = ?";
                            RecordSet rs = new RecordSet();
                            rs.executeUpdate(updateSql,0,lcbh);
                        }
                    }*/
                }
            }
        }
        return twSales;
    }
}
