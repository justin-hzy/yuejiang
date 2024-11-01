package weaver.interfaces.hzy.k3.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icbc.api.internal.apache.http.impl.cookie.S;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.pojo.SaleDt1;
import weaver.interfaces.hzy.k3.pojo.SaleDt2;
import weaver.interfaces.hzy.k3.pojo.SupSale;
import weaver.interfaces.hzy.me.util.MeApiUtil;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

public class InventoryService extends BaseBean {

    private String getInventoryUrl = getPropValue("k3_api_config","getInventoryUrl");

    private String meIp = getPropValue("fulun_api_config","meIp");

    private String k3Ip = getPropValue("fulun_api_config","k3Ip");

    private String getTwBatchInventoryUrl = getPropValue("k3_api_config","getTwBatchInventoryUrl");

    public Map<String,List<SupSale>> getInventory(Map<String, List<SaleDt1>> dt1Map, String fhdc){

        K3Service k3Service = new K3Service();

        Map<String,List<SupSale>> supMap = new HashMap<>();

        boolean state = true;

        for (String key : dt1Map.keySet()){
            //writeLog(key+"="+dt1Map.get(key).size());

            List<SaleDt1> dt1List = dt1Map.get(key);

            for(SaleDt1 saleDt1 : dt1List){
                String hptxm = saleDt1.getHptxm();

                Integer fhl = saleDt1.getFhl();
                String gg = saleDt1.getGg();
                String sellPrice = saleDt1.getSellPrice();

                JSONObject reqJson = new JSONObject();

                reqJson.put("sku",hptxm);
                reqJson.put("stockNumber",fhdc);
                reqJson.put("storeType","TW");

                String params = reqJson.toJSONString();

                String respStr = k3Service.doK3Action(params,meIp,getInventoryUrl);

                if(respStr.length()>0){
                    //writeLog("respStr="+respStr);
                    JSONObject respJson = JSONObject.parseObject(respStr);
                    String code = respJson.getString("code");
                    if("200".equals(code)){
                        String data = respJson.getString("data");
                        JSONArray dataJsonArr = JSONArray.parseArray(data);
                        for(int i=0;i<dataJsonArr.size();i++){
                            JSONObject dataJson = dataJsonArr.getJSONObject(i);
                            Integer fBaseQty = dataJson.getInteger("FBaseQty");
                            writeLog("fBaseQty="+fBaseQty);
                            writeLog("fhl="+fhl);
                            if(Integer.compare(fBaseQty,fhl) < 0){
                                //仓库数量<单据发货数量,仓库数量-单据发货数量 = 香港主体发货数/台湾采购入货数

                                //所属主体
                                String szzt = getOrg(hptxm);


                                if("ZT026".equals(szzt)){

                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    String now = dateFormat.format(new Date());

                                    now = "'"+now+"'";

                                    String input = "2024-06-18 16:29:35";
                                    StringTokenizer tokenizer = new StringTokenizer(input, " ");

                                    String dateString = tokenizer.nextToken();
                                    String timeString = tokenizer.nextToken();

                                    String insertError = "insert into dms_k3_error_log (billNo,message,createTime,time,date) values ("
                                            +key+","+hptxm+"库存不足"+","+","+now+","+timeString+","+dateString+")";;
                                }else {
                                    Integer hkQty = fhl - fBaseQty;
                                    SupSale supSale = new SupSale();
                                    supSale.setQuantity(hkQty);
                                    supSale.setSellPrice(sellPrice);
                                    supSale.setHptxm(hptxm);
                                    supSale.setTaxRate(gg);
                                    if (supMap.containsKey(key)){
                                        List<SupSale> supSales = supMap.get(key);
                                        supSales.add(supSale);
                                    }else {
                                        List<SupSale> supSales = new ArrayList<>();
                                        supSales.add(supSale);
                                        supMap.put(key,supSales);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if(true == state){
            return supMap;
        }else {
            supMap = new HashMap<>();
            return supMap;
        }
    }



    public void compareInv(){

    }







    public Map<String,List<SupSale>> getSapInventory(Map<String, List<SaleDt2>> dt2Map, String fhdc){

        Map<String,List<SupSale>> supMap = new HashMap<>();
        K3Service k3Service = new K3Service();

        boolean state = true;

        for (String key : dt2Map.keySet()){
            List<SaleDt2> dt2List = dt2Map.get(key);

            for(SaleDt2 saleDt2 : dt2List){
                String hptxm = saleDt2.getHptxm();
                Integer fhsl = saleDt2.getFhsl();
                String sellPrice = saleDt2.getSellPrice();

                JSONObject reqJson = new JSONObject();

                reqJson.put("sku",hptxm);
                reqJson.put("stockNumber",fhdc);
                reqJson.put("storeType","TW");

                String params = reqJson.toJSONString();

                String respStr = k3Service.doK3Action(params,meIp,getInventoryUrl);

                if(respStr.length()>0){
                    //writeLog("respStr="+respStr);
                    JSONObject respJson = JSONObject.parseObject(respStr);
                    String code = respJson.getString("code");
                    if("200".equals(code)){
                        String data = respJson.getString("data");
                        JSONArray dataJsonArr = JSONArray.parseArray(data);
                        for(int i=0;i<dataJsonArr.size();i++){
                            JSONObject dataJson = dataJsonArr.getJSONObject(i);
                            Integer fBaseQty = dataJson.getInteger("FBaseQty");
                            fBaseQty = 0;
                            //writeLog("fBaseQty="+fBaseQty);
                            if(Integer.compare(fBaseQty,fhsl) < 0){
                                //仓库数量<单据发货数量,仓库数量-单据发货数量 = 香港主体发货数/台湾采购入货数
                                //String szzt = getOrg(hptxm);
                                Integer hkQty = fhsl - fBaseQty;
                                SupSale supSale = new SupSale();
                                supSale.setQuantity(hkQty);
                                supSale.setSellPrice(sellPrice);
                                supSale.setHptxm(hptxm);
                                //supSale.setTaxRate(gg);
                                if (supMap.containsKey(key)){
                                    List<SupSale> supSales = supMap.get(key);
                                    supSales.add(supSale);
                                }else {
                                    List<SupSale> supSales = new ArrayList<>();
                                    supSales.add(supSale);
                                    supMap.put(key,supSales);
                                }
                            }
                        }
                    }
                }
            }
        }
        if(true == state){
            return supMap;
        }else {
            supMap = new HashMap<>();
            return supMap;
        }
    }

    public Map<String,List<SupSale>> getTWPro(Map<String, List<SaleDt1>> dt1Map){
        Map<String,List<SupSale>> supMap = new HashMap<>();
        if(dt1Map.size()>0){
            for (String key : dt1Map.keySet()){
                List<SaleDt1> dt1List = dt1Map.get(key);
                for(SaleDt1 saleDt1 : dt1List){
                    String hptxm = saleDt1.getHptxm();
                    Integer fhl = saleDt1.getFhl();
                    String gg = saleDt1.getGg();
                    String sellPrice = saleDt1.getSellPrice();

                    SupSale supSale = new SupSale();
                    supSale.setQuantity(fhl);
                    supSale.setSellPrice(sellPrice);
                    supSale.setHptxm(hptxm);
                    supSale.setTaxRate(gg);

                    if (supMap.containsKey(key)){
                        List<SupSale> supSales = supMap.get(key);
                        supSales.add(supSale);
                    }else {
                        List<SupSale> supSales = new ArrayList<>();
                        supSales.add(supSale);
                        supMap.put(key,supSales);
                    }
                }
            }
        }
        return supMap;
    }

    public Map<String,List<SupSale>> getTWSap(Map<String, List<SaleDt2>> dt2Map){
        Map<String,List<SupSale>> supMap = new HashMap<>();

        for (String key : dt2Map.keySet()) {
            List<SaleDt2> dt2List = dt2Map.get(key);

            for (SaleDt2 saleDt2 : dt2List) {
                String hptxm = saleDt2.getHptxm();
                Integer fhsl = saleDt2.getFhsl();
                String sellPrice = saleDt2.getSellPrice();

                SupSale supSale = new SupSale();
                supSale.setQuantity(fhsl);
                supSale.setSellPrice(sellPrice);
                supSale.setHptxm(hptxm);

                if (supMap.containsKey(key)){
                    List<SupSale> supSales = supMap.get(key);
                    supSales.add(supSale);
                }else {
                    List<SupSale> supSales = new ArrayList<>();
                    supSales.add(supSale);
                    supMap.put(key,supSales);
                }
            }
        }
        return supMap;
    }

    public Map<String,List<Map<String,String>>> getTrfInventory(List<Map<String,String>> list,String fhdc,String lcbh){

        Map<String,List<Map<String,String>>> resMap = new HashMap<>();

        K3Service k3Service = new K3Service();

        InventoryService inventoryService = new InventoryService();

        List<Map<String,String>> hkMapList = new ArrayList<>();

        List<Map<String,String>> twMapList = new ArrayList<>();

        for (Map<String,String> map : list){

            HashMap<String,String> hkMap = new HashMap<>();

            HashMap<String,String> twMap = new HashMap<>();

            String hpbh = map.get("hpbh");
            String sl = map.get("sl");

            JSONObject reqJson = new JSONObject();
            reqJson.put("sku",hpbh);
            reqJson.put("stockNumber",fhdc);
            reqJson.put("storeType","TW");

            String params = reqJson.toJSONString();

            String respStr = k3Service.doK3Action(params,meIp,getInventoryUrl);
            writeLog("respStr="+respStr);
            if(respStr.length()>0){
                JSONObject respJson = JSONObject.parseObject(respStr);
                String code = respJson.getString("code");
                if("200".equals(code)){
                    String data = respJson.getString("data");
                    JSONArray dataJsonArr = JSONArray.parseArray(data);
                    if(dataJsonArr.size()>0){
                        JSONObject dataJson = dataJsonArr.getJSONObject(0);
                        Integer fBaseQty = dataJson.getInteger("FBaseQty");

                        writeLog("compare="+Integer.compare(fBaseQty,Integer.valueOf(sl)));
                        if(Integer.compare(fBaseQty,Integer.valueOf(sl)) < 0){
                            writeLog("获取商品所属组织");
                            String szzt = inventoryService.getOrg(hpbh);
                            if("ZT026".equals(szzt)){
                                writeLog("该商品的所属组织为台湾，进入错误日志");
                                k3Service.addDmsKErrorLog(hpbh,lcbh);
                            }else {
                                writeLog("计算香港商品的发货数");
                                //台湾库存不足，订单数量-台湾库存=香港出库数量
                                Integer hkNumber = Integer.valueOf(sl) - fBaseQty;

                                //台湾出库数量 = 订单数量
                                Integer twNumber = fBaseQty;

                                if(hkNumber>0){
                                    hkMap.put("tm",hpbh);
                                    hkMap.put("sl",String.valueOf(hkNumber));
                                    hkMapList.add(hkMap);
                                }

                                if(twNumber>0){
                                    twMap.put("tm",hpbh);
                                    twMap.put("sl",String.valueOf(twNumber));
                                    twMapList.add(twMap);
                                }

                            }
                        }else {
                            writeLog("台湾库存充足，在台湾生成单据");
                            Integer twNumber = Integer.valueOf(sl);
                            twMap.put("tm",hpbh);
                            twMap.put("sl",String.valueOf(twNumber));
                            twMapList.add(twMap);

                            writeLog("twMapList="+twMapList.toString());
                        }
                    }
                }
            }
        }

        resMap.put("tw",twMapList);

        resMap.put("hk",hkMapList);

        return resMap;
    }



    public String getBatchTwInventory(List<String> skus,String fhdcxs){
        writeLog("skus="+skus.toString());
        JSONObject reqJson = new JSONObject();
        JSONArray skuArr = new JSONArray();

        for(String sku:skus){
            writeLog("sku="+sku);
            skuArr.add(sku);
        }

        writeLog("skuArr="+skuArr.toJSONString());

        reqJson.put("skus",skuArr);
        reqJson.put("stockNumber",fhdcxs);
        reqJson.put("storeType","TW");
        K3Service k3Service = new K3Service();
        String params = reqJson.toJSONString();
        writeLog("params="+params);
        String respStr = k3Service.doK3Action(params,k3Ip,getTwBatchInventoryUrl);

        //writeLog("respStr="+respStr);

        return respStr;
    }

    public String getOrg(String sku){
        String sql = "select szzt from uf_spk where txm = ? and sxjzt = 5 and szzt is not null";

        RecordSet rs = new RecordSet();
        rs.executeQuery(sql,sku);

        String szzt= "";

        if(rs.next()){
            szzt = rs.getString("szzt");
        }
        return szzt;
    }


    public List<Map<String,String>> anlysBatIn(String respStr){

        List<Map<String,String>> k3InvList = new ArrayList<>();
        /*解析批量库存*/
        JSONObject resJson = JSONObject.parseObject(respStr);

        JSONArray arr = resJson.getJSONArray("data");

        for (int i = 0 ;i<arr.size();i++){
            JSONObject jsonObj = arr.getJSONObject(i);

            String sku = jsonObj.getString("FMaterialId.fnumber");

            String fBaseQty = jsonObj.getString("FBaseQty");

            fBaseQty = fBaseQty.substring(0,fBaseQty.indexOf("."));

            Map<String,String> k3Inv = new HashMap<>();

            k3Inv.put("sku",sku);
            k3Inv.put("fBaseQty",fBaseQty);

            k3InvList.add(k3Inv);
        }

        return k3InvList;
    }
}
