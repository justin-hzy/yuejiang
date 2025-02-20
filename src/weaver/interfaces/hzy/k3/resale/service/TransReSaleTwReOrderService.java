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

public class TransReSaleTwReOrderService extends BaseBean {


    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    private String putTwReSaleUrl = getPropValue("k3_api_config","putTwReSaleUrl");

    private String getTwPurPriceUrl = getPropValue("k3_api_config","getTwPurPriceUrl");

    public String putTwReSale(String requestid){

        writeLog("执行putTwReSale");
        String mainSql = "select lcbh,rkrq,kh,bb from formtable_main_263 where requestId =  ?";

        RecordSet rsMain = new RecordSet();
        rsMain.executeQuery(mainSql,requestid);
        JSONObject jsonObject = new JSONObject();
        String processCode = "";
        while (rsMain.next()){
            processCode = Util.null2String(rsMain.getString("lcbh"));

            String rkrq = Util.null2String(rsMain.getString("rkrq"));
            String kh = Util.null2String(rsMain.getString("kh"));
            String bb = Util.null2String(rsMain.getString("bb"));

            jsonObject.put("fbillno","TW_"+processCode);
            jsonObject.put("fstockorgid","ZT026");
            jsonObject.put("fsaleorgid","ZT026");
            jsonObject.put("fsettleorgid","ZT026");
            jsonObject.put("fretcustid",kh);


            jsonObject.put("fthirdbillno",processCode);

            jsonObject.put("fdate",rkrq);
            jsonObject.put("fsettlecurrid",bb);

        }
        writeLog("jsonObject="+jsonObject.toJSONString());

        String param = getDtl(requestid,jsonObject);

        writeLog("param="+param);

        String resStr = doK3Action(param,k3Ip,putTwReSaleUrl);

        writeLog("resStr="+resStr);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            addLog(processCode,"200");
            writeLog("同步金蝶台湾销售退货单成功");
            updateIsNext(requestid,0);
        }else {
            addLog(processCode,"500");
            writeLog("同步金蝶台湾销售退货单失败");
            updateIsNext(requestid,1);
        }
        return code;
    }

    public String getDtl(String requestid,JSONObject jsonObject){

        String dt1Sql = "select dt1.hptxm tm,dt1.fhl sl,dt1.ddje xsj,dt1.rkrq,main.shdc from formtable_main_263 main " +
                "inner join formtable_main_263_dt1 dt1 on main.id = dt1.mainid  where requestId = ? and dt1.fhl > 0 and dt1.fhl is not null";

        RecordSet rsDt1 = new RecordSet();

        rsDt1.executeQuery(dt1Sql,requestid);
        JSONArray jsonArray = new JSONArray();

        while (rsDt1.next()){
            String tm = Util.null2String(rsDt1.getString("tm"));
            String sl = Util.null2String(rsDt1.getString("sl"));
            String xsj = Util.null2String(rsDt1.getString("xsj"));

            String shdc = Util.null2String(rsDt1.getString("shdc"));


            JSONObject dt1Json = new JSONObject();

            dt1Json.put("fmaterialId",tm);
            //默认税率5
            dt1Json.put("fentrytaxrate","5");
            dt1Json.put("ftaxprice",xsj);

            dt1Json.put("frealqty",sl);

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
