package weaver.interfaces.hzy.tha.refund;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.*;

public class ThRefundCheckAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("��ʼִ��ThRefundCheckAction");

        List<Map<String, String>> detailDatas1 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);

        String requestId = requestInfo.getRequestid();

        writeLog("detailDatas=" + detailDatas1.toString());

        List<String> orderNos = new ArrayList<>();

        if (detailDatas1.size() > 0) {
            Set<String> hashSet = new HashSet<>();

            for (Map<String, String> detailData : detailDatas1) {
                String erpOrder = detailData.get("erp_order");
                if (!hashSet.contains(erpOrder)){
                    hashSet.add(erpOrder);
                }
            }

            writeLog("hashSet="+hashSet.toString());

            for (String orderNo : hashSet){
                String sql = "select * from formtable_main_348 main where erp_order = ?";
                RecordSet dtRs = new RecordSet();
                dtRs.executeQuery(sql, orderNo);

                if(dtRs.next()){
                    orderNos.add(orderNo);
                }
            }
            writeLog("orderNos="+orderNos.toString());

            if(orderNos.size()>0){
                String cause = "";

                for (String orderNo :  orderNos){
                    cause = cause + orderNo+",";
                }

                cause = cause + "�ظ�����";

                writeLog("cause="+cause);

                String updateSql = "update formtable_main_355 set cause = ? , isRpt = ? where requestid = ?";
                RecordSet dtRs = new RecordSet();
                dtRs.executeUpdate(updateSql,cause,0,requestId);
            }else {
                String updateSql = "update formtable_main_355 set cause = '' ,isRpt = ? where requestid = ?";
                RecordSet dtRs = new RecordSet();
                dtRs.executeUpdate(updateSql,1,requestId);
            }
        }

        return SUCCESS;
    }
}
