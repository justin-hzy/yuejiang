package com.engine.interfaces.tx.field.cmd;

import com.engine.common.biz.AbstractCommonCommand;
import com.engine.common.entity.BizLogContext;
import com.engine.core.interceptor.CommandContext;
import com.engine.interfaces.tx.field.biz.FieldSignBiz;
import com.engine.interfaces.tx.field.constant.FieldSignConstant;
import com.engine.interfaces.tx.field.entity.FieldSignEntity;
import com.wbi.util.Util;
import weaver.conn.RecordSet;
import weaver.formmode.data.ModeDataIdUpdate;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.hrm.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * FileName: VisitReportCmd.java
 * 拜访报告写入拜访报告台账和问题跟踪台账中
 *
 * @Author tx
 * @Date 2023/6/15
 * @Version 1.00
 **/
public class VisitReportCmd extends AbstractCommonCommand<Map<String, Object>> {

    public VisitReportCmd(Map<String, Object> params, User user) {
        writeLog("VisitReportCmd==>params=>"+params.toString());
        this.user = user;
        this.params = params;
    }

    @Override
    public BizLogContext getLogContext() {
        return null;
    }

    @Override
    public Map<String, Object> execute(CommandContext commandContext) {
        Map<String, Object> apidatas = new HashMap<>();
        FieldSignEntity entity = (FieldSignEntity) params.get("entity");
        boolean status = false;
        int reportid = inserReportData(entity);
        status = reportid==-1 ? false:true;
        apidatas.put("inserReport_status",status);
        apidatas.put("inserReport_reportid",reportid);
        status = inserBugReportData(entity,reportid);
        apidatas.put("inserBugReport_status",status);
        return apidatas;
    }

    //新增拜访报告信息
    public int inserReportData(FieldSignEntity entity){
        FieldSignBiz biz = new FieldSignBiz();//公共类
        int modeid = Integer.valueOf(new BaseBean().getPropValue("sec_dev_config","bfbgModeId"));
        //获取拜访计划数据
        Map<String,String> planMap = biz.getPlanVisit(entity.getUser(),entity.getDateTime(),entity.getCus());
        //获取打卡数据
        Map<String,String> visitMap = biz.getSignData(entity.getUser(),entity.getDateTime(),entity.getCus());
        //查询该客户是否已有拜访报告 有则更新，无则新增
        Map<String,String> reportData = getReportData(planMap.get("bflb"),entity.getCus(),planMap.get("id"));
        int billid = -1;
        ModeRightInfo ModeRightInfo = new ModeRightInfo();
        ModeRightInfo.setNewRight(true);
        ModeDataIdUpdate idUpdate = new ModeDataIdUpdate();
        if(Util.null2String(reportData.get("reportid")).equals("")){
            //参数:建模表   模块ID 数据创建人   数据所属人员  创建日期YYYY-MM-DD  创建时间HH:mm:ss
            billid = idUpdate.getModeDataNewId("uf_bfbg", modeid, 1, 1, new SimpleDateFormat("yyyy-MM-dd").format(new Date()), new SimpleDateFormat("HH:mm:ss").format(new Date()));
        }else {
            billid = Util.getIntValue(reportData.get("reportid"));
        }
        RecordSet rs = new RecordSet();
        String sql = "update uf_bfbg set sqr=?,sqrq=? ,ssbm=?,ssgs=?,kh=?,bgpz=?,bfjh=?,bflb=?,jhbfkssj=?,jhbfjssj=?,bfsy=?,sjbfkssj=?,sjbfjssj=?,dkpz=?,ysdkjlid=? where id=?";
        boolean sqlStatus = rs.executeUpdate(sql,entity.getUser(), biz.calculateAverage(entity.getDateTime()),entity.getDep(),
                entity.getSub(), entity.getCus(), entity.getPhoto(),planMap.get("id"),planMap.get("bflb"),planMap.get("kssj"),planMap.get("jssj"),planMap.get("bfsy"),
                visitMap.get("dksj"),entity.getDateTime(),visitMap.get("dkzp"),entity.getId(),billid);
        writeLog("将拜访报告写入拜访报告台账："+sqlStatus);
        ModeRightInfo.editModeDataShare(1, modeid, billid);//重置建模权限
        //更新拜访计划为已完成
        biz.upPlanVisitData(entity.getUser(),entity.getCus(),entity.getDateTime(),FieldSignConstant.SIGN_SUCCESS);
        return billid;
    }

    public Map<String,String> getReportData(String bflb,String kh,String jh){
        Map<String,String> reportData = new HashMap<>();
        RecordSet rs = new RecordSet();
        String sql = "";
        if(bflb.equals(FieldSignConstant.PLAN_TYPE0)){
            sql = "select id from uf_bfbg where bflb = ? and kh = ? and bfjh = ?";
            rs.executeQuery(sql,bflb,kh,jh);
        }else if(bflb.equals(FieldSignConstant.PLAN_TYPE1)){
            sql = "select id from uf_bfbg where bflb = ? and kh = ?";
            rs.executeQuery(sql,bflb,kh);
        }
        while (rs.next()){
            reportData.put("reportid",rs.getString("id"));
        }
        return reportData;
    }

    //新增问题跟踪台账信息
    public boolean inserBugReportData(FieldSignEntity entity,int reportid){
        FieldSignBiz biz = new FieldSignBiz();//公共类
        int modeid = Integer.valueOf(new BaseBean().getPropValue("sec_dev_config","wtgzModeId"));
        ModeRightInfo ModeRightInfo = new ModeRightInfo();
        ModeRightInfo.setNewRight(true);
        RecordSet updateRs = new RecordSet();
        RecordSet queryRs = new RecordSet();
        queryRs.executeQuery("select id from uf_wtgz where wtlylx='0' and ysdkjlid = ? ",entity.getId());
        boolean sqlStatus = false;
        while (queryRs.next()){
            String billid = queryRs.getString("id");
            String sql = "update uf_wtgz set formmodeid=?,modedatacreater=?,modedatacreatertype=?,modedatacreatedate=?,modedatacreatetime=?,khmc=?,zt=?,bfbg=? where id=?";
            sqlStatus = updateRs.executeUpdate(sql,modeid,1,0,new SimpleDateFormat("yyyy-MM-dd").format(new Date()), new SimpleDateFormat("HH:mm:ss").format(new Date()),
                    entity.getCus(), FieldSignConstant.PROBLEM_STATUS0, reportid,billid);
            writeLog("将拜访报告写入问题跟踪台账："+sqlStatus);
            ModeRightInfo.editModeDataShare(1, modeid, Integer.parseInt(billid));//重置建模权限
        }
        return sqlStatus;
    }
}
