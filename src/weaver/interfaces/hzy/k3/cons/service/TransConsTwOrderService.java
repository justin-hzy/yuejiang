package weaver.interfaces.hzy.k3.cons.service;

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

public class TransConsTwOrderService extends BaseBean {

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    private String putTWSaleUrl = getPropValue("k3_api_config","putTWSaleUrl");

    private String getTwPurPriceUrl = getPropValue("k3_api_config","getTwPurPriceUrl");

    public String putConsTwSale(String requestid){


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
            jsonObject.put("fcustomerid",kh);
            jsonObject.put("fdsgbase","ZT026");
            jsonObject.put("fsettleorgid","ZT026");
            jsonObject.put("type","TW");

            lcbh = lcbh.substring(lcbh.indexOf("TW_")+3,lcbh.length());
            jsonObject.put("fthirdbillno",lcbh);
            jsonObject.put("fdate",djrq);
            jsonObject.put("fhdc",fhdc);

        }

        writeLog("jsonObject="+jsonObject.toJSONString());

        String param = getDtl(requestid,jsonObject);

        writeLog("param="+param);

        String resStr = doK3Action(param,k3Ip,putTWSaleUrl);

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

    public String getDtl(String requestid,JSONObject jsonObject){
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
            //香港税率为0
            dt1Json.put("fentrytaxrate","5");

            //查询价目表
            //queryPriceTable(tm,dt1Json);
            //getPrice(tm,dt1Json);
            dt1Json.put("ftaxprice",xsj);

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

    public String doK3Action(String param,String meIp,String url){
        CloseableHttpResponse response;// 响应类,
        CloseableHttpClient httpClient = HttpClients.createDefault();


        HttpPost httpPost = new HttpPost(meIp+url);

        writeLog("ip+url="+meIp+url);

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

        String fTaxPrice = doK3Action(jsonObject.toJSONString(),k3Ip,getTwPurPriceUrl);

        dt1Json.put("ftaxprice",fTaxPrice);
    }

    public void updateIsNext(String requestid,Integer isNext){
        String updateSql = "update formtable_main_249 set is_next = ? where requestId = ?";
        RecordSet updateRs = new RecordSet();
        updateRs.executeUpdate(updateSql,isNext,requestid);
    }
}
