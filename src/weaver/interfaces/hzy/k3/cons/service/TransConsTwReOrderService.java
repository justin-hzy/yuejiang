package weaver.interfaces.hzy.k3.cons.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wbi.util.Util;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.common.service.CommonService;

public class TransConsTwReOrderService extends BaseBean {

    private String k3Ip  = getPropValue("fulun_api_config","k3Ip");

    private String putTwReSaleUrl = getPropValue("k3_api_config","putTwReSaleUrl");


    public String putConsTwReOrder(String requestid){

        writeLog("执行putConsTwReOrder");
        CommonService commonService = new CommonService();
        String mainSql = "select lcbh,djrq,kh,bb from formtable_main_249 where requestId = ?";

        RecordSet rsMain = new RecordSet();
        rsMain.executeQuery(mainSql,requestid);
        JSONObject jsonObject = new JSONObject();
        String lcbh = "";
        while (rsMain.next()){
            lcbh = Util.null2String(rsMain.getString("lcbh"));
            String djrq = Util.null2String(rsMain.getString("djrq"));
            String kh = Util.null2String(rsMain.getString("kh"));
            String bb = Util.null2String(rsMain.getString("bb"));

            jsonObject.put("fbillno",lcbh);

            jsonObject.put("fstockorgid","ZT026");
            jsonObject.put("fsaleorgid","ZT026");
            jsonObject.put("fsettleorgid","ZT026");
            jsonObject.put("fretcustid",kh);

            lcbh = lcbh.substring(lcbh.indexOf("TW_")+3,lcbh.length());
            jsonObject.put("fthirdbillno",lcbh);
            jsonObject.put("fdate",djrq);
            jsonObject.put("fsettlecurrid",bb);
        }

        writeLog("jsonObject="+jsonObject.toJSONString());


        String dt1Sql = "select dt1.tm,dt1.sl,dt1.xsj,dt1.taxrate,main.shdc from formtable_main_249 as main inner join formtable_main_249_dt1 dt1 on main.id = dt1.mainid where requestId = ? and dt1.sl > 0";

        RecordSet rsDt1 = new RecordSet();

        rsDt1.executeQuery(dt1Sql,requestid);
        JSONArray jsonArray = new JSONArray();

        while (rsDt1.next()){
            String tm = Util.null2String(rsDt1.getString("tm"));
            String sl = Util.null2String(rsDt1.getString("sl"));
            String xsj = Util.null2String(rsDt1.getString("xsj"));
            String taxrate = Util.null2String(rsDt1.getString("taxrate"));
            String shdc = Util.null2String(rsDt1.getString("shdc"));


            JSONObject dt1Json = new JSONObject();

            dt1Json.put("fmaterialId",tm);
            dt1Json.put("fentrytaxrate",taxrate);
            dt1Json.put("ftaxprice",xsj);

            dt1Json.put("frealqty",sl);
            dt1Json.put("fstockid",shdc);

            jsonArray.add(dt1Json);
        }
        jsonObject.put("fentitylist",jsonArray);

        String param = jsonObject.toJSONString();

        writeLog("param="+param);

        String resStr = commonService.doK3Action(param,k3Ip,putTwReSaleUrl);

        writeLog("resStr="+resStr);

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
