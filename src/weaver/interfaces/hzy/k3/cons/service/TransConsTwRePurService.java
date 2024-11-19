package weaver.interfaces.hzy.k3.cons.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wbi.util.Util;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.common.service.CommonService;

public class TransConsTwRePurService extends BaseBean {

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    private String putTwRePurUrl = getPropValue("k3_api_config","putTwRePurUrl");

    public String putHkRePur(String requestid){

        writeLog("��ʼִ��putHkRePur");
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


            String fbillno = lcbh.replace("HK_","TW_");
            jsonObject.put("fbillno",fbillno);
            jsonObject.put("fstockorgid","ZT026");
            jsonObject.put("fpurchaseorgid","ZT026");
            jsonObject.put("fsupplierid","ZT021");
            jsonObject.put("fdemandorgid","ZT026");

            lcbh = lcbh.substring(lcbh.indexOf("HK_")+3,lcbh.length());
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

            dt1Json.put("fmaterialid",tm);

            //Ĭ��˰��0
            dt1Json.put("fentrytaxrate","0");
            //��ѯ��Ŀ��

            commonService.getPrice(tm,dt1Json);

            dt1Json.put("frmrealqty",sl);

            dt1Json.put("fstockid",shdc);

            jsonArray.add(dt1Json);

        }

        jsonObject.put("fentitylist",jsonArray);

        String param = jsonObject.toJSONString();

        writeLog("param="+param);

        String resStr = commonService.doK3Action(param,k3Ip,putTwRePurUrl);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            commonService.addLog(lcbh,"200");
            writeLog("ͬ����������˻����ɹ�");
            commonService.updateIsNext(requestid,0);
        }else {
            commonService.addLog(lcbh,"500");
            writeLog("ͬ����������˻���ʧ��");
            commonService.updateIsNext(requestid,1);
        }
        return code;
    }
}
