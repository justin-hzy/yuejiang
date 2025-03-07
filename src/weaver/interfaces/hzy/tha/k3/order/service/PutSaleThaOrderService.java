package weaver.interfaces.hzy.tha.k3.order.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wbi.util.Util;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.common.service.CommonService;

public class PutSaleThaOrderService extends BaseBean {

    private String putHKSaleUrl = getPropValue("k3_api_config","putHKSaleUrl");

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    public void putSale(String requestid){
        CommonService commonService = new CommonService();
        String mainSql = "select lcbh,receiver_address,fcustomer_id,send_date,fhdc from formtable_main_347 where requestId = ?";

        RecordSet rsMain = new RecordSet();

        rsMain.executeQuery(mainSql,requestid);
        JSONObject jsonObject = new JSONObject();

        String processCode = "";

        while (rsMain.next()){

            processCode = Util.null2String(rsMain.getString("lcbh"));

            String fcustomerId = Util.null2String(rsMain.getString("fcustomer_id"));
            String sendDate = Util.null2String(rsMain.getString("send_date"));
            String fhdc = Util.null2String(rsMain.getString("fhdc"));

            jsonObject.put("fbillno","THA_"+processCode);
            jsonObject.put("fstockorgid","ZT031");
            jsonObject.put("fsaleorgid","ZT031");
            jsonObject.put("fcustomerid",fcustomerId);
            //jsonObject.put("fdsgbase","");
            jsonObject.put("fsettleorgid","ZT031");
            jsonObject.put("fsettlecurrid","PRE012");
            jsonObject.put("fthirdbillno",processCode);
            jsonObject.put("fdate",sendDate);
            jsonObject.put("fhdc",fhdc);
        }


        String param = getDtl(requestid,jsonObject);

        String resStr = commonService.doK3Action(param,k3Ip,putHKSaleUrl);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");
        if("200".equals(code)){
            writeLog("同步金蝶销售出库单成功");
            updateIsNext(requestid,0);
        }else {
            writeLog("同步金蝶销售出库单失败");
            updateIsNext(requestid,1);
        }
    }

    public String getDtl(String requestid,JSONObject jsonObject){

        String dt1Sql = "select dt1.sku_no,dt1.frealqty,dt1.ftaxprice from formtable_main_347 as main " +
                "inner join formtable_main_347_dt1 as dt1 on main.id = dt1.mainid " +
                "where requestid = ? and dt1.frealqty > 0 and dt1.frealqty is not null";

        RecordSet rsDt1 = new RecordSet();

        rsDt1.executeQuery(dt1Sql,requestid);
        JSONArray jsonArray = new JSONArray();
        while (rsDt1.next()){
            String skuNo = Util.null2String(rsDt1.getString("sku_no"));
            String fRealQty = Util.null2String(rsDt1.getString("frealqty"));
            String fTaxPrice = Util.null2String(rsDt1.getString("ftaxprice"));

            JSONObject dt1Json = new JSONObject();
            dt1Json.put("fentryid",0);
            dt1Json.put("fmaterialId",skuNo);

            //泰国税率为0
            dt1Json.put("fentrytaxrate","7");

            //获取价格
            dt1Json.put("ftaxprice",fTaxPrice);

            dt1Json.put("frealqty",fRealQty);
            String fhdc = jsonObject.getString("fhdc");
            //发货仓
            dt1Json.put("fstockid",fhdc);

            dt1Json.put("fsoorderno",jsonObject.getString("fbillno"));
            dt1Json.put("fdsgsrcoid",jsonObject.getString("fbillno"));
            jsonArray.add(dt1Json);
        }
        //出去发货店仓
        jsonObject.remove("fhdc");

        jsonObject.put("fentitylist",jsonArray);

        String param = jsonObject.toJSONString();

        return param;
    }

    public void updateIsNext(String requestid,Integer isNext){
        String updateSql = "update formtable_main_347 set is_next = ? where requestId = ?";
        RecordSet updateRs = new RecordSet();
        updateRs.executeUpdate(updateSql,isNext,requestid);
    }
}
