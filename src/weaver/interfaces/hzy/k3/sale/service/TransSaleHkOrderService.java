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
import org.h2.command.dml.Update;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TransSaleHkOrderService extends BaseBean {

    private String putHkSale = getPropValue("k3_api_config","putHKSaleUrl");

    private String getTwPurPriceUrl = getPropValue("k3_api_config","getTwPurPriceUrl");

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    public void putSale(String requestid,Integer id){

        String mainSql = "select lcbh,chrq,fhdc,bb from formtable_main_272 where requestId = ?";

        RecordSet rsMain = new RecordSet();

        rsMain.executeQuery(mainSql,requestid);
        JSONObject jsonObject = new JSONObject();
        String lcbh = "";
        while (rsMain.next()){
            lcbh = Util.null2String(rsMain.getString("lcbh"));
            String sendDate = Util.null2String(rsMain.getString("chrq"));
            String sendWareHouse = Util.null2String(rsMain.getString("fhdc"));
            String currencyId =  Util.null2String(rsMain.getString("bb"));


            jsonObject.put("fbillno","HK_"+lcbh);
            jsonObject.put("fstockorgid","ZT021");
            jsonObject.put("fsaleorgid","ZT021");
            jsonObject.put("fcustomerid","CUST0558");
            jsonObject.put("fdsgbase","ZT026");
            jsonObject.put("fsettleorgid","ZT021");
            jsonObject.put("fsettlecurrid",currencyId);
            jsonObject.put("fthirdbillno",lcbh);
            jsonObject.put("fdate",sendDate);
            jsonObject.put("fhdc",sendWareHouse);
        }

        String param = getDtl(id,jsonObject);

        writeLog("param="+param);

        String resStr = doK3Action(param,k3Ip,putHkSale);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");
        if("200".equals(code)){
            addLog(lcbh,"200");
            writeLog("同步金蝶销售出库单成功");
            updateIsNext(requestid,0);
        }else {
            addLog(lcbh,"500");
            writeLog("同步金蝶销售出库单失败");
            updateIsNext(requestid,1);
        }



        /*String mainSql = "select lcbh,chrq,fhdc,djrq,kh,ddje,bb,ydh,ck from formtable_main_249 where requestId = ?";

        RecordSet rsMain = new RecordSet();

        rsMain.executeQuery(mainSql,requestid);
        JSONObject jsonObject = new JSONObject();
        String lcbh = "";
        while (rsMain.next()){
            lcbh = Util.null2String(rsMain.getString("lcbh"));
            String chrq = Util.null2String(rsMain.getString("chrq"));
            String fhdc = Util.null2String(rsMain.getString("fhdc"));
            String bb = Util.null2String(rsMain.getString("bb"));

            jsonObject.put("fbillno",lcbh);
            jsonObject.put("fstockorgid","ZT021");
            jsonObject.put("fsaleorgid","ZT021");
            jsonObject.put("fcustomerid","CUST0558");
            jsonObject.put("fdsgbase","ZT026");
            jsonObject.put("fsettleorgid","ZT021");
            jsonObject.put("fsettlecurrid",bb);
            jsonObject.put("fthirdbillno",lcbh);
            jsonObject.put("fdate",chrq);
            jsonObject.put("fhdc",fhdc);
        }

        String param = getDtl(requestid,jsonObject);

        writeLog("param="+param);

        String resStr = doK3Action(param,k3Ip,putHkSale);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");
        if("200".equals(code)){
            addLog(lcbh,"200");
            writeLog("同步金蝶销售出库单成功");
            updateIsNext(requestid,0);
        }else {
            addLog(lcbh,"500");
            writeLog("同步金蝶销售出库单失败");
            updateIsNext(requestid,1);
        }

        return code;*/
    }


    // 获取销售发货金蝶子流程明细方法(废弃)
    public String getDtl(String requestid,JSONObject jsonObject){

        String dt1Sql = "select dt1.tm,dt1.sl,dt1.xsj,dt1.hplx,dt1.taxrate " +
                "from formtable_main_249 as main " +
                "inner join formtable_main_249_dt1 dt1 " +
                "on main.id = dt1.mainid " +
                "where requestId = ? and sl is not null and sl > 0";

        RecordSet rsDt1 = new RecordSet();

        rsDt1.executeQuery(dt1Sql,requestid);
        JSONArray jsonArray = new JSONArray();
        while (rsDt1.next()){
            String tm = Util.null2String(rsDt1.getString("tm"));
            String sl = Util.null2String(rsDt1.getString("sl"));

            JSONObject dt1Json = new JSONObject();
            dt1Json.put("fentryid",0);
            dt1Json.put("fmaterialId",tm);

            //香港税率为0
            dt1Json.put("fentrytaxrate","0");

            //查询价目表
            getPrice(tm,dt1Json);

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


    public String getDtl(Integer id,JSONObject jsonObject){

        String dt1Sql = "select tm,sl from formtable_main_272_dt3 where mainid = ?";

        RecordSet rsDt1 = new RecordSet();

        rsDt1.executeQuery(dt1Sql,id);
        JSONArray jsonArray = new JSONArray();
        while (rsDt1.next()){
            String tm = Util.null2String(rsDt1.getString("tm"));
            String sl = Util.null2String(rsDt1.getString("sl"));

            JSONObject dt1Json = new JSONObject();
            dt1Json.put("fentryid",0);
            dt1Json.put("fmaterialId",tm);

            //香港税率为0
            dt1Json.put("fentrytaxrate","0");

            //查询价目表
            getPrice(tm,dt1Json);

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


    public void getPrice(String sku,JSONObject dt1Json){

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sku",sku);

        String fTaxPrice = doK3Action(jsonObject.toJSONString(),k3Ip,getTwPurPriceUrl);

        dt1Json.put("ftaxprice",fTaxPrice);
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

    public void updateIsNext(String requestid,Integer isNext){
        String updateSql = "update formtable_main_272 set is_next = ? where requestId = ?";
        RecordSet updateRs = new RecordSet();
        updateRs.executeUpdate(updateSql,isNext,requestid);
    }
}
