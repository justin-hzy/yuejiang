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
import weaver.interfaces.hzy.common.service.CommonService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TransConsGyjPurService extends BaseBean {

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    private String putTWPurUrl = getPropValue("k3_api_config","putTWPurUrl");

    private String getTwPurPriceUrl = getPropValue("k3_api_config","getTwPurPriceUrl");

    public String putGYJConsPur(String requestid){

        CommonService commonService = new CommonService();

        String mainSql = "select lcbh,fhdcxs,ddrq from formtable_main_238 where requestId = ?";


        RecordSet rsMain = new RecordSet();

        rsMain.executeQuery(mainSql,requestid);
        JSONObject jsonObject = new JSONObject();
        String lcbh = "";
        while (rsMain.next()){
            lcbh = Util.null2String(rsMain.getString("lcbh"));
            String ddrq = Util.null2String(rsMain.getString("ddrq"));
            String fhdcxs = Util.null2String(rsMain.getString("fhdcxs"));

            jsonObject.put("fbillno","GYJ_"+lcbh);
            jsonObject.put("fstockorgid","ZT030");
            jsonObject.put("fpurchaseorgid","ZT030");
            jsonObject.put("fsupplierId","ZT026");
            jsonObject.put("fdemandorgid","ZT030");
            jsonObject.put("fsettleorgid","ZT030");
            jsonObject.put("fthirdbillno",lcbh);
            jsonObject.put("fdate",ddrq);
            jsonObject.put("fhdcxs",fhdcxs);
            jsonObject.put("fsettlecurrid","PRE005");
            jsonObject.put("fisincludedtax","true");
        }
        writeLog("jsonObject="+jsonObject.toJSONString());

        String dt5Sql = "select dt5.sku,dt5.quantity from formtable_main_238 as main inner join formtable_main_238_dt5 dt5 on main.id = dt5.mainid where requestId = ?";
        RecordSet rsDt5 = new RecordSet();

        rsDt5.executeQuery(dt5Sql,requestid);
        JSONArray jsonArray = new JSONArray();

        while (rsDt5.next()){
            String tm = Util.null2String(rsDt5.getString("sku"));
            String sl = Util.null2String(rsDt5.getString("quantity"));

            JSONObject dt1Json = new JSONObject();

            dt1Json.put("fmaterialId",tm);
            //税率5
            dt1Json.put("fentrytaxrate","5");

            commonService.queryRetPrice(tm,dt1Json);

            dt1Json.put("frealqty",sl);
            String fhdcxs = jsonObject.getString("fhdcxs");
            dt1Json.put("fstockid",fhdcxs);

            jsonArray.add(dt1Json);
        }

        jsonObject.remove("fhdcxs");
        jsonObject.put("fentrylist",jsonArray);

        String param = jsonObject.toJSONString();

        writeLog("param="+param);

        String resStr = doK3Action(param,k3Ip,putTWPurUrl);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            addLog(lcbh,"200");
            writeLog("同步金蝶采购入库单成功");
            updateIsNext(requestid,0);
        }else {
            addLog(lcbh,"500");
            writeLog("同步金蝶采购入库单失败");
            updateIsNext(requestid,1);
        }
        return "success";
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
        String updateSql = "update formtable_main_249 set is_next = ? where requestId = ?";
        RecordSet updateRs = new RecordSet();
        updateRs.executeUpdate(updateSql,isNext,requestid);
    }
}
