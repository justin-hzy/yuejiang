package weaver.interfaces.hzy.trf.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wbi.util.Util;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.common.service.CommonService;
import weaver.soa.workflow.request.RequestInfo;

public class TransTrfGyjRePurService extends BaseBean {

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    private String putGyjRePurUrl = getPropValue("k3_api_config","putGyjRePurUrl");

    public void putGyjRePur(RequestInfo requestInfo){
        String requestid = requestInfo.getRequestid();
        writeLog("执行putGyjTwReSale");

        CommonService commonService = new CommonService();
        String mainSql = "select lcbh,rhrq from formtable_main_228 where requestId = ?";

        RecordSet rsMain = new RecordSet();
        rsMain.executeQuery(mainSql,requestid);
        JSONObject jsonObject = new JSONObject();
        String processCode = "";
        while (rsMain.next()){

            processCode = Util.null2String(rsMain.getString("lcbh"));
            String receiveDate = Util.null2String(rsMain.getString("rhrq"));
            String currencyId = "PRE005";

            jsonObject.put("fstockorgid","ZT030");
            jsonObject.put("fpurchaseorgid","ZT030");
            jsonObject.put("fsupplierid","ZT026");
            jsonObject.put("fdemandorgid","ZT030");
            processCode = "GYJ_"+processCode;
            jsonObject.put("fbillno",processCode);
            jsonObject.put("fthirdbillno",processCode);
            jsonObject.put("fdate",receiveDate);
            jsonObject.put("fsettlecurrid",currencyId);

        }

        writeLog("jsonObject="+jsonObject.toJSONString());

        String dt3Sql = "select dt3.sku,dt3.resale_qty,main.fhdc2 from formtable_main_228 as main inner join formtable_main_228_dt3 dt3 on main.id = dt3.mainid where requestId = ? and dt3.resale_qty > 0";

        RecordSet rsDt3 = new RecordSet();

        rsDt3.executeQuery(dt3Sql,requestid);
        JSONArray jsonArray = new JSONArray();
        while (rsDt3.next()){
            String sku = Util.null2String(rsDt3.getString("sku"));
            String resaleQty = Util.null2String(rsDt3.getString("resale_qty"));
            //广悦进特有逻辑，广悦进S1仓退到台湾S1仓，即发货仓等于台湾的收货仓
            String receiveWareHouse = Util.null2String(rsDt3.getString("fhdc2"));

            JSONObject dt1Json = new JSONObject();

            dt1Json.put("fmaterialid",sku);
            dt1Json.put("fentrytaxrate","5");

            commonService.queryRetPrice(sku,dt1Json);

            dt1Json.put("frmrealqty",resaleQty);
            //String fhdc = jsonObject.getString("fhdc");
            dt1Json.put("fstockid",receiveWareHouse);

            jsonArray.add(dt1Json);
        }

        jsonObject.put("fentitylist",jsonArray);

        String param = jsonObject.toJSONString();

        writeLog("param="+param);

        String resStr = commonService.doK3Action(param,k3Ip,putGyjRePurUrl);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            commonService.addLog(processCode,"200");
            writeLog("同步金蝶广悦进-香港-退货单成功");
            updateIsNext(requestid,0);
        }else {
            commonService.addLog(processCode,"500");
            writeLog("同步金蝶广悦进-香港-退货单失败");
            updateIsNext(requestid,1);
        }
    }


    public void updateIsNext(String requestid,Integer isNext){
        String updateSql = "update formtable_main_249 set is_next = ? where requestId = ?";
        RecordSet updateRs = new RecordSet();
        updateRs.executeUpdate(updateSql,isNext,requestid);
    }
}
