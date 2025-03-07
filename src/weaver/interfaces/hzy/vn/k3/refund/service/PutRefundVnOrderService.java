package weaver.interfaces.hzy.vn.k3.refund.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wbi.util.Util;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.common.service.CommonService;

public class PutRefundVnOrderService extends BaseBean {

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    private String putHkReSaleUrl = getPropValue("k3_api_config","putHkReSaleUrl");


    public void putRefund(String requestid){

        CommonService commonService = new CommonService();
        //待补充
        String mainSql = "select lcbh,fretcust_id,entry_date,shdc from formtable_main_352 where requestId = ?";


        RecordSet rsMain = new RecordSet();

        rsMain.executeQuery(mainSql,requestid);
        JSONObject jsonObject = new JSONObject();

        String processCode = "";
        while (rsMain.next()){
            processCode = Util.null2String(rsMain.getString("lcbh"));
            String fRetCustId = Util.null2String(rsMain.getString("fretcust_id"));
            String entryDate = Util.null2String(rsMain.getString("entry_date"));
            String shdc = Util.null2String(rsMain.getString("shdc"));

            jsonObject.put("fbillno","VN_"+processCode);
            jsonObject.put("fstockorgid","ZT029");
            jsonObject.put("fsaleorgid","ZT029");
            jsonObject.put("fretcustid",fRetCustId);
            //jsonObject.put("fdsgbase","");
            jsonObject.put("fsettleorgid","ZT029");
            jsonObject.put("fsettlecurrid","PRE009");
            jsonObject.put("fthirdbillno",processCode);
            jsonObject.put("fdate",entryDate);
            jsonObject.put("shdc",shdc);
        }

        String param = getDtl(requestid,jsonObject);

        String resStr = commonService.doK3Action(param,k3Ip,putHkReSaleUrl);

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

        //待修改，字段还没定
        String dt1Sql = "select dt1.bar_code,dt1.num,dt1.price from formtable_main_352 as main " +
                "inner join formtable_main_352_dt1 as dt1 on main.id = dt1.mainid " +
                "where requestid = ? and dt1.num > 0 and dt1.num is not null";

        RecordSet rsDt1 = new RecordSet();

        rsDt1.executeQuery(dt1Sql,requestid);
        JSONArray jsonArray = new JSONArray();
        while (rsDt1.next()){
            String barCode = Util.null2String(rsDt1.getString("bar_code"));
            String num = Util.null2String(rsDt1.getString("num"));
            String price = Util.null2String(rsDt1.getString("price"));

            JSONObject dt1Json = new JSONObject();

            dt1Json.put("fmaterialId",barCode);

            //越南税率为0
            dt1Json.put("fentrytaxrate","10");

            //查询价目表
            dt1Json.put("ftaxprice",price);

            dt1Json.put("frealqty",num);

            String shdc = jsonObject.getString("shdc");
            //发货仓
            dt1Json.put("fstockid",shdc);

            jsonArray.add(dt1Json);
        }
        //出去发货店仓
        jsonObject.remove("shdc");

        jsonObject.put("fentitylist",jsonArray);

        String param = jsonObject.toJSONString();

        return param;
    }


    public void updateIsNext(String requestid,Integer isNext){
        String updateSql = "update formtable_main_308 set is_next = ? where requestId = ?";
        RecordSet updateRs = new RecordSet();
        updateRs.executeUpdate(updateSql,isNext,requestid);
    }
}
