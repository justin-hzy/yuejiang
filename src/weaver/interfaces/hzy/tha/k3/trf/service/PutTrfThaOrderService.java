package weaver.interfaces.hzy.tha.k3.trf.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.common.service.CommonService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class PutTrfThaOrderService extends BaseBean {

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    private String putHkTrfUrl = getPropValue("k3_api_config","putHkTrfUrl");

    public void putTrf(String requestId, Map<String,String> mainData){
        CommonService commonService = new CommonService();

        JSONObject jsonObject = new JSONObject();

        /*�������*/
        String fhdc = mainData.get("out_dc");
        /*�ջ����*/
        String shdc = mainData.get("in_dc");
        /*��������*/
        String receiveDate = mainData.get("receive_date");
        /*���̱��*/
        String lcbh = mainData.get("lcbh");

        jsonObject.put("fstockoutorgid","ZT031");

        jsonObject.put("fbillNo",lcbh);
        jsonObject.put("fdate",receiveDate);
        jsonObject.put("fthirdsrcbillno",lcbh);

        String trfType = mainData.get("dbxzpd");

        JSONArray jsonArray = new JSONArray();
        RecordSet rs = new RecordSet();
        if("1".equals(trfType)){
            String dtSql = "select dt2.sku_no,dt2.actual_qty from formtable_main_356 main inner join formtable_main_356_dt2 dt2 on main.id = dt2.mainid where main.requestId = ?";
            rs.executeQuery(dtSql,requestId);
        }else if("2".equals(trfType) || "3".equals(trfType)){
            String dtSql = "select dt3.sku_no,dt3.actual_qty from formtable_main_356 main inner join formtable_main_356_dt3 dt3 on main.id = dt3.mainid where main.requestId = ?";
            rs.executeQuery(dtSql,requestId);
        }


        while (rs.next()){
            /*sku����*/
            String skuNo = rs.getString("sku_no");

            String actualQty = rs.getString("actual_qty");

            JSONObject dt1Json = new JSONObject();

            dt1Json.put("fmaterialid",skuNo);
            dt1Json.put("fqty",actualQty);
            dt1Json.put("fsrcstockid",fhdc);
            dt1Json.put("fdeststockid",shdc);

            jsonArray.add(dt1Json);
        }


        jsonObject.put("fentitylist",jsonArray);

        String param = jsonObject.toJSONString();

        String resStr = commonService.doK3Action(param,k3Ip,putHkTrfUrl);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            writeLog("ͬ������ɹ���ⵥ�ɹ�");
            updateIsNext(requestId,0);
        }else {
            writeLog("ͬ������ɹ���ⵥʧ��");
            updateIsNext(requestId,1);
        }
    }


    public void updateIsNext(String requestid,Integer isNext){
        String updateSql = "update formtable_main_356 set is_next = ? where requestId = ?";
        RecordSet updateRs = new RecordSet();
        updateRs.executeUpdate(updateSql,isNext,requestid);
    }

}
