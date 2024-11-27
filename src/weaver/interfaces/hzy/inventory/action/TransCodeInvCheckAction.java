package weaver.interfaces.hzy.inventory.action;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.icbc.api.internal.apache.http.impl.cookie.S;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.common.service.CommonService;
import weaver.interfaces.hzy.inventory.service.TransCodeInvCheckService;
import weaver.interfaces.hzy.k3.service.InventoryService;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransCodeInvCheckAction extends BaseBean implements Action {
    @Override
    public String execute(RequestInfo requestInfo) {

        InventoryService inventoryService = new InventoryService();
        TransCodeInvCheckService transCodeInvCheckService = new TransCodeInvCheckService();
        writeLog("开始执行TransCodeInvCheckAction");

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        Integer id = requestInfo.getRequestManager().getBillid();
        List<Map<String, String>> detailDatas1 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);
        writeLog("detailDatas1="+detailDatas1.toString());
        List<Map<String,String>> twNotEnoughList = new ArrayList<>();
        List<Map<String,String>> hkNotEnoughList = new ArrayList<>();
        List<Map<String,String>> hkTransCodeList = new ArrayList<>();
        List<Map<String,String>> twTransCodeList = new ArrayList<>();

        List<String> twSkus = new ArrayList<>();

        if (detailDatas1.size() > 0) {
            //获取当前仓库和变更后仓库
            String sql1 = "select dt1.dqcw1,dt1.bghcw1 from formtable_main_244_dt1 dt1 where dt1.sjbgsl is not null and dt1.sjbgsl > 0 and mainid = ? limit 0,1";

            RecordSet recordSet1 = new RecordSet();
            recordSet1.executeQuery(sql1,id);
            //当前仓库
            String oriWareHouse = "";
            //变更好仓库
            String finWareHouse = "";



            while (recordSet1.next()){
                oriWareHouse = recordSet1.getString("dqcw1");
                finWareHouse = recordSet1.getString("bghcw1");

            }

            //获取变更后sku、当前sku、实际数量、数量
            String sql2 = "select dt1.hpbmbghtw,dt1.hpbmtw,dt1.sjbgsl,dt1.sl from formtable_main_244_dt1 dt1 where dt1.sjbgsl is not null and dt1.sjbgsl > 0 and mainid = ?";

            RecordSet recordSet2 = new RecordSet();
            recordSet2.executeQuery(sql2,id);

            JSONArray assyFEntities = new JSONArray();

            List<Map<String, String>> dtSums = new ArrayList<>();

            while (recordSet2.next()) {
                Map<String, String> dtSum = new HashMap<>();
                //转换前货品编码
                String oriSku = recordSet2.getString("hpbmtw");
                //转换后货品编码
                String finSku = recordSet2.getString("hpbmbghtw");
                //实际变更数量
                String realQty = recordSet2.getString("sjbgsl");
                //数量
                String qty = recordSet2.getString("sl");

                twSkus.add(oriSku);

                dtSum.put("oriSku", oriSku);
                dtSum.put("finSku",finSku);
                dtSum.put("oriWareHouse",oriWareHouse);
                dtSum.put("finWareHouse",finWareHouse);
                dtSum.put("realQty", realQty);
                dtSums.add(dtSum);
            }

            String respTwStr = inventoryService.getBatchTwInventory(twSkus,oriWareHouse);
            writeLog("respTwStr="+respTwStr);

            List<Map<String, String>> k3TwInvList = inventoryService.anlysBatIn(respTwStr,twSkus);

            writeLog("k3TwInvList=" + k3TwInvList);

            Map<String,List<Map<String,String>>> respTwMap = transCodeInvCheckService.compareTwInv(k3TwInvList,dtSums,inventoryService);

            if (respTwMap.containsKey("twNotEnoughList")){
                twNotEnoughList = respTwMap.get("twNotEnoughList");
                writeLog("twNotEnoughList="+twNotEnoughList.toString());
                String str1 = "update formtable_main_244 set is_tw_enough = ? where id = ?";
                RecordSet rs1 = new RecordSet();
                rs1.executeUpdate(str1,1,id);
            }else {
                if(respTwMap.containsKey("hkTransCodeList")){
                    hkTransCodeList = respTwMap.get("hkTransCodeList");
                    writeLog("hkTransCodeList="+hkTransCodeList.toString());

                    List<Map<String, String>> dtHkSums = new ArrayList<>();

                    List<String> hkSkus = new ArrayList<>();
                    for (Map<String,String> hkTransCode : hkTransCodeList){
                        Map<String,String> dtHkSum = new HashMap<>();
                        hkSkus.add(hkTransCode.get("oriSku"));

                        dtHkSum.put("oriSku",hkTransCode.get("oriSku"));
                        //dtHkSum.put("finSku",hkTransCode.get("finSku"));
                        dtHkSum.put("oriWareHouse",hkTransCode.get("oriWareHouse"));
                        //dtHkSum.put("finWareHouse",hkTransCode.get("finWareHouse"));
                        dtHkSum.put("realQty",hkTransCode.get("realQty"));

                        dtHkSums.add(dtHkSum);
                    }

                    //匹配香港库存
                    String respHkStr = inventoryService.getBatchHkInventory(hkSkus,oriWareHouse);
                    writeLog("respHkStr="+respHkStr);

                    List<Map<String, String>> k3HkInvList = inventoryService.anlysBatIn(respHkStr,hkSkus);
                    writeLog("k3HkInvList=" + k3HkInvList);


                    Map<String,List<Map<String,String>>> respHkMap = transCodeInvCheckService.compareHkInv(k3HkInvList,dtHkSums,inventoryService);

                    if (respHkMap.containsKey("hkNotEnoughList")){
                        hkNotEnoughList = respHkMap.get("hkNotEnoughList");
                        writeLog("hkNotEnoughList="+hkNotEnoughList.toString());
                        String str1 = "update formtable_main_244 set is_tw_enough = ?,is_hk_enough = ? where id = ?";
                        RecordSet rs1 = new RecordSet();
                        rs1.executeUpdate(str1,0,1,id);
                    }else {
                        //更新状态
                        String str1 = "update formtable_main_244 set is_tw_enough = ?,is_hk_enough = ? where id = ?";
                        RecordSet rs1 = new RecordSet();
                        rs1.executeUpdate(str1,0,0,id);

                        //插入前清除历史数据
                        String deleteStr = "delete from formtable_main_244_dt3 where mainid = ?";
                        RecordSet deleteRs = new RecordSet();
                        deleteRs.executeUpdate(deleteStr,id);


                        //插入香港转码数据
                        for (Map<String,String> hkTransCode :  hkTransCodeList){
                            String insertHk = "insert into formtable_main_244_dt3 (mainid,ori_sku,fin_sku,ori_warehouse,fin_warehouse,qty) values (?,?,?,?,?,?)";
                            RecordSet insertHkRs = new RecordSet();
                            insertHkRs.executeUpdate(insertHk,id,hkTransCode.get("oriSku"),hkTransCode.get("finSku"),hkTransCode.get("oriWareHouse"),hkTransCode.get("finWareHouse"),hkTransCode.get("realQty"));
                        }
                    }
                }

                if(respTwMap.containsKey("twTransCodeList")){

                    //更新状态
                    String str1 = "update formtable_main_244 set is_tw_enough = ? where id = ?";
                    RecordSet rs1 = new RecordSet();
                    rs1.executeUpdate(str1,0,id);

                    twTransCodeList = respTwMap.get("twTransCodeList");
                    writeLog("twTransCodeList="+twTransCodeList.toString());

                    //插入前清除历史数据
                    String deleteStr = "delete from formtable_main_244_dt4 where mainid = ?";
                    RecordSet deleteRs = new RecordSet();
                    deleteRs.executeUpdate(deleteStr,id);

                    //插入台湾转码数据
                    for (Map<String,String> twTransCode :  twTransCodeList){

                        String insertTw = "insert into formtable_main_244_dt4 (mainid,ori_sku,fin_sku,ori_warehouse,fin_warehouse,qty) values (?,?,?,?,?,?)";
                        RecordSet insertTwRs = new RecordSet();
                        insertTwRs.executeUpdate(insertTw,id,twTransCode.get("oriSku"),twTransCode.get("finSku"),twTransCode.get("oriWareHouse"),twTransCode.get("finWareHouse"),twTransCode.get("realTwQty"));
                    }
                }


            }
        }

        return SUCCESS;
    }
}
