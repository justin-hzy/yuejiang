package weaver.interfaces.hzy.trf.service;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wbi.util.Util;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.common.service.CommonService;
import weaver.interfaces.tx.util.WorkflowUtil;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransTrfTwReSaleService extends BaseBean {

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    private String putGyjTwReSaleUrl = getPropValue("k3_api_config","putGyjTwReSaleUrl");

    public String putGyjTwReSale(RequestInfo requestInfo){
        String requestid = requestInfo.getRequestid();
        writeLog("执行putGyjTwReSale");

        CommonService commonService = new CommonService();
        String mainSql = "select lcbh,rhrq from formtable_main_228 where requestId = ?";

        RecordSet rsMain = new RecordSet();
        rsMain.executeQuery(mainSql,requestid);
        JSONObject jsonObject = new JSONObject();
        String lcbh = "";
        while (rsMain.next()){

            lcbh = Util.null2String(rsMain.getString("lcbh"));
            String rhrq = Util.null2String(rsMain.getString("rhrq"));
            String bb = "PRE005";

            jsonObject.put("fbillno",lcbh);
            jsonObject.put("fstockorgid","ZT026");
            jsonObject.put("fsaleorgid","ZT026");
            jsonObject.put("fsettleorgid","ZT026");
            jsonObject.put("fretcustid","CUST0354");
            lcbh = lcbh.substring(lcbh.indexOf("GYJTW_")+6,lcbh.length());
            jsonObject.put("fthirdbillno",lcbh);

            jsonObject.put("fdate",rhrq);
            jsonObject.put("fsettlecurrid",bb);
        }

        writeLog("jsonObject="+jsonObject.toJSONString());

        String dt3Sql = "select dt3.sku,dt3.resale_qty ,main.fhdc2 from formtable_main_228 as main inner join formtable_main_228_dt3 dt3 on main.id = dt3.mainid where requestId = ? and dt3.resale_qty > 0";

        RecordSet rsDt3 = new RecordSet();

        rsDt3.executeQuery(dt3Sql,requestid);
        JSONArray jsonArray = new JSONArray();
        while (rsDt3.next()){
            String sku = Util.null2String(rsDt3.getString("sku"));
            String resale_qty = Util.null2String(rsDt3.getString("resale_qty"));
            //广悦进特有逻辑，广悦进S1仓退到台湾S1仓，即发货仓等于台湾的收货仓
            String fhdc2 = Util.null2String(rsDt3.getString("fhdc2"));

            JSONObject dt1Json = new JSONObject();

            dt1Json.put("fmaterialId",sku);

            dt1Json.put("fentrytaxrate","5");

            commonService.getPrice(sku,dt1Json);

            dt1Json.put("frealqty",resale_qty);
            //String fhdc = jsonObject.getString("fhdc");
            dt1Json.put("fstockid",fhdc2);

            jsonArray.add(dt1Json);
        }


        jsonObject.put("fentitylist",jsonArray);

        String param = jsonObject.toJSONString();

        writeLog("param="+param);

        String resStr = commonService.doK3Action(param,k3Ip,putGyjTwReSaleUrl);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            commonService.addLog(lcbh,"200");
            writeLog("同步金蝶销售退货单成功");
            commonService.updateIsNext(requestid,0);
        }else {
            commonService.addLog(lcbh,"500");
            writeLog("同步金蝶销售退货单失败");
            commonService.updateIsNext(requestid,1);
        }
        return code;

    }
}
