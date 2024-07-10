package weaver.interfaces.hzy.k3.service;

import com.wbi.util.Util;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.pojo.SaleDt1;
import weaver.interfaces.hzy.k3.pojo.SaleDt2;
import weaver.interfaces.hzy.k3.pojo.SupSale;
import weaver.interfaces.tx.util.WorkflowUtil;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static weaver.interfaces.workflow.action.Action.FAILURE_AND_CONTINUE;
import static weaver.interfaces.workflow.action.Action.SUCCESS;

public class SaleService extends BaseBean {

    public String sale(RequestInfo requestInfo){

        RequestManager requestManager = requestInfo.getRequestManager();
        String requestid = requestInfo.getRequestid();
        ProDtService proDtService = new ProDtService();

        /*��ȡ��������������*/
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);


        InventoryService inventoryService = new InventoryService();

        /*�������*/
        String fhdc = mainData.get("fhdc");

        //��Ʒ��
        Map<String,List<SaleDt1>> dt1Map = proDtService.getProDt(requestid);


        //��Ʒ��
        Map<String,List<SaleDt2>> dt2Map = proDtService.getProDt2(requestid);



        //��Ʒ��ѯ��ʱ��棬��������������ɹ�������
        if (dt1Map.size()>0){

            String flag = "HK";

            writeLog(dt1Map.toString());

            Map<String,List<SupSale>> supMap = inventoryService.getInventory(dt1Map,fhdc);

            if(supMap.size()>0){
                writeLog("supMap="+supMap.toString());

                creatRequest(supMap,requestid,requestManager,flag,"pro");
            }else {
                requestManager.setMessageid("1000");
                requestManager.setMessagecontent("����ʧ�ܣ�����ϵϵͳ����Ա��ʧ��ԭ��" + "�����д���̨���Ʒ��治��");
                //return FAILURE_AND_CONTINUE;
            }
        }

        //��Ʒ��ѯ��ʱ��棬��������������ɹ�������
        if(dt2Map.size()>0){

            String flag = "HK";

            writeLog(dt2Map.toString());

            Map<String,List<SupSale>> supMap = inventoryService.getSapInventory(dt2Map ,fhdc);


            if (supMap.size()>0){
                writeLog("supMap="+supMap.toString());

                creatRequest(supMap,requestid,requestManager,flag,"sap");
            }else {
                requestManager.setMessageid("1000");
                requestManager.setMessagecontent("����ʧ�ܣ�����ϵϵͳ����Ա��ʧ��ԭ��" + "�����д���̨���Ʒ��治��");
                //return FAILURE_AND_CONTINUE;
            }
        }

        //��Ʒ̨��������ɳ��ⵥ
        if (dt1Map.size()>0){

            String flag = "TW";

            writeLog(dt1Map.toString());

            Map<String,List<SupSale>> supMap = inventoryService.getTWPro(dt1Map);

            writeLog("supMap="+supMap.toString());

            creatRequest(supMap,requestid,requestManager,flag,"pro");
        }

        //��Ʒ̨�����ɳ��ⵥ
        if (dt2Map.size()>0){

            String flag = "TW";

            writeLog(dt2Map.toString());

            Map<String,List<SupSale>> supMap = inventoryService.getTWSap(dt2Map);

            writeLog("supMap="+supMap.toString());

            creatRequest(supMap,requestid,requestManager,flag,"sap");
        }

        return SUCCESS;
    }


    public Map<String,String> getSaleMainData(String requestid,String cNumber,String proType){
        writeLog("ִ��getMainData");
        Map<String,String> mainTableData = new HashMap<>();

        //��ѯ���ݿ⣬��ȡ���۳�����������������
        RecordSet rsMain  = new RecordSet();
        String mainSql = "";
        if("pro".equals(proType)){
            mainSql = "select main.kh,main.ddrq,main.fhdc,main.shdc,main.hzhkjeddje,main.fhje,main.shzjey,dt1.fhrq,main.lclj,main.bb from formtable_main_226 main inner join formtable_main_226_dt1 dt1 on main.id = dt1.mainid where requestid = ? limit 0,1";
        }else if("sap".equals(proType)){
            mainSql = "select main.kh,main.ddrq,main.fhdc,main.shdc,main.hzhkjeddje,main.fhje,main.shzjey,dt2.fhrq,main.lclj,main.bb from formtable_main_226 main inner join formtable_main_226_dt2 dt2 on main.id = dt2.mainid where requestid = ? limit 0,1";
        }

        writeLog("mainSql="+mainSql);
        rsMain.executeQuery(mainSql,requestid);

        while (rsMain.next()){
            String kh = Util.null2String(rsMain.getString("kh"));
            String ddrq = Util.null2String(rsMain.getString("ddrq"));
            String fhdc_1 = Util.null2String(rsMain.getString("fhdc"));
            String shdc = Util.null2String(rsMain.getString("shdc"));
            String hzhkjeddje = Util.null2String(rsMain.getString("hzhkjeddje"));
            String fhje = Util.null2String(rsMain.getString("fhje"));
            String shzjey = Util.null2String(rsMain.getString("shzjey"));
            String fhrq = Util.null2String(rsMain.getString("fhrq"));
            String lclj = Util.null2String(rsMain.getString("lclj"));
            String bb = Util.null2String(rsMain.getString("bb"));

            mainTableData.put("kh",kh);
            mainTableData.put("djrq",ddrq);
            mainTableData.put("fhdc",fhdc_1);
            mainTableData.put("shdc",shdc);
            mainTableData.put("ddje",hzhkjeddje);
            mainTableData.put("chrq",fhrq);
            mainTableData.put("zlclj",lclj);
            mainTableData.put("bb",bb);

            /*writeLog("kh="+kh);
            writeLog("ddrq="+ddrq);
            writeLog("fhdc_1="+fhdc_1);
            writeLog("shdc="+shdc);
            writeLog("hzhkjeddje="+hzhkjeddje);
            writeLog("fhrq="+fhrq);*/


        }
        mainTableData.put("lcbh",cNumber);

        return mainTableData;
    }

    public String creatRequest(Map<String,List<SupSale>> supMap,String requestid,RequestManager requestManager,String flag,String proType){
        WorkflowUtil workflowUtil = new WorkflowUtil();
        //��ȡ���۳�����������ϸ��-��Ҫ����������
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
                String taxRate = supSale.getSellPrice();
                map.put("tm",hptxm);
                map.put("sl",String.valueOf(quantity));
                map.put("xsj",sellPrice);
                map.put("hplx",proType);
                mapList.add(map);
            }
            detail.put(String.valueOf(i),mapList);
            writeLog("detail="+detail.toString());

            //��ȡ������������Ϣ
            String cNumber = flag+"_"+key;
            Map<String,String> mainTableData  = getSaleMainData(requestid,cNumber,proType);

            writeLog("mainTableData="+mainTableData);

            int result = workflowUtil.creatRequest("1","165","TW_���۷���_���"+"�������̣�",mainTableData,detail,"1");//����������
            writeLog("result="+result);
            if(result == 0){
                requestManager.setMessageid("1000");
                requestManager.setMessagecontent("����������ʧ�ܣ�����ϵϵͳ����Ա��");
                return FAILURE_AND_CONTINUE;
            }
            writeLog("�����ɹ�������������id��" + result);
        }
        return SUCCESS;
    }

}
