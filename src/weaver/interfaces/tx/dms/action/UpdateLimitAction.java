package weaver.interfaces.tx.dms.action;

import weaver.conn.RecordSet;
import weaver.formmode.data.ModeDataIdUpdate;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.interfaces.tx.util.WorkflowToolMethods;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * FileName: UpdateLimitAction.java
 * 促销政策流程更新货补额度明细表
 *
 * @Author tx
 * @Date 2023/8/11
 * @Version 1.00
 **/
public class UpdateLimitAction extends BaseBean implements Action {


    @Override
    public String execute(RequestInfo requestInfo) {
        writeLog("开始执行UpdateLimitAction！");
        RequestManager requestManager = requestInfo.getRequestManager();
        RecordSet rs = new RecordSet();
        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        String cxlx = mainData.get("cxlx");
        //当促销类型不为贴花、调额则不需要更新货补额度明细表
        if(!(cxlx.equals("3")||cxlx.equals("4"))){
            return SUCCESS;
        }
        mainData.put("requestid",requestInfo.getRequestid());
        String[] khs = mainData.get("khmc").split(",");
        for (String kh : khs) {
            mainData.put("kh",kh);
            if(!updateData(mainData, rs)){
                requestManager.setMessageid("1000");
                requestManager.setMessagecontent("客户编码为"+kh+"更新至货补额度明细表失败，请联系系统管理员！");
                return FAILURE_AND_CONTINUE;
            }
        }
        return SUCCESS;
    }

    //更新数据
    public boolean updateData(Map<String,String> mainData, RecordSet rs){
        ModeRightInfo ModeRightInfo = new ModeRightInfo();
        ModeRightInfo.setNewRight(true);
        ModeDataIdUpdate idUpdate = new ModeDataIdUpdate();
        //参数:建模表   模块ID 数据创建人   数据所属人员  创建日期YYYY-MM-DD  创建时间HH:mm:ss
        int modeid = Integer.valueOf(new BaseBean().getPropValue("sec_dev_config","hbedModeId"));
        int billid = idUpdate.getModeDataNewId("uf_hbedmx", modeid, 1, 1, new SimpleDateFormat("yyyy-MM-dd").format(new Date()), new SimpleDateFormat("HH:mm:ss").format(new Date()));
        String sql = "update uf_hbedmx set kh=?,pp=?,lx=?,ddbh=?,je=?,lylc=?,rq=?,hbedlx=? where id=?";
        boolean status = rs.executeUpdate(sql, mainData.get("kh"), mainData.get("pp"), mainData.get("lx"),
                mainData.get("lcbh"), mainData.get("hbed"), mainData.get("requestid"),
                mainData.get("sqrq"), mainData.get("hbedlx"), billid);
        ModeRightInfo.editModeDataShare(1, modeid, billid);//重置建模权限
        return status;
    }

}
