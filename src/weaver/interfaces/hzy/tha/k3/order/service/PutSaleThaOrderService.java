package weaver.interfaces.hzy.tha.k3.order.service;

import com.alibaba.fastjson.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.common.service.CommonService;

public class PutSaleThaOrderService extends BaseBean {

    private String putHKSaleUrl = getPropValue("k3_api_config","putHKSaleUrl");

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    public void putSale(String requestid){
        CommonService commonService = new CommonService();
        String mainSql = "select lcbh,send_date,consignee_address,fcustomer_id,send_date,warehouse_id from formtable_main_347 where requestId = ?";

        RecordSet rsMain = new RecordSet();

        rsMain.executeQuery(mainSql,requestid);
        JSONObject jsonObject = new JSONObject();

        String processCode = "";
    }
}
