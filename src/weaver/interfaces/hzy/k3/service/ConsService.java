package weaver.interfaces.hzy.k3.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.pojo.SupSale;
import weaver.interfaces.tx.util.WorkflowUtil;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;

import java.text.SimpleDateFormat;
import java.util.*;

import static weaver.interfaces.workflow.action.Action.SUCCESS;

public class ConsService extends BaseBean {

    public void cons(RequestInfo requestInfo){

        String requestid = requestInfo.getRequestid();

        RequestManager requestManager = requestInfo.getRequestManager();

        K3Service k3Service = new K3Service();

        WorkflowUtil workflowUtil = new WorkflowUtil();


        /*��ȡ��������������*/
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        String kh = mainData.get("kh");
        //����
        String lx = mainData.get("lx");
        //��������
        String sqrq = mainData.get("sqrq");
        //���̱��
        String lcbh = mainData.get("lcbh");

        InventoryService inventoryService = new InventoryService();

        writeLog("mainData="+mainData.toString());

        Map<String,String> mainTableData = new HashMap<>();

        mainTableData.put("kh",kh);
        //��������Ϊ�������
        mainTableData.put("djrq",sqrq);

        mainTableData.put("shdc",kh);

        if("0".equals(lx)){
            String fhdcxs = mainData.get("fhdcxs");
            mainTableData.put("fhdc",fhdcxs);

            //�������� ��ѯ�ֶ�Ϊ sku��������������˰����
            String sql = "select dt1.wlbm tm,dt1.xssl sl,dt1.hsdj xsj from formtable_main_238 main inner join formtable_main_238_dt1 dt1 on main.id = dt1.mainid where main.requestId = ? ";

            RecordSet dtRs = new RecordSet();

            dtRs.executeQuery(sql,requestid);

            List<Map<String,String>> dtSums = new ArrayList<>();

            List<String> skus = new ArrayList<>();

            while (dtRs.next()){
                Map<String,String> dtSum = new HashMap<>();
                String tm = dtRs.getString("tm");
                String sl = dtRs.getString("sl");
                //��˰����
                String xsj = dtRs.getString("xsj");

                dtSum.put("tm",tm);
                dtSum.put("sl",sl);
                dtSum.put("xsj",xsj);

                skus.add(tm);
                dtSums.add(dtSum);
            }

            String respStr = inventoryService.getBatchInventory(skus,fhdcxs);

            List<Map<String,String>> k3InvList = anlysBatIn(respStr);

            writeLog("k3InvList="+k3InvList);

            List<Map<String,String>> hkSales = compareInv(k3InvList,dtSums,inventoryService,lcbh);


            writeLog("hkSales="+hkSales.toString());


            if(hkSales.size()>0){
                //����������۵���̨��ɹ���
                for (Map<String,String> hkSale : hkSales){
                    String tm = hkSale.get("tm");

                    String sellPrice = k3Service.queryPriceTable(tm);

                    sellPrice = sellPrice.substring(0,sellPrice.indexOf("."));

                    hkSale.put("xsj",sellPrice);
                }
                writeLog("hkSales="+hkSales.toString());

                Map<String, List<Map<String, String>>> hkDetail = new HashMap<>();

                hkDetail.put("1",hkSales);

                mainTableData.put("lcbh","HK_"+lcbh);

                mainTableData.put("zlclj","7");

                writeLog("mainTableData="+mainTableData.toString());

                writeLog("hkDetail="+hkDetail.toString());

                int result = workflowUtil.creatRequest("1","165","HK_���۷���_���"+"�������̣�",mainTableData,hkDetail,"1");

                writeLog("�����ɹ�������������id��" + result);

            }

            if (dtSums.size()>0){

                Map<String, List<Map<String, String>>> twDetail = new HashMap<>();

                mainTableData.put("lcbh","TW_"+lcbh);

                mainTableData.put("zlclj","7");

                writeLog("mainTableData="+mainTableData.toString());

                twDetail.put("1",dtSums);

                writeLog("twDetail="+twDetail.toString());



                int result = workflowUtil.creatRequest("1","165","TW_���۷���_���"+"�������̣�",mainTableData,twDetail,"1");
            }


        }else if("1".equals(lx)){
            //������

        }





    }



    public List<Map<String,String>> anlysBatIn(String respStr){

        List<Map<String,String>> k3InvList = new ArrayList<>();
        /*�����������*/
        JSONObject resJson = JSONObject.parseObject(respStr);

        JSONArray arr = resJson.getJSONArray("data");

        for (int i = 0 ;i<arr.size();i++){
            JSONObject jsonObj = arr.getJSONObject(i);

            String sku = jsonObj.getString("FMaterialId.fnumber");

            String fBaseQty = jsonObj.getString("FBaseQty");

            fBaseQty = fBaseQty.substring(0,fBaseQty.indexOf("."));

            Map<String,String> k3Inv = new HashMap<>();

            k3Inv.put("sku",sku);
            k3Inv.put("fBaseQty",fBaseQty);

            k3InvList.add(k3Inv);
        }

        return k3InvList;
    }

    public List<Map<String,String>> anlysBatIn(String respStr,List<String> skus){

        List<Map<String,String>> k3InvList = new ArrayList<>();
        /*�����������*/
        JSONObject resJson = JSONObject.parseObject(respStr);

        JSONArray arr = resJson.getJSONArray("data");

        for (int i = 0 ;i<arr.size();i++){
            JSONObject jsonObj = arr.getJSONObject(i);

            String sku = jsonObj.getString("FMaterialId.fnumber");

            String fBaseQty = jsonObj.getString("FBaseQty");

            fBaseQty = fBaseQty.substring(0,fBaseQty.indexOf("."));

            Map<String,String> k3Inv = new HashMap<>();

            k3Inv.put("sku",sku);
            k3Inv.put("fBaseQty",fBaseQty);

            k3InvList.add(k3Inv);
        }

        List<String> k3InvListSkus = new ArrayList<>();
        for (Map<String,String> k3Inv : k3InvList){
            k3InvListSkus.add(k3Inv.get("sku"));
        }


        for (String sku : skus){
            if(!k3InvListSkus.contains(sku)){
                Map<String,String> k3Inv = new HashMap<>();
                k3Inv.put("sku",sku);
                k3Inv.put("fBaseQty","0");
                k3InvList.add(k3Inv);
            }
        }

        return k3InvList;
    }


    public List<Map<String,String>> compareInv(List<Map<String,String>> k3InvList,List<Map<String,String>> dtSums, InventoryService inventoryService,String lcbh){

        List<Map<String,String>> hkSales = new ArrayList<>();
        writeLog("dtSums="+dtSums);
        for (Map<String,String> dtSum : dtSums) {

            String wlbm = dtSum.get("tm");

            String xssl = dtSum.get("sl");

            for (Map<String,String> k3Inv : k3InvList){
                String sku = k3Inv.get("sku");
                writeLog("sku="+sku);
                writeLog("wlbm="+wlbm);
                if(sku.equals(wlbm)){

                    Map<String,String> hkSale = new HashMap<>();
                    String fBaseQty = k3Inv.get("fBaseQty");


                    if(Integer.compare(Integer.valueOf(fBaseQty),Integer.valueOf(xssl)) < 0){
                        writeLog("�����������");

                        String szzt = inventoryService.getOrg(wlbm);


                        if("ZT026".equals(szzt)){
                            writeLog("���ֿ�治��");

                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String now = dateFormat.format(new Date());

                            //now = "'"+now+"'";

                            StringTokenizer tokenizer = new StringTokenizer(now, " ");

                            String dateString = tokenizer.nextToken();
                            String timeString = tokenizer.nextToken();

//                            dateString= "'"+dateString+"'";
//
//                            timeString= "'"+timeString+"'";
//                            lcbh = "'"+lcbh+"'";
//
                            sku = sku+"��治��";

                            String insertError = "insert into dms_k3_error_log (billNo,message,createTime,date,time) values ('" +lcbh+"','"+sku+"','"+now+"','"+dateString+"','"+timeString+"')";

                            RecordSet insertRs = new RecordSet();

                            insertRs.executeUpdate(insertError);
                        }else {
                            Integer hkNumber = Integer.valueOf(xssl) - Integer.valueOf(fBaseQty) ;
                            hkSale.put("tm",wlbm);
                            hkSale.put("sl",String.valueOf(hkNumber));
                            hkSales.add(hkSale);
                        }
                    }
                }
            }
        }
        return  hkSales;
    }
}
