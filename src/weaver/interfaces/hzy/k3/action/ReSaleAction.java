package weaver.interfaces.hzy.k3.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.service.InventoryService;
import weaver.interfaces.tx.util.WorkflowUtil;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReSaleAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {

        /*寄售月结退*/
        writeLog("执行 ReSaleAction");

        String requestid = requestInfo.getRequestid();

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        InventoryService inventoryService = new InventoryService();

        List<Map<String, String>> detailDatas1 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);

        List<Map<String, String>> detailDatas2 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 2);


        writeLog("mainData=" + mainData.toString());

        writeLog("detailDatas1=" + detailDatas1.toString());

        writeLog("detailDatas2=" + detailDatas2.toString());

        //流程编码
        String lcbh = mainData.get("lcbh");
        //客户
        String kh = mainData.get("kh");

        //发货店仓
        String fhdc = mainData.get("fhdc");
        //收货店仓
        String shdc = mainData.get("shdc");
        //退货入库金额
        String thrkje = mainData.get("thrkje");
        //流程路径
        String lclj = mainData.get("lclj");

        //币别
        String bb = mainData.get("bb");

        Map<String,List<Map<String,String>>> twDtl = new HashMap<>();

        List<Map<String,String>> twList = new ArrayList<>();

        Map<String,List<Map<String,String>>> hkDtl = new HashMap<>();

        List<Map<String,String>> hkList = new ArrayList<>();

        WorkflowUtil workflowUtil = new WorkflowUtil();

        if(detailDatas1.size()>0){

            //入库日期
            String rkrq = "";
            if (detailDatas1.size()>0){
                rkrq = detailDatas1.get(0).get("rkrq");
            }

            String dt1Sql = "select dt1.hptxm tm,dt1.fhl sl,dt1.ddje xsj,dt1.rkrq from formtable_main_227 main inner join formtable_main_227_dt1 dt1 on main.id = dt1.mainid  where requestId = ?";

            RecordSet dt1Rs = new RecordSet();

            dt1Rs.executeQuery(dt1Sql,requestid);


            while (dt1Rs.next()){
                Map<String,String> map = new HashMap<>();
                String tm = dt1Rs.getString("tm");
                String sl = dt1Rs.getString("sl");
                String xsj = dt1Rs.getString("xsj");

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
                twList.add(map);
            }

            writeLog("hkList="+hkList.toString());

            writeLog("twList="+twList.toString());

            if (hkList.size()>0){
                Map<String,String> mainTableData = new HashMap<>();
                mainTableData.put("zlclj",lclj);
                String hklcbh = "HK_"+lcbh;
                mainTableData.put("lcbh",hklcbh);
                mainTableData.put("kh",kh);
                mainTableData.put("shdc",shdc);
                mainTableData.put("fhdc",fhdc);
                //单据日期 = 订单日期
                mainTableData.put("djrq",rkrq);
                //币别
                mainTableData.put("bb",bb);

                mainTableData.put("ydh",lcbh);

                hkDtl.put("1",hkList);

                writeLog("mainTableData="+mainTableData.toString());
                writeLog("hkDtl="+hkDtl.toString());

                int result = workflowUtil.creatRequest("1","165","HK_寄售退货_金蝶子流程",mainTableData,hkDtl,"1");
                writeLog("触发成功的子流程请求id：" + result);
            }

            if (twList.size()>0){
                Map<String,String> mainTableData = new HashMap<>();
                mainTableData.put("zlclj",lclj);
                String twlcbh = "TW_"+lcbh;
                mainTableData.put("lcbh",twlcbh);
                mainTableData.put("kh",kh);
                mainTableData.put("shdc",shdc);
                mainTableData.put("fhdc",fhdc);
                //单据日期 = 订单日期
                mainTableData.put("djrq",rkrq);
                //币别
                mainTableData.put("bb",bb);
                mainTableData.put("ydh",lcbh);

                twDtl.put("1",twList);

                writeLog("mainTableData="+mainTableData.toString());
                writeLog("twDtl="+twDtl.toString());
                int result = workflowUtil.creatRequest("1","165","TW_寄售退货_金蝶子流程",mainTableData,twDtl,"1");
                writeLog("触发成功的子流程请求id：" + result);
            }
        }else {
            writeLog("流程编号"+lcbh+"，没有明细数据");
        }



        return SUCCESS;
    }
}
