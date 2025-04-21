package weaver.interfaces.hzy.commk3.pur;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.pur.service.PurService;
import weaver.interfaces.hzy.k3.service.K3Service;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.util.List;
import java.util.Map;

public class AbroadPurAction extends BaseBean implements Action {

    private String tableName;

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("ִ�к���ɹ�Action");

        K3Service k3Service = new K3Service();

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        List<Map<String, String>> detailDatas1 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);

        writeLog("mainData=" + mainData.toString());

        writeLog("detailDatas1=" + detailDatas1.toString());

        int id = requestInfo.getRequestManager().getBillid();

        //���̱���
        String lcbh = mainData.get("lcbh");

        //��Ӧ��
        String gys = mainData.get("gys");

        //���ֿ�
        String rkck = mainData.get("rkck");

        //�ұ�
        String bb = mainData.get("bb");

        //�����̵������ڣ���ʱȡԤ�ƽ�����
        String rkrq = mainData.get("rkrq");


        PurService purService = new PurService();

        String area = mainData.get("area");


        String org = "";

        if("237".equals(area)){
            org = "ZT029";
        }else if("216".equals(area)){
            org = "ZT031";
        }

        if (StrUtil.isNotEmpty(org)){
            JSONObject respJson = purService.tranHkPur_3(lcbh,org,gys,rkrq,rkck,bb,detailDatas1,k3Service,"0");
            String code = respJson.getString("code");
            if("200".equals(code)){
                String updateSql = "update "+tableName+" set is_next = ? where id = ? ";
                RecordSet updateRs = new RecordSet();
                updateRs.executeUpdate(updateSql,0,id);
            }else {
                String message = respJson.getString("message");
                String updateSql = "update "+tableName+" set is_next = ?,error_message = ? where id = ? ";
                RecordSet updateRs = new RecordSet();
                updateRs.executeUpdate(updateSql,1,message,id);
            }
        }else {
            String message = "��֯Ϊ�գ��޷��ύ";
            writeLog(message);
            String updateSql = "update "+tableName+" set is_next = ? , error_message = ? where id = ? ";
            RecordSet updateRs = new RecordSet();
            updateRs.executeUpdate(updateSql,1,message,id);
        }

        return SUCCESS;
    }
}
