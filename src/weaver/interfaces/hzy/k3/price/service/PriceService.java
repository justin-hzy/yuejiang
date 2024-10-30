package weaver.interfaces.hzy.k3.price.service;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;

import java.io.IOException;

public class PriceService extends BaseBean {

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    public void queryPriceTable(String sku, JSONObject dt1Json){

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

        String fTaxPrice = doK3Action(jsonObject.toJSONString(),k3Ip,"/dmsBridge/k3/getPrice");

        dt1Json.put("ftaxprice",fTaxPrice);
    }

    public void getDailyNecPrice(String sku,JSONObject dt1Json){
        writeLog("查询香港日用品价目表");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sku",sku);

        String fTaxPrice = doK3Action(jsonObject.toJSONString(),k3Ip,"/dmsBridge/k3/getDailyNecPrice");

        dt1Json.put("ftaxprice",fTaxPrice);
    }

    public String getDailyNecPrice(String sku){
        writeLog("查询香港日用品价目表");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sku",sku);

        String fTaxPrice = doK3Action(jsonObject.toJSONString(),k3Ip,"/dmsBridge/k3/getDailyNecPrice");

        return fTaxPrice;

    }

    public String getPrice(String sku){
        writeLog("sku="+sku);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sku",sku);

        String fTaxPrice = doK3Action(jsonObject.toJSONString(),k3Ip,"/dmsBridge/k3/getPrice");

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
}
