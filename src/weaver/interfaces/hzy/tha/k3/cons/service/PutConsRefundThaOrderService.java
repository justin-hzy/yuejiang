package weaver.interfaces.hzy.tha.k3.cons.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wbi.util.Util;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.common.service.CommonService;

public class PutConsRefundThaOrderService extends BaseBean {

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    private String putHkReSaleUrl = getPropValue("k3_api_config","putHkReSaleUrl");

    public void putRefund(String requestid){
        CommonService commonService = new CommonService();

        String mainSql = "select lcbh,fcustomer_id,receive_date,receive_warehouse from formtable_main_270 where requestId = ?";

        RecordSet rsMain = new RecordSet();

        rsMain.executeQuery(mainSql,requestid);
        JSONObject jsonObject = new JSONObject();

        String processCode = "";

        while (rsMain.next()){
            processCode = Util.null2String(rsMain.getString("lcbh"));
            String fRetCustId = Util.null2String(rsMain.getString("fcustomer_id"));
            String receiveDate = Util.null2String(rsMain.getString("receive_date"));
            String receiveWarehouse = Util.null2String(rsMain.getString("receive_warehouse"));


            processCode = "THA_"+processCode;

            jsonObject.put("fbillno",processCode);
            jsonObject.put("fstockorgid","ZT031");
            jsonObject.put("fsaleorgid","ZT031");
            jsonObject.put("fretcustid",fRetCustId);
            //jsonObject.put("fdsgbase","");
            jsonObject.put("fsettleorgid","ZT031");
            jsonObject.put("fsettlecurrid","PRE012");
            jsonObject.put("fthirdbillno",processCode);
            jsonObject.put("fdate",receiveDate);
            jsonObject.put("receiveWarehouse",receiveWarehouse);
        }

        String param = getDtl(requestid,jsonObject);

        String resStr = commonService.doK3Action(param,k3Ip,putHkReSaleUrl);

        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");
        if("200".equals(code)){
            writeLog("ͬ��������۳��ⵥ�ɹ�");
            updateIsNext(requestid,0);
        }else {
            writeLog("ͬ��������۳��ⵥʧ��");
            updateIsNext(requestid,1);
        }

    }

    public String getDtl(String requestid,JSONObject jsonObject){

        //���޸ģ��ֶλ�û��
        String dt1Sql = "select dt2.sku_no,dt2.qty,dt2.ftaxprice from formtable_main_270 as main " +
                "inner join formtable_main_270_dt2 as dt2 on main.id = dt2.mainid " +
                "where requestid = ? and dt2.qty > 0 and dt2.qty is not null";

        RecordSet rsDt1 = new RecordSet();

        rsDt1.executeQuery(dt1Sql,requestid);
        JSONArray jsonArray = new JSONArray();
        while (rsDt1.next()){
            String skuNo = Util.null2String(rsDt1.getString("sku_no"));
            String fRealQty = Util.null2String(rsDt1.getString("qty"));
            String fTaxPrice = Util.null2String(rsDt1.getString("ftaxprice"));

            JSONObject dt1Json = new JSONObject();

            dt1Json.put("fmaterialId",skuNo);

            //̩��˰��Ϊ0
            dt1Json.put("fentrytaxrate","7");

            //��ѯ��Ŀ��
            dt1Json.put("ftaxprice",fTaxPrice);

            dt1Json.put("frealqty",fRealQty);

            String receiveWarehouse = jsonObject.getString("receiveWarehouse");
            //������
            dt1Json.put("fstockid",receiveWarehouse);

            jsonArray.add(dt1Json);
        }
        //��ȥ�ջ����
        jsonObject.remove("receiveWarehouse");

        jsonObject.put("fentitylist",jsonArray);

        String param = jsonObject.toJSONString();

        return param;
    }

    public void updateIsNext(String requestid,Integer isNext){
        String updateSql = "update formtable_main_270 set is_next = ? where requestId = ?";
        RecordSet updateRs = new RecordSet();
        updateRs.executeUpdate(updateSql,isNext,requestid);
    }
}
