    package weaver.interfaces.hzy.resale.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.service.InventoryService;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

    public class CheckOutHkGoodsAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("开始执行CheckOutHkGoodsAction");

        String requestid = requestInfo.getRequestid();

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        Integer id = requestInfo.getRequestManager().getBillid();

        InventoryService inventoryService = new InventoryService();

        List<Map<String, String>> detailDatas1 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);

        List<Map<String,String>> hkList = new ArrayList<>();

        if(detailDatas1.size()>0){
            String dt1Sql = "select dt1.hptxm tm,dt1.fhl sl,main.rkrq from formtable_main_263 main " +
                    "inner join formtable_main_263_dt1 dt1 on main.id = dt1.mainid  where requestId = ?";

            RecordSet dt1Rs = new RecordSet();

            writeLog("dt1Sql="+dt1Sql+"requestid="+requestid);

            dt1Rs.executeQuery(dt1Sql,requestid);

            while (dt1Rs.next()){
                Map<String,String> map = new HashMap<>();
                String tm = dt1Rs.getString("tm");
                String sl = dt1Rs.getString("sl");
                String xsj = dt1Rs.getString("xsj");


                writeLog("tm="+tm+",sl="+sl+",xsj="+xsj);

                //条码
                map.put("tm",tm);
                //数量
                map.put("sl",sl);
                //含税单价
                map.put("xsj",xsj);
                //税率
                map.put("taxrate","5");

                String org = inventoryService.getOrg(tm);
                if("ZT021".equals(org)){
                    hkList.add(map);
                }
            }
            writeLog("hkList="+hkList.toString());

            if (hkList.size()>0){

                String updateSql = "update formtable_main_263 set spsfzyxgzt  = ? where requestid = ? ";

                RecordSet updateRs  = new RecordSet();

                updateRs.executeUpdate(updateSql,"0",requestid);


                String deleteSql = "delete from formtable_main_263_dt5 where mainid = ?";
                RecordSet deleteRs = new RecordSet();
                deleteRs.executeUpdate(deleteSql,id);


                //生成台湾采购信息跟香港退货信息
                for (Map<String,String> hkMap : hkList){
                   String sku = hkMap.get("tm");
                   String qty = hkMap.get("sl");

                   String insertSql = "insert formtable_main_263_dt5 (mainid,sku,refund_qty) values (?,?,?)";
                   RecordSet insertRs = new RecordSet();
                   insertRs.executeUpdate(insertSql,id,sku,qty);
                }
            }else {
                String updateSql = "update formtable_main_263 set spsfzyxgzt  = ? where requestid = ? ";

                RecordSet updateRs  = new RecordSet();

                updateRs.executeUpdate(updateSql,"1",requestid);
            }
        }

        writeLog("CheckOutHkGoodsAction执行完毕");
        return SUCCESS;
    }
}
