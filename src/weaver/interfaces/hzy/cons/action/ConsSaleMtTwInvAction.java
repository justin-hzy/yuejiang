package weaver.interfaces.hzy.cons.action;

import cn.hutool.core.collection.CollUtil;
import com.icbc.api.internal.apache.http.impl.cookie.S;
import org.xlsx4j.sml.Col;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.cons.service.ConsService;
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

public class ConsSaleMtTwInvAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("ִ��ConsSaleMtTwInvAction");

        String requestid = requestInfo.getRequestid();

        RequestManager requestManager = requestInfo.getRequestManager();

        int id = requestInfo.getRequestManager().getBillid();

        K3Service k3Service = new K3Service();

        WorkflowUtil workflowUtil = new WorkflowUtil();

        ConsService consService = new ConsService();

        List<Map<String, String>> hkSales = new ArrayList<>();

        List<Map<String,String>> twNotEnoughList = new ArrayList<>();

        /*��ȡ��������������*/
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        String kh = mainData.get("kh");
        //����
        String lx = mainData.get("lx");
        //��������
        String sqrq = mainData.get("sqrq");
        //���̱��
        String lcbh = mainData.get("lcbh");

        InventoryService inventoryService = new InventoryService();

        writeLog("mainData="+mainData.toString());

        Map<String,String> mainTableData = new HashMap<>();

        mainTableData.put("kh",kh);
        //��������Ϊ�������
        mainTableData.put("djrq",sqrq);

        mainTableData.put("shdc",kh);


        Map<String,List<Map<String,String>>> respMap = new HashMap<>();
        if("0".equals(lx)){
            String fhdcxs = mainData.get("fhdcxs");

            mainTableData.put("fhdc",fhdcxs);

            //�������� ��ѯ�ֶ�Ϊ sku��������������˰����
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
                //��˰����
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

            respMap = consService.compareInv(k3InvList,dtSums,inventoryService,lcbh);

        }

        if (respMap.containsKey("twNotEnoughList")){
            twNotEnoughList = respMap.get("twNotEnoughList");
            if (twNotEnoughList.size()>0){
                String errorMessage = "";
                writeLog("twNotEnoughList=" + twNotEnoughList.toString());
                for (Map<String,String> twNotEnough : twNotEnoughList){
                    String sku = twNotEnough.get("tm");
                    String qty = twNotEnough.get("qty");

                    errorMessage = errorMessage+"sku = "+sku+",qty= -"+qty+";";
                }

                String updateSql = "update formtable_main_238 set hk_status  = ?,is_tw_enough = ?,error_message = ? where requestid = ? ";
                writeLog("updateSql="+updateSql);
                RecordSet rs  = new RecordSet();
                rs.executeUpdate(updateSql,null,"1",errorMessage,requestid);
            }
        }else {
            if (respMap.containsKey("hkSales")){
                hkSales = respMap.get("hkSales");

                writeLog("hkSales=" + hkSales.toString());

                String updateSql = "update formtable_main_238 set hk_status  = ?,is_tw_enough = ? where requestid = ? ";

                RecordSet rs  = new RecordSet();

                if(hkSales.size()>0){
                    rs.executeUpdate(updateSql,"0","0",requestid);

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
                }
            }else if(hkSales.size()==0){
                String updateSql = "update formtable_main_238 set hk_status  = ?,is_tw_enough = ? where requestid = ? ";
                RecordSet rs  = new RecordSet();
                rs.executeUpdate(updateSql,"1","0",requestid);
            }
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
