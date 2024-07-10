package weaver.interfaces.tx.dms.action;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;

import java.util.HashMap;
import java.util.Map;

/**
 * FileName: InventoryCheckAction.java
 * 校验可用库存
 *
 * @Author tx
 * @Date 2023/9/19
 * @Version 1.00
 **/
public class InventoryCheckAction extends BaseBean implements Action  {

    private String dtTable; //需要校验的明细表，如明细表1和明细表2，则dt1,dt2
    private String fhdcField; //发货店仓字段名，若是主表则标识为“t.字段名”，若是明细表则标识为“dt1.字段名”
    private String hpbhField; //货品编号字段名，若是主表则标识为“t.字段名”，若是明细表则标识为“dt1.字段名”
    private String ddlField; //订单量字段名，若是主表则标识为“t.字段名”，若是明细表则标识为“dt1.字段名”

    @Override
    public String execute(RequestInfo requestInfo) {
        writeLog("开始执行InventoryCheckAction！");

        String wfTableName = requestInfo.getRequestManager().getBillTableName();//获取流程表单名称
        RequestManager requestManager = requestInfo.getRequestManager();
        String requestid = requestInfo.getRequestid();
        String msg = "";
        RecordSet rs = new RecordSet();
        RecordSetDataSource rsd = new RecordSetDataSource("bojun");
        String sql = "";

        Map<String,Double> ddlMap = new HashMap<>();

        //拼接流程表以及表关系，获取表单字段数据
        String[] dtTables = dtTable.split(",");
        String[] fhdcFields = fhdcField.split(",");
        String[] hpbhFields = hpbhField.split(",");
        String[] ddlFields = ddlField.split(",");


        for (int i = 0; i < dtTables.length; i++) {
            sql = "select " + fhdcFields[i] + " fhdc," + hpbhFields[i] + " hpbh," +
                    ddlFields[i] + " ddl from " + wfTableName + " t," + wfTableName + "_" + dtTables[i] + " " + dtTables[i] +
                    " where t.id = " + dtTables[i] + ".mainid and t.requestid = " + requestid;
            rs.executeQuery(sql);
            writeLog(dtTables[i] + "明细表校验查询sql：" + sql);
            while (rs.next()){
                String  qtycan = "";//可用库存
                rsd.execute("select QTYCAN from DMS_FA_STORAGE_CAN where STORE_CODE='"+
                        rs.getString("fhdc") + "' and SKUNO='" + rs.getString("hpbh") + "'");
                if(rsd.next()) qtycan = rsd.getString("QTYCAN");
                writeLog("商品编码为" + rs.getString("hpbh") + "的订单量：" + rs.getString("ddl") + "，可用量：" + qtycan);

                String key = rs.getString("fhdc")+"-"+rs.getString("hpbh");
                if(ddlMap.containsKey(key)){
                    double temp = ddlMap.get(key) + Util.getDoubleValue(rs.getString("ddl"));
                    ddlMap.put(key, temp);
                    if(Util.getDoubleValue(qtycan) < temp ){
                        msg = msg + "商品编码为" + rs.getString("hpbh") + "订货量大于可用库存不能提交! \r\n ";
                    }
                }else {
                    if(Util.getDoubleValue(qtycan) < Util.getDoubleValue(rs.getString("ddl"))){
                        msg = msg + "商品编码为" + rs.getString("hpbh") + "订货量大于可用库存不能提交! \r\n ";
                    }
                    ddlMap.put(key, Util.getDoubleValue(rs.getString("ddl")));
                }
            }
        }


        if(!msg.equals("")){
            requestManager.setMessageid("1000");
            requestManager.setMessagecontent(msg);
            return FAILURE_AND_CONTINUE;
        }

        return SUCCESS;
    }
}
