package weaver.interfaces.hzy.me.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icbc.api.internal.apache.http.impl.cookie.S;
import com.wbi.util.Util;
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
import weaver.interfaces.tx.bojun.util.TicketUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MeApiUtil extends BaseBean {

    //private String bojunIp = getPropValue("bojun_api_config","bojunIp");

    private String meIp = getPropValue("fulun_api_config","meIp");

    private String ticket; //ticket
    private String tableName = "uf_mejkpz"; //接口配置表
    private RecordSet fieldRs = new RecordSet(); //字段属性数据库连接




    /**
     * 请求ME接口
     *
     * @param apiId 接口id
     * @param params 接口入参
     * @return 接口返回体
     */
    public JSONObject doAction(String apiId, String params){
        JSONObject apiRes = new JSONObject();

/*        Map<String,String> apiMap = getApiConfig(apiId);
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
        }*/

        return apiRes;
    }



    /**
     * 请求ME接口
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


    /*请求ME接口*/
    public String doMeAction(String params,String apiId){
        CloseableHttpResponse response;// 响应类,
        CloseableHttpClient httpClient = HttpClients.createDefault();


        Map<String,String> apiMap = getApiConfig(apiId);

        HttpPost httpPost = new HttpPost(meIp+apiMap.get("mejk"));


        //设置请求头
        httpPost.addHeader("Content-Type", "application/json;charset=utf-8");
        httpPost.setEntity(new StringEntity(params, "UTF-8"));
        try {
            response = httpClient.execute(httpPost);
            String resulString = EntityUtils.toString(response.getEntity());
            writeLog("获取接口数据成功，接口返回体：" + resulString);
            return resulString;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        rs.executeQuery("select jkmc,mejk,mxbxh from " + tableName + " where id = ?", apiId);
        while (rs.next()){
            apiMap.put("jkmc",rs.getString("jkmc"));
            apiMap.put("mejk",rs.getString("mejk"));
            apiMap.put("mxbxh",rs.getString("mxbxh"));
        }
        return apiMap;
    }

    /**
     * 构建参数
     *
     * @param requestId 请求id
     * @return json格式的参数字符串
     */
    public List<String> getParams(String requestId, String apiId){
        writeLog("开始构建me接口-apiId为" + apiId + "的接口参数-------");
        List<String> params = null;
        if ("1".equals(apiId)){
            params = getPurInJson(requestId);
        }else if("2".equals(apiId)){
            params = getRePurJson(requestId);
        }else if("3".equals(apiId)){
            params = getSetFrJson(requestId);
        }else if("4".equals(apiId)){
            params = getSetSonJson(requestId);
        }else if("5".equals(apiId)){
            params = getDismantleFrJon(requestId);
        }
        /*else if("3".equals(apiId)){
            params = getSetFrJson(requestId);
        }else if("4".equals(apiId)){
            params = getSetSonJson(requestId);
        }else if("5".equals(apiId)){
            params = getDismantleFrJon(requestId);
        }
        else if("6".equals(apiId)){
            params = getDismantleSonJon(requestId);
        }else if("7".equals(apiId)){
            params = getTransCodeFrJson(requestId);
        }else if("8".equals(apiId)){
            params = getTransCodeSonJson(requestId);
        }*/
        return params;
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
     * 收集伯俊接口错误信息
     *
     * @param apiId 接口id
     * @param requestId 流程请求id
     * @param errMessage 接口错误信息
     */
    public void errlogMessage(String apiId, String requestId, String params, String errMessage){
        writeLog("收集ME接口错误信息----------");

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

    public List<String> getPurInJson(String requestId){
        writeLog("---------开始组装ME接口采购进仓单参数-----------");
        List<String> params = new ArrayList<>();
        RecordSet rs = new RecordSet();
        String sql1 = "select main.lcbh,main.rkck,dt2.wlbm,dt2.rksl,dt2.rkrq from formtable_main_234 as main inner join formtable_main_234_dt2 dt2 on main.id = dt2.mainid where main.requestId = ?";
        writeLog(sql1+","+requestId);
        rs.executeQuery(sql1, requestId);
        JSONObject jsonObject = new JSONObject();
        JSONArray subOthers = new JSONArray();
        while (rs.next()){

            String wlbm = Util.null2String(rs.getString("wlbm")); //物料编码
            String rksl = Util.null2String(rs.getString("rksl")); //入库数量

            JSONObject subOther = new JSONObject();
            subOther.put("sku",wlbm);
            subOther.put("qty",rksl);

            subOthers.add(subOther);

            String rkck = Util.null2String(rs.getString("rkck")); //入库仓库

            String rkrq = Util.null2String(rs.getString("rkrq")); //入库日期


            String lcbh = Util.null2String(rs.getString("lcbh")); //流程编号

            jsonObject.put("cstore",rkck);




            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String pushDate = today.format(formatter);

            rkrq = pushDate;

            rkrq= rkrq.replace("-","");
            jsonObject.put("billDate",rkrq);
            jsonObject.put("description","台湾采购申请流程-入库-"+lcbh);
            jsonObject.put("requestId",requestId);
        }

        jsonObject.put("subOthers",subOthers);

        String param = jsonObject.toJSONString();

        writeLog("param="+param);

        params.add(param);


        return params;
    }

    public List<String> getRePurJson(String requestId){
        writeLog("---------开始组装ME接口采购退货出库单参数-----------");
        List<String> params = new ArrayList<>();
        RecordSet rs1 = new RecordSet();
        String sql1 = "select main.rkck,dt2.wlbm,dt2.cksl from formtable_main_233 as main inner join formtable_main_233_dt2 dt2 on main.id = dt2.mainid where main.requestId = ?";
        writeLog(sql1+","+requestId);
        rs1.executeQuery(sql1, requestId);

        while (rs1.next()){
            JSONObject jsonObject = new JSONObject();

            String rkck = Util.null2String(rs1.getString("rkck")); //出库仓库
            String wlbm = Util.null2String(rs1.getString("wlbm")); //物料编码
            String cksl = Util.null2String(rs1.getString("cksl")); //出库数量

            jsonObject.put("cstore",rkck);
            jsonObject.put("sku",wlbm);
            jsonObject.put("qty",cksl);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String now = dateFormat.format(new Date());
            jsonObject.put("billDate",now);
            jsonObject.put("description","台湾采购申请流程-出库");
            jsonObject.put("requestId",requestId);

            String param = jsonObject.toJSONString();

            writeLog("param="+param);

            params.add(param);
        }
        writeLog("params="+params.toString());
        return params;
    }

    public List<String> getSetFrJson(String requestId){
        writeLog("---------开始组装ME接口组套-父项出库单参数-----------");
        List<String> params = new ArrayList<>();
        RecordSet rs1 = new RecordSet();

        String sql1 = "select main.lcbh,dt3.spbm,main.shdc1,dt3.sjrksl from formtable_main_242 as main inner join formtable_main_242_dt3 dt3 on main.id = dt3.mainid where dt3.sjrksl is not null and dt3.sjcksl is null and main.requestId = ?";
        writeLog(sql1+","+requestId);
        rs1.executeQuery(sql1, requestId);

        JSONObject jsonObject = new JSONObject();
        JSONArray subOthers = new JSONArray();

        while (rs1.next()){

            String shdc1 = Util.null2String(rs1.getString("shdc1")); //出库仓库
            String spbm = Util.null2String(rs1.getString("spbm")); //物料编码
            String sjrksl = Util.null2String(rs1.getString("sjrksl")); //父项实际入库数量

            String lcbh = Util.null2String(rs1.getString("lcbh")); //流程编号

            JSONObject subOther = new JSONObject();

            jsonObject.put("cstore",shdc1);

            subOther.put("sku",spbm);
            subOther.put("qty",sjrksl);
            subOthers.add(subOther);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String now = dateFormat.format(new Date());
            jsonObject.put("billDate",now);
            jsonObject.put("description","组装流程-入库-"+lcbh);
            jsonObject.put("requestId",requestId);

        }

        jsonObject.put("subOthers",subOthers);

        String param = jsonObject.toJSONString();

        writeLog("param="+param);
        params.add(param);

        return params;
    }


    public List<String> getSetSonJson(String requestId){
        writeLog("---------开始组装ME接口组套-子项出库单参数-----------");
        List<String> params = new ArrayList<>();
        RecordSet rs1 = new RecordSet();
        String sql1 = "select main.lcbh,dt1.spbm,main.fhdc1,dt1.sjztsl from formtable_main_242 as main inner join formtable_main_242_dt1 dt1 on main.id = dt1.mainid where main.requestId = ?";
        writeLog(sql1+","+requestId);
        rs1.executeQuery(sql1, requestId);

        JSONObject jsonObject = new JSONObject();

        JSONArray subOthers = new JSONArray();

        while (rs1.next()){


            String fhdc1 = Util.null2String(rs1.getString("fhdc1")); //子项-入库仓库
            String spbm = Util.null2String(rs1.getString("spbm")); //物料编码
            String sjztsl = Util.null2String(rs1.getString("sjztsl")); //子项-出库数量
            String lcbh = Util.null2String(rs1.getString("lcbh")); //子项-出库数量

            jsonObject.put("cstore",fhdc1);
            JSONObject subOther = new JSONObject();

            subOther.put("sku",spbm);
            subOther.put("qty",sjztsl);
            subOthers.add(subOther);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String now = dateFormat.format(new Date());
            jsonObject.put("billDate",now);
            jsonObject.put("description","组套流程-出库-"+lcbh);
            jsonObject.put("requestId",requestId);


        }

        jsonObject.put("subOthers",subOthers);

        String param = jsonObject.toJSONString();

        writeLog("param="+param);

        params.add(param);

        writeLog("params="+params.toString());
        return params;
    }

    public List<String> getDismantleSonJon(String requestId){
        writeLog("---------开始组装ME接口拆卸-子项入库单参数-----------");
        List<String> params = new ArrayList<>();
        RecordSet rs1 = new RecordSet();

        String sql1 = "select dt3.spbm,main.shdc1,dt3.sjrksl from formtable_main_242 as main inner join formtable_main_242_dt3 dt3 on main.id = dt3.mainid where dt3.sjrksl is not null and dt3.sjcksl is null and main.requestId = ?";

        writeLog(sql1+","+requestId);
        rs1.executeQuery(sql1, requestId);

        while (rs1.next()) {
            JSONObject jsonObject = new JSONObject();

            String shdc1 = Util.null2String(rs1.getString("shdc1")); //子项-入库仓库
            String spbm = Util.null2String(rs1.getString("spbm")); //物料编码
            String sjrksl = Util.null2String(rs1.getString("sjrksl")); //子项-出库数量

            jsonObject.put("cstore",shdc1);
            jsonObject.put("sku",spbm);
            jsonObject.put("qty",sjrksl);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String now = dateFormat.format(new Date());
            jsonObject.put("billDate",now);
            jsonObject.put("description","拆卸流程-入库单");
            jsonObject.put("requestId",requestId);

            String param = jsonObject.toJSONString();

            writeLog("param="+param);

            params.add(param);
        }
        writeLog("params="+params.toString());
        return params;
    }


    public List<String> getDismantleFrJon(String requestId){
        writeLog("---------开始组装ME接口拆卸-父项出库单参数-----------");
        List<String> params = new ArrayList<>();
        RecordSet rs1 = new RecordSet();

        String sql1 = "select main.lcbh,dt3.spbm,main.fhdc1,dt3.sjcksl from formtable_main_242 as main inner join formtable_main_242_dt3 dt3 on main.id = dt3.mainid where dt3.sjcksl is not null and dt3.sjrksl is null and main.requestId = ?";
        writeLog(sql1+","+requestId);
        rs1.executeQuery(sql1, requestId);

        JSONArray subOthers = new JSONArray();
        JSONObject jsonObject = new JSONObject();

        while (rs1.next()){



            String fhdc1 = Util.null2String(rs1.getString("fhdc1")); //出库仓库
            String spbm = Util.null2String(rs1.getString("spbm")); //物料编码
            String sjcksl = Util.null2String(rs1.getString("sjcksl")); //父项实际入库数量
            String lcbh = Util.null2String(rs1.getString("lcbh")); //流程编号


            jsonObject.put("cstore",fhdc1);

            JSONObject subOther = new JSONObject();
            subOther.put("sku",spbm);
            subOther.put("qty",sjcksl);
            subOthers.add(subOther);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String now = dateFormat.format(new Date());
            jsonObject.put("billDate",now);
            jsonObject.put("description","拆卸流程-出库-"+lcbh);
            jsonObject.put("requestId",requestId);


        }

        jsonObject.put("subOthers",subOthers);

        String param = jsonObject.toJSONString();

        writeLog("param="+param);

        params.add(param);

        writeLog("params="+params.toString());
        return params;
    }

    public List<String> getTransCodeFrJson(String requestId){
        writeLog("---------开始组装ME接口转码-父项出库单参数-----------");
        List<String> params = new ArrayList<>();
        RecordSet rs1 = new RecordSet();

        String sql1 = "select dt2.hpbh,dt2.ck,dt2.cksl from formtable_main_244 as main inner join formtable_main_244_dt2 as dt2 on main.id = dt2.mainid where dt2.cksl is not null and dt2.rksl is null and main.requestid = ?";

        writeLog(sql1+","+requestId);
        rs1.executeQuery(sql1, requestId);

        while (rs1.next()){
            JSONObject jsonObject = new JSONObject();

            String ck = Util.null2String(rs1.getString("ck")); //出库仓库
            String hpbh = Util.null2String(rs1.getString("hpbh")); //物料编码
            String cksl = Util.null2String(rs1.getString("cksl")); //父项实际入库数量

            jsonObject.put("cstore",ck);
            jsonObject.put("sku",hpbh);
            jsonObject.put("qty",cksl);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String now = dateFormat.format(new Date());
            jsonObject.put("billDate",now);
            jsonObject.put("description","转码流程-出库");
            jsonObject.put("requestId",requestId);

            String param = jsonObject.toJSONString();

            writeLog("param="+param);

            params.add(param);
        }
        writeLog("params="+params.toString());
        return params;
    }

    public List<String> getTransCodeSonJson(String requestId){
        writeLog("---------开始组装ME接口转码-父项出库单参数-----------");
        List<String> params = new ArrayList<>();
        RecordSet rs1 = new RecordSet();



        String sql1 = "select dt2.hpbh,dt2.ck,dt2.rksl from formtable_main_244 as main inner join formtable_main_244_dt2 as dt2 on main.id = dt2.mainid where dt2.rksl is not null and dt2.cksl is null and main.requestid = ?";

        writeLog(sql1+","+requestId);
        rs1.executeQuery(sql1, requestId);

        while (rs1.next()){
            JSONObject jsonObject = new JSONObject();

            String ck = Util.null2String(rs1.getString("ck")); //入库仓库
            String hpbh = Util.null2String(rs1.getString("hpbh")); //物料编码
            String rksl = Util.null2String(rs1.getString("rksl")); //父项实际入库数量

            jsonObject.put("cstore",ck);
            jsonObject.put("sku",hpbh);
            jsonObject.put("qty",rksl);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String now = dateFormat.format(new Date());
            jsonObject.put("billDate",now);
            jsonObject.put("description","转码流程-入库");
            jsonObject.put("requestId",requestId);

            String param = jsonObject.toJSONString();

            writeLog("param="+param);

            params.add(param);
        }
        writeLog("params="+params.toString());
        return params;
    }
}
