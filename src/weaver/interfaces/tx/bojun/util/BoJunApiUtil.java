package weaver.interfaces.tx.bojun.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wbi.util.Util;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import weaver.conn.RecordSet;
import weaver.formmode.data.ModeDataIdUpdate;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * FileName: BoJunApiUtil.java
 * 调用伯俊接口工具类
 *
 * @Author tx
 * @Date 2023/7/14
 * @Version 1.00
 **/
public class BoJunApiUtil extends BaseBean {

    private String bojunIp = getPropValue("bojun_api_config","bojunIp");
    private String ticket; //ticket
    private String tableName = "uf_bjjkpz"; //接口配置表
    private RecordSet fieldRs = new RecordSet(); //字段属性数据库连接

    public BoJunApiUtil(){
        ticket = new TicketUtil().getTicket();
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public void refreshTicket() {
        this.ticket = new TicketUtil().refreshTicket();
    }


    /**
     * 请求伯俊接口
     *
     * @param apiId 接口id
     * @param params 接口入参
     * @return 接口返回体
     */
    public JSONObject doAction(String apiId, String params){
        JSONObject apiRes = new JSONObject();

        Map<String,String> apiMap = getApiConfig(apiId);
        writeLog("开始调用apiId为" + apiId + "的接口：" + apiMap.toString());

        CloseableHttpResponse response;// 响应类,
        CloseableHttpClient httpClient = HttpClients.createDefault();

        //restful接口url
        HttpPost httpPost = new HttpPost(bojunIp + apiMap.get("bjjk"));

        //设置请求头
        httpPost.addHeader("Content-Type","application/json;charset=utf-8");
        httpPost.addHeader("Authorization", ticket);
        try {
            //入参
//            String params = getParams(apiId, mainData, detailData);
            httpPost.setEntity(new StringEntity(params, "UTF-8"));
            response = httpClient.execute(httpPost);
            if (response != null && response.getEntity() != null) {
                //返回信息
                String resulString = EntityUtils.toString(response.getEntity());
                writeLog("获取接口数据成功，接口返回体：" + resulString);

                //处理返回信息
                apiRes = JSON.parseObject(resulString);

            }else{
                apiRes.put("result","false");
                apiRes.put("message","接口错误返回错误！");
                writeLog("获取接口数据失败，接口返回体为空！");
            }
        } catch (Exception e) {
            apiRes.put("result","false");
            apiRes.put("message","接口请求失败！");
            writeLog("请求失败:"+e);
        }

        return apiRes;
    }


    /**
     * 请求伯俊接口
     *
     * @param apiId 接口id
     * @param mainData 流程主表数据
     * @param detailData  流程明细表数据
     * @return 接口返回体
     */
    public JSONObject doAction(String apiId, Map<String,String> mainData, List<Map<String, String>> detailData){
        String params = getParams(apiId, mainData, detailData);
        JSONObject apiRes = doAction(apiId, params);
        return apiRes;
    }


    /**
     * 构建参数
     *
     * @param apiId 接口配置表id
     * @param mainData 主表数据
     * @param detailData 明细表数据
     * @return json格式的参数字符串
     */
    public String getParams(String apiId, Map<String,String> mainData, List<Map<String, String>> detailData){
        writeLog("开始构建apiId为" + apiId + "的接口参数-------");

        String params = "";
        JSONObject mainJson = new JSONObject(); //主表参数
        JSONArray detailArr = new JSONArray(); //明细参数
        List<Map<String,String>> dtFields = new ArrayList<>(); //保存流程明细字段

        //配置表中获取参数字段
        RecordSet rs = new RecordSet();
        rs.executeQuery("select zdlx,bjzdm,lczdm,zdsx from " + tableName + "_dt1 where mainid = ? ", apiId);
        while (rs.next()){

            String zdlx = Util.null2String(rs.getString("zdlx")); //字段类型
            String bjzdm = Util.null2String(rs.getString("bjzdm")); //伯俊字段名
            String lczdm = Util.null2String(rs.getString("lczdm")); //流程字段名
            String zdsx = Util.null2String(rs.getString("zdsx")); //字段属性

            if(zdlx.equals("0")){
                //获取主参数
                String lczdVal = getMainFieldVal(lczdm,zdsx,mainData);
                if(!"".equals(Util.null2String(lczdVal))){
                    mainJson.put(bjzdm,lczdVal);
                }
            }else if(zdlx.equals("1")){
                Map<String,String> fieldMap = new HashMap<>();
                fieldMap.put("bjzdm",bjzdm);
                fieldMap.put("lczdm",lczdm);
                fieldMap.put("zdsx",zdsx);
                dtFields.add(fieldMap);
            }
        }
        writeLog("主参数" + mainJson.toJSONString());

        if(detailData != null && detailData.size()>0) {
            //获取明细参数
            detailArr = getDtFieldVal(dtFields, detailData);
            mainJson.put("details", detailArr);
            writeLog("明细参数" + detailArr.toJSONString());
        }

        params = mainJson.toJSONString();

        writeLog("apiId为" + apiId + "的接口参数:" + params);
        return params;
    }






    /**
     * 获取主参数
     *
     * @param lczdm 流程字段名
     * @param zdsx 字段属性
     * @param mainData 主表数据
     * @return json格式的参数字符串
     */
    public String getMainFieldVal(String lczdm, String zdsx, Map<String,String> mainData){
        String fieldValue = mainData.get(lczdm);
        if(!"".equals(Util.null2String(zdsx))){
            writeLog(lczdm + "的字段属性：" + zdsx );
            fieldValue = fieldProps(zdsx,fieldValue);
        }
        return fieldValue;
    }


    /**
     * 获取明细参数
     *
     * @param dtFields 流程明细字段集
     * @param detailData 明细表数据
     * @return json格式的参数字符串
     */
    public JSONArray getDtFieldVal(List<Map<String,String>> dtFields, List<Map<String, String>> detailData){
        JSONArray detailArr = new JSONArray();

        for (Map<String,String> dtDataMap : detailData) {
            JSONObject dtObj = new JSONObject();
            for (Map<String,String> fieldMap : dtFields) {
                String bjzdm = fieldMap.get("bjzdm");
                String lczdm = fieldMap.get("lczdm");
                String zdsx = fieldMap.get("zdsx");
                String fieldValue = dtDataMap.get(lczdm);
                if(!"".equals(Util.null2String(zdsx))){
                    writeLog("明细字段" + lczdm + "的字段属性：" + zdsx );
                    fieldValue = fieldProps(zdsx,fieldValue);
                }
                if(!"".equals(Util.null2String(fieldValue))){
                    dtObj.put(bjzdm,fieldValue);
                }
            }
            detailArr.add(dtObj);
        }
        return detailArr;
    }

    /**
     * 根据字段属性获取流程字段值
     *
     * @param zdsx 字段属性
     * @param fieldVal 字段值
     * @return 转换后的字段值
     */
    public String fieldProps(String zdsx, String fieldVal) {
        String fieldValue = fieldVal;
        writeLog("转换前：" + fieldValue );
        fieldRs.executeQuery(zdsx,fieldVal);
        if(fieldRs.next()){
            fieldValue = fieldRs.getString(1);
            writeLog("转换后：" + fieldValue );
        }else {
            writeLog( "转换失败！");
        }
        return fieldValue;
    }

    /**
     * 根据伯俊接口配置表id获取接口信息
     *
     * @param apiId 接口id
     * @return 接口信息
     */
    public Map<String,String> getApiConfig(String apiId){
        Map<String,String> apiMap = new HashMap<>();
        RecordSet rs = new RecordSet();
        rs.executeQuery("select jkmc,bjjk,mxbxh from " + tableName + " where id = ?", apiId);
        while (rs.next()){
            apiMap.put("jkmc",rs.getString("jkmc"));
            apiMap.put("bjjk",rs.getString("bjjk"));
            apiMap.put("mxbxh",rs.getString("mxbxh"));
        }
        return apiMap;
    }

    /**
     * 收集伯俊接口错误信息
     *
     * @param apiId 接口id
     * @param requestId 流程请求id
     * @param errMessage 接口错误信息
     */
    public void errlogMessage(String apiId, String requestId, String params, String errMessage){
        writeLog("收集伯俊接口错误信息----------");

        ModeRightInfo ModeRightInfo = new ModeRightInfo();
        ModeRightInfo.setNewRight(true);
        ModeDataIdUpdate idUpdate = new ModeDataIdUpdate();

        //参数:建模表   模块ID 数据创建人   数据所属人员  创建日期YYYY-MM-DD  创建时间HH:mm:ss
        int modeid = Integer.valueOf(getPropValue("bojun_api_config","errlogModeId"));
        int billid = idUpdate.getModeDataNewId("uf_bjjkcwrzb", modeid, 1, 1, new SimpleDateFormat("yyyy-MM-dd").format(new Date()), new SimpleDateFormat("HH:mm:ss").format(new Date()));

        RecordSet rs = new RecordSet();
        String sql = "update uf_bjjkcwrzb set jkmc=?,dylc=?,jkcs=?,cwrz=? where id=?";
        rs.executeUpdate(sql, apiId, requestId, params, errMessage, billid);

        ModeRightInfo.editModeDataShare(1, modeid, billid);//重置建模权限
    }



}
