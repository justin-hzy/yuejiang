package weaver.interfaces.hzy.k3.resale.service;

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

public class TransReSaleTwRePurService extends BaseBean {

    private String putTwRePurUrl = getPropValue("k3_api_config","putTwRePurUrl");

    private String getTwPurPriceUrl = getPropValue("k3_api_config","getTwPurPriceUrl");

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    public String putTwRePur(String requestid) {
        writeLog("执行putTwRePur");

        String mainSql = "select lcbh,rkrq,hk_bb from formtable_main_263 where requestId = ?";

        RecordSet rsMain = new RecordSet();
        rsMain.executeQuery(mainSql, requestid);
        JSONObject jsonObject = new JSONObject();
        String processCode = "";

        while (rsMain.next()){

            processCode = Util.null2String(rsMain.getString("lcbh"));

            String receiveDate = Util.null2String(rsMain.getString("rkrq"));
            String hkCurrency = Util.null2String(rsMain.getString("hk_bb"));

            jsonObject.put("fbillno","TW_"+processCode);
            jsonObject.put("fstockorgid","ZT026");
            jsonObject.put("fpurchaseorgid","ZT026");
            jsonObject.put("fsupplierid","ZT021");
            jsonObject.put("fdemandorgid","ZT026");
            jsonObject.put("fthirdbillno",processCode);
            jsonObject.put("fdate",receiveDate);
            jsonObject.put("fsettlecurrid",hkCurrency);
        }

        writeLog("jsonObject="+jsonObject.toJSONString());

        String param = getDtl(requestid,jsonObject);

        writeLog("param="+param);

        String resStr = doK3Action(param,k3Ip,putTwRePurUrl);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            addLog(processCode,"200");
            writeLog("同步金蝶销售退货单成功");
            updateIsNext(requestid,0);
        }else {
            addLog(processCode,"500");
            writeLog("同步金蝶销售退货单失败");
            updateIsNext(requestid,1);
        }
        return code;
    }

    public String getDtl(String requestid,JSONObject jsonObject){
        String dt1Sql = "select dt5.sku,dt5.refund_qty,main.shdc " +
                "from formtable_main_263 as main inner join formtable_main_263_dt5 dt5 " +
                "on main.id = dt5.mainid where requestId = ? and dt5.refund_qty > 0 and dt5.refund_qty is not null";
        RecordSet rsDt1 = new RecordSet();
        rsDt1.executeQuery(dt1Sql,requestid);
        JSONArray jsonArray = new JSONArray();

        while (rsDt1.next()){
            String sku = Util.null2String(rsDt1.getString("sku"));
            String refundQty = Util.null2String(rsDt1.getString("refund_qty"));
            String shdc = Util.null2String(rsDt1.getString("shdc"));

            JSONObject dt1Json = new JSONObject();
            dt1Json.put("fmaterialid",sku);
            dt1Json.put("fentrytaxrate","0");
            //查询价目表
            getPrice(sku,dt1Json);
            dt1Json.put("frmrealqty",refundQty);
            dt1Json.put("fstockid",shdc);
            jsonArray.add(dt1Json);
        }
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
        String updateSql = "update formtable_main_263 set is_next = ? where requestId = ?";
        RecordSet updateRs = new RecordSet();
        updateRs.executeUpdate(updateSql,isNext,requestid);
    }
}
