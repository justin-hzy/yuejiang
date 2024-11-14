package weaver.interfaces.hzy.inventory.service;

import cn.hutool.core.collection.CollUtil;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.common.service.CommonService;
import weaver.interfaces.hzy.k3.service.InventoryService;

import java.text.SimpleDateFormat;
import java.util.*;

public class VirtualInventoryCheckService extends BaseBean {


    public Map<String,List<Map<String,String>>> compareInv(List<Map<String,String>> k3InvList, List<Map<String,String>> dtSums, InventoryService inventoryService, String processId){

        Map<String,List<Map<String,String>>> respMap = new HashMap<>();

        List<Map<String,String>> hkReTrfList = new ArrayList<>();

        List<Map<String,String>> twReTrfList = new ArrayList<>();

        List<Map<String,String>> twNotEnoughList = new ArrayList<>();

        writeLog("dtSums="+dtSums.toString());

        for (Map<String,String> dtSum : dtSums) {

            String wlbm = dtSum.get("tm");

            String xssl = dtSum.get("sl");


            for (Map<String,String> k3Inv : k3InvList){
                String sku = k3Inv.get("sku");
                //writeLog("sku="+sku);

                if(sku.equals(wlbm)){

                    /*writeLog("���-sku="+sku);
                    writeLog("dms-wlbm="+wlbm);*/


                    Map<String,String> hkReTrf = new HashMap<>();
                    Map<String,String> twReTrf = new HashMap<>();
                    Map<String,String> twNotEnough = new HashMap<>();
                    String fBaseQty = k3Inv.get("fBaseQty");
//                    writeLog("fBaseQty="+fBaseQty);


                    /*writeLog("fBaseQty="+fBaseQty);
                    writeLog("xssl="+xssl);*/

                    if(Integer.compare(Integer.valueOf(fBaseQty),Integer.valueOf(xssl)) < 0){
                        writeLog("�����������");

                        String szzt = inventoryService.getOrg(wlbm);


                        if("ZT026".equals(szzt)){
                            writeLog("���ֿ�治��");
                            twNotEnough.put("sku",sku);
                            twNotEnoughList.add(twNotEnough);
                        }else {
                            if(Integer.valueOf(fBaseQty)<0){
                                writeLog(wlbm+"��̨�弴ʱ���Ϊ����");
                                Integer hkNumber = Integer.valueOf(xssl);
                                hkReTrf.put("tm",wlbm);
                                hkReTrf.put("sl",String.valueOf(hkNumber));
                                hkReTrfList.add(hkReTrf);
                            }else {
                                //̨���治�㣬��������-̨����=��۳�������
                                Integer hkNumber = Integer.valueOf(xssl) - Integer.valueOf(fBaseQty) ;


                                //̨��������� = ��������
                                Integer twNumber = Integer.valueOf(fBaseQty);

                                if(hkNumber>0){
                                    hkReTrf.put("tm",wlbm);
                                    hkReTrf.put("sl",String.valueOf(hkNumber));
                                    hkReTrfList.add(hkReTrf);
                                }

                                if(twNumber>0){
                                    twReTrf.put("tm",wlbm);
                                    twReTrf.put("sl",String.valueOf(twNumber));
                                    twReTrfList.add(twReTrf);
                                }
                            }
                        }
                    }else {
                        writeLog("̨������㣬��̨�����ɵ���");
                        Integer twNumber = Integer.valueOf(xssl);
                        twReTrf.put("tm",wlbm);
                        twReTrf.put("sl",String.valueOf(twNumber));
                        twReTrfList.add(twReTrf);
                    }
                }
            }
        }

        if(CollUtil.isNotEmpty(twNotEnoughList)){
            respMap.put("twNotEnoughList",twNotEnoughList);
        }

        if(CollUtil.isNotEmpty(hkReTrfList)){
            respMap.put("hkReTrfList",hkReTrfList);
        }

        if (CollUtil.isNotEmpty(twReTrfList)){
            respMap.put("twReTrfList",twReTrfList);
        }

        return  respMap;
    }




}
