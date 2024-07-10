package weaver.interfaces.tx.krm.action;


import weaver.conn.RecordSet;
import weaver.formmode.data.ModeDataIdUpdate;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * FileName: SpecMisAction.java
 * 类的详细说明
 *
 * @Author tx
 * @Date 2023/6/17
 * @Version 1.00
 **/
public class SpecMisAction extends BaseBean implements Action {

    @Override
    public String execute(RequestInfo requestInfo) {
        writeLog("开始执行SpecMisAction！");
        RecordSet rs = new RecordSet();
        RecordSet exeRs = new RecordSet();
        RecordSet queryRs = new RecordSet();
        rs.executeQuery("select GROUP_CONCAT(dt.id) ids,requestId,lcbh,sqr,sqrq,ssbm,szgs,rwlx,qd," +
                " zg,dm,bam,jhksrq,jhjsrq from formtable_main_62 t left join" +
                " formtable_main_62_dt1 dt on t.id = dt.mainid where requestId = ?" +
                " GROUP BY requestId,lcbh,sqr,sqrq,ssbm,szgs,rwlx,qd,zg,dm,bam,jhksrq,jhjsrq",
                requestInfo.getRequestid());
        while (rs.next()){
            int mainid = inserTaskData(rs,exeRs);
            inserTaskDtData(rs,exeRs,queryRs,mainid);
        }
        return SUCCESS;
    }

    //新增特派任务台账数据
    public int inserTaskData(RecordSet rs,RecordSet exeRs){
        ModeRightInfo ModeRightInfo = new ModeRightInfo();
        ModeRightInfo.setNewRight(true);
        ModeDataIdUpdate idUpdate = new ModeDataIdUpdate();
        //参数:建模表   模块ID 数据创建人   数据所属人员  创建日期YYYY-MM-DD  创建时间HH:mm:ss
        int modeid = Integer.valueOf(new BaseBean().getPropValue("sec_dev_config","tprwtzModeId"));
        int billid = idUpdate.getModeDataNewId("uf_tprwtz", modeid, 1, 1, new SimpleDateFormat("yyyy-MM-dd").format(new Date()), new SimpleDateFormat("HH:mm:ss").format(new Date()));
        String sql = "update uf_tprwtz set tprwlc=?,lcbh=?,sqr=?,sqrq=?,ssbm=?,ssgs=?,rwlx=?,qd=?,zg=?,dm=?,bam=?,jhksrq=?,jhjsrq=?,zt='0' where id=?";
        exeRs.executeUpdate(sql,rs.getString("requestId"),rs.getString("lcbh"),
                rs.getString("sqr"),rs.getString("sqrq"),rs.getString("ssbm"),rs.getString("szgs"),
                rs.getString("rwlx"),rs.getString("qd"),rs.getString("zg"),rs.getString("dm"),
                        rs.getString("bam"),rs.getString("jhksrq"),rs.getString("jhjsrq"),billid);
        ModeRightInfo.editModeDataShare(1, modeid, billid);//重置建模权限
        return billid;
    }

    //新增特派任务详情数据
    public void inserTaskDtData(RecordSet rs,RecordSet exeRs,RecordSet queryRs,int mainid){
        ModeRightInfo ModeRightInfo = new ModeRightInfo();
        ModeRightInfo.setNewRight(true);
        ModeDataIdUpdate idUpdate = new ModeDataIdUpdate();
        int modeid = Integer.valueOf(new BaseBean().getPropValue("sec_dev_config","tprwxqModeId"));
        queryRs.executeQuery("select rwx,sfpz,pzyq,sfxtxnr from  formtable_main_62_dt1 where id in ("+rs.getString("ids")+")");
        while (queryRs.next()){
            int billid = idUpdate.getModeDataNewId("uf_tprwxq", modeid, 1, 1, new SimpleDateFormat("yyyy-MM-dd").format(new Date()), new SimpleDateFormat("HH:mm:ss").format(new Date()));
            ModeRightInfo.editModeDataShare(1, modeid, billid);//重置建模权限
            String sql = "update uf_tprwxq set tprwtz=?,rwx=?,sfpz=?,pzyq=?,sfxtxnr=? where id=?";
            exeRs.executeUpdate(sql,mainid,queryRs.getString("rwx"), queryRs.getString("sfpz"),
                    queryRs.getString("pzyq"),queryRs.getString("sfxtxnr"),billid);
        }
    }
}
