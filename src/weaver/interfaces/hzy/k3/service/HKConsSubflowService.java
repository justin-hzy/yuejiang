package weaver.interfaces.hzy.k3.service;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.tx.util.WorkflowUtil;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HKConsSubflowService extends BaseBean {

    public void getHkConsSaleDt(RequestInfo requestInfo){

        writeLog("开始执行getHkConsSaleDt");

        WorkflowUtil workflowUtil = new WorkflowUtil();

        String requestid = requestInfo.getRequestid();

        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        List<Map<String, String>> detailDatas1 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);

        writeLog("mainData="+mainData.toString());

        writeLog("detailData="+detailDatas1.toString());

        if(detailDatas1.size()>0){
            String fhdc = mainData.get("fhdcxs");

            String shdc = mainData.get("shdcxs");
            String lcbh = mainData.get("lcbh");
            String lclj = mainData.get("lclj");

            String ddrq = mainData.get("ddrq");

            List<String> skus = new ArrayList<>();

            //寄售销售 查询字段为 sku、销售数量、含税单价
            String sql = "select dt3.tm tm,dt3.sl sl from formtable_main_238 main inner join formtable_main_238_dt3 dt3 on main.id = dt3.mainid where main.requestId = ?";

            RecordSet dtRs = new RecordSet();

            dtRs.executeQuery(sql,requestid);

            List<Map<String,String>> dtSums = new ArrayList<>();

            while (dtRs.next()){
                Map<String,String> dtSum = new HashMap<>();
                String tm = dtRs.getString("tm");
                String sl = dtRs.getString("sl");
                //含税单价
                //String xsj = dtRs.getString("xsj");

                dtSum.put("tm",tm);
                dtSum.put("sl",sl);
                //dtSum.put("xsj",xsj);

                skus.add(tm);
                dtSums.add(dtSum);
                //skus.add(tm);
            }

            writeLog("dtSums="+dtSums);

            if (dtSums.size()>0){
                writeLog("生成香港寄售出库单");
                Map<String,String> mainTableData = new HashMap<>();

                Map<String, List<Map<String, String>>> twDetail = new HashMap<>();

                String kh = mainData.get("kh");
                String bb  = "PRE005";

                mainTableData.put("kh",kh);
                //单据日期为入库日期
                String fhrq = ddrq;

                mainTableData.put("chrq",fhrq);

                mainTableData.put("djrq",ddrq);

                mainTableData.put("shdc",shdc);

                mainTableData.put("lcbh","HK_"+lcbh);

                mainTableData.put("zlclj",lclj);


                mainTableData.put("bb",bb);

                mainTableData.put("fhdc",fhdc);

                mainTableData.put("zlcqqid",requestid);

                mainTableData.put("ydh",lcbh);

                writeLog("mainTableData="+mainTableData.toString());

                twDetail.put("1",dtSums);

                writeLog("twDetail="+twDetail.toString());

                int result = workflowUtil.creatRequest("1","165","HK_寄售发货_金蝶"+"（子流程）",mainTableData,twDetail,"1");
                writeLog("触发成功的子流程请求id：" + result);

                lcbh = "HK_"+lcbh;

                String id = getConsSaleId(requestid);

                String insertSql = "insert formtable_main_238_dt4 (mainid,jdzlcid,jdzlcbh,jdzlcsfgd) values ('"+id+"','"+result+"','"+lcbh+"','"+1+"')";

                RecordSet rs1 = new RecordSet();
                rs1.executeUpdate(insertSql);
            }
        }
    }

    public String getConsSaleId(String requestid){
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
