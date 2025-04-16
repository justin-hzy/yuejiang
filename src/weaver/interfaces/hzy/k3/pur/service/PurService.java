package weaver.interfaces.hzy.k3.pur.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.log.LogService;
import weaver.interfaces.hzy.k3.price.service.PriceService;
import weaver.interfaces.hzy.k3.service.K3Service;

import java.util.List;
import java.util.Map;

public class PurService extends BaseBean {

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");



    private String putHKPurUrl = getPropValue("k3_api_config","putHKPurUrl");

    private String putHKSaleUrl = getPropValue("k3_api_config","putHKSaleUrl");

    private String putTWPurUrl = getPropValue("k3_api_config","putTWPurUrl");


    public void tranTwPur_0(String lcbh, String gys, String rkrq, String rkck, String bb, List<Map<String,String>> detailDatas1, K3Service k3Service, String fentrytaxrate, String falldiscount){
        JSONObject jsonObject = new JSONObject();

        String fbillno = "TW_"+lcbh;
        jsonObject.put("fbillno",fbillno);
        jsonObject.put("fstockorgid","ZT026");
        jsonObject.put("fpurchaseorgid","ZT026");
        jsonObject.put("fsupplierId",gys);
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


        String resStr = k3Service.doK3Action(param,k3Ip,putTWPurUrl);
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
        writeLog("jsonObject="+jsonObject.toJSONString());
        //传进口税
        if("0.00".equals(falldiscount)){
            jsonObject.put("falldiscount",falldiscount);
        }else {
            jsonObject.put("falldiscount","-"+falldiscount);
        }
        writeLog("jsonObject="+jsonObject.toJSONString());
        jsonObject.put("fisincludedtax","true");



        JSONArray jsonArray = new JSONArray();


        for (Map<String, String> detailData : detailDatas1){
            JSONObject dtl = new JSONObject();

            String rksl = detailData.get("rksl");
            writeLog("rksl="+rksl);
            if(StrUtil.isNotEmpty(rksl)){
                writeLog("rksl="+rksl);
                dtl.put("fmaterialId",detailData.get("wlbm"));
                writeLog("fmaterialId="+detailData.get("wlbm"));
                //暂时写死
                dtl.put("fentrytaxrate",fentrytaxrate);
                writeLog("fentrytaxrate="+fentrytaxrate);
                dtl.put("frealqty",rksl);
                writeLog("frealqty="+rksl);
                dtl.put("fstockid",rkck);
                writeLog("fstockid="+rkck);

                //String ftaxprice = k3Service.queryPriceTable(detailData.get("wlbm"));
                String ftaxprice = k3Service.getPrice(detailData.get("wlbm"));

                writeLog("ftaxprice="+ftaxprice);

                //价目表
                dtl.put("ftaxprice",ftaxprice);

                writeLog("dtl="+dtl.toJSONString());

                jsonArray.add(dtl);
            }
        }

        jsonObject.put("fentrylist",jsonArray);

        String param = jsonObject.toJSONString();

        writeLog("json="+param);


        String resStr = k3Service.doK3Action(param,k3Ip,putTWPurUrl);
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

    /*大陆出口-工厂直发，台湾入库*/
    public void tranTwPur_2(String lcbh, String gys, String rkrq, String rkck, String bb, List<Map<String,String>> detailDatas1, K3Service k3Service,String fentrytaxrate,String falldiscount,String purOrganization){
        JSONObject jsonObject = new JSONObject();

        String fbillno = lcbh;
        jsonObject.put("fbillno",fbillno);
        jsonObject.put("fstockorgid",purOrganization);
        jsonObject.put("fpurchaseorgid",purOrganization);
        jsonObject.put("fsupplierId","ZT021");
        jsonObject.put("fdemandorgid",purOrganization);
        jsonObject.put("fsettleorgid",purOrganization);
        jsonObject.put("fthirdbillno",lcbh);

        jsonObject.put("fdate",rkrq);
        jsonObject.put("fhdc",rkck);
        jsonObject.put("fsettlecurrid",bb);
        writeLog("jsonObject="+jsonObject.toJSONString());
        //传进口税
        if("0.00".equals(falldiscount)){
            jsonObject.put("falldiscount",falldiscount);
        }else {
            jsonObject.put("falldiscount","-"+falldiscount);
        }
        writeLog("jsonObject="+jsonObject.toJSONString());
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

            //价目表
            dtl.put("ftaxprice",detailData.get("twprice"));

            writeLog("dtl="+dtl.toJSONString());

            jsonArray.add(dtl);
        }

        jsonObject.put("fentrylist",jsonArray);

        String param = jsonObject.toJSONString();

        writeLog("json="+param);


        String resStr = k3Service.doK3Action(param,k3Ip,putTWPurUrl);
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
            //writeLog("cgsl="+detailData.get("cgsl"));
            dtl.put("frealqty",detailData.get("cgsl"));
            dtl.put("fstockid",rkck);

            dtl.put("ftaxprice",detailData.get("cgdj"));

            jsonArray.add(dtl);
        }

        jsonObject.put("fentrylist",jsonArray);

        String param = jsonObject.toJSONString();

        writeLog("json="+param);

        String resStr = k3Service.doK3Action(param,k3Ip,putHKPurUrl);
        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            k3Service.addLog(lcbh,"200");
            writeLog("同步金蝶香港采购入库单成功");
            return code;
        }else {
            k3Service.addLog(lcbh,"500");
            writeLog("同步金蝶香港采购入库单失败");
            return code;
        }
    }

    //海外采购
    public JSONObject tranHkPur_3(String fbillno,String org ,String gys, String rkrq, String rkck, String bb, List<Map<String,String>> detailDatas1, K3Service k3Service,String fentrytaxrate){

        JSONObject jsonObject = new JSONObject();
        JSONObject respJson = new JSONObject();

        jsonObject.put("fbillno",fbillno);

        jsonObject.put("fbillno",fbillno);
        jsonObject.put("fstockorgid",org);
        jsonObject.put("fpurchaseorgid",org);
        jsonObject.put("fsupplierId",gys);
        jsonObject.put("fdemandorgid",org);
        jsonObject.put("fsettleorgid",org);
        jsonObject.put("fthirdbillno",fbillno);

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

        String resStr = k3Service.doK3Action(param,k3Ip,putHKPurUrl);
        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            k3Service.addLog(fbillno,code);
            writeLog("同步金蝶香港采购入库单成功");
            respJson.put("code",code);
            return respJson;
        }else {
            String message = resJson.getString("message");
            k3Service.addLog(fbillno,code);
            writeLog("同步金蝶香港采购入库单失败");
            respJson.put("code",code);
            respJson.put("errorMessage",message);
            return respJson;
        }
    }

    public String tranHkPur_2(String lcbh, String gys, String rkrq, String rkck, String bb, List<Map<String,String>> detailDatas1, K3Service k3Service,String fentrytaxrate){

        PriceService priceService = new PriceService();
        LogService logService = new LogService();

        JSONObject jsonObject = new JSONObject();
        String fbillno = "HK_"+lcbh;
        jsonObject.put("fbillno",fbillno);

        jsonObject.put("fbillno",fbillno);
        jsonObject.put("fstockorgid","ZT021");
        jsonObject.put("fpurchaseorgid","ZT021");
        jsonObject.put("fsupplierId","ZT025");
        jsonObject.put("fdemandorgid","ZT021");
        jsonObject.put("fsettleorgid","ZT021");
        jsonObject.put("fthirdbillno",lcbh);

        jsonObject.put("fdate",rkrq);
        jsonObject.put("fhdc",rkck);

        jsonObject.put("fsettlecurrid","PRE001");
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

            //dtl.put("ftaxprice",detailData.get("cgdj"));
            //之前是取dms的采购单机字段，目前改为取日用品价目表的价格
            priceService.getDailyNecPrice(detailData.get("wlbm"),dtl);

            jsonArray.add(dtl);
        }

        jsonObject.put("fentrylist",jsonArray);

        String param = jsonObject.toJSONString();

        writeLog("json="+param);

        String resStr = priceService.doK3Action(param,k3Ip,putHKPurUrl);
        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        return code;
    }


    public String tranAbroadPur(String lcbh, String gys, String rkrq, String rkck, String hkBb, List<Map<String,String>> detailDatas1, K3Service k3Service,String fentrytaxrate,String purOrganization){

        PriceService priceService = new PriceService();
        LogService logService = new LogService();

        JSONObject jsonObject = new JSONObject();
        String fbillno = "HK_"+lcbh;
        jsonObject.put("fbillno",fbillno);

        jsonObject.put("fbillno",fbillno);
        jsonObject.put("fstockorgid",purOrganization);
        jsonObject.put("fpurchaseorgid",purOrganization);
        jsonObject.put("fsupplierId","ZT021");
        jsonObject.put("fdemandorgid",purOrganization);
        jsonObject.put("fsettleorgid",purOrganization);
        jsonObject.put("fthirdbillno",lcbh);

        jsonObject.put("fdate",rkrq);
        jsonObject.put("fhdc",rkck);

        jsonObject.put("fsettlecurrid",hkBb);
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

            dtl.put("ftaxprice",detailData.get("twprice"));

            jsonArray.add(dtl);
        }

        jsonObject.put("fentrylist",jsonArray);

        String param = jsonObject.toJSONString();

        writeLog("json="+param);

        String resStr = priceService.doK3Action(param,k3Ip,putHKPurUrl);
        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");


        if("200".equals(code)){
            k3Service.addLog(fbillno,code);
            writeLog("同步金蝶香港采购入库单成功");
        }else {
            String message = resJson.getString("message");
            k3Service.addLog(fbillno,code);
            writeLog("同步金蝶香港采购入库单失败");
        }


        return code;
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

        String resStr = k3Service.doK3Action(param,k3Ip,putHKSaleUrl);
        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        if("200".equals(code)){
            k3Service.addLog(lcbh,"200");
            writeLog("同步金蝶香港销售单成功");
        }else {
            k3Service.addLog(lcbh,"500");
            writeLog("同步金蝶香港销售单失败");
        }

        return code;
    }

    public String tranHkSale_2(String lcbh, String gys, String yjjcr, String rkck, String bb, List<Map<String,String>> detailDatas1, K3Service k3Service,String fentrytaxrate,String purCustomer){

        PriceService priceService = new PriceService();
        LogService logService = new LogService();

        JSONObject jsonObject = new JSONObject();
        String fbillno = "HK_"+lcbh;
        jsonObject.put("fbillno",fbillno);
        jsonObject.put("fstockorgid","ZT021");
        jsonObject.put("fsaleorgid","ZT021");
        jsonObject.put("fcustomerid",purCustomer);
        jsonObject.put("fdsgbase","ZT026");
        jsonObject.put("fsettleorgid","ZT021");
        jsonObject.put("type","HK");
        jsonObject.put("fsettlecurrid","PRE005");
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

            String ftaxprice = k3Service.getPrice(detailData.get("wlbm"));

            dtl.put("ftaxprice",ftaxprice);

            jsonArray.add(dtl);
        }

        jsonObject.put("fentitylist",jsonArray);
        String param = jsonObject.toJSONString();

        String resStr = priceService.doK3Action(param,k3Ip,putHKSaleUrl);
        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        /*if("200".equals(code)){
            logService.addLog(lcbh,"200");
            writeLog("同步金蝶采购入库单成功");
        }else {
            logService.addLog(lcbh,"500");
            writeLog("同步金蝶采购入库单失败");
        }*/

        return code;
    }

    public String tranDailyNecCnSale(String lcbh,String gys,String yjjcr, String ckck, String bb,List<Map<String,String>> detailDatas1,K3Service k3Service,String fentrytaxrate){

        PriceService priceService = new PriceService();

        JSONObject jsonObject = new JSONObject();
        String fbillno = "DN_"+lcbh;
        jsonObject.put("fbillno",fbillno);
        jsonObject.put("fstockorgid","ZT025");
        jsonObject.put("fsaleorgid","ZT025");
        jsonObject.put("fcustomerid","ZT021");
        jsonObject.put("fdsgbase","ZT021");
        jsonObject.put("fsettleorgid","ZT025");
        jsonObject.put("type","DN");
        jsonObject.put("fsettlecurrid","PRE001");
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

            dtl.put("fstockid",ckck);
            dtl.put("fsoorderno",lcbh);
            dtl.put("fdsgsrcoid",lcbh);


            priceService.getDailyNecPrice(detailData.get("wlbm"),dtl);

            // 放弃采取采购单价
            // dtl.put("ftaxprice",detailData.get("cgdj"));

            jsonArray.add(dtl);
        }

        jsonObject.put("fentitylist",jsonArray);
        String param = jsonObject.toJSONString();

        String resStr = k3Service.doK3Action(param,k3Ip,putHKSaleUrl);
        JSONObject resJson = JSONObject.parseObject(resStr);
        String code = resJson.getString("code");

        return code;
    }
}
