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

public class TransReSaleGyjReOrderService extends BaseBean {

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    private String putGyjReSaleUrl = getPropValue("k3_api_config","putGyjReSaleUrl");

    public String putGyjReSale(String requestid){

        writeLog("putGyjReSale ");

        String mainSql = "select lcbh,rkrq,kh,bb from formtable_main_263 where requestId =  ?";

        RecordSet rsMain = new RecordSet();
        rsMain.executeQuery(mainSql,requestid);
        JSONObject jsonObject = new JSONObject();

        String processCode = "";
        while (rsMain.next()){

            processCode = Util.null2String(rsMain.getString("lcbh"));

            String receiveDate = Util.null2String(rsMain.getString("rkrq"));
            String fRetCustId = Util.null2String(rsMain.getString("kh"));
            String currency = Util.null2String(rsMain.getString("bb"));

            jsonObject.put("fbillno","GYJ_"+processCode);

            jsonObject.put("fstockorgid","ZT030");
            jsonObject.put("fsaleorgid","ZT030");
            jsonObject.put("fsettleorgid","ZT030");
            jsonObject.put("fretcustid",fRetCustId);
            jsonObject.put("fthirdbillno",processCode);

            jsonObject.put("fdate",receiveDate);
            jsonObject.put("fsettlecurrid",currency);

        }

        writeLog("jsonObject="+jsonObject.toJSONString());


        String dt1Sql = "select dt1.hptxm tm,dt1.fhl sl,main.shdc from formtable_main_263 main " +
                "inner join formtable_main_263_dt1 dt1 on main.id = dt1.mainid  where requestId = ? and dt1.fhl > 0 and dt1.fhl is not null";

        RecordSet rsDt1 = new RecordSet();

        rsDt1.executeQuery(dt1Sql,requestid);
        JSONArray jsonArray = new JSONArray();


        while (rsDt1.next()){

            String tm = Util.null2String(rsDt1.getString("tm"));
            String sl = Util.null2String(rsDt1.getString("sl"));
            String shdc = Util.null2String(rsDt1.getString("shdc"));


            JSONObject dt1Json = new JSONObject();

            dt1Json.put("fmaterialId",tm);

            dt1Json.put("fentrytaxrate","5");
            queryRetPrice(tm,dt1Json);

            dt1Json.put("frealqty",sl);
            //String fhdc = jsonObject.getString("fhdc");
            dt1Json.put("fstockid",shdc);

            jsonArray.add(dt1Json);
        }
        jsonObject.put("fentitylist",jsonArray);

        String param = jsonObject.toJSONString();

        writeLog("param="+param);

        String resStr = doK3Action(param,k3Ip,putGyjReSaleUrl);

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
            double discountedPrice = price * 0.36;
            dt1Json.put("ftaxprice",discountedPrice);
        }else {
            //writeLog("22222222222222222222222222222222");
            dt1Json.put("ftaxprice","0.0");
        }
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
