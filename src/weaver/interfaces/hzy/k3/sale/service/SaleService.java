package weaver.interfaces.hzy.k3.sale.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SaleService extends BaseBean {

    private String meIp = getPropValue("fulun_api_config","meIp");

    private String putSaleUrl = getPropValue("k3_api_config","putSaleUrl");

    private String putPurUrl = getPropValue("k3_api_config","putPurUrl");

    private String putTWPurUrl = getPropValue("k3_api_config","putTWPurUrl");


    public String putHKSale(String requestid,String flag){

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
            writeLog("同步香港金蝶销售出库单成功");

        }else {
            addLog(lcbh,"500");
            writeLog("同步香港金蝶销售出库单失败");
        }

        return code;
    }

    public String putTWSale(String requestid,String flag){


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
            writeLog("同步台湾金蝶销售出库单成功");

        }else {
            addLog(lcbh,"500");
            writeLog("同步台湾金蝶销售出库单失败");
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

    public String putTWPur(String requestid,String flag){
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

        String resStr = doK3Action(param,meIp,putTWPurUrl);

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

    public void addLog(String lcbh,String status){
        String insertSql = "insert into k3_tran_log (lcbh,status,createTime) values (?,?,?)";
        RecordSet insertRs = new RecordSet();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String todayString = today.format(formatter);

        todayString = "'" + todayString + "'";
        insertRs.executeUpdate(insertSql,lcbh,status,todayString);
    }

    public void getPrice(String sku,JSONObject dt1Json){

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sku",sku);

        String fTaxPrice = doK3Action(jsonObject.toJSONString(),meIp,"/dmsBridge/k3/getPrice");

        dt1Json.put("ftaxprice",fTaxPrice);
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
}
