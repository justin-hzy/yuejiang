package weaver.interfaces.hzy.resale.action;

import cn.hutool.core.collection.CollUtil;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.*;

public class CheckRefundAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("开始执行CheckRefundAction");
        List<Map<String, String>> detailDatas = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);
        String requestId = requestInfo.getRequestid();
        writeLog("detailDatas=" + detailDatas.toString());
        List<String> orderNos = new ArrayList<>();
        if (CollUtil.isNotEmpty(detailDatas)) {
            Set<String> hashSet = new HashSet<>();

            for (Map<String, String> detailData : detailDatas) {
                String khddh = detailData.get("khddh");
                if (!hashSet.contains(khddh)){
                    hashSet.add(khddh);
                }
            }
            writeLog("hashSet="+hashSet.toString());

            for (String orderNo : hashSet){
                String sql = "select * from formtable_main_263 main where khddh = ?";
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

            String updateSql = "update formtable_main_227 set cause = ? , is_rpt = ? where requestid = ?";
            RecordSet dtRs = new RecordSet();
            dtRs.executeUpdate(updateSql,cause,0,requestId);
        }else {
            String updateSql = "update formtable_main_227 set cause = '' ,is_rpt = ? where requestid = ?";
            RecordSet dtRs = new RecordSet();
            dtRs.executeUpdate(updateSql,1,requestId);
        }

        return SUCCESS;
    }
}
