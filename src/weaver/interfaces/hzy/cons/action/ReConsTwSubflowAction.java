package weaver.interfaces.hzy.cons.action;

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

public class ReConsTwSubflowAction extends BaseBean implements Action {

    /*寄售完结*/
    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("月结接受退执行ReConsSubflowAction");

        String requestid = requestInfo.getRequestid();

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        InventoryService inventoryService = new InventoryService();

        List<Map<String, String>> detailDatas1 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 2);


        writeLog("mainData=" + mainData.toString());

        writeLog("detailDatas=" + detailDatas1.toString());

        String lcbh = mainData.get("lcbh");
        //客户
        String kh = mainData.get("kh");
        //发货店仓
        String fhdcth = mainData.get("fhdcth");
        //收货店仓
        String shdcth = mainData.get("shdcth");
        //退货金额
        String thje = mainData.get("thje");
        //流程路径
        String lclj = mainData.get("lclj");
        //订单日期
        String ddrq = mainData.get("ddrq");

        Map<String,List<Map<String,String>>> twDtl = new HashMap<>();

        List<Map<String,String>> twList = new ArrayList<>();

        Map<String,List<Map<String,String>>> hkDtl = new HashMap<>();

        //List<Map<String,String>> hkList = new ArrayList<>();

        WorkflowUtil workflowUtil = new WorkflowUtil();

        if(detailDatas1.size()>0){

            String dt1Sql = "select dt2.wlbm tm,dt2.thsl sl,dt2.hsdj xsj from formtable_main_238 main inner join formtable_main_238_dt2 dt2 on main.id = dt2.mainid  where requestId = ?";

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
                /*if("ZT021".equals(org)){
                    hkList.add(map);
                }*/
                twList.add(map);
            }

            //writeLog("hkList="+hkList.toString());

            writeLog("twList="+twList.toString());



            /*if (hkList.size()>0){
                Map<String,String> mainTableData = new HashMap<>();
                mainTableData.put("zlclj",lclj);
                String hklcbh = "HK_"+lcbh;
                mainTableData.put("lcbh",hklcbh);
                mainTableData.put("kh",kh);
                mainTableData.put("shdc",shdcth);
                mainTableData.put("fhdc",fhdcth);
                //单据日期 = 订单日期
                mainTableData.put("djrq",ddrq);
                //币别
                mainTableData.put("bb","PRE005");

                mainTableData.put("ydh",lcbh);

                hkDtl.put("1",hkList);

                writeLog("mainTableData="+mainTableData.toString());
                writeLog("hkDtl="+hkDtl.toString());

                int result = workflowUtil.creatRequest("1","165","HK_寄售退货_金蝶子流程",mainTableData,hkDtl,"1");
                writeLog("触发成功的子流程请求id：" + result);
            }*/

            if (twList.size()>0){
                Map<String,String> mainTableData = new HashMap<>();
                mainTableData.put("zlclj",lclj);
                String twlcbh = "TW_"+lcbh;
                mainTableData.put("lcbh",twlcbh);
                mainTableData.put("kh",kh);
                mainTableData.put("shdc",shdcth);
                mainTableData.put("fhdc",fhdcth);
                //单据日期 = 订单日期
                mainTableData.put("djrq",ddrq);
                //币别
                mainTableData.put("bb","PRE005");
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
