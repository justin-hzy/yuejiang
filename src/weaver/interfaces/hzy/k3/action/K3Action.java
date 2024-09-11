package weaver.interfaces.hzy.k3.action;

import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.service.K3Service;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Map;

public class K3Action extends BaseBean implements Action {

    private String apiId;

    /*ҵ������*/
    private String type;

    private String is_gyj;

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("��ʼִ��hzy-K3Action��");

        K3Service k3Service = new K3Service();

        String requestid = requestInfo.getRequestid();

        //��ȡ����������ϸ������
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        writeLog("mainData="+mainData);
        String lcbh = mainData.get("lcbh");
        if("sale".equals(type)){
            if(lcbh != null){
                if(lcbh.contains("HK_")){
                    String code = k3Service.putSale(requestid,"HK");
                    writeLog("code="+code);
                    if("200".equals(code)){
                        code = k3Service.putPur(requestid,"TW");
                        writeLog("code="+code);
                        writeLog("���̺�"+lcbh+"������۳��ⵥ��ͬ���ɹ�");
                        if("200".equals(code)){
                            // todo ����ME�ӿ�ʵ���ύ�ڵ㹦��
                            writeLog("���̺�"+lcbh+"̨��ɹ�����ͬ���ɹ�");
                            return SUCCESS;
                        }else {
                            writeLog("���̺�"+lcbh+"̨��ɹ�����ͬ��ʧ��");
                            return FAILURE_AND_CONTINUE;
                        }
                    }else {
                        writeLog("���̺�"+lcbh+"������۳��ⵥ��ͬ��ʧ��");
                        return FAILURE_AND_CONTINUE;
                    }
                }else if(lcbh.contains("TW_")) {
                    String code = k3Service.putSale(requestid,"TW");
                    if("200".equals(code)){
                        return SUCCESS;
                    }else {
                        return FAILURE_AND_CONTINUE;
                    }
                }
            }
        }else if("trf".equals(type)){
            if(lcbh != null){
                if(lcbh.contains("HK_")){
                    String code = k3Service.putTrf(requestid,mainData,"HK");

                }else if (lcbh.contains("TW_")){
                    k3Service.putTrf(requestid,mainData,"TW");
                }
            }
        }else if("cons".equals(type)){
            if(lcbh != null){
                if(lcbh.contains("HK_")){
                    String code = k3Service.putConsSale(requestid,"HK");
                    if("200".equals(code)){
                        k3Service.putConsPur(requestid,"TW");
                    }
                    //k3Service.putConsPur(requestid,"TW");
                }else if (lcbh.contains("TW_")){
                    k3Service.putConsSale(requestid,"TW");
                }
            }
        }else if("resale".equals(type)){
            if(lcbh != null){
                if ("false".equals(is_gyj)){
                    if(lcbh.contains("HK_")){
                        String code = k3Service.putRePur(requestid,"HK");
                        if("200".equals(code)){
                            k3Service.putReSale(requestid,"HK");
                        }
                    }else if(lcbh.contains("TW_")){
                        k3Service.putReSale(requestid,"TW");
                    }
                }else if ("true".equals(is_gyj)){
                    if(lcbh.contains("GYJ_")){
                        //�ͻ�->���ý�������
                        k3Service.putGyjReSale(requestid,"GYJ");
                    }else if(lcbh.contains("GYJTW_")){
                        writeLog("ִ�й��ý���̨����߼�");
                        //���ý��ɹ���
                        String code = k3Service.putGYJRePur(requestid,"GYJ");
                        writeLog("code="+code);
                        if("200".equals(code)){
                            //̨��������
                            k3Service.putGyjReSale(requestid,"GYJTW");
                        }
                    }else if(lcbh.contains("GYJHK_")){
                        //̨��ɹ���
                        String code = k3Service.putGYJRePur(requestid,"GYJTW");
                        if("200".equals(code)){
                            //���������
                            k3Service.putGyjReSale(requestid,"GYJHK");
                        }
                    }
                }
            }
        }else if("recons".equals(type)){
            if(lcbh != null){
                if(lcbh.contains("HK_")){
                    String code = k3Service.putRePur(requestid,"HK");
                    if("200".equals(code)){
                        k3Service.putReSale(requestid,"HK");
                    }
                }else if(lcbh.contains("TW_")){
                    k3Service.putReSale(requestid,"TW");
                }
            }
        }
        return SUCCESS;
    }

}
