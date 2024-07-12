package weaver.interfaces.hzy.k3.action;

import com.wbi.util.Util;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.pojo.SaleDt1;
import weaver.interfaces.hzy.k3.pojo.SaleDt2;
import weaver.interfaces.hzy.k3.pojo.SupSale;
import weaver.interfaces.hzy.k3.service.InventoryService;
import weaver.interfaces.hzy.k3.service.ProDtService;
import weaver.interfaces.tx.util.WorkflowUtil;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TWConsSubflowAction extends BaseBean implements Action {


    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("开始执行TWConsSubflowAction");

        RequestManager requestManager = requestInfo.getRequestManager();

        ProDtService proDtService = new ProDtService();

        String requestid = requestInfo.getRequestid();

        /*获取主流程主表数据*/
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        InventoryService inventoryService = new InventoryService();

        /*发货店仓*/
        String fhdc = mainData.get("fhdc");

        //正品拆单
        Map<String, List<SaleDt1>> dt1Map = getConDt1(requestid);

        if (dt1Map.size()>0){

            String flag = "TW";

            writeLog(dt1Map.toString());

            Map<String,List<SupSale>> supMap = inventoryService.getTWPro(dt1Map);

            writeLog("supMap="+supMap.toString());

            creatRequest(supMap,requestid,requestManager,flag);
        }

        return SUCCESS;
    }


    public Map<String, List<SaleDt1>> getConDt1(String requestid){

        RecordSet dtRs1 = new RecordSet();
        String dt1Sql = "select main.lcbh,dt1.wlbm hptxm,dt1.hsdj ddje,dt1.xssl fhl,dt1.taxrate from formtable_main_238_dt1 as dt1,formtable_main_238 main where dt1.mainid = main.id and main.requestid = ?";
        writeLog("dt1Sql="+dt1Sql);
        dtRs1.executeQuery(dt1Sql,requestid);

        Map<String,List<SaleDt1>> dt1Map = new HashMap<>();

        while (dtRs1.next()){
            //流程编号
            String lcbh = Util.null2String(dtRs1.getString("lcbh"));
            //sku
            String hptxm = Util.null2String(dtRs1.getString("hptxm"));
            //发货量
            Integer fhl = dtRs1.getInt("fhl");
            //含税单价
            String ddje = Util.null2String(dtRs1.getString("ddje"));
            //税率
            String taxrate = Util.null2String(dtRs1.getString("taxrate"));

            if(dt1Map.containsKey(lcbh)){
                List<SaleDt1> dt1List = dt1Map.get(lcbh);
                SaleDt1 saleDt1 = new SaleDt1();

                saleDt1.setFhl(fhl);
                saleDt1.setHptxm(hptxm);
                saleDt1.setSellPrice(ddje);
                saleDt1.setGg(taxrate);
                dt1List.add(saleDt1);
            }else {
                List<SaleDt1> dt1List = new ArrayList<>();
                SaleDt1 saleDt1 = new SaleDt1();

                saleDt1.setFhl(fhl);
                saleDt1.setHptxm(hptxm);
                saleDt1.setSellPrice(ddje);
                saleDt1.setGg(taxrate);
                dt1List.add(saleDt1);
                dt1Map.put(lcbh,dt1List);
            }
        }

        return dt1Map;
    }

    public String creatRequest(Map<String,List<SupSale>> supMap, String requestid, RequestManager requestManager, String flag){
        WorkflowUtil workflowUtil = new WorkflowUtil();
        //获取寄售出库主流程明细表-需要补货的数据
        for(String key : supMap.keySet()){
            List<SupSale> supSales = supMap.get(key);
            Map<String, List<Map<String, String>>> detail = new HashMap<>();
            int i = 1;
            List<Map<String, String>> mapList = new ArrayList<>();
            for(SupSale supSale : supSales){
                Map<String, String> map = new HashMap<>();
                String hptxm = supSale.getHptxm();
                Integer quantity = supSale.getQuantity();
                String sellPrice = supSale.getSellPrice();
                String taxRate = supSale.getTaxRate();
                map.put("tm",hptxm);
                map.put("sl",String.valueOf(quantity));
                map.put("xsj",sellPrice);
                map.put("taxRate",taxRate);
                //map.put("hplx",proType);
                mapList.add(map);
            }
            detail.put(String.valueOf(i),mapList);
            writeLog("detail="+detail.toString());

            //获取主流程主表信息
            String cNumber = flag+"_"+key;
            Map<String,String> mainTableData  = getConsMainData(requestid,cNumber);

            writeLog("mainTableData="+mainTableData);

            int result = workflowUtil.creatRequest("1","165","TW_寄售发货_金蝶"+"（子流程）",mainTableData,detail,"1");//创建子流程
            writeLog("result="+result);
            if(result == 0){
                requestManager.setMessageid("1000");
                requestManager.setMessagecontent("触发子流程失败，请联系系统管理员！");
                return FAILURE_AND_CONTINUE;
            }
            writeLog("触发成功的子流程请求id：" + result);
        }
        return SUCCESS;
    }

    public Map<String,String> getConsMainData(String requestid,String cNumber){
        writeLog("执行getMainData");
        Map<String,String> mainTableData = new HashMap<>();

        //查询数据库，获取销售出库主流程主表数据
        RecordSet rsMain  = new RecordSet();
        String mainSql = "";
        /*if("pro".equals(proType)){
            mainSql = "select main.kh,main.ddrq,main.fhdc,main.shdc,main.hzhkjeddje,main.fhje,main.shzjey,dt1.fhrq,main.lclj,main.bb,main.lcbh from formtable_main_226 main inner join formtable_main_226_dt1 dt1 on main.id = dt1.mainid where requestid = ? limit 0,1";
        }else if("sap".equals(proType)){
            mainSql = "select main.kh,main.ddrq,main.fhdc,main.shdc,main.hzhkjeddje,main.fhje,main.shzjey,dt2.fhrq,main.lclj,main.bb,main.lcbh from formtable_main_226 main inner join formtable_main_226_dt2 dt2 on main.id = dt2.mainid where requestid = ? limit 0,1";
        }*/

        mainSql = "select main.kh,main.ddrq,main.fhdcxs,main.shdcxs,main.fhjey,main.ddrq fhrq,main.lclj,main.lcbh from formtable_main_238 main inner join formtable_main_238_dt1 dt1 on main.id = dt1.mainid where requestid = ? limit 0,1";


        writeLog("mainSql="+mainSql);
        rsMain.executeQuery(mainSql,requestid);

        while (rsMain.next()){
            String kh = Util.null2String(rsMain.getString("kh"));
            String ddrq = Util.null2String(rsMain.getString("ddrq"));
            String fhdcxs = Util.null2String(rsMain.getString("fhdcxs"));
            String shdcxs = Util.null2String(rsMain.getString("shdcxs"));
            String fhjey = Util.null2String(rsMain.getString("fhjey"));
            String shzjey = Util.null2String(rsMain.getString("shzjey"));
            String lclj = Util.null2String(rsMain.getString("lclj"));
            String bb = Util.null2String(rsMain.getString("bb"));
            String lcbh = Util.null2String(rsMain.getString("lcbh"));

            mainTableData.put("kh",kh);
            mainTableData.put("djrq",ddrq);
            mainTableData.put("fhdc",fhdcxs);
            mainTableData.put("shdc",shdcxs);
            mainTableData.put("ddje",fhjey);
            mainTableData.put("zlclj",lclj);
            mainTableData.put("bb","PRE005");
            mainTableData.put("ydh",lcbh);

        }
        mainTableData.put("lcbh",cNumber);

        return mainTableData;
    }
}
