package weaver.interfaces.hzy.tha.k3.refund.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wbi.util.Util;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.common.service.CommonService;

public class PutSaleThaRefundService extends BaseBean {

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    private String putHkReSaleUrl = getPropValue("k3_api_config","putHkReSaleUrl");

    public void putRefund(String requestid){
        CommonService commonService = new CommonService();

        //������
        String mainSql = "select lcbh,fretcust_id,entry_date,shdc from formtable_main_348 where requestId = ?";

        RecordSet rsMain = new RecordSet();

        rsMain.executeQuery(mainSql,requestid);
        JSONObject jsonObject = new JSONObject();

        String processCode = "";

        while (rsMain.next()){
            processCode = Util.null2String(rsMain.getString("lcbh"));
            String fRetCustId = Util.null2String(rsMain.getString("fretcust_id"));
            String entryDate = Util.null2String(rsMain.getString("entry_date"));
            String shdc = Util.null2String(rsMain.getString("shdc"));

            jsonObject.put("fbillno","THA_"+processCode);
            jsonObject.put("fstockorgid","ZT031");
            jsonObject.put("fsaleorgid","ZT031");
            jsonObject.put("fretcustid",fRetCustId);
            //jsonObject.put("fdsgbase","");
            jsonObject.put("fsettleorgid","ZT031");
            jsonObject.put("fsettlecurrid","PRE012");
            jsonObject.put("fthirdbillno",processCode);
            jsonObject.put("fdate",entryDate);
            jsonObject.put("shdc",shdc);
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
        String dt1Sql = "select dt1.sku_no,dt1.actual_qty,dt1.price from formtable_main_348 as main " +
                "inner join formtable_main_348_dt1 as dt1 on main.id = dt1.mainid " +
                "where requestid = ? and dt1.actual_qty > 0 and dt1.actual_qty is not null";

        RecordSet rsDt1 = new RecordSet();

        rsDt1.executeQuery(dt1Sql,requestid);
        JSONArray jsonArray = new JSONArray();
        while (rsDt1.next()){
            String skuNo = Util.null2String(rsDt1.getString("sku_no"));
            String fRealQty = Util.null2String(rsDt1.getString("actual_qty"));
            String fTaxPrice = Util.null2String(rsDt1.getString("price"));

            JSONObject dt1Json = new JSONObject();

            dt1Json.put("fmaterialId",skuNo);

            //̩��˰��Ϊ0
            dt1Json.put("fentrytaxrate","7");

            //��ѯ��Ŀ��
            dt1Json.put("ftaxprice",fTaxPrice);

            dt1Json.put("frealqty",fRealQty);

            String shdc = jsonObject.getString("shdc");
            //������
            dt1Json.put("fstockid",shdc);

            jsonArray.add(dt1Json);
        }
        //��ȥ�ջ����
        jsonObject.remove("shdc");

        jsonObject.put("fentitylist",jsonArray);

        String param = jsonObject.toJSONString();

        return param;
    }

    public void updateIsNext(String requestid,Integer isNext){
        String updateSql = "update formtable_main_348 set is_next = ? where requestId = ?";
        RecordSet updateRs = new RecordSet();
        updateRs.executeUpdate(updateSql,isNext,requestid);
    }



}
