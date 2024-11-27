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

            //ת��ǰ���ϱ���
            String oriSku = dtSum.get("oriSku");
            //ת�������ϱ���
            String finSku = dtSum.get("finSku");
            //ת��ǰ�ֿ�
            String oriWareHouse = dtSum.get("oriWareHouse");
            //ת����ֿ�
            String finWareHouse = dtSum.get("finWareHouse");
            //ʵ��ת������
            String realQty = dtSum.get("realQty");




            for (Map<String,String> k3Inv : k3InvList){
                String sku = k3Inv.get("sku");
                //writeLog("sku="+sku);

                if(sku.equals(oriSku)){

                    /*writeLog("���-sku="+sku);
                    writeLog("dms-wlbm="+wlbm);*/


                    Map<String,String> hkTransCode = new HashMap<>();
                    Map<String,String> twTransCode = new HashMap<>();
                    Map<String,String> twNotEnough = new HashMap<>();
                    String fBaseQty = k3Inv.get("fBaseQty");
//                    writeLog("fBaseQty="+fBaseQty);


                    /*writeLog("fBaseQty="+fBaseQty);
                    writeLog("xssl="+xssl);*/

                    if(Integer.compare(Integer.valueOf(fBaseQty),Integer.valueOf(realQty)) < 0){
                        writeLog("�����������");

                        String szzt = inventoryService.getOrg(oriSku);


                        if("ZT026".equals(szzt)){
                            writeLog("���ֿ�治��");
                            twNotEnough.put("sku",sku);
                            twNotEnoughList.add(twNotEnough);
                        }else {
                            if(Integer.valueOf(fBaseQty)<0){
                                writeLog(oriSku+"��̨�弴ʱ���Ϊ����");
                                Integer hkNumber = Integer.valueOf(realQty);
                                hkTransCode.put("oriSku",oriSku);
                                hkTransCode.put("finSku",finSku);
                                hkTransCode.put("oriWareHouse",oriWareHouse);
                                hkTransCode.put("finWareHouse",finWareHouse);
                                hkTransCode.put("realQty",String.valueOf(hkNumber));
                                hkTransCodeList.add(hkTransCode);
                            }else {
                                //̨���治�㣬��������-̨����=��۳�������
                                Integer hkNumber = Integer.valueOf(realQty) - Integer.valueOf(fBaseQty) ;


                                //̨��������� = ��������
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
                        writeLog("̨������㣬��̨�����ɵ���");
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

            //���ϱ���
            String oriSku = dtHkSum.get("oriSku");
            //����
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
