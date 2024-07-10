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

        /*�����½���*/
        writeLog("ִ�� ReSaleAction");

        String requestid = requestInfo.getRequestid();

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        InventoryService inventoryService = new InventoryService();

        List<Map<String, String>> detailDatas1 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);

        List<Map<String, String>> detailDatas2 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 2);


        writeLog("mainData=" + mainData.toString());

        writeLog("detailDatas1=" + detailDatas1.toString());

        writeLog("detailDatas2=" + detailDatas2.toString());

        //���̱���
        String lcbh = mainData.get("lcbh");
        //�ͻ�
        String kh = mainData.get("kh");

        //�������
        String fhdc = mainData.get("fhdc");
        //�ջ����
        String shdc = mainData.get("shdc");
        //�˻������
        String thrkje = mainData.get("thrkje");
        //����·��
        String lclj = mainData.get("lclj");

        //�ұ�
        String bb = mainData.get("bb");

        Map<String,List<Map<String,String>>> twDtl = new HashMap<>();

        List<Map<String,String>> twList = new ArrayList<>();

        Map<String,List<Map<String,String>>> hkDtl = new HashMap<>();

        List<Map<String,String>> hkList = new ArrayList<>();

        WorkflowUtil workflowUtil = new WorkflowUtil();

        if(detailDatas1.size()>0){

            //�������
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

                //����
                map.put("tm",tm);
                //����
                map.put("sl",sl);
                //��˰����
                map.put("xsj",xsj);
                //˰��
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
                //�������� = ��������
                mainTableData.put("djrq",rkrq);
                //�ұ�
                mainTableData.put("bb",bb);

                mainTableData.put("ydh",lcbh);

                hkDtl.put("1",hkList);

                writeLog("mainTableData="+mainTableData.toString());
                writeLog("hkDtl="+hkDtl.toString());

                int result = workflowUtil.creatRequest("1","165","HK_�����˻�_���������",mainTableData,hkDtl,"1");
                writeLog("�����ɹ�������������id��" + result);
            }

            if (twList.size()>0){
                Map<String,String> mainTableData = new HashMap<>();
                mainTableData.put("zlclj",lclj);
                String twlcbh = "TW_"+lcbh;
                mainTableData.put("lcbh",twlcbh);
                mainTableData.put("kh",kh);
                mainTableData.put("shdc",shdc);
                mainTableData.put("fhdc",fhdc);
                //�������� = ��������
                mainTableData.put("djrq",rkrq);
                //�ұ�
                mainTableData.put("bb",bb);
                mainTableData.put("ydh",lcbh);

                twDtl.put("1",twList);

                writeLog("mainTableData="+mainTableData.toString());
                writeLog("twDtl="+twDtl.toString());
                int result = workflowUtil.creatRequest("1","165","TW_�����˻�_���������",mainTableData,twDtl,"1");
                writeLog("�����ɹ�������������id��" + result);
            }
        }else {
            writeLog("���̱��"+lcbh+"��û����ϸ����");
        }



        return SUCCESS;
    }
}
