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

        writeLog("dtFields=" + dtFields.toString());

        if(detailData != null && detailData.size()>0) {
            //��ȡ��ϸ����
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
                writeLog("fzdm="+fzdm);
                writeLog("lczdm="+lczdm);
                writeLog("zdzx="+zdzx);
                writeLog("fieldValue="+fieldValue);
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

    public JSONObject getOrderJSONObject(JSONObject mainJson,JSONArray detailArr){

        writeLog("��ϸ����" + detailArr.toJSONString());
        writeLog("������"+mainJson.toJSONString());
        mainJson.put("goods",detailArr);
        writeLog("������"+mainJson.toJSONString());

        return mainJson;
    }

    public JSONObject getRefundJSONObject(JSONObject mainJson,JSONArray detailArr){

        writeLog("��ϸ����" + detailArr.toJSONString());
        writeLog("������"+mainJson.toJSONString());
        mainJson.put("goods",detailArr);
        writeLog("������"+mainJson.toJSONString());

        return mainJson;
    }
}
