package weaver.interfaces.hzy.k3.service;


import com.icbc.api.internal.apache.http.impl.cookie.S;
import com.wbi.util.Util;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.pojo.ReSaleDt1;
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

public class ReSaleService extends BaseBean {

    public String reSale(RequestInfo requestInfo){

        writeLog("ִ��reSale");

        RequestManager requestManager = requestInfo.getRequestManager();

        String requestid = requestInfo.getRequestid();

        /*��ȡ��������������*/
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        /*�������*/
        String fhdc = mainData.get("fhdc");


        List<ReSaleDt1> nullDt1 = matchNullOrg(requestid);


        if(nullDt1.size()>0){
            //�˻������ϸ
            List<ReSaleDt1> reHKDt1s = getReDt(requestid,"HK");

            //�˻�̨����ϸ
            List<ReSaleDt1> reTWDt1s = getReDt(requestid,"TW");

            writeLog("reHKDt1s="+reHKDt1s);
            writeLog("reTWDt1s="+reTWDt1s);

            /*if (reHKDt1s.size()>0){
                String flag = "HK";
                Map<String,List<SupSale>> supMap =  getSupMap(reHKDt1s);

                writeLog("supMap="+supMap.toString());

                creatRequest(supMap,requestid,requestManager,flag);
            }


            if (reTWDt1s.size()>0){
                String flag = "TW";
                Map<String,List<SupSale>> supMap =  getSupMap(reHKDt1s);
                writeLog("supMap="+supMap.toString());
                creatRequest(supMap,requestid,requestManager,flag);
            }*/
        }else {
            requestManager.setMessageid("1000");
            requestManager.setMessagecontent("����ʧ�ܣ�����ϵϵͳ����Ա��ʧ��ԭ��" + "��ǰ�˻���ϸ�д���δ֪������֯�Ļ�Ʒ!");
            return FAILURE_AND_CONTINUE;
        }





        return SUCCESS;

    }

    public List<ReSaleDt1> getReDt(String requestid,String flag){
        String sql = null;

        if("HK".equals(flag)){
            sql = "select dt1.hptxm,dt1.fhl,dt1.zfhje dj ,main.lcbh from formtable_main_227 main inner join formtable_main_227_dt1 dt1 on main.id = dt1.mainid inner join uf_spk spk on dt1.hptxm = spk.txm where main.requestid = ? and spk.szzt = 'ZT021' ";
        }else if("TW".equals(flag)){
            sql = "select dt1.hptxm,dt1.fhl,dt1.ddje dj ,main.lcbh from formtable_main_227 main inner join formtable_main_227_dt1 dt1 on main.id = dt1.mainid inner join uf_spk spk on dt1.hptxm = spk.txm where main.requestid = ? and spk.szzt = 'ZT026'";
        }

        RecordSet rs = new RecordSet();
        List<ReSaleDt1> reDt1s = new ArrayList<>();
        if (sql != null){
            rs.executeQuery(sql,requestid);

            while (rs.next()){

                ReSaleDt1 reDt1 = new ReSaleDt1();
                //sku
                String hptxm = rs.getString("hptxm");
                reDt1.setHptxm(hptxm);
                //�����
                String fhl = rs.getString("fhl");
                reDt1.setFhl(fhl);
                //��װ����˰���۸���˰���ۣ�ִ��hksql����
                String dj = rs.getString("dj");
                reDt1.setDj(dj);
                //���̱��
                String lcbh = rs.getString("lcbh");
                reDt1.setLcbh(lcbh);

                //�ұ�
                String bb = rs.getString("bb");
                reDt1.setBb(bb);

                reDt1s.add(reDt1);
            }
        }
        return reDt1s;
    }


    public Map<String,List<SupSale>> getSupMap(List<ReSaleDt1> reDt1s){

        Map<String,List<SupSale>> supMap = new HashMap<>();

        for (ReSaleDt1 reDt1 : reDt1s){
            SupSale supSale = new SupSale();
            supSale.setQuantity(Integer.valueOf(reDt1.getFhl()));
            supSale.setHptxm(reDt1.getHptxm());
            supSale.setSellPrice(reDt1.getDj());
            String lcbh = reDt1.getLcbh();

            if (supMap.containsKey(lcbh)){
                List<SupSale> supSales = supMap.get(lcbh);
                supSales.add(supSale);
            }else {
                List<SupSale> supSales = new ArrayList<>();
                supSales.add(supSale);
                supMap.put(lcbh,supSales);
            }
        }

        return supMap;
    }

    public void creatRequest(Map<String,List<SupSale>> supMap,String requestid,RequestManager requestManager,String flag){
        WorkflowUtil workflowUtil = new WorkflowUtil();

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
                mapList.add(map);
            }
            detail.put(String.valueOf(i),mapList);
            writeLog("detail="+detail.toString());


            //��ȡ������������Ϣ
            String cNumber = flag+"_"+key;
            Map<String,String> mainTableData  = getReSaleMainData(requestid,cNumber);

            writeLog("mainTableData="+mainTableData);

            int result = workflowUtil.creatRequest("1","165","TW_�����˻�_���"+"�������̣�",mainTableData,detail,"1");//����������

            if(result == 0){
                requestManager.setMessageid("1000");
                requestManager.setMessagecontent("����������ʧ�ܣ�����ϵϵͳ����Ա��");
                //return FAILURE_AND_CONTINUE;
            }
            writeLog("�����ɹ�������������id��" + result);
        }
        //return null;
    }

    public Map<String,String> getReSaleMainData(String requestid,String cNumber){
        writeLog("ִ��getReSaleMainData");
        Map<String,String> mainTableData = new HashMap<>();
        //��ѯ���ݿ⣬��ȡ���۳�����������������
        RecordSet rsMain  = new RecordSet();
        String mainSql = "select main.kh,main.fhdc,main.shdc,dt2.rkrq,main.bb from formtable_main_227 main inner join formtable_main_227_dt2 dt2 on main.id = dt2.mainid where requestid = ? limit 0,1";

        writeLog("mainSql="+mainSql);
        rsMain.executeQuery(mainSql,requestid);

        while (rsMain.next()){
            String kh = Util.null2String(rsMain.getString("kh"));
            String rkrq = Util.null2String(rsMain.getString("rkrq"));
            String fhdc = Util.null2String(rsMain.getString("fhdc"));
            String shdc = Util.null2String(rsMain.getString("shdc"));
            String bb = Util.null2String(rsMain.getString("bb"));

            mainTableData.put("kh",kh);
            //��������Ϊ�������
            mainTableData.put("djrq",rkrq);
            mainTableData.put("fhdc",fhdc);
            mainTableData.put("shdc",shdc);
            mainTableData.put("bb",bb);
        }
        mainTableData.put("lcbh",cNumber);
        mainTableData.put("zlclj","5");

        return mainTableData;
    }

    public List<ReSaleDt1> matchNullOrg(String requestid){

        String sql = "select dt1.hptxm,dt1.fhl,dt1.zfhje dj ,main.lcbh from formtable_main_227 main inner join formtable_main_227_dt1 dt1 on main.id = dt1.mainid inner join uf_spk spk on dt1.hptxm = spk.txm where main.requestid = ? and spk.szzt is null";

        RecordSet recordSet  = new RecordSet();

        recordSet.executeQuery(sql,requestid);

        List<ReSaleDt1> nullDt1 = new ArrayList<>();

        while (recordSet.next()){
            while (recordSet.next()){

                ReSaleDt1 reDt1 = new ReSaleDt1();
                //sku
                String hptxm = recordSet.getString("hptxm");
                reDt1.setHptxm(hptxm);
                //�����
                String fhl = recordSet.getString("fhl");
                reDt1.setFhl(fhl);
                //��װ����˰���۸���˰���ۣ�ִ��hksql����
                String dj = recordSet.getString("dj");
                reDt1.setDj(dj);
                //���̱��
                String lcbh = recordSet.getString("lcbh");
                reDt1.setLcbh(lcbh);

                //�ұ�
                String bb = recordSet.getString("bb");
                reDt1.setBb(bb);

                nullDt1.add(reDt1);
            }
        }
        return nullDt1;

    }

}
