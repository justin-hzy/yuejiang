package weaver.interfaces.hzy.mabang.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wbi.util.Util;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.common.service.CommonService;

public class TransMaBangSaleOrderService extends BaseBean {

    private String putHKSaleUrl = getPropValue("k3_api_config","putHKSaleUrl");

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    public void putMaBangSaleOrder(String requestid){
        CommonService commonService = new CommonService();

        String mainSql = "select lcbh,currency_id,express_time,fcustomer_id," +
                "fstockorg_id,fsaleorg_id,fsettleorg_id,fstock_id from " +
                "formtable_main_341 where requestId = ?";

        RecordSet rsMain = new RecordSet();
        rsMain.executeQuery(mainSql,requestid);
        JSONObject jsonObject = new JSONObject();
        String processCode = "";
        while (rsMain.next()){
            processCode = Util.null2String(rsMain.getString("lcbh"));
            String currencyId = Util.null2String(rsMain.getString("currency_id"));
            String expressTime = Util.null2String(rsMain.getString("express_time"));
            String fcustomerId = Util.null2String(rsMain.getString("fcustomer_id"));
            String fstockorgId = Util.null2String(rsMain.getString("fstockorg_id"));
            String fsaleorgId = Util.null2String(rsMain.getString("fsaleorg_id"));
            String fsettleorgId = Util.null2String(rsMain.getString("fsettleorg_id"));
            String fstockId = Util.null2String(rsMain.getString("fstock_id"));

            jsonObject.put("fbillno",processCode);
            jsonObject.put("fstockorgid",fstockorgId);
            jsonObject.put("fsaleorgid",fsaleorgId);
            jsonObject.put("fcustomerid",fcustomerId);
            //jsonObject.put("fdsgbase","");
            jsonObject.put("fsettleorgid",fsettleorgId);
            jsonObject.put("fsettlecurrid",currencyId);
            jsonObject.put("fthirdbillno",processCode);
            jsonObject.put("fdate",expressTime);
            jsonObject.put("warehouseId",fstockId);
        }

        String param = getDtl(requestid,jsonObject);

        String resStr = commonService.doK3Action(param,k3Ip,putHKSaleUrl);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");
        if("200".equals(code)){
            writeLog("同步金蝶销售出库单成功");
            updateIsNext(requestid,0,0);
        }else {
            writeLog("同步金蝶销售出库单失败");
            updateIsNext(requestid,1,1);
        }

    }

    public String getDtl(String requestid,JSONObject jsonObject){

        String dt1Sql = "select dt1.stock_sku,dt1.quantity,dt1.sell_price from formtable_main_341 as main " +
                "inner join formtable_main_341_dt1 as dt1 on main.id = dt1.mainid " +
                "where requestid = ? and dt1.quantity > 0 and dt1.quantity is not null";

        RecordSet rsDt1 = new RecordSet();

        rsDt1.executeQuery(dt1Sql,requestid);
        JSONArray jsonArray = new JSONArray();
        while (rsDt1.next()){
            String stockSku = Util.null2String(rsDt1.getString("stock_sku"));
            String quantity = Util.null2String(rsDt1.getString("quantity"));
            String sellPrice = Util.null2String(rsDt1.getString("sell_price"));

            JSONObject dt1Json = new JSONObject();
            dt1Json.put("fentryid",0);
            dt1Json.put("fmaterialId",stockSku);

            //越南税率为10 泰国税率为7
            if ("PRE009".equals(jsonObject.getString("fsettlecurrid"))){
                dt1Json.put("fentrytaxrate","10");
            }else if("PRE012".equals(jsonObject.getString("fsettlecurrid"))){
                dt1Json.put("fentrytaxrate","7");
            }


            //查询价目表
            dt1Json.put("ftaxprice",sellPrice);

            dt1Json.put("frealqty",quantity);
            String warehouseId = jsonObject.getString("warehouseId");
            //发货仓
            dt1Json.put("fstockid",warehouseId);

            dt1Json.put("fsoorderno",jsonObject.getString("fbillno"));
            dt1Json.put("fdsgsrcoid",jsonObject.getString("fbillno"));
            jsonArray.add(dt1Json);
        }
        //出去发货店仓
        jsonObject.remove("warehouseId");

        jsonObject.put("fentitylist",jsonArray);

        String param = jsonObject.toJSONString();

        return param;
    }

    public void updateIsNext(String requestid,Integer isNext,Integer isTransK3){
        String updateSql = "update formtable_main_341 set is_next = ? , is_trans_k3 where requestId = ?";
        RecordSet updateRs = new RecordSet();
        updateRs.executeUpdate(updateSql,isNext,requestid);
    }
}
