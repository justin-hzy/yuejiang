package weaver.interfaces.hzy.k3.trf.service;

import com.alibaba.fastjson.JSONArray;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class TransHkTrfService extends BaseBean {


    private String putHkTrfUrl = getPropValue("k3_api_config","putHkTrfUrl");

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");


    public String putTrf(String requestid, Map<String,String> mainData){

        JSONObject jsonObject = new JSONObject();

        /*�������*/
        String fhdc = mainData.get("fhdc");
        /*�ջ����*/
        String shdc = mainData.get("shdc");
        /*��������*/
        String chrq = mainData.get("chrq");
        /*���̱��*/
        String lcbh = mainData.get("lcbh");

        String ydn = mainData.get("ydh");

        jsonObject.put("fstockoutorgid","ZT021");

        jsonObject.put("fbillNo",lcbh);
        jsonObject.put("fdate",chrq);
        jsonObject.put("fthirdsrcbillno",ydn);

        String dtSql = "select dt1.tm,dt1.sl from formtable_main_249 main inner join formtable_main_249_dt1 dt1 on main.id = dt1.mainid where main.requestId = ? ";

        RecordSet rs = new RecordSet();

        rs.executeQuery(dtSql,requestid);

        JSONArray jsonArray = new JSONArray();

        while (rs.next()){
            /*sku����*/
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

        String resStr = doK3Action(param,k3Ip,putHkTrfUrl);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            addLog(lcbh,"200");
            writeLog("ͬ������ɹ���ⵥ�ɹ�");

        }else {
            addLog(lcbh,"500");
            writeLog("ͬ������ɹ���ⵥʧ��");
        }
        return "success";
    }


    public String doK3Action(String param,String meIp,String url){
        CloseableHttpResponse response;// ��Ӧ��,
        CloseableHttpClient httpClient = HttpClients.createDefault();


        HttpPost httpPost = new HttpPost(meIp+url);

        writeLog("ip+url="+meIp+url);

        //��������ͷ
        httpPost.addHeader("Content-Type", "application/json;charset=utf-8");
        httpPost.setEntity(new StringEntity(param, "UTF-8"));
        try {
            response = httpClient.execute(httpPost);
            String resulString = EntityUtils.toString(response.getEntity());
            writeLog("��ȡ�ӿ����ݳɹ����ӿڷ����壺" + resulString);
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
}
