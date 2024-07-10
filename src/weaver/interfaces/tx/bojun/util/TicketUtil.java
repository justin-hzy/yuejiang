package weaver.interfaces.tx.bojun.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloudstore.dev.api.util.Util_DataCache;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import weaver.general.BaseBean;

/**
 * FileName: TicketUtil.java
 * 获取用户权限，得到Ticket
 *
 * @Author tx
 * @Date 2023/7/14
 * @Version 1.00
 **/
public class TicketUtil extends BaseBean {

    private String bojunIp = getPropValue("bojun_api_config","bojunIp"); //接口ip
    private String basicAuth = getPropValue("bojun_api_config","basicAuth"); //BasicAuth
    private String ticketKey = "bojunTicket"; //ticket缓存key
    private int expireTimeSecond = 7200; //ticket过期时间


    /**
     * 获取用户权限，得到Ticket
     *
     * @return Ticket
     */
    public String getTicket(){
        String ticket = "";
        if(Util_DataCache.containsKey(ticketKey)){
            ticket = (String) Util_DataCache.getObjVal(ticketKey);
        }else {
            ticket = refreshTicket();
        }
        return ticket;
    }

    /**
     * 刷新Ticket
     *
     * @return Ticket
     */
    public String refreshTicket(){
        String ticket = "";
        String user = getPropValue("bojun_api_config","user");
        String pwd = getPropValue("bojun_api_config","pwd");
        CloseableHttpResponse response;// 响应类,
        CloseableHttpClient httpClient = HttpClients.createDefault();

        //restful接口url
//        HttpGet httpGet = new HttpGet("http://120.24.183.69:8089/api/User/Login?strUser=test@burgeon.com.cn&strPwd=bos20");
        HttpGet httpGet = new HttpGet(bojunIp+"/api/AuthorLogin/Login?strUser="+user+"&strPwd="+pwd);
        try{
            response = httpClient.execute(httpGet);
            if (response != null && response.getEntity() != null) {
                //返回信息
                String resulString = EntityUtils.toString(response.getEntity());
                writeLog("获取ticket成功"+ resulString);

                JSONObject res = JSON.parseObject(resulString);
                ticket = res.getJSONObject("data").get("Ticket").toString();

                //将ticket存入缓存并设置两个小时有效
                Util_DataCache.setObjVal(ticketKey, ticket, expireTimeSecond);

            }else{
                writeLog("获取ticket失败!");
            }
        }catch (Exception e){
            writeLog("获取ticket请求失败"+e);
        }

        return basicAuth +" "+ ticket;
    }

}
