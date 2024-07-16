package weaver.interfaces.hzy.k3.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icbc.api.internal.apache.http.impl.cookie.S;
import com.wbi.util.Util;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.List;
import java.util.Map;

public class PurService extends BaseBean {

    private String meIp = getPropValue("fulun_api_config","meIp");

    private String putPurUrl = getPropValue("k3_api_config","putPurUrl");

    private String putSaleUrl = getPropValue("k3_api_config","putSaleUrl");


    public void tranTwPur_0(String lcbh, String gys, String rkrq, String rkck, String bb, List<Map<String,String>> detailDatas1, K3Service k3Service,String fentrytaxrate,String falldiscount){
        JSONObject jsonObject = new JSONObject();

        String fbillno = "TW_"+lcbh;
        jsonObject.put("fbillno",fbillno);
        jsonObject.put("fstockorgid","ZT026");
        jsonObject.put("fpurchaseorgid","ZT026");
        jsonObject.put("fsupplierId","ZT021");
        jsonObject.put("fdemandorgid","ZT026");
        jsonObject.put("fsettleorgid","ZT026");
        jsonObject.put("fthirdbillno",lcbh);

        jsonObject.put("fdate",rkrq);
        jsonObject.put("fhdc",rkck);
        jsonObject.put("fsettlecurrid",bb);
        jsonObject.put("falldiscount",falldiscount);
        jsonObject.put("fisincludedtax","true");



        JSONArray jsonArray = new JSONArray();


        for (Map<String, String> detailData : detailDatas1){
            JSONObject dtl = new JSONObject();


            dtl.put("fmaterialId",detailData.get("wlbm"));
            //暂时写死
            dtl.put("fentrytaxrate",fentrytaxrate);
            dtl.put("frealqty",detailData.get("rksl"));
            dtl.put("fstockid",rkck);


            dtl.put("ftaxprice",detailData.get("cgdj"));

            jsonArray.add(dtl);
        }

        jsonObject.put("fentrylist",jsonArray);

        String param = jsonObject.toJSONString();

        writeLog("json="+param);


        String resStr = k3Service.doK3Action(param,meIp,putPurUrl);
        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            k3Service.addLog(lcbh,"200");
            writeLog("同步金蝶采购入库单成功");
        }else {
            k3Service.addLog(lcbh,"500");
            writeLog("同步金蝶采购入库单失败");
        }
    }


    public void tranTwPur_1(String lcbh, String gys, String rkrq, String rkck, String bb, List<Map<String,String>> detailDatas1, K3Service k3Service,String fentrytaxrate,String falldiscount){
        JSONObject jsonObject = new JSONObject();

        String fbillno = "TW_"+lcbh;
        jsonObject.put("fbillno",fbillno);
        jsonObject.put("fstockorgid","ZT026");
        jsonObject.put("fpurchaseorgid","ZT026");
        jsonObject.put("fsupplierId","ZT021");
        jsonObject.put("fdemandorgid","ZT026");
        jsonObject.put("fsettleorgid","ZT026");
        jsonObject.put("fthirdbillno",lcbh);

        jsonObject.put("fdate",rkrq);
        jsonObject.put("fhdc",rkck);
        jsonObject.put("fsettlecurrid",bb);
        jsonObject.put("falldiscount",falldiscount);
        jsonObject.put("fisincludedtax","true");



        JSONArray jsonArray = new JSONArray();


        for (Map<String, String> detailData : detailDatas1){
            JSONObject dtl = new JSONObject();


            dtl.put("fmaterialId",detailData.get("wlbm"));
            //暂时写死
            dtl.put("fentrytaxrate",fentrytaxrate);
            dtl.put("frealqty",detailData.get("rksl"));
            dtl.put("fstockid",rkck);


            String ftaxprice = k3Service.queryPriceTable(detailData.get("wlbm"));

            dtl.put("ftaxprice",ftaxprice);

            jsonArray.add(dtl);
        }

        jsonObject.put("fentrylist",jsonArray);

        String param = jsonObject.toJSONString();

        writeLog("json="+param);


        String resStr = k3Service.doK3Action(param,meIp,putPurUrl);
        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            k3Service.addLog(lcbh,"200");
            writeLog("同步金蝶采购入库单成功");
        }else {
            k3Service.addLog(lcbh,"500");
            writeLog("同步金蝶采购入库单失败");
        }
    }

    public String tranHkPur_1(String lcbh, String gys, String rkrq, String rkck, String bb, List<Map<String,String>> detailDatas1, K3Service k3Service,String fentrytaxrate){

        JSONObject jsonObject = new JSONObject();
        String fbillno = "HK_"+lcbh;
        jsonObject.put("fbillno",fbillno);

        jsonObject.put("fbillno",fbillno);
        jsonObject.put("fstockorgid","ZT021");
        jsonObject.put("fpurchaseorgid","ZT021");
        jsonObject.put("fsupplierId",gys);
        jsonObject.put("fdemandorgid","ZT021");
        jsonObject.put("fsettleorgid","ZT021");
        jsonObject.put("fthirdbillno",lcbh);

        jsonObject.put("fdate",rkrq);
        jsonObject.put("fhdc",rkck);

        jsonObject.put("fsettlecurrid",bb);
        jsonObject.put("fisincludedtax","true");

        JSONArray jsonArray = new JSONArray();

        for (Map<String, String> detailData : detailDatas1){
            JSONObject dtl = new JSONObject();


            dtl.put("fmaterialId",detailData.get("wlbm"));
            //暂时写死
            dtl.put("fentrytaxrate",fentrytaxrate);
            writeLog("cgsl="+detailData.get("cgsl"));
            dtl.put("frealqty",detailData.get("cgsl"));
            dtl.put("fstockid",rkck);

            dtl.put("ftaxprice",detailData.get("cgdj"));

            jsonArray.add(dtl);
        }

        jsonObject.put("fentrylist",jsonArray);

        String param = jsonObject.toJSONString();

        writeLog("json="+param);

        String resStr = k3Service.doK3Action(param,meIp,putPurUrl);
        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            k3Service.addLog(lcbh,"200");
            writeLog("同步金蝶采购入库单成功");
            return code;
        }else {
            k3Service.addLog(lcbh,"500");
            writeLog("同步金蝶采购入库单失败");
            return code;
        }
    }



    public String tranHkSale_1(String lcbh, String gys, String yjjcr, String rkck, String bb, List<Map<String,String>> detailDatas1, K3Service k3Service,String fentrytaxrate){
        JSONObject jsonObject = new JSONObject();
        String fbillno = "HK_"+lcbh;
        jsonObject.put("fbillno",fbillno);
        jsonObject.put("fstockorgid","ZT021");
        jsonObject.put("fsaleorgid","ZT021");
        jsonObject.put("fcustomerid","CUST0558");
        jsonObject.put("fdsgbase","ZT026");
        jsonObject.put("fsettleorgid","ZT021");
        jsonObject.put("type","HK");
        jsonObject.put("fsettlecurrid",bb);
        jsonObject.put("fthirdbillno",lcbh);
        jsonObject.put("fdate",yjjcr);


        JSONArray jsonArray = new JSONArray();

        for (Map<String, String> detailData : detailDatas1){
            JSONObject dtl = new JSONObject();
            dtl.put("fentryid",0);
            dtl.put("fmaterialId",detailData.get("wlbm"));
            //暂时写死
            dtl.put("fentrytaxrate",fentrytaxrate);

            dtl.put("frealqty",detailData.get("cgsl"));

            dtl.put("fstockid",rkck);
            dtl.put("fsoorderno",lcbh);
            dtl.put("fdsgsrcoid",lcbh);

            String ftaxprice = k3Service.queryPriceTable(detailData.get("wlbm"));

            dtl.put("ftaxprice",ftaxprice);

            jsonArray.add(dtl);
        }

        jsonObject.put("fentitylist",jsonArray);
        String param = jsonObject.toJSONString();

        String resStr = k3Service.doK3Action(param,meIp,putSaleUrl);
        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            k3Service.addLog(lcbh,"200");
            writeLog("同步金蝶采购入库单成功");
        }else {
            k3Service.addLog(lcbh,"500");
            writeLog("同步金蝶采购入库单失败");
        }

        return code;
    }
}
