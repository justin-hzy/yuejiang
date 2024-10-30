package weaver.interfaces.hzy.k3.sale.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.*;

public class CheckSaleAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("开始执行CheckSaleAction");

        List<Map<String, String>> detailDatas1 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);

        List<Map<String, String>> detailDatas2 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 2);

        String requestId = requestInfo.getRequestid();

        writeLog("detailDatas=" + detailDatas1.toString());

        List<String> orderNos = new ArrayList<>();

        if (detailDatas1.size() > 0) {

            Set<String> hashSet = new HashSet<>();

            for (Map<String, String> detailData : detailDatas1) {
                String khddh = detailData.get("khddh");
                if (!hashSet.contains(khddh)){
                    hashSet.add(khddh);
                }
            }


            writeLog("hashSet="+hashSet.toString());

            for (String orderNo : hashSet){
                String sql = "select * from formtable_main_272 main where fddh = ?";
                RecordSet dtRs = new RecordSet();
                dtRs.executeQuery(sql, orderNo);

                if(dtRs.next()){
                    orderNos.add(orderNo);
                }
            }
            writeLog("orderNos="+orderNos.toString());
        }


        if (detailDatas2.size() > 0) {
            Set<String> hashSet = new HashSet<>();

            for (Map<String, String> detailData : detailDatas2) {
                String khddh = detailData.get("khddh");
                if (!hashSet.contains(khddh)){
                    hashSet.add(khddh);
                }
            }

            for (String orderNo : hashSet){
                String sql = "select * from formtable_main_272 main where fddh = ?";
                RecordSet dtRs = new RecordSet();
                dtRs.executeQuery(sql, orderNo);

                if(dtRs.next()){
                    orderNos.add(orderNo);
                }
            }
            writeLog("orderNos="+orderNos.toString());
        }

        if(orderNos.size()>0){
            String cause = "";

            for (String orderNo :  orderNos){
                cause = cause + orderNo+",";
            }

            cause = cause + "重复订单";

            writeLog("cause="+cause);

            String updateSql = "update formtable_main_226 set cause = ? , isRpt = ? where requestid = ?";
            RecordSet dtRs = new RecordSet();
            dtRs.executeUpdate(updateSql,cause,0,requestId);
        }else {
            String updateSql = "update formtable_main_226 set cause = '' ,isRpt = ? where requestid = ?";
            RecordSet dtRs = new RecordSet();
            dtRs.executeUpdate(updateSql,1,requestId);
        }

        return SUCCESS;
    }

}
