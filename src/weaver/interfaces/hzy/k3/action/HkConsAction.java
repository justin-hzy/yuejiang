package weaver.interfaces.hzy.k3.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.service.ConsService;
import weaver.interfaces.hzy.k3.service.InventoryService;
import weaver.interfaces.hzy.k3.service.K3Service;
import weaver.interfaces.tx.util.WorkflowUtil;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HkConsAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("执行HkConsAction");

        String requestid = requestInfo.getRequestid();

        RequestManager requestManager = requestInfo.getRequestManager();

        K3Service k3Service = new K3Service();

        WorkflowUtil workflowUtil = new WorkflowUtil();

        ConsService consService = new ConsService();

        List<Map<String, String>> hkSales = new ArrayList<>();

        /*获取主流程主表数据*/
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        String kh = mainData.get("kh");
        //类型
        String lx = mainData.get("lx");
        //单据日期
        String sqrq = mainData.get("sqrq");
        //流程编号
        String lcbh = mainData.get("lcbh");

        InventoryService inventoryService = new InventoryService();

        writeLog("mainData="+mainData.toString());

        Map<String,String> mainTableData = new HashMap<>();

        mainTableData.put("kh",kh);
        //单据日期为入库日期
        mainTableData.put("djrq",sqrq);

        mainTableData.put("shdc",kh);


        if("0".equals(lx)){
            String fhdcxs = mainData.get("fhdcxs");

            mainTableData.put("fhdc",fhdcxs);

            //寄售销售 查询字段为 sku、销售数量、含税单价
            //String sql = "select dt1.wlbm tm,dt1.xssl sl,dt1.hsdj xsj from formtable_main_238 main inner join formtable_main_238_dt1 dt1 on main.id = dt1.mainid where main.requestId = ? ";

            String sql = "select dt1.wlbm tm,sum(dt1.xssl) sl from formtable_main_238 main inner join formtable_main_238_dt1 dt1 on main.id = dt1.mainid where main.requestId = ? group by tm";

            RecordSet dtRs = new RecordSet();

            dtRs.executeQuery(sql,requestid);

            List<Map<String,String>> dtSums = new ArrayList<>();

            List<String> skus = new ArrayList<>();

            while (dtRs.next()){
                Map<String,String> dtSum = new HashMap<>();
                String tm = dtRs.getString("tm");
                String sl = dtRs.getString("sl");
                //含税单价
                String xsj = dtRs.getString("xsj");

                dtSum.put("tm",tm);
                dtSum.put("sl",sl);
                dtSum.put("xsj",xsj);

                skus.add(tm);
                dtSums.add(dtSum);
            }

            String respStr = inventoryService.getBatchTwInventory(skus,fhdcxs);

            List<Map<String,String>> k3InvList = consService.anlysBatIn(respStr,skus);

            writeLog("k3InvList="+k3InvList);

            hkSales = consService.compareInv(k3InvList,dtSums,inventoryService,lcbh);

            writeLog("hkSales="+hkSales.toString());
        }

        writeLog("hkSales=" + hkSales.toString());

        String updateSql = "update formtable_main_238 set hk_status  = ? where requestid = ? ";

        RecordSet rs  = new RecordSet();

        String id = getSaleId(requestid);

        if(hkSales.size()>0){

            rs.executeUpdate(updateSql,"0",requestid);

            String deleteSql = "DELETE FROM formtable_main_238_dt3 where mainid = ?";
            RecordSet deleteRs = new RecordSet();
            deleteRs.executeUpdate(deleteSql,id);

            for (Map<String,String> hkSale: hkSales){

                String tm = hkSale.get("tm");
                String sl = hkSale.get("sl");

                String insertSql = "insert into formtable_main_238_dt3 (mainid,tm,sl) values ('"+id+"','"+tm+"','"+sl+"')";
                RecordSet insertRs = new RecordSet();
                insertRs.executeUpdate(insertSql);
            }
        }else if(hkSales.size()==0){
            rs.executeUpdate(updateSql,"1",requestid);
        }

        return SUCCESS;
    }


    public String getSaleId(String requestid){
        String sql = "select id from formtable_main_238 where requestid = ?";
        RecordSet recordSet = new RecordSet();
        recordSet.executeQuery(sql,requestid);
        String id = "";
        if (recordSet.next()){
            id = recordSet.getString("id");
        }
        return id;
    }

}
