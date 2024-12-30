package weaver.interfaces.hzy.vn.flash.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wbi.util.Util;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlashApiUtil extends BaseBean {

    private String tableName = "uf_flash";

    private RecordSet fieldRs = new RecordSet(); //字段属性数据库连接

    public Map<String,String> getApiConfig(String apiId){
        Map<String,String> apiMap = new HashMap<>();
        RecordSet rs = new RecordSet();
        rs.executeQuery("select jkmc,fjk,mxbxh from " + tableName + " where id = ?", apiId);
        while (rs.next()){
            apiMap.put("jkmc",rs.getString("jkmc"));
            apiMap.put("fjk",rs.getString("fjk"));
            apiMap.put("mxbxh",rs.getString("mxbxh"));
        }
        return apiMap;
    }


    public String getParams(String apiId, Map<String,String> mainData, List<Map<String, String>> detailData){
        writeLog("开始构建apiId为" + apiId + "的接口参数-------");

        String params = "";
        JSONObject mainJson = new JSONObject(); //主表参数
        JSONArray detailArr = new JSONArray(); //明细参数

        JSONArray detailArr1 = new JSONArray(); //富仑明细参数

        List<Map<String,String>> dtFields = new ArrayList<>(); //保存流程明细字段
        //配置表中获取参数字段
        RecordSet rs = new RecordSet();
        rs.executeQuery("select zdlx,fzdm,lczdm,zdzx from " + tableName + "_dt1 where mainid = ? ", apiId);

        while (rs.next()){

            String zdlx = Util.null2String(rs.getString("zdlx")); //字段类型
            String fzdm = Util.null2String(rs.getString("fzdm")); //伯俊字段名
            String lczdm = Util.null2String(rs.getString("lczdm")); //流程字段名
            String zdzx = Util.null2String(rs.getString("zdzx")); //字段属性

            if(zdlx.equals("0")){
                //获取主参数
                String lczdVal = getMainFieldVal(lczdm,zdzx,mainData);
                if(!"".equals(Util.null2String(lczdVal))){
                    mainJson.put(fzdm,lczdVal);
                }
            }else if(zdlx.equals("1")){
                Map<String,String> fieldMap = new HashMap<>();
                fieldMap.put("fzdm",fzdm);
                fieldMap.put("lczdm",lczdm);
                fieldMap.put("zdzx",zdzx);
                dtFields.add(fieldMap);
            }
        }

        writeLog("主参数" + mainJson.toJSONString());

        writeLog("dtFields=" + dtFields.toString());

        if(detailData != null && detailData.size()>0) {
            //获取明细参数
            detailArr = getDtFieldVal(dtFields, detailData);
            writeLog("detailArr="+detailArr);
            if("1".equals(apiId)){
                mainJson = getOrderJSONObject(mainJson,detailArr);
            }else if("2".equals(apiId)){
                mainJson = getRefundJSONObject(mainJson,detailArr);
            }
        }

        params = mainJson.toJSONString();

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
                String fzdm = fieldMap.get("fzdm");
                String lczdm = fieldMap.get("lczdm");
                String zdzx = fieldMap.get("zdzx");
                String fieldValue = dtDataMap.get(lczdm);
                writeLog("fzdm="+fzdm);
                writeLog("lczdm="+lczdm);
                writeLog("zdzx="+zdzx);
                writeLog("fieldValue="+fieldValue);
                if(!"".equals(Util.null2String(zdzx))){
                    writeLog("明细字段" + lczdm + "的字段属性：" + zdzx );
                    fieldValue = fieldProps(zdzx,fieldValue);
                }
                if(!"".equals(Util.null2String(fieldValue))){
                    dtObj.put(fzdm,fieldValue);
                }
            }
            detailArr.add(dtObj);
        }
        return detailArr;
    }

    public JSONObject getOrderJSONObject(JSONObject mainJson,JSONArray detailArr){

        writeLog("明细参数" + detailArr.toJSONString());
        writeLog("主参数"+mainJson.toJSONString());
        mainJson.put("goods",detailArr);
        writeLog("主参数"+mainJson.toJSONString());

        return mainJson;
    }

    public JSONObject getRefundJSONObject(JSONObject mainJson,JSONArray detailArr){

        writeLog("明细参数" + detailArr.toJSONString());
        writeLog("主参数"+mainJson.toJSONString());
        mainJson.put("goods",detailArr);
        writeLog("主参数"+mainJson.toJSONString());

        return mainJson;
    }
}
