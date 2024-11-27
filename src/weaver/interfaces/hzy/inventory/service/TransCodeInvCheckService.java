package weaver.interfaces.hzy.inventory.service;

import cn.hutool.core.collection.CollUtil;
import com.icbc.api.internal.apache.http.M;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.service.InventoryService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransCodeInvCheckService extends BaseBean {

    public Map<String,List<Map<String,String>>> compareTwInv(List<Map<String,String>> k3InvList, List<Map<String,String>> dtSums, InventoryService inventoryService){

        Map<String,List<Map<String,String>>> respMap = new HashMap<>();

        List<Map<String,String>> hkTransCodeList = new ArrayList<>();

        List<Map<String,String>> twTransCodeList = new ArrayList<>();

        List<Map<String,String>> twNotEnoughList = new ArrayList<>();

        writeLog("dtSums="+dtSums.toString());

        for (Map<String,String> dtSum : dtSums) {

            //转换前物料编码
            String oriSku = dtSum.get("oriSku");
            //转换后物料编码
            String finSku = dtSum.get("finSku");
            //转换前仓库
            String oriWareHouse = dtSum.get("oriWareHouse");
            //转换后仓库
            String finWareHouse = dtSum.get("finWareHouse");
            //实际转换数量
            String realQty = dtSum.get("realQty");




            for (Map<String,String> k3Inv : k3InvList){
                String sku = k3Inv.get("sku");
                //writeLog("sku="+sku);

                if(sku.equals(oriSku)){

                    /*writeLog("金蝶-sku="+sku);
                    writeLog("dms-wlbm="+wlbm);*/


                    Map<String,String> hkTransCode = new HashMap<>();
                    Map<String,String> twTransCode = new HashMap<>();
                    Map<String,String> twNotEnough = new HashMap<>();
                    String fBaseQty = k3Inv.get("fBaseQty");
//                    writeLog("fBaseQty="+fBaseQty);


                    /*writeLog("fBaseQty="+fBaseQty);
                    writeLog("xssl="+xssl);*/

                    if(Integer.compare(Integer.valueOf(fBaseQty),Integer.valueOf(realQty)) < 0){
                        writeLog("生成香港数据");

                        String szzt = inventoryService.getOrg(oriSku);


                        if("ZT026".equals(szzt)){
                            writeLog("发现库存不足");
                            twNotEnough.put("sku",sku);
                            twNotEnoughList.add(twNotEnough);
                        }else {
                            if(Integer.valueOf(fBaseQty)<0){
                                writeLog(oriSku+"的台湾即时库存为负数");
                                Integer hkNumber = Integer.valueOf(realQty);
                                hkTransCode.put("oriSku",oriSku);
                                hkTransCode.put("finSku",finSku);
                                hkTransCode.put("oriWareHouse",oriWareHouse);
                                hkTransCode.put("finWareHouse",finWareHouse);
                                hkTransCode.put("realQty",String.valueOf(hkNumber));
                                hkTransCodeList.add(hkTransCode);
                            }else {
                                //台湾库存不足，订单数量-台湾库存=香港出库数量
                                Integer hkNumber = Integer.valueOf(realQty) - Integer.valueOf(fBaseQty) ;


                                //台湾出库数量 = 订单数量
                                Integer twNumber = Integer.valueOf(fBaseQty);

                                if(hkNumber>0){
                                    hkTransCode.put("oriSku",oriSku);
                                    hkTransCode.put("finSku",finSku);
                                    hkTransCode.put("oriWareHouse",oriWareHouse);
                                    hkTransCode.put("finWareHouse",finWareHouse);
                                    hkTransCode.put("realQty",String.valueOf(hkNumber));
                                    hkTransCodeList.add(hkTransCode);
                                }

                                if(twNumber>0){
                                    twTransCode.put("oriSku",oriSku);
                                    twTransCode.put("finSku",finSku);
                                    twTransCode.put("oriWareHouse",oriWareHouse);
                                    twTransCode.put("finWareHouse",finWareHouse);
                                    twTransCode.put("realTwQty",String.valueOf(twNumber));
                                    twTransCodeList.add(twTransCode);
                                }
                            }
                        }
                    }else {
                        writeLog("台湾库存充足，在台湾生成单据");
                        Integer twNumber = Integer.valueOf(realQty);
                        twTransCode.put("oriSku",oriSku);
                        twTransCode.put("finSku",finSku);
                        twTransCode.put("oriWareHouse",oriWareHouse);
                        twTransCode.put("finWareHouse",finWareHouse);
                        twTransCode.put("realTwQty",String.valueOf(twNumber));
                        twTransCodeList.add(twTransCode);
                    }
                }
            }
        }

        if(CollUtil.isNotEmpty(twNotEnoughList)){
            respMap.put("twNotEnoughList",twNotEnoughList);
        }

        if(CollUtil.isNotEmpty(hkTransCodeList)){
            respMap.put("hkTransCodeList",hkTransCodeList);
        }

        if (CollUtil.isNotEmpty(twTransCodeList)){
            respMap.put("twTransCodeList",twTransCodeList);
        }

        return  respMap;
    }

    public Map<String,List<Map<String,String>>> compareHkInv(List<Map<String,String>> k3HkInvList, List<Map<String,String>> dtHkSums, InventoryService inventoryService){
        Map<String,List<Map<String,String>>> respMap = new HashMap<>();

        List<Map<String,String>> hkNotEnoughList = new ArrayList<>();

        writeLog("dtHkSums="+dtHkSums.toString());

        for (Map<String,String> dtHkSum : dtHkSums) {

            //物料编码
            String oriSku = dtHkSum.get("oriSku");
            //数量
            String realQty = dtHkSum.get("realQty");

            for (Map<String,String> k3HkInv : k3HkInvList){
                String sku = k3HkInv.get("sku");

                if(sku.equals(oriSku)){


                    String fBaseQty = k3HkInv.get("fBaseQty");



                    if(Integer.compare(Integer.valueOf(fBaseQty),Integer.valueOf(realQty)) < 0){
                        Map<String,String> hkNotEnough = new HashMap<>();
                        hkNotEnough.put("oriSku",oriSku);
                        hkNotEnoughList.add(hkNotEnough);
                    }
                }

            }
        }


        if(CollUtil.isNotEmpty(hkNotEnoughList)){
            respMap.put("hkNotEnoughList",hkNotEnoughList);
        }


        return respMap;
    }

}
