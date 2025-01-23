package weaver.interfaces.hzy.vn.k3.trf.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.common.service.CommonService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class PutTrfVnOrderService extends BaseBean {

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    private String putHkTrfUrl = getPropValue("k3_api_config","putHkTrfUrl");

    public void putTrf(String requestId, Map<String,String> mainData){

        CommonService commonService = new CommonService();

        JSONObject jsonObject = new JSONObject();

        /*发货店仓*/
        String fhdc = mainData.get("fhdc");
        /*收货店仓*/
        String shdc = mainData.get("shdc");
        /*发货日期*/
        String receiveDate = mainData.get("receive_date");
        /*流程编号*/
        String lcbh = mainData.get("lcbh");

        jsonObject.put("fstockoutorgid","ZT029");

        jsonObject.put("fbillNo",lcbh);
        jsonObject.put("fdate",receiveDate);
        jsonObject.put("fthirdsrcbillno",lcbh);

        String dtSql = "select dt3.sku_no,dt3.actual_qty from formtable_main_359 main inner join formtable_main_359_dt3 dt3 on main.id = dt3.mainid where main.requestId = ?";

        RecordSet rs = new RecordSet();

        rs.executeQuery(dtSql,requestId);

        JSONArray jsonArray = new JSONArray();


        while (rs.next()){
            /*sku条码*/
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
            addLog(lcbh,"200");
            writeLog("同步金蝶采购入库单成功");
            commonService.updateIsNext(requestId,0);
        }else {
            addLog(lcbh,"500");
            writeLog("同步金蝶采购入库单失败");
            commonService.updateIsNext(requestId,1);
        }
    }


    public void updateIsNext(String requestid,Integer isNext){
        String updateSql = "update formtable_main_359 set is_next = ? where requestId = ?";
        RecordSet updateRs = new RecordSet();
        updateRs.executeUpdate(updateSql,isNext,requestid);
    }

    public void addLog(String lcbh,String status){
        String insertSql = "insert into k3_tran_log (lcbh,status,createTime) values (?,?,?)";
        RecordSet insertRs = new RecordSet();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String todayString = today.format(formatter);

        todayString = "'" + todayString + "'";
        insertRs.executeUpdate(insertSql,lcbh,status,todayString);
    }
}
