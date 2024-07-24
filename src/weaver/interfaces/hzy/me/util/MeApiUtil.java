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
    private String tableName = "uf_mejkpz"; //�ӿ����ñ�
    private RecordSet fieldRs = new RecordSet(); //�ֶ��������ݿ�����




    /**
     * ����ME�ӿ�
     *
     * @param apiId �ӿ�id
     * @param params �ӿ����
     * @return �ӿڷ�����
     */
    public JSONObject doAction(String apiId, String params){
        JSONObject apiRes = new JSONObject();

/*        Map<String,String> apiMap = getApiConfig(apiId);
        writeLog("��ʼ����apiIdΪ" + apiId + "�Ľӿڣ�" + apiMap.toString());

        CloseableHttpResponse response;// ��Ӧ��,
        CloseableHttpClient httpClient = HttpClients.createDefault();

        //restful�ӿ�url
        HttpPost httpPost = new HttpPost(bojunIp + apiMap.get("bjjk"));

        //��������ͷ
        httpPost.addHeader("Content-Type","application/json;charset=utf-8");
        httpPost.addHeader("Authorization", ticket);
        try {
            //���
//            String params = getParams(apiId, mainData, detailData);
            httpPost.setEntity(new StringEntity(params, "UTF-8"));
            response = httpClient.execute(httpPost);
            if (response != null && response.getEntity() != null) {
                //������Ϣ
                String resulString = EntityUtils.toString(response.getEntity());
                writeLog("��ȡ�ӿ����ݳɹ����ӿڷ����壺" + resulString);

                //��������Ϣ
                apiRes = JSON.parseObject(resulString);

            }else{
                apiRes.put("result","false");
                apiRes.put("message","�ӿڴ��󷵻ش���");
                writeLog("��ȡ�ӿ�����ʧ�ܣ��ӿڷ�����Ϊ�գ�");
            }
        } catch (Exception e) {
            apiRes.put("result","false");
            apiRes.put("message","�ӿ�����ʧ�ܣ�");
            writeLog("����ʧ��:"+e);
        }*/

        return apiRes;
    }



    /**
     * ����ME�ӿ�
     *
     * @param apiId �ӿ�id
     * @param mainData ������������
     * @param detailData  ������ϸ������
     * @return �ӿڷ�����
     */
    public JSONObject doAction(String apiId, Map<String,String> mainData, List<Map<String, String>> detailData){
        String params = getParams(apiId, mainData, detailData);
        JSONObject apiRes = doAction(apiId, params);
        return apiRes;
    }


    /*����ME�ӿ�*/
    public String doMeAction(String params,String apiId){
        CloseableHttpResponse response;// ��Ӧ��,
        CloseableHttpClient httpClient = HttpClients.createDefault();


        Map<String,String> apiMap = getApiConfig(apiId);

        HttpPost httpPost = new HttpPost(meIp+apiMap.get("mejk"));


        //��������ͷ
        httpPost.addHeader("Content-Type", "application/json;charset=utf-8");
        httpPost.setEntity(new StringEntity(params, "UTF-8"));
        try {
            response = httpClient.execute(httpPost);
            String resulString = EntityUtils.toString(response.getEntity());
            writeLog("��ȡ�ӿ����ݳɹ����ӿڷ����壺" + resulString);
            return resulString;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }




    /**
     * ���ݲ����ӿ����ñ�id��ȡ�ӿ���Ϣ
     *
     * @param apiId �ӿ�id
     * @return �ӿ���Ϣ
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
     * ��������
     *
     * @param requestId ����id
     * @return json��ʽ�Ĳ����ַ���
     */
    public List<String> getParams(String requestId, String apiId){
        writeLog("��ʼ����me�ӿ�-apiIdΪ" + apiId + "�Ľӿڲ���-------");
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
     * ��������
     *
     * @param apiId �ӿ����ñ�id
     * @param mainData ��������
     * @param detailData ��ϸ������
     * @return json��ʽ�Ĳ����ַ���
     */
    public String getParams(String apiId, Map<String,String> mainData, List<Map<String, String>> detailData){
        writeLog("��ʼ����apiIdΪ" + apiId + "�Ľӿڲ���-------");

        String params = "";
        JSONObject mainJson = new JSONObject(); //�������
        JSONArray detailArr = new JSONArray(); //��ϸ����
        List<Map<String,String>> dtFields = new ArrayList<>(); //����������ϸ�ֶ�

        //���ñ��л�ȡ�����ֶ�
        RecordSet rs = new RecordSet();
        rs.executeQuery("select zdlx,bjzdm,lczdm,zdsx from " + tableName + "_dt1 where mainid = ? ", apiId);
        while (rs.next()){

            String zdlx = Util.null2String(rs.getString("zdlx")); //�ֶ�����
            String bjzdm = Util.null2String(rs.getString("bjzdm")); //�����ֶ���
            String lczdm = Util.null2String(rs.getString("lczdm")); //�����ֶ���
            String zdsx = Util.null2String(rs.getString("zdsx")); //�ֶ�����

            if(zdlx.equals("0")){
                //��ȡ������
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
        writeLog("������" + mainJson.toJSONString());

        if(detailData != null && detailData.size()>0) {
            //��ȡ��ϸ����
            detailArr = getDtFieldVal(dtFields, detailData);
            mainJson.put("details", detailArr);
            writeLog("��ϸ����" + detailArr.toJSONString());
        }

        params = mainJson.toJSONString();

        writeLog("apiIdΪ" + apiId + "�Ľӿڲ���:" + params);
        return params;
    }

    /**
     * ��ȡ������
     *
     * @param lczdm �����ֶ���
     * @param zdsx �ֶ�����
     * @param mainData ��������
     * @return json��ʽ�Ĳ����ַ���
     */
    public String getMainFieldVal(String lczdm, String zdsx, Map<String,String> mainData){
        String fieldValue = mainData.get(lczdm);
        if(!"".equals(Util.null2String(zdsx))){
            writeLog(lczdm + "���ֶ����ԣ�" + zdsx );
            fieldValue = fieldProps(zdsx,fieldValue);
        }
        return fieldValue;
    }

    /**
     * ��ȡ��ϸ����
     *
     * @param dtFields ������ϸ�ֶμ�
     * @param detailData ��ϸ������
     * @return json��ʽ�Ĳ����ַ���
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
                    writeLog("��ϸ�ֶ�" + lczdm + "���ֶ����ԣ�" + zdsx );
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
     * �����ֶ����Ի�ȡ�����ֶ�ֵ
     *
     * @param zdsx �ֶ�����
     * @param fieldVal �ֶ�ֵ
     * @return ת������ֶ�ֵ
     */
    public String fieldProps(String zdsx, String fieldVal) {
        String fieldValue = fieldVal;
        writeLog("ת��ǰ��" + fieldValue );
        fieldRs.executeQuery(zdsx,fieldVal);
        if(fieldRs.next()){
            fieldValue = fieldRs.getString(1);
            writeLog("ת����" + fieldValue );
        }else {
            writeLog( "ת��ʧ�ܣ�");
        }
        return fieldValue;
    }

    /**
     * �ռ������ӿڴ�����Ϣ
     *
     * @param apiId �ӿ�id
     * @param requestId ��������id
     * @param errMessage �ӿڴ�����Ϣ
     */
    public void errlogMessage(String apiId, String requestId, String params, String errMessage){
        writeLog("�ռ�ME�ӿڴ�����Ϣ----------");

        ModeRightInfo ModeRightInfo = new ModeRightInfo();
        ModeRightInfo.setNewRight(true);
        ModeDataIdUpdate idUpdate = new ModeDataIdUpdate();

        //����:��ģ��   ģ��ID ���ݴ�����   ����������Ա  ��������YYYY-MM-DD  ����ʱ��HH:mm:ss
        int modeid = Integer.valueOf(getPropValue("bojun_api_config","errlogModeId"));
        int billid = idUpdate.getModeDataNewId("uf_bjjkcwrzb", modeid, 1, 1, new SimpleDateFormat("yyyy-MM-dd").format(new Date()), new SimpleDateFormat("HH:mm:ss").format(new Date()));

        RecordSet rs = new RecordSet();
        String sql = "update uf_bjjkcwrzb set jkmc=?,dylc=?,jkcs=?,cwrz=? where id=?";
        rs.executeUpdate(sql, apiId, requestId, params, errMessage, billid);

        ModeRightInfo.editModeDataShare(1, modeid, billid);//���ý�ģȨ��
    }

    public List<String> getPurInJson(String requestId){
        writeLog("---------��ʼ��װME�ӿڲɹ����ֵ�����-----------");
        List<String> params = new ArrayList<>();
        RecordSet rs = new RecordSet();
        String sql1 = "select main.lcbh,main.rkck,dt2.wlbm,dt2.rksl,dt2.rkrq from formtable_main_234 as main inner join formtable_main_234_dt2 dt2 on main.id = dt2.mainid where main.requestId = ?";
        writeLog(sql1+","+requestId);
        rs.executeQuery(sql1, requestId);
        JSONObject jsonObject = new JSONObject();
        JSONArray subOthers = new JSONArray();
        while (rs.next()){

            String wlbm = Util.null2String(rs.getString("wlbm")); //���ϱ���
            String rksl = Util.null2String(rs.getString("rksl")); //�������

            JSONObject subOther = new JSONObject();
            subOther.put("sku",wlbm);
            subOther.put("qty",rksl);

            subOthers.add(subOther);

            String rkck = Util.null2String(rs.getString("rkck")); //���ֿ�

            String rkrq = Util.null2String(rs.getString("rkrq")); //�������


            String lcbh = Util.null2String(rs.getString("lcbh")); //���̱��

            jsonObject.put("cstore",rkck);




            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String pushDate = today.format(formatter);

            rkrq = pushDate;

            rkrq= rkrq.replace("-","");
            jsonObject.put("billDate",rkrq);
            jsonObject.put("description","̨��ɹ���������-���-"+lcbh);
            jsonObject.put("requestId",requestId);
        }

        jsonObject.put("subOthers",subOthers);

        String param = jsonObject.toJSONString();

        writeLog("param="+param);

        params.add(param);


        return params;
    }

    public List<String> getRePurJson(String requestId){
        writeLog("---------��ʼ��װME�ӿڲɹ��˻����ⵥ����-----------");
        List<String> params = new ArrayList<>();
        RecordSet rs1 = new RecordSet();
        String sql1 = "select main.rkck,dt2.wlbm,dt2.cksl from formtable_main_233 as main inner join formtable_main_233_dt2 dt2 on main.id = dt2.mainid where main.requestId = ?";
        writeLog(sql1+","+requestId);
        rs1.executeQuery(sql1, requestId);

        while (rs1.next()){
            JSONObject jsonObject = new JSONObject();

            String rkck = Util.null2String(rs1.getString("rkck")); //����ֿ�
            String wlbm = Util.null2String(rs1.getString("wlbm")); //���ϱ���
            String cksl = Util.null2String(rs1.getString("cksl")); //��������

            jsonObject.put("cstore",rkck);
            jsonObject.put("sku",wlbm);
            jsonObject.put("qty",cksl);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String now = dateFormat.format(new Date());
            jsonObject.put("billDate",now);
            jsonObject.put("description","̨��ɹ���������-����");
            jsonObject.put("requestId",requestId);

            String param = jsonObject.toJSONString();

            writeLog("param="+param);

            params.add(param);
        }
        writeLog("params="+params.toString());
        return params;
    }

    public List<String> getSetFrJson(String requestId){
        writeLog("---------��ʼ��װME�ӿ�����-������ⵥ����-----------");
        List<String> params = new ArrayList<>();
        RecordSet rs1 = new RecordSet();

        String sql1 = "select main.lcbh,dt3.spbm,main.shdc1,dt3.sjrksl from formtable_main_242 as main inner join formtable_main_242_dt3 dt3 on main.id = dt3.mainid where dt3.sjrksl is not null and dt3.sjcksl is null and main.requestId = ?";
        writeLog(sql1+","+requestId);
        rs1.executeQuery(sql1, requestId);

        JSONObject jsonObject = new JSONObject();
        JSONArray subOthers = new JSONArray();

        while (rs1.next()){

            String shdc1 = Util.null2String(rs1.getString("shdc1")); //����ֿ�
            String spbm = Util.null2String(rs1.getString("spbm")); //���ϱ���
            String sjrksl = Util.null2String(rs1.getString("sjrksl")); //����ʵ���������

            String lcbh = Util.null2String(rs1.getString("lcbh")); //���̱��

            JSONObject subOther = new JSONObject();

            jsonObject.put("cstore",shdc1);

            subOther.put("sku",spbm);
            subOther.put("qty",sjrksl);
            subOthers.add(subOther);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String now = dateFormat.format(new Date());
            jsonObject.put("billDate",now);
            jsonObject.put("description","��װ����-���-"+lcbh);
            jsonObject.put("requestId",requestId);

        }

        jsonObject.put("subOthers",subOthers);

        String param = jsonObject.toJSONString();

        writeLog("param="+param);
        params.add(param);

        return params;
    }


    public List<String> getSetSonJson(String requestId){
        writeLog("---------��ʼ��װME�ӿ�����-������ⵥ����-----------");
        List<String> params = new ArrayList<>();
        RecordSet rs1 = new RecordSet();
        String sql1 = "select main.lcbh,dt1.spbm,main.fhdc1,dt1.sjztsl from formtable_main_242 as main inner join formtable_main_242_dt1 dt1 on main.id = dt1.mainid where main.requestId = ?";
        writeLog(sql1+","+requestId);
        rs1.executeQuery(sql1, requestId);

        JSONObject jsonObject = new JSONObject();

        JSONArray subOthers = new JSONArray();

        while (rs1.next()){


            String fhdc1 = Util.null2String(rs1.getString("fhdc1")); //����-���ֿ�
            String spbm = Util.null2String(rs1.getString("spbm")); //���ϱ���
            String sjztsl = Util.null2String(rs1.getString("sjztsl")); //����-��������
            String lcbh = Util.null2String(rs1.getString("lcbh")); //����-��������

            jsonObject.put("cstore",fhdc1);
            JSONObject subOther = new JSONObject();

            subOther.put("sku",spbm);
            subOther.put("qty",sjztsl);
            subOthers.add(subOther);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String now = dateFormat.format(new Date());
            jsonObject.put("billDate",now);
            jsonObject.put("description","��������-����-"+lcbh);
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
        writeLog("---------��ʼ��װME�ӿڲ�ж-������ⵥ����-----------");
        List<String> params = new ArrayList<>();
        RecordSet rs1 = new RecordSet();

        String sql1 = "select dt3.spbm,main.shdc1,dt3.sjrksl from formtable_main_242 as main inner join formtable_main_242_dt3 dt3 on main.id = dt3.mainid where dt3.sjrksl is not null and dt3.sjcksl is null and main.requestId = ?";

        writeLog(sql1+","+requestId);
        rs1.executeQuery(sql1, requestId);

        while (rs1.next()) {
            JSONObject jsonObject = new JSONObject();

            String shdc1 = Util.null2String(rs1.getString("shdc1")); //����-���ֿ�
            String spbm = Util.null2String(rs1.getString("spbm")); //���ϱ���
            String sjrksl = Util.null2String(rs1.getString("sjrksl")); //����-��������

            jsonObject.put("cstore",shdc1);
            jsonObject.put("sku",spbm);
            jsonObject.put("qty",sjrksl);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String now = dateFormat.format(new Date());
            jsonObject.put("billDate",now);
            jsonObject.put("description","��ж����-��ⵥ");
            jsonObject.put("requestId",requestId);

            String param = jsonObject.toJSONString();

            writeLog("param="+param);

            params.add(param);
        }
        writeLog("params="+params.toString());
        return params;
    }


    public List<String> getDismantleFrJon(String requestId){
        writeLog("---------��ʼ��װME�ӿڲ�ж-������ⵥ����-----------");
        List<String> params = new ArrayList<>();
        RecordSet rs1 = new RecordSet();

        String sql1 = "select main.lcbh,dt3.spbm,main.fhdc1,dt3.sjcksl from formtable_main_242 as main inner join formtable_main_242_dt3 dt3 on main.id = dt3.mainid where dt3.sjcksl is not null and dt3.sjrksl is null and main.requestId = ?";
        writeLog(sql1+","+requestId);
        rs1.executeQuery(sql1, requestId);

        JSONArray subOthers = new JSONArray();
        JSONObject jsonObject = new JSONObject();

        while (rs1.next()){



            String fhdc1 = Util.null2String(rs1.getString("fhdc1")); //����ֿ�
            String spbm = Util.null2String(rs1.getString("spbm")); //���ϱ���
            String sjcksl = Util.null2String(rs1.getString("sjcksl")); //����ʵ���������
            String lcbh = Util.null2String(rs1.getString("lcbh")); //���̱��


            jsonObject.put("cstore",fhdc1);

            JSONObject subOther = new JSONObject();
            subOther.put("sku",spbm);
            subOther.put("qty",sjcksl);
            subOthers.add(subOther);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String now = dateFormat.format(new Date());
            jsonObject.put("billDate",now);
            jsonObject.put("description","��ж����-����-"+lcbh);
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
        writeLog("---------��ʼ��װME�ӿ�ת��-������ⵥ����-----------");
        List<String> params = new ArrayList<>();
        RecordSet rs1 = new RecordSet();

        String sql1 = "select dt2.hpbh,dt2.ck,dt2.cksl from formtable_main_244 as main inner join formtable_main_244_dt2 as dt2 on main.id = dt2.mainid where dt2.cksl is not null and dt2.rksl is null and main.requestid = ?";

        writeLog(sql1+","+requestId);
        rs1.executeQuery(sql1, requestId);

        while (rs1.next()){
            JSONObject jsonObject = new JSONObject();

            String ck = Util.null2String(rs1.getString("ck")); //����ֿ�
            String hpbh = Util.null2String(rs1.getString("hpbh")); //���ϱ���
            String cksl = Util.null2String(rs1.getString("cksl")); //����ʵ���������

            jsonObject.put("cstore",ck);
            jsonObject.put("sku",hpbh);
            jsonObject.put("qty",cksl);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String now = dateFormat.format(new Date());
            jsonObject.put("billDate",now);
            jsonObject.put("description","ת������-����");
            jsonObject.put("requestId",requestId);

            String param = jsonObject.toJSONString();

            writeLog("param="+param);

            params.add(param);
        }
        writeLog("params="+params.toString());
        return params;
    }

    public List<String> getTransCodeSonJson(String requestId){
        writeLog("---------��ʼ��װME�ӿ�ת��-������ⵥ����-----------");
        List<String> params = new ArrayList<>();
        RecordSet rs1 = new RecordSet();



        String sql1 = "select dt2.hpbh,dt2.ck,dt2.rksl from formtable_main_244 as main inner join formtable_main_244_dt2 as dt2 on main.id = dt2.mainid where dt2.rksl is not null and dt2.cksl is null and main.requestid = ?";

        writeLog(sql1+","+requestId);
        rs1.executeQuery(sql1, requestId);

        while (rs1.next()){
            JSONObject jsonObject = new JSONObject();

            String ck = Util.null2String(rs1.getString("ck")); //���ֿ�
            String hpbh = Util.null2String(rs1.getString("hpbh")); //���ϱ���
            String rksl = Util.null2String(rs1.getString("rksl")); //����ʵ���������

            jsonObject.put("cstore",ck);
            jsonObject.put("sku",hpbh);
            jsonObject.put("qty",rksl);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String now = dateFormat.format(new Date());
            jsonObject.put("billDate",now);
            jsonObject.put("description","ת������-���");
            jsonObject.put("requestId",requestId);

            String param = jsonObject.toJSONString();

            writeLog("param="+param);

            params.add(param);
        }
        writeLog("params="+params.toString());
        return params;
    }
}
