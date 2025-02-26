package weaver.interfaces.hzy.k3.pur.action;

import com.weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.pur.service.PurService;
import weaver.interfaces.hzy.k3.service.K3Service;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DailyNecAction extends BaseBean implements Action {

    private String apiId;

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("ִ�� DailyNecAction");

        PurService purService = new PurService();

        K3Service k3Service = new K3Service();

        Map<String, String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);

        List<Map<String, String>> detailDatas1 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);

        writeLog("mainData=" + mainData.toString());

        writeLog("detailDatas1=" + detailDatas1.toString());

        //���̱���
        String lcbh = mainData.get("lcbh");

        //��Ӧ��
        String gys = mainData.get("gys");

        //������
        String fhdc1 = mainData.get("fhdc1");

        //����ֿ�,������ϵͳ�ֶ����
        String ckck = fhdc1;

        //���ֿ�
        String rkck = mainData.get("rkck");

        //�ұ�
        String bb = mainData.get("bb");
        //���-̨��ұ�
        String hkBb = mainData.get("hk_bb");

        //�����̵������ڣ���ʱȡԤ�ƽ�����
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String pushDate = today.format(formatter);


        if("1".equals(apiId)){
            String code = purService.tranDailyNecCnSale(lcbh,gys,pushDate,ckck,bb,detailDatas1,k3Service,"0");
            if("200".equals(code)){
                k3Service.addLog(lcbh,"200");
                writeLog("ͬ�����-����Ʒ-���۳��ⵥ�ɹ�");
                return SUCCESS;
            }else {
                k3Service.addLog(lcbh,"500");
                writeLog("ͬ�����-����Ʒ-���۳��ⵥʧ��");
                return FAILURE_AND_CONTINUE;
            }
        }else if("2".equals(apiId)){
            String code = purService.tranHkPur_2(lcbh,gys,pushDate,rkck,bb,detailDatas1,k3Service,"0");
            if("200".equals(code)){
                k3Service.addLog(lcbh,"200");
                writeLog("ͬ�����-���-�ɹ����ɹ�");
                return SUCCESS;
            }else {
                k3Service.addLog(lcbh,"500");
                writeLog("ͬ�����-���-�ɹ����ⵥʧ��");
                return FAILURE_AND_CONTINUE;
            }
        }else if("3".equals(apiId)){
            String code = purService.tranHkSale_2(lcbh,gys,pushDate,rkck,hkBb,detailDatas1,k3Service,"0");
            if("200".equals(code)){
                k3Service.addLog(lcbh,"200");
                writeLog("ͬ�����-���-���۳��ⵥ�ɹ�");
                return SUCCESS;
            }else {
                k3Service.addLog(lcbh,"500");
                writeLog("ͬ�����-���-���۳��ⵥʧ��");
                return FAILURE_AND_CONTINUE;
            }
        }
        else {
            return SUCCESS;
        }
    }

}
