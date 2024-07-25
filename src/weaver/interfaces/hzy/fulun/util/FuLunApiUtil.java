package weaver.interfaces.hzy.fulun.util;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.engine.mobilemode.biz.json.MJSONArray;
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

import java.util.*;

public class FuLunApiUtil extends BaseBean {


    private String meIp = getPropValue("fulun_api_config","meIp");

    private String returnOrderMain = getPropValue("fulun_api_config","returnOrderMain");

    private String returnOrderMainDetail = getPropValue("fulun_api_config","returnOrderMainDetail");

    private String newReturnOrderMain = getPropValue("fulun_api_config","newReturnOrderMain");

    private String newReturnOrderMainDetail = getPropValue("fulun_api_config","newReturnOrderMainDetail");

    private String orderCallBackUrl = getPropValue("fulun_api_config","orderCallBackUrl");

    private String transferOrderCallBackUrl = getPropValue("fulun_api_config","transferOrderCallBackUrl");

    private String reConsOrderCallBackUrl = getPropValue("fulun_api_config","reConsOrderCallBackUrl");

    private String consOrderCallBackUrl = getPropValue("fulun_api_config","consOrderCallBackUrl");

    private String purchaseInCallBackUrl = getPropValue("fulun_api_config","purchaseInCallBackUrl");

    private String rePurchaseCallBackUrl = getPropValue("fulun_api_config","rePurchaseCallBackUrl");

    private String getTransFrCBUrl = getPropValue("fulun_api_config","getTransFrCBUrl");

    private String getTransSonCBUrl = getPropValue("fulun_api_config","getTransSonCBUrl");

    private String getSetDismantleCBUrl = getPropValue("fulun_api_config","getSetDismantleCBUrl");



    private String tableName = "uf_fljkpz"; //�ӿ����ñ�

    private RecordSet fieldRs = new RecordSet(); //�ֶ��������ݿ�����


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

        writeLog("��ʼ����apiIdΪ" + apiId + "�Ľӿڲ���-------");

        String params = "";
        JSONObject mainJson = new JSONObject(); //�������
        JSONArray detailArr = new JSONArray(); //��ϸ����

        JSONArray detailArr1 = new JSONArray(); //������ϸ����

        List<Map<String,String>> dtFields = new ArrayList<>(); //����������ϸ�ֶ�
        //���ñ��л�ȡ�����ֶ�
        RecordSet rs = new RecordSet();
        rs.executeQuery("select zdlx,fzdm,lczdm,zdzx from " + tableName + "_dt1 where mainid = ? ", apiId);

        while (rs.next()){

            String zdlx = Util.null2String(rs.getString("zdlx")); //�ֶ�����
            String fzdm = Util.null2String(rs.getString("fzdm")); //�����ֶ���
            String lczdm = Util.null2String(rs.getString("lczdm")); //�����ֶ���
            String zdzx = Util.null2String(rs.getString("zdzx")); //�ֶ�����

            if(zdlx.equals("0")){
                //��ȡ������
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

        writeLog("������" + mainJson.toJSONString());

        if(detailData != null && detailData.size()>0) {
            //��ȡ��ϸ����
            detailArr = getDtFieldVal(dtFields, detailData);
            if("1".equals(apiId)){
                mainJson = getOrderJSONObject(mainJson,detailArr,detailArr1);
            }else if("2".equals(apiId)){
                mainJson = getReturnOrderJSONObject(mainJson,detailArr,detailArr1,mainData);
            }else if("3".equals(apiId)){
                mainJson = getTransferOrderJSONObject(mainData,mainJson,detailArr);
            }else if("4".equals(apiId)){
                mainJson = getConsOrderJSONObject(mainJson,detailArr);
            }else if("5".equals(apiId)){
                mainJson = getReConsOrderJSONObject(mainJson,detailArr);
            }else if("6".equals(apiId)){
                mainJson = getPurchaseInJSONObject(mainJson,detailArr);
            }else if("7".equals(apiId)){
                mainJson = getRePurchaseJSONObject(mainJson,detailArr);
            }else if("12".equals(apiId)){
                mainJson = getSetJson(mainData,detailData);
            }else if("13".equals(apiId)){
                mainJson = getDismantleJson(mainData,detailData);
            }else if("17".equals(apiId)){
                mainJson = getNewReturnOrderJSONObject(mainJson,detailArr,detailArr1,mainData);
            }else if("14".equals(apiId)){
                mainJson = getTranscodeJson(mainData,detailData);
            }/*else if("15".equals(apiId)){
                mainJson = getTranscodeJson(mainData);
            }*/
            /*else if("16".equals(apiId)){

                writeLog("mainJson="+mainJson.toJSONString());
                if(detailData != null && detailData.size()>0) {
                    //��ȡ��ϸ����
                    detailArr = getDtFieldVal(dtFields, detailData);
                    mainJson.put("products", detailArr);
                    writeLog("��ϸ����" + detailArr.toJSONString());
                }
            }*/
        }

        params = mainJson.toJSONString();

        writeLog("���ز���apiIdΪ" + apiId + "�Ľӿڲ���:" + params);

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
                String fzdm = fieldMap.get("fzdm");
                String lczdm = fieldMap.get("lczdm");
                String zdzx = fieldMap.get("zdzx");
                String fieldValue = dtDataMap.get(lczdm);
                if(!"".equals(Util.null2String(zdzx))){
                    writeLog("��ϸ�ֶ�" + lczdm + "���ֶ����ԣ�" + zdzx );
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

    /**
     * �ռ������м�������Ϣ
     *
     * @param apiId �ӿ�id
     * @param requestId ��������id
     * @param errMessage �ӿڴ�����Ϣ
     */
    public void errlogMessage(String apiId, String requestId, String params, String errMessage){
        writeLog("�ռ����ؽӿڴ�����Ϣ----------");

        String id = UUID.randomUUID().toString().replace("-","");


        RecordSet rs = new RecordSet();
        String sql = "insert into uf_fl_mq_error_log (id,requestId,apiid,params,errMessage) values ("
                +id+","+requestId+","+apiId+","+params+","+errMessage+")";
        rs.executeUpdate(sql);
    }


    /*
    * ��ȡ�����ӿڲ���
    * */
    public JSONObject getOrderJSONObject(JSONObject mainJson,JSONArray detailArr,JSONArray detailArr1){

        String delivery_date = detailArr.getJSONObject(0).getString("delivery_date");
        String shipping_type = detailArr.getJSONObject(0).getString("shipping_type");
        mainJson.put("delivery_date",delivery_date);
        mainJson.put("shipping_type",shipping_type);
        mainJson.put("callback_url",meIp+orderCallBackUrl);

        //��װͨ·
        RecordSet rs = new RecordSet();
        rs.executeQuery("select kh.khmcst from formtable_main_151 main inner join uf_kh kh on main.kh = khbh where fddh = ?",mainJson.get("name"));
        while (rs.next()){
            String khmcst = rs.getString("khmcst");
            writeLog("khmcst",khmcst);
            mainJson.put("channel",khmcst);
        }

        for(int i=0;i<detailArr.size();i++){
            JSONObject detail = new JSONObject();
            detail.put("sku",detailArr.getJSONObject(i).getString("sku"));
            detail.put("quantity",detailArr.getJSONObject(i).getString("quantity"));
            detail.put("batch",detailArr.getJSONObject(i).getString("batch"));
            detail.put("storage_type",mainJson.getString("storage_type"));
            detail.put("expiration_deadline",detailArr.getJSONObject(i).getString("expiration_deadline"));
            detailArr1.add(detail);
        }

        mainJson.remove("storage_type");

        mainJson.put("products", detailArr1);
        //mainJson.put("note","���۷�������(̨��)-����");

        String note = mainJson.getString("note");
        if(note != null){
            note = note + "-���۷�������(̨��)-����";
            mainJson.put("note",note);
        }else {
            mainJson.put("note","���۷�������(̨��)-����");
        }


        writeLog("��ϸ����" + detailArr1.toJSONString());
        return mainJson;
    }

    public JSONObject getReturnOrderJSONObject(JSONObject mainJson,JSONArray detailArr,JSONArray detailArr1,Map<String,String> mainData){

        RecordSet rs = new RecordSet();
        JSONArray jsonArray = new JSONArray();
        writeLog("requestid="+mainData.get("requestid"));
        rs.executeQuery("select dt1.id,dt1.ph,dt1.ddl from "+returnOrderMain+" main,"+returnOrderMainDetail+" dt1 where main.id=dt1.mainid and main.requestid = ?", mainData.get("requestid"));

        while (rs.next()){

            String id = Util.null2String(rs.getString("id")); //	ʶ��ID
            String ph = Util.null2String(rs.getString("ph")); //	��Ʒsku
            String ddl = Util.null2String(rs.getString("ddl")); // ��Ʒ����
            // �ֱ�
            //String cb = Util.null2String(rs.getString("cb"));

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("identifier",id);
            jsonObject.put("sku",ph);
            jsonObject.put("quantity",ddl);
            jsonObject.put("storage_type",mainJson.get("storage_type"));
            jsonArray.add(jsonObject);
        }

        mainJson.remove("storage_type");
        mainJson.put("products", jsonArray);
        mainJson.put("note","���۷�������(̨��)-���");
        writeLog("��ϸ����" + jsonArray.toJSONString());

        return mainJson;
    }

    //�˻�2.0
    public JSONObject getNewReturnOrderJSONObject(JSONObject mainJson,JSONArray detailArr,JSONArray detailArr1,Map<String,String> mainData){

        RecordSet rs = new RecordSet();
        JSONArray jsonArray = new JSONArray();
        writeLog("requestid="+mainData.get("requestid"));

        rs.executeQuery("select dt1.id,dt1.hptxm,dt1.ddl,dt1.bz from "+newReturnOrderMain+" main,"+newReturnOrderMainDetail+" dt1 where main.id=dt1.mainid and main.requestid = ?", mainData.get("requestid"));

        String bz = "";

        while (rs.next()){

            String id = Util.null2String(rs.getString("id")); //	ʶ��ID
            String hptxm = Util.null2String(rs.getString("hptxm")); //	��Ʒsku
            String ddl = Util.null2String(rs.getString("ddl")); // ��Ʒ����
            bz = Util.null2String(rs.getString("bz")); // ��Ʒ����
            // �ֱ�
            //String cb = Util.null2String(rs.getString("cb"));

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("identifier",id);
            jsonObject.put("sku",hptxm);
            jsonObject.put("quantity",ddl);
            jsonObject.put("storage_type",mainJson.get("storage_type"));
            jsonArray.add(jsonObject);
        }

        mainJson.remove("storage_type");
        mainJson.put("products", jsonArray);
        if (bz.length()>0){
            mainJson.put("note","���۷�������(̨��)-���-"+bz);
        }else {
            mainJson.put("note","���۷�������(̨��)-���");
        }

        writeLog("��ϸ����" + jsonArray.toJSONString());

        return mainJson;
    }


    //���ڵ���
    public JSONObject getTransferOrderJSONObject(Map<String,String> mainData,JSONObject mainJson,JSONArray detailArr){


        String storageType = mainJson.getString("storage_type");

        String modifiedStorageType = mainJson.getString("modified_storage_type");

        for(int i = 0;i<detailArr.size();i++){
            JSONObject detailJson = detailArr.getJSONObject(i);
            detailJson.put("storage_type",storageType);
            detailJson.put("modified_storage_type",modifiedStorageType);
        }
        mainJson.remove("storage_type");
        mainJson.remove("modified_storage_type");

        mainJson.put("callback_url",meIp +transferOrderCallBackUrl);
        mainJson.put("items",detailArr);
        mainJson.put("note","��������(̨��)-���ڵ���");


        writeLog("��ϸ����" + detailArr.toJSONString());
        return mainJson;
    }

    public JSONObject getConsOrderJSONObject(JSONObject mainJson,JSONArray detailArr){

        String shipping_type = detailArr.getJSONObject(0).getString("shipping_type");

        mainJson.put("shipping_type",shipping_type);

        mainJson.put("callback_url",meIp+consOrderCallBackUrl);

        for(int i=0;i<detailArr.size();i++){
            JSONObject detail = detailArr.getJSONObject(i);
            detail.put("storage_type",mainJson.getString("storage_type"));
            detail.remove("shipping_type");
        }
        mainJson.remove("storage_type");
        mainJson.put("note","��������(̨��)-����-����");
        mainJson.put("products", detailArr);
        writeLog("��ϸ����" + detailArr.toJSONString());
        return mainJson;
    }

    public JSONObject getReConsOrderJSONObject(JSONObject mainJson,JSONArray detailArr){

        String storage_type = mainJson.getString("storage_type");

        for (int i =0;i<detailArr.size();i++){
            JSONObject detail = detailArr.getJSONObject(i);
            detail.put("storage_type",storage_type);
        }

        mainJson.put("callback_url",meIp+reConsOrderCallBackUrl);
        mainJson.put("items", detailArr);
        mainJson.put("note","��������(̨��)-����-���");
        mainJson.remove("storage_type");
        writeLog("��ϸ����" + detailArr.toJSONString());

        return mainJson;
    }

    public JSONObject getPurchaseInJSONObject(JSONObject mainJson,JSONArray detailArr){

        String storage_type = mainJson.getString("storage_type");

        for (int i =0;i<detailArr.size();i++){
            JSONObject detail = detailArr.getJSONObject(i);
            detail.put("storage_type",storage_type);
        }

        mainJson.put("callback_url",meIp+purchaseInCallBackUrl);
        mainJson.put("items",detailArr);
        mainJson.put("note","̨��ɹ���������(̨��)-���");
        mainJson.remove("storage_type");
        writeLog("��ϸ����" + detailArr.toJSONString());

        return mainJson;
    }


    public JSONObject getRePurchaseJSONObject(JSONObject mainJson,JSONArray detailArr){
        String storage_type = mainJson.getString("storage_type");

        for (int i =0;i<detailArr.size();i++){
            JSONObject detail = detailArr.getJSONObject(i);
            detail.put("storage_type",storage_type);
        }

        mainJson.put("callback_url",meIp+rePurchaseCallBackUrl);
        mainJson.put("products", detailArr);
        mainJson.put("note","̨��ɹ��˻�����(̨��)-����");
        mainJson.remove("storage_type");
        writeLog("��ϸ����" + detailArr.toJSONString());

        return mainJson;
    }

    public JSONObject getSetJson(Map<String,String> mainData,List<Map<String,String>> detailDatas){

        //�������
        String name = mainData.get("lcbh");

        //��������
        String shippingType = "hct";

        //�ش�����
        String callbackUrl = meIp+getSetDismantleCBUrl;
        //�ջ����
        String fhdc1 = mainData.get("fhdc1");

        String shdc1 = mainData.get("shdc1");

        String requestid = mainData.get("requestid");

        JSONObject mainJson = new JSONObject();

        mainJson.put("name",name);
        mainJson.put("callback_url",callbackUrl);
        mainJson.put("note","��������(̨��)-����");

        JSONArray detailArr1 = new JSONArray();

        for (Map<String,String> detailData :detailDatas){
            //��Ʒ����
            String sku = detailData.get("spbm");
            //�����-����
            String jhztsl = detailData.get("jhztsl");


            JSONObject detail = new JSONObject();
            detail.put("sku",sku);
            detail.put("quantity",jhztsl);
            detail.put("storage_type",fhdc1);

            String xq = detailData.get("xq");
            if(StrUtil.isNotEmpty(xq)){
                detail.put("expiration_deadline",xq);
            }
            String pch = detailData.get("pch");
            if (StrUtil.isNotEmpty(pch)){
                detail.put("batch",pch);
            }
            detailArr1.add(detail);
        }

        mainJson.put("source_items",detailArr1);


        RecordSet rs = new RecordSet();

        rs.executeQuery("select dt2.fxwlbm,dt2.jhztfxsl from formtable_main_242 as main inner join formtable_main_242_dt2 as dt2 on main.id  = dt2.mainid where main.requestid = ?", requestid);



        JSONArray detailArr2 = new MJSONArray();

        while (rs.next()){
            JSONObject detailJson = new JSONObject();
            String fxwlbm = rs.getString("fxwlbm");
            String jhztfxsl = rs.getString("jhztfxsl");

            detailJson.put("sku",fxwlbm);
            detailJson.put("quantity",jhztfxsl);
            detailJson.put("storage_type",shdc1);
            detailArr2.add(detailJson);
        }
        mainJson.put("target_items",detailArr2);


        return mainJson;
    }


    public JSONObject getDismantleJson(Map<String,String> mainData,List<Map<String, String>> detailDatas){

        String requestid = mainData.get("requestid");

        String name = mainData.get("lcbh");

        //�ش�����
        String callbackUrl = meIp+getSetDismantleCBUrl;

        //��������
        String shippingType = "hct";

        String storage_type = mainData.get("fhdc1");

        JSONObject mainJson = new JSONObject();

        mainJson.put("name",name);
        //mainJson.put("shipping_type",shippingType);
        mainJson.put("callback_url",callbackUrl);
        mainJson.put("note","��ж����(̨��)-����");

        RecordSet rs = new RecordSet();
        rs.executeQuery("select dt2.fxwlbm,dt2.jhztfxsl from formtable_main_242 as main inner join formtable_main_242_dt2 as dt2 on main.id  = dt2.mainid where main.requestid = ?", requestid);

        JSONArray detailArr = new MJSONArray();

        while (rs.next()){
            JSONObject detailJson = new JSONObject();
            String fxwlbm = rs.getString("fxwlbm");
            String jhztfxsl = rs.getString("jhztfxsl");

            detailJson.put("sku",fxwlbm);
            detailJson.put("quantity",jhztfxsl);
            detailJson.put("storage_type",storage_type);

            //����û��Ч�ڸ�����
            detailArr.add(detailJson);
        }
        mainJson.put("source_items",detailArr);

        JSONArray detailArr1 = new JSONArray();

        for (Map<String,String> detailData :detailDatas){
            //��Ʒ����
            String sku = detailData.get("spbm");
            //�����-����
            String jhztsl = detailData.get("jhztsl");


            JSONObject detail = new JSONObject();
            detail.put("sku",sku);
            detail.put("quantity",jhztsl);
            detail.put("storage_type",storage_type);

            String xq = detailData.get("xq");
            if(StrUtil.isNotEmpty(xq)){
                detail.put("expiration_deadline",xq);
            }
            String pch = detailData.get("pch");
            if (StrUtil.isNotEmpty(pch)){
                detail.put("batch",pch);
            }
            detailArr1.add(detail);
        }

        mainJson.put("target_items",detailArr1);

        return mainJson;
    }


    public JSONObject getTranscodeJson(Map<String,String> mainData,List<Map<String,String>> detailDatas){

        String requestid = mainData.get("requestid");

        String name = mainData.get("lcbh");

        //�ش�����
        String callbackUrl = meIp+getTransFrCBUrl;

        JSONObject mainJson = new JSONObject();

        mainJson.put("name",name);
        mainJson.put("callback_url",callbackUrl);
        mainJson.put("note","ת����������(̨��)-����");


        RecordSet rs = new RecordSet();
        rs.executeQuery("select dt1.hpbmtw,dt1.sl,dt1.dqcw1,dt1.bz from formtable_main_244 as main inner join formtable_main_244_dt1 as dt1 on main.id = dt1.mainid where main.requestid = ?",requestid);

        JSONArray detailArr = new MJSONArray();

        while (rs.next()){

            JSONObject detailJson = new JSONObject();
            String hpbh = rs.getString("hpbmtw");
            String sl = rs.getString("sl");
            String dqcw1 = rs.getString("dqcw1");
            String bz = rs.getString("bz");


            detailJson.put("sku",hpbh);
            detailJson.put("quantity",sl);
            detailJson.put("storage_type",dqcw1);
            detailJson.put("note",bz);

            //����û��Ч�ڸ�����
            detailArr.add(detailJson);
        }
        mainJson.put("products",detailArr);
        return mainJson;
    }
}
