package weaver.interfaces.hzy.k3.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icbc.api.internal.apache.http.impl.cookie.S;
import com.wbi.util.Util;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;

public class K3Service extends BaseBean {


    private String meIp = getPropValue("fulun_api_config","meIp");

    private String putSaleUrl = getPropValue("k3_api_config","putSaleUrl");

    private String putPurUrl = getPropValue("k3_api_config","putPurUrl");

    private String putTrfUrl = getPropValue("k3_api_config","putTrfUrl");

    private String putReSaleUrl = getPropValue("k3_api_config","putReSaleUrl");

    private String putRePurUrl = getPropValue("k3_api_config","putRePurUrl");


    public String putSale(String requestid,String flag){


        String mainSql = "select lcbh,chrq,fhdc,djrq,kh,ddje,bb,ydh,ck from formtable_main_249 where requestId = ?";

        RecordSet rsMain = new RecordSet();

        rsMain.executeQuery(mainSql,requestid);
        JSONObject jsonObject = new JSONObject();
        String lcbh = "";
        while (rsMain.next()){
            lcbh = Util.null2String(rsMain.getString("lcbh"));
            String chrq = Util.null2String(rsMain.getString("chrq"));
            String fhdc = Util.null2String(rsMain.getString("fhdc"));
            String kh = Util.null2String(rsMain.getString("kh"));
            //String ddje = Util.null2String(rsMain.getString("ddje"));
            String bb = Util.null2String(rsMain.getString("bb"));
            //String ydh = Util.null2String(rsMain.getString("ydh"));
            String ck = Util.null2String(rsMain.getString("ck"));


            jsonObject.put("fbillno",lcbh);
            if("HK".equals(flag)){
                jsonObject.put("fstockorgid","ZT021");
                jsonObject.put("fsaleorgid","ZT021");
                jsonObject.put("fcustomerid","CUST0558");
                jsonObject.put("fdsgbase","ZT026");
                jsonObject.put("fsettleorgid","ZT021");
                jsonObject.put("type","HK");
            }else if("TW".equals(flag)){
                jsonObject.put("fstockorgid","ZT026");
                jsonObject.put("fsaleorgid","ZT026");
                jsonObject.put("fcustomerid",kh);
                jsonObject.put("fdsgbase","ZT026");
                jsonObject.put("fsettleorgid","ZT026");
                jsonObject.put("type","TW");
                jsonObject.put("freceiveaddress",ck);
            }

            jsonObject.put("fsettlecurrid",bb);
            if("HK".equals(flag)){
                jsonObject.put("fthirdbillno",lcbh);
            }else if("TW".equals(flag)){
                lcbh = lcbh.substring(lcbh.indexOf("TW_")+3,lcbh.length());
                jsonObject.put("fthirdbillno",lcbh);
            }

            jsonObject.put("fdate",chrq);
            jsonObject.put("fhdc",fhdc);
        }

        String param = getDtl(requestid,jsonObject,flag);

        writeLog("param="+param);

        String resStr = doK3Action(param,meIp,putSaleUrl);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");
        if("200".equals(code)){
            addLog(lcbh,"200");
            writeLog("同步金蝶销售出库单成功");

        }else {
            addLog(lcbh,"500");
            writeLog("同步金蝶销售出库单失败");
        }

        return code;
    }

    public String putConsSale(String requestid,String flag){


        String mainSql = "select lcbh,fhdc,djrq,kh from formtable_main_249 where requestId = ?";

        RecordSet rsMain = new RecordSet();

        rsMain.executeQuery(mainSql,requestid);
        JSONObject jsonObject = new JSONObject();
        String lcbh = "";
        while (rsMain.next()){
            lcbh = Util.null2String(rsMain.getString("lcbh"));
            String fhdc = Util.null2String(rsMain.getString("fhdc"));
            String kh = Util.null2String(rsMain.getString("kh"));
            String djrq = Util.null2String(rsMain.getString("djrq"));
            writeLog("djrq="+djrq);
            //String bb = Util.null2String(rsMain.getString("bb"));

            jsonObject.put("fbillno",lcbh);
            if("HK".equals(flag)){
                jsonObject.put("fstockorgid","ZT021");
                jsonObject.put("fsaleorgid","ZT021");
                jsonObject.put("fcustomerid","CUST0558");
                jsonObject.put("fdsgbase","ZT026");
                jsonObject.put("fsettleorgid","ZT021");
                jsonObject.put("type","HK");
            }else if("TW".equals(flag)){
                jsonObject.put("fstockorgid","ZT026");
                jsonObject.put("fsaleorgid","ZT026");
                jsonObject.put("fcustomerid",kh);
                jsonObject.put("fdsgbase","ZT026");
                jsonObject.put("fsettleorgid","ZT026");
                jsonObject.put("type","TW");
            }
            jsonObject.put("fsettlecurrid","PRE005");
            if("HK".equals(flag)){
                lcbh = lcbh.substring(lcbh.indexOf("HK_")+3,lcbh.length());
                jsonObject.put("fthirdbillno",lcbh);
            }else if("TW".equals(flag)){
                lcbh = lcbh.substring(lcbh.indexOf("TW_")+3,lcbh.length());
                jsonObject.put("fthirdbillno",lcbh);
            }
            jsonObject.put("fdate",djrq);
            jsonObject.put("fhdc",fhdc);



        }

        writeLog("jsonObject="+jsonObject.toJSONString());

        String param = getDtl(requestid,jsonObject,flag);

        writeLog("param="+param);

        String resStr = doK3Action(param,meIp,putSaleUrl);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");
        if("200".equals(code)){
            addLog(lcbh,"200");
            writeLog("同步金蝶寄售出库单成功");

        }else {
            addLog(lcbh,"500");
            writeLog("同步金蝶寄售出库单失败");
        }

        return code;
    }

    public String putGyjConsSale(String requestid){

        String mainSql = "select lcbh,fhdc,djrq,kh from formtable_main_249 where requestId = ?";

        RecordSet rsMain = new RecordSet();

        rsMain.executeQuery(mainSql,requestid);
        JSONObject jsonObject = new JSONObject();
        String lcbh = "";
        while (rsMain.next()){
            lcbh = Util.null2String(rsMain.getString("lcbh"));
            String fhdc = Util.null2String(rsMain.getString("fhdc"));
            String kh = Util.null2String(rsMain.getString("kh"));
            String djrq = Util.null2String(rsMain.getString("djrq"));
            writeLog("djrq="+djrq);
            //String bb = Util.null2String(rsMain.getString("bb"));

            jsonObject.put("fbillno",lcbh);

            jsonObject.put("fstockorgid","ZT030");
            jsonObject.put("fsaleorgid","ZT030");
            jsonObject.put("fcustomerid","Shopee");
            jsonObject.put("fdsgbase","ZT030");
            jsonObject.put("fsettleorgid","ZT030");
            jsonObject.put("fthirdbillno",lcbh);

            jsonObject.put("fsettlecurrid","PRE005");
            jsonObject.put("fdate",djrq);
            jsonObject.put("fhdc",fhdc);

        }

        writeLog("jsonObject="+jsonObject.toJSONString());

        String param = getDtl(requestid,jsonObject,"GYJ");

        writeLog("param="+param);

        String resStr = doK3Action(param,meIp,putSaleUrl);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");
        if("200".equals(code)){
            addLog(lcbh,"200");
            writeLog("同步金蝶寄售出库单成功");

        }else {
            addLog(lcbh,"500");
            writeLog("同步金蝶寄售出库单失败");
        }
        return code;
    }


    public String putGyjTWConsSale(String requestid){

        String mainSql = "select lcbh,fhdc,djrq,kh from formtable_main_249 where requestId = ?";

        RecordSet rsMain = new RecordSet();

        rsMain.executeQuery(mainSql,requestid);
        JSONObject jsonObject = new JSONObject();
        String lcbh = "";
        while (rsMain.next()){
            lcbh = Util.null2String(rsMain.getString("lcbh"));
            String fhdc = Util.null2String(rsMain.getString("fhdc"));
            String kh = Util.null2String(rsMain.getString("kh"));
            String djrq = Util.null2String(rsMain.getString("djrq"));
            writeLog("djrq="+djrq);
            //String bb = Util.null2String(rsMain.getString("bb"));

            jsonObject.put("fbillno",lcbh);

            jsonObject.put("fstockorgid","ZT026");
            jsonObject.put("fsaleorgid","ZT026");
            jsonObject.put("fcustomerid","Shopee");
            jsonObject.put("fdsgbase","Shopee");
            jsonObject.put("fsettleorgid","ZT026");
            jsonObject.put("fsettlecurrid","PRE005");
            jsonObject.put("fthirdbillno",lcbh);
            jsonObject.put("fdate",djrq);
            jsonObject.put("fhdc",fhdc);

        }

        writeLog("jsonObject="+jsonObject.toJSONString());

        String param = getDtl(requestid,jsonObject,"GYJ_TW");

        writeLog("param="+param);

        String resStr = doK3Action(param,meIp,putSaleUrl);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");
        if("200".equals(code)){
            addLog(lcbh,"200");
            writeLog("同步金蝶寄售出库单成功");

        }else {
            addLog(lcbh,"500");
            writeLog("同步金蝶寄售出库单失败");
        }

        return code;
    }

    public String putGyjHKConsSale(String requestid){

        String mainSql = "select lcbh,fhdc,djrq,kh from formtable_main_249 where requestId = ?";

        RecordSet rsMain = new RecordSet();

        rsMain.executeQuery(mainSql,requestid);
        JSONObject jsonObject = new JSONObject();
        String lcbh = "";
        while (rsMain.next()){
            lcbh = Util.null2String(rsMain.getString("lcbh"));
            String fhdc = Util.null2String(rsMain.getString("fhdc"));
            String kh = Util.null2String(rsMain.getString("kh"));
            String djrq = Util.null2String(rsMain.getString("djrq"));
            writeLog("djrq="+djrq);
            //String bb = Util.null2String(rsMain.getString("bb"));

            jsonObject.put("fbillno",lcbh);

            jsonObject.put("fstockorgid","ZT021");
            jsonObject.put("fsaleorgid","ZT021");
            jsonObject.put("fcustomerid","CUST0558");
            jsonObject.put("fdsgbase","ZT026");
            jsonObject.put("fsettleorgid","ZT021");
            jsonObject.put("fsettlecurrid","PRE005");
            jsonObject.put("fthirdbillno",lcbh);
            jsonObject.put("fdate",djrq);
            jsonObject.put("fhdc",fhdc);

        }

        writeLog("jsonObject="+jsonObject.toJSONString());

        String param = getDtl(requestid,jsonObject,"GYJ_HK");

        writeLog("param="+param);

        String resStr = doK3Action(param,meIp,putSaleUrl);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");
        if("200".equals(code)){
            addLog(lcbh,"200");
            writeLog("同步金蝶寄售出库单成功");

        }else {
            addLog(lcbh,"500");
            writeLog("同步金蝶寄售出库单失败");
        }

        return code;
    }

    public String getDtl(String requestid,JSONObject jsonObject,String flag){
        String dt1Sql = "select dt1.tm,dt1.sl,dt1.xsj,dt1.hplx,dt1.taxrate from formtable_main_249 as main inner join formtable_main_249_dt1 dt1 on main.id = dt1.mainid where requestId = ?";

        RecordSet rsDt1 = new RecordSet();

        rsDt1.executeQuery(dt1Sql,requestid);
        JSONArray jsonArray = new JSONArray();
        while (rsDt1.next()){
            String tm = Util.null2String(rsDt1.getString("tm"));
            String sl = Util.null2String(rsDt1.getString("sl"));
            String xsj = Util.null2String(rsDt1.getString("xsj"));
            //String hplx = Util.null2String(rsDt1.getString("hplx"));
            String taxrate = Util.null2String(rsDt1.getString("taxrate"));


            JSONObject dt1Json = new JSONObject();
            dt1Json.put("fentryid",0);
            dt1Json.put("fmaterialId",tm);

            if("HK".equals(flag) || "GYJ_HK".equals(flag)){
                //香港税率为0
                dt1Json.put("fentrytaxrate","0");

                //查询价目表
                //queryPriceTable(tm,dt1Json);
                getPrice(tm,dt1Json);
            }else if("TW".equals(flag)){
                //台湾税率5
                dt1Json.put("fentrytaxrate",taxrate);
                dt1Json.put("ftaxprice",xsj);
            }else if("GYJ_TW".equals(flag) || "GYJ".equals(flag)){
                dt1Json.put("fentrytaxrate","5");
                getPrice(tm,dt1Json);
            }
            dt1Json.put("frealqty",sl);
            String fhdc = jsonObject.getString("fhdc");
            dt1Json.put("fstockid",fhdc);

            dt1Json.put("fsoorderno",jsonObject.getString("fbillno"));
            dt1Json.put("fdsgsrcoid",jsonObject.getString("fbillno"));
            jsonArray.add(dt1Json);
        }
        jsonObject.remove("fhdc");

        jsonObject.put("fentitylist",jsonArray);

        String param = jsonObject.toJSONString();

        return param;
    }



    public String putPur(String requestid,String flag){
        String mainSql = "select lcbh,chrq,fhdc,djrq,kh,ddje,bb from formtable_main_249 where requestId = ?";

        RecordSet rsMain = new RecordSet();

        rsMain.executeQuery(mainSql,requestid);
        JSONObject jsonObject = new JSONObject();
        String lcbh = "";
        while (rsMain.next()){
            lcbh = Util.null2String(rsMain.getString("lcbh"));
            String chrq = Util.null2String(rsMain.getString("chrq"));
            String fhdc = Util.null2String(rsMain.getString("fhdc"));
            String bb = Util.null2String(rsMain.getString("bb"));

            if ("TW".equals(flag)){
                lcbh= lcbh.replace("HK_","TW_");
            }

            jsonObject.put("fbillno",lcbh);
            jsonObject.put("fstockorgid","ZT026");
            jsonObject.put("fpurchaseorgid","ZT026");
            jsonObject.put("fsupplierId","ZT021");
            jsonObject.put("fdemandorgid","ZT026");
            jsonObject.put("fsettleorgid","ZT021");
            jsonObject.put("fthirdbillno",lcbh);
            jsonObject.put("fdate",chrq);
            jsonObject.put("fhdc",fhdc);
            jsonObject.put("fsettlecurrid",bb);
        }

        String dt1Sql = "select dt1.tm,dt1.sl,dt1.xsj from formtable_main_249 as main inner join formtable_main_249_dt1 dt1 on main.id = dt1.mainid where requestId = ?";

        RecordSet rsDt1 = new RecordSet();

        rsDt1.executeQuery(dt1Sql,requestid);
        JSONArray jsonArray = new JSONArray();

        while (rsDt1.next()){
            String tm = Util.null2String(rsDt1.getString("tm"));
            String sl = Util.null2String(rsDt1.getString("sl"));
            String xsj = Util.null2String(rsDt1.getString("xsj"));

            JSONObject dt1Json = new JSONObject();

            dt1Json.put("fmaterialId",tm);
            //默认税率5
            if("TW".equals(flag)){
                dt1Json.put("fentrytaxrate","0");
                //查询价目表
                queryPriceTable(tm,dt1Json);
                //dt1Json.put("ftaxprice",xsj);
            }

            dt1Json.put("frealqty",sl);
            String fhdc = jsonObject.getString("fhdc");
            dt1Json.put("fstockid",fhdc);

            jsonArray.add(dt1Json);
        }
        jsonObject.remove("fhdc");
        jsonObject.put("fentrylist",jsonArray);

        String param = jsonObject.toJSONString();

        writeLog("param="+param);

        String resStr = doK3Action(param,meIp,putPurUrl);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            addLog(lcbh,"200");
            writeLog("同步金蝶采购入库单成功");
        }else {
            addLog(lcbh,"500");
            writeLog("同步金蝶采购入库单失败");
        }
        return code;
    }


    public String putRePur(String requestid,String flag){
        writeLog("执行putRePur");

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


            if ("HK".equals(flag)){
                String fbillno = lcbh.replace("HK_","TW_");
                jsonObject.put("fbillno",fbillno);
            }


            if("HK".equals(flag)){
                jsonObject.put("fstockorgid","ZT026");
                jsonObject.put("fpurchaseorgid","ZT026");
                jsonObject.put("fsupplierid","ZT021");
                jsonObject.put("fdemandorgid","ZT026");
            }else if("TW".equals(flag)){

            }

            if("HK".equals(flag)){
                lcbh = lcbh.substring(lcbh.indexOf("HK_")+3,lcbh.length());
                jsonObject.put("fthirdbillno",lcbh);
            }else if("TW".equals(flag)){
                lcbh = lcbh.substring(lcbh.indexOf("TW_")+3,lcbh.length());
                jsonObject.put("fthirdbillno",lcbh);
            }

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

            //默认税率5
            if("HK".equals(flag)){
                dt1Json.put("fentrytaxrate","0");
                //查询价目表
                queryPriceTable(tm,dt1Json);
                //dt1Json.put("ftaxprice",xsj);
            }else if("TW".equals(flag)){
                dt1Json.put("fentrytaxrate",taxrate);
                dt1Json.put("ftaxprice",xsj);
            }

            dt1Json.put("frmrealqty",sl);
            //String fhdc = jsonObject.getString("fhdc");
            dt1Json.put("fstockid",shdc);

            jsonArray.add(dt1Json);

        }


        jsonObject.put("fentitylist",jsonArray);

        String param = jsonObject.toJSONString();

        writeLog("param="+param);

        String resStr = doK3Action(param,meIp,putRePurUrl);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            addLog(lcbh,"200");
            writeLog("同步金蝶销售退货单成功");

        }else {
            addLog(lcbh,"500");
            writeLog("同步金蝶销售退货单失败");
        }


        return code;
    }

    public String putGYJRePur(String requestid,String flag){
        writeLog("执行putGYJRePur");

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


            if("GYJTW".equals(flag)){
                jsonObject.put("fstockorgid","ZT026");
                jsonObject.put("fpurchaseorgid","ZT026");
                jsonObject.put("fsupplierid","ZT021");
                jsonObject.put("fdemandorgid","ZT026");
                //香港的逻辑 : 先生成台湾销售退 所以把GYJHK替换成GYJTW
                lcbh = lcbh.replace("GYJHK","GYJTW");
                jsonObject.put("fbillno",lcbh);
                lcbh = lcbh.substring(lcbh.indexOf("GYJHK_")+6,lcbh.length());
                jsonObject.put("fthirdbillno",lcbh);
            }else if ("GYJ".equals(flag)){
                jsonObject.put("fstockorgid","ZT030");
                jsonObject.put("fpurchaseorgid","ZT030");
                jsonObject.put("fsupplierid","ZT026");
                jsonObject.put("fdemandorgid","ZT030");
                //GYJTW_-> GYJ_
                lcbh = lcbh.replace("GYJTW_","GYJ_");
                jsonObject.put("fbillno",lcbh);
                lcbh = lcbh.substring(lcbh.indexOf("GYJ_")+4,lcbh.length());
                jsonObject.put("fthirdbillno",lcbh);
            }


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
            dt1Json.put("fentrytaxrate","0");


            //queryPriceTable(tm,dt1Json);
            if ("GYJ".equals(flag)){
                queryRetPrice(tm,dt1Json);
            }else if("GYJTW".equals(flag)){
                queryPriceTable(tm,dt1Json);
            }


            dt1Json.put("frmrealqty",sl);
            //String fhdc = jsonObject.getString("fhdc");
            dt1Json.put("fstockid",shdc);

            jsonArray.add(dt1Json);

        }


        jsonObject.put("fentitylist",jsonArray);

        String param = jsonObject.toJSONString();

        writeLog("param="+param);

        String resStr = doK3Action(param,meIp,putRePurUrl);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            addLog(lcbh,"200");
            writeLog("同步金蝶广悦进-香港-退货单成功");

        }else {
            addLog(lcbh,"500");
            writeLog("同步金蝶广悦进-香港-退货单失败");
        }


        return code;
    }

    public String putConsPur(String requestid,String flag){
        String mainSql = "select lcbh,fhdc,djrq,kh from formtable_main_249 where requestId = ?";

        RecordSet rsMain = new RecordSet();

        rsMain.executeQuery(mainSql,requestid);
        JSONObject jsonObject = new JSONObject();
        String lcbh = "";
        while (rsMain.next()){
            lcbh = Util.null2String(rsMain.getString("lcbh"));
            String djrq = Util.null2String(rsMain.getString("djrq"));
            writeLog("djrq="+djrq);
            String fhdc = Util.null2String(rsMain.getString("fhdc"));
            //String bb = Util.null2String(rsMain.getString("bb"));

            if ("TW".equals(flag)){
                lcbh= lcbh.replace("HK_","TW_");
            }

            jsonObject.put("fbillno",lcbh);
            jsonObject.put("fstockorgid","ZT026");
            jsonObject.put("fpurchaseorgid","ZT026");
            jsonObject.put("fsupplierId","ZT021");
            jsonObject.put("fdemandorgid","ZT026");
            jsonObject.put("fsettleorgid","ZT021");
            if("HK".equals(flag)){
                jsonObject.put("fthirdbillno",lcbh);
            }else if("TW".equals(flag)){
                lcbh = lcbh.substring(lcbh.indexOf("TW_")+3,lcbh.length());
                jsonObject.put("fthirdbillno",lcbh);
            }
            jsonObject.put("fdate",djrq);
            jsonObject.put("fhdc",fhdc);
            jsonObject.put("fsettlecurrid","PRE005");
        }
        writeLog("jsonObject="+jsonObject.toJSONString());

        String dt1Sql = "select dt1.tm,dt1.sl,dt1.xsj from formtable_main_249 as main inner join formtable_main_249_dt1 dt1 on main.id = dt1.mainid where requestId = ?";

        RecordSet rsDt1 = new RecordSet();

        rsDt1.executeQuery(dt1Sql,requestid);
        JSONArray jsonArray = new JSONArray();

        while (rsDt1.next()){
            String tm = Util.null2String(rsDt1.getString("tm"));
            String sl = Util.null2String(rsDt1.getString("sl"));
            String xsj = Util.null2String(rsDt1.getString("xsj"));

            JSONObject dt1Json = new JSONObject();

            dt1Json.put("fmaterialId",tm);
            //默认税率5
            if("TW".equals(flag)){
                dt1Json.put("fentrytaxrate","0");
                //查询价目表
                queryPriceTable(tm,dt1Json);
                //dt1Json.put("ftaxprice",xsj);
            }

            dt1Json.put("frealqty",sl);
            String fhdc = jsonObject.getString("fhdc");
            dt1Json.put("fstockid",fhdc);

            jsonArray.add(dt1Json);
        }
        jsonObject.remove("fhdc");
        jsonObject.put("fentrylist",jsonArray);

        String param = jsonObject.toJSONString();

        writeLog("param="+param);

        String resStr = doK3Action(param,meIp,putPurUrl);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            addLog(lcbh,"200");
            writeLog("同步金蝶采购入库单成功");

        }else {
            addLog(lcbh,"500");
            writeLog("同步金蝶采购入库单失败");
        }
        return "success";
    }

    public String putGYJConsPur(String requestid){

        String mainSql = "select lcbh,fhdc,djrq,kh from formtable_main_249 where requestId = ?";


        RecordSet rsMain = new RecordSet();

        rsMain.executeQuery(mainSql,requestid);
        JSONObject jsonObject = new JSONObject();
        String lcbh = "";
        while (rsMain.next()){
            lcbh = Util.null2String(rsMain.getString("lcbh"));
            String djrq = Util.null2String(rsMain.getString("djrq"));
            writeLog("djrq="+djrq);
            String fhdc = Util.null2String(rsMain.getString("fhdc"));
            //String bb = Util.null2String(rsMain.getString("bb"));

            lcbh= lcbh.replace("GYJ_TW_","GYJ_");

            jsonObject.put("fbillno",lcbh);
            jsonObject.put("fstockorgid","ZT030");
            jsonObject.put("fpurchaseorgid","ZT030");
            jsonObject.put("fsupplierId","ZT026");
            jsonObject.put("fdemandorgid","ZT030");
            jsonObject.put("fsettleorgid","ZT030");
            jsonObject.put("fthirdbillno",lcbh);
            jsonObject.put("fdate",djrq);
            jsonObject.put("fhdc",fhdc);
            jsonObject.put("fsettlecurrid","PRE005");
            jsonObject.put("fisincludedtax","true");
        }
        writeLog("jsonObject="+jsonObject.toJSONString());

        String dt1Sql = "select dt1.tm,dt1.sl from formtable_main_249 as main inner join formtable_main_249_dt1 dt1 on main.id = dt1.mainid where requestId = ?";
        RecordSet rsDt1 = new RecordSet();

        rsDt1.executeQuery(dt1Sql,requestid);
        JSONArray jsonArray = new JSONArray();

        while (rsDt1.next()){
            String tm = Util.null2String(rsDt1.getString("tm"));
            String sl = Util.null2String(rsDt1.getString("sl"));

            JSONObject dt1Json = new JSONObject();

            dt1Json.put("fmaterialId",tm);
            //税率5
            dt1Json.put("fentrytaxrate","5");

            getPrice(tm,dt1Json);

            dt1Json.put("frealqty",sl);
            String fhdc = jsonObject.getString("fhdc");
            dt1Json.put("fstockid",fhdc);

            jsonArray.add(dt1Json);
        }

        jsonObject.remove("fhdc");
        jsonObject.put("fentrylist",jsonArray);

        String param = jsonObject.toJSONString();

        writeLog("param="+param);

        String resStr = doK3Action(param,meIp,putPurUrl);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            addLog(lcbh,"200");
            writeLog("同步金蝶采购入库单成功");

        }else {
            addLog(lcbh,"500");
            writeLog("同步金蝶采购入库单失败");
        }
        return "success";
    }

    public String putGYJTWConsPur(String requestid){

        String mainSql = "select lcbh,fhdc,djrq,kh from formtable_main_249 where requestId = ?";


        RecordSet rsMain = new RecordSet();

        rsMain.executeQuery(mainSql,requestid);
        JSONObject jsonObject = new JSONObject();
        String lcbh = "";
        while (rsMain.next()){
            lcbh = Util.null2String(rsMain.getString("lcbh"));
            String djrq = Util.null2String(rsMain.getString("djrq"));
            writeLog("djrq="+djrq);
            String fhdc = Util.null2String(rsMain.getString("fhdc"));
            //String bb = Util.null2String(rsMain.getString("bb"));

            lcbh= lcbh.replace("GYJ_HK_","GYJ_TW_");

            jsonObject.put("fbillno",lcbh);
            jsonObject.put("fstockorgid","ZT026");
            jsonObject.put("fpurchaseorgid","ZT026");
            jsonObject.put("fsupplierId","ZT021");
            jsonObject.put("fdemandorgid","ZT026");
            jsonObject.put("fsettleorgid","ZT026");
            jsonObject.put("fthirdbillno",lcbh);
            jsonObject.put("fdate",djrq);
            jsonObject.put("fhdc",fhdc);
            jsonObject.put("fsettlecurrid","PRE005");
            jsonObject.put("fisincludedtax","true");
        }
        writeLog("jsonObject="+jsonObject.toJSONString());

        String dt1Sql = "select dt1.tm,dt1.sl from formtable_main_249 as main inner join formtable_main_249_dt1 dt1 on main.id = dt1.mainid where requestId = ?";
        RecordSet rsDt1 = new RecordSet();

        rsDt1.executeQuery(dt1Sql,requestid);
        JSONArray jsonArray = new JSONArray();

        while (rsDt1.next()){
            String tm = Util.null2String(rsDt1.getString("tm"));
            String sl = Util.null2String(rsDt1.getString("sl"));

            JSONObject dt1Json = new JSONObject();

            dt1Json.put("fmaterialId",tm);
            //税率5
            dt1Json.put("fentrytaxrate","5");

            getPrice(tm,dt1Json);

            dt1Json.put("frealqty",sl);
            String fhdc = jsonObject.getString("fhdc");
            dt1Json.put("fstockid",fhdc);

            jsonArray.add(dt1Json);
        }

        jsonObject.remove("fhdc");
        jsonObject.put("fentrylist",jsonArray);

        String param = jsonObject.toJSONString();

        writeLog("param="+param);

        String resStr = doK3Action(param,meIp,putPurUrl);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            addLog(lcbh,"200");
            writeLog("同步金蝶采购入库单成功");

        }else {
            addLog(lcbh,"500");
            writeLog("同步金蝶采购入库单失败");
        }
        return "success";
    }



    public String putTrf(String requestid, Map<String,String> mainData, String flag){

        JSONObject jsonObject = new JSONObject();

        /*发货店仓*/
        String fhdc = mainData.get("fhdc");
        /*收货店仓*/
        String shdc = mainData.get("shdc");
        /*发货日期*/
        String chrq = mainData.get("chrq");
        /*流程编号*/
        String lcbh = mainData.get("lcbh");

        String ydn = mainData.get("ydh");

        if ("HK".equals(flag)){
            jsonObject.put("fstockoutorgid","ZT021");
        }else if("TW".equals(flag)){
            jsonObject.put("fstockoutorgid","ZT026");
        }

        jsonObject.put("fbillNo",lcbh);
        jsonObject.put("fdate",chrq);
        jsonObject.put("fthirdsrcbillno",ydn);

        String dtSql = "select dt1.tm,dt1.sl from formtable_main_249 main inner join formtable_main_249_dt1 dt1 on main.id = dt1.mainid where main.requestId = ? ";

        RecordSet rs = new RecordSet();

        rs.executeQuery(dtSql,requestid);

        JSONArray jsonArray = new JSONArray();

        while (rs.next()){
            /*sku条码*/
            String tm = rs.getString("tm");

            String sl = rs.getString("sl");

            JSONObject dt1Json = new JSONObject();

            dt1Json.put("fmaterialid",tm);
            dt1Json.put("fqty",sl);
            dt1Json.put("fsrcstockid",fhdc);
            dt1Json.put("fdeststockid",shdc);

            jsonArray.add(dt1Json);
        }

        jsonObject.put("fentitylist",jsonArray);

        String param = jsonObject.toJSONString();

        String resStr = doK3Action(param,meIp,putTrfUrl);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            addLog(lcbh,"200");
            writeLog("同步金蝶采购入库单成功");

        }else {
            addLog(lcbh,"500");
            writeLog("同步金蝶采购入库单失败");
        }
        return "success";
    }


    public String putReSale(String requestid,String flag){

        writeLog("执行putReSale ");

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

            if("HK".equals(flag)){
                jsonObject.put("fstockorgid","ZT021");
                jsonObject.put("fsaleorgid","ZT021");
                jsonObject.put("fsettleorgid","ZT021");
                jsonObject.put("fretcustid","CUST0558");
            }else if("TW".equals(flag)){
                jsonObject.put("fstockorgid","ZT026");
                jsonObject.put("fsaleorgid","ZT026");
                jsonObject.put("fsettleorgid","ZT026");
                jsonObject.put("fretcustid",kh);
            }


            if("HK".equals(flag)){
                lcbh = lcbh.substring(lcbh.indexOf("HK_")+3,lcbh.length());
                jsonObject.put("fthirdbillno",lcbh);
            }else if("TW".equals(flag)){
                lcbh = lcbh.substring(lcbh.indexOf("TW_")+3,lcbh.length());
                jsonObject.put("fthirdbillno",lcbh);
            }
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
            //默认税率5
            if("HK".equals(flag)){
                dt1Json.put("fentrytaxrate","0");
                //查询价目表
                queryPriceTable(tm,dt1Json);
                //dt1Json.put("ftaxprice",xsj);
            }else if("TW".equals(flag)){
                dt1Json.put("fentrytaxrate",taxrate);
                dt1Json.put("ftaxprice",xsj);
            }

            dt1Json.put("frealqty",sl);
            //String fhdc = jsonObject.getString("fhdc");
            dt1Json.put("fstockid",shdc);

            jsonArray.add(dt1Json);
        }

        jsonObject.put("fentitylist",jsonArray);

        String param = jsonObject.toJSONString();

        writeLog("param="+param);

        String resStr = doK3Action(param,meIp,putReSaleUrl);

        writeLog("resStr="+resStr);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            addLog(lcbh,"200");
            writeLog("同步金蝶销售退货单成功");

        }else {
            addLog(lcbh,"500");
            writeLog("同步金蝶销售退货单失败");
        }


        return code;
    }

    public String putGyjReSale(String requestid,String flag){

        writeLog("putGyjReSale ");

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

            if("GYJHK".equals(flag)){
                jsonObject.put("fstockorgid","ZT021");
                jsonObject.put("fsaleorgid","ZT021");
                jsonObject.put("fsettleorgid","ZT021");
                jsonObject.put("fretcustid","CUST0558");
                lcbh = lcbh.substring(lcbh.indexOf("GYJHK_")+6,lcbh.length());
                jsonObject.put("fthirdbillno",lcbh);
            }else if("GYJTW".equals(flag)){
                jsonObject.put("fstockorgid","ZT026");
                jsonObject.put("fsaleorgid","ZT026");
                jsonObject.put("fsettleorgid","ZT026");
                jsonObject.put("fretcustid","CUST0354");
                lcbh = lcbh.substring(lcbh.indexOf("GYJTW_")+6,lcbh.length());
                jsonObject.put("fthirdbillno",lcbh);
            }else if("GYJ".equals(flag)){
                jsonObject.put("fstockorgid","ZT030");
                jsonObject.put("fsaleorgid","ZT030");
                jsonObject.put("fsettleorgid","ZT030");
                jsonObject.put("fretcustid",kh);
                lcbh = lcbh.substring(lcbh.indexOf("GYJ_")+4,lcbh.length());
                jsonObject.put("fthirdbillno",lcbh);
            }


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

            if("GYJHK".equals(flag)){
                dt1Json.put("fentrytaxrate","0");
                queryPriceTable(tm,dt1Json);
            }else if ("GYJ".equals(flag)){
                dt1Json.put("fentrytaxrate",taxrate);
                dt1Json.put("ftaxprice",xsj);
            }else if("GYJTW".equals(flag)){
                dt1Json.put("fentrytaxrate","0");
                queryRetPrice(tm,dt1Json);
            }


            dt1Json.put("frealqty",sl);
            //String fhdc = jsonObject.getString("fhdc");
            dt1Json.put("fstockid",shdc);

            jsonArray.add(dt1Json);
        }

        jsonObject.put("fentitylist",jsonArray);

        String param = jsonObject.toJSONString();

        writeLog("param="+param);

        String resStr = doK3Action(param,meIp,putReSaleUrl);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            addLog(lcbh,"200");
            writeLog("同步金蝶销售退货单成功");

        }else {
            addLog(lcbh,"500");
            writeLog("同步金蝶销售退货单失败");
        }


        return code;
    }




    public String doK3Action(String param,String meIp,String url){
        CloseableHttpResponse response;// 响应类,
        CloseableHttpClient httpClient = HttpClients.createDefault();


        HttpPost httpPost = new HttpPost(meIp+url);

        writeLog("meIp+url="+meIp+url);

        //设置请求头
        httpPost.addHeader("Content-Type", "application/json;charset=utf-8");
        httpPost.setEntity(new StringEntity(param, "UTF-8"));
        try {
            response = httpClient.execute(httpPost);
            String resulString = EntityUtils.toString(response.getEntity());
            writeLog("获取接口数据成功，接口返回体：" + resulString);
            return resulString;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void queryPriceTable(String sku,JSONObject dt1Json){

        String sql = "select FTAXPRICE from uf_T_PUR_PRICELIST where fnumber = "+"'"+sku+"'"+" and pricelist_fnumber = ?";

        RecordSet rs = new RecordSet();

        writeLog("价目表sql="+sql);

        rs.executeQuery(sql,"CGJM000032");

        if(rs.next()){
            //writeLog("1111111111111111111111111111111");
            String price = rs.getString("FTAXPRICE");
            dt1Json.put("ftaxprice",price);
        }else {
            //writeLog("22222222222222222222222222222222");
            dt1Json.put("ftaxprice","0.0");
        }
    }

    public void getPrice(String sku,JSONObject dt1Json){

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sku",sku);

        String fTaxPrice = doK3Action(jsonObject.toJSONString(),meIp,"/dmsBridge/k3/getPrice");

        dt1Json.put("ftaxprice",fTaxPrice);
    }

    public void getDailyNecPrice(String sku,JSONObject dt1Json){

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sku",sku);

        String fTaxPrice = doK3Action(jsonObject.toJSONString(),meIp,"/dmsBridge/k3/getDailyNecPrice");

        dt1Json.put("ftaxprice",fTaxPrice);
    }

    public String getPrice(String sku){
        writeLog("sku="+sku);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sku",sku);

        String fTaxPrice = doK3Action(jsonObject.toJSONString(),meIp,"/dmsBridge/k3/getPrice");

        return fTaxPrice;
    }

    public void queryRetPrice(String sku,JSONObject dt1Json){

        String sql = "select lsdj from uf_spk where hpbh = " +"'"+sku+"'";

        RecordSet rs = new RecordSet();

        writeLog("零售价sql="+sql);

        rs.executeQuery(sql);

        if(rs.next()){
            //writeLog("1111111111111111111111111111111");
            //零售定价
            String lsdj = rs.getString("lsdj");
            double price = Double.parseDouble(lsdj);
            double discountedPrice = price * 0.35;
            dt1Json.put("ftaxprice",discountedPrice);
        }else {
            //writeLog("22222222222222222222222222222222");
            dt1Json.put("ftaxprice","0.0");
        }

    }

    public String queryPriceTable(String sku){

        String sql = "select FTAXPRICE from uf_T_PUR_PRICELIST where fnumber = "+"'"+sku+"'"+" and pricelist_fnumber = ?";

        RecordSet rs = new RecordSet();

        writeLog("价目表sql="+sql);

        rs.executeQuery(sql,"CGJM000032");

        String price = "";

        if(rs.next()){
            //writeLog("333333333333333333333333");
            price = rs.getString("FTAXPRICE");
        }

        return price;
    }

    public void addLog(String lcbh,String status){
        String insertSql = "insert into k3_tran_log (lcbh,status,createTime) values (?,?,?)";
        RecordSet insertRs = new RecordSet();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String todayString = today.format(formatter);

        todayString = "'" + todayString + "'";
        insertRs.executeUpdate(insertSql,lcbh,status,todayString);
    }


    public void addDmsKErrorLog(String sku,String lcbh){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = dateFormat.format(new Date());

        //now = "'"+now+"'";

        StringTokenizer tokenizer = new StringTokenizer(now, " ");

        String dateString = tokenizer.nextToken();
        String timeString = tokenizer.nextToken();


        sku = sku+"库存不足";

        String insertError = "insert into dms_k3_error_log (billNo,message,createTime,date,time) values ('" +lcbh+"','"+sku+"','"+now+"','"+dateString+"','"+timeString+"')";

        RecordSet insertRs = new RecordSet();

        insertRs.executeUpdate(insertError);
    }
}
