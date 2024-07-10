package com.engine.interfaces.tx.field.cmd;

import com.engine.common.biz.AbstractCommonCommand;
import com.engine.common.entity.BizLogContext;
import com.engine.core.interceptor.CommandContext;
import com.engine.interfaces.tx.field.biz.FieldSignBiz;
import com.engine.interfaces.tx.field.constant.FieldSignConstant;
import com.engine.interfaces.tx.field.entity.FieldSignEntity;
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
 * FileName: InsertCusStoreCmd.java
 * 根据不同拜访方式将数据写入到不同库中
 *
 * @Author tx
 * @Date 2023/6/15
 * @Version 1.00
 **/
public class InsertCusStoreCmd extends AbstractCommonCommand<Map<String, Object>> {

    public InsertCusStoreCmd(Map<String, Object> params, User user) {
        writeLog("InsertCusStoreCmd==>params=>"+params.toString());
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
        String cusnum = "";
        if(entity.getVisitMethod().equals(FieldSignConstant.VISIT_TYPE1)){//拜访新客户
            cusnum = inserKhData(entity);
        }else if(entity.getVisitMethod().equals(FieldSignConstant.VISIT_TYPE2)){//拜访新店铺
            cusnum = inserDpData(entity);
        }
        if(!cusnum.equals("")){
            status = true;
        }
        apidatas.put("insert_status",status);
        apidatas.put("insert_cusnum",cusnum);
        return apidatas;
    }

    //新增客户信息
    public String inserKhData(FieldSignEntity entity){
        FieldSignBiz biz = new FieldSignBiz();//公共类
        ModeRightInfo ModeRightInfo = new ModeRightInfo();
        ModeRightInfo.setNewRight(true);
        ModeDataIdUpdate idUpdate = new ModeDataIdUpdate();
        //参数:建模表   模块ID 数据创建人   数据所属人员  创建日期YYYY-MM-DD  创建时间HH:mm:ss
        int modeid = Integer.valueOf(new BaseBean().getPropValue("sec_dev_config","khModeId"));
        int billid = idUpdate.getModeDataNewId("uf_kh", modeid, 1, 1, new SimpleDateFormat("yyyy-MM-dd").format(new Date()), new SimpleDateFormat("HH:mm:ss").format(new Date()));
        RecordSet rs = new RecordSet();
        String sql = "update uf_kh set sqr=?,sqrq=?,ssbm=?,ssgs=?,sfhz=?,dq=?,province=?,city=?,khmcst=?,khbh=?,xszg=? where id=?";
        String custNum = biz.generateCUSTNumber();//生成客户编号
        boolean sqlStatus = rs.executeUpdate(sql,entity.getUser(), biz.calculateAverage(entity.getDateTime()),entity.getDep(),
                entity.getSub(), "1", entity.getRegion(),entity.getProvince(), entity.getCity(), entity.getNewCus(), custNum, entity.getUser(),  billid);
        //写入地址信息
        rs.executeUpdate("INSERT uf_kh_dt1 (mainid,dz,jd,wd) VALUES (?,?,?,?)",
                billid,entity.getAddress(),entity.getLng(),entity.getLat());
        writeLog("将打卡数据中的客户信息写入到客户库："+sqlStatus);
        ModeRightInfo.editModeDataShare(1, modeid, billid);//重置建模权限
        inserKhDpData(entity,custNum,"0");
        return custNum;
    }

    //新增店铺信息
    public String inserDpData(FieldSignEntity entity){
        FieldSignBiz biz = new FieldSignBiz();//公共类
        ModeRightInfo ModeRightInfo = new ModeRightInfo();
        ModeRightInfo.setNewRight(true);
        ModeDataIdUpdate idUpdate = new ModeDataIdUpdate();
        //参数:建模表   模块ID 数据创建人   数据所属人员  创建日期YYYY-MM-DD  创建时间HH:mm:ss
        int modeid = Integer.valueOf(new BaseBean().getPropValue("sec_dev_config","dpModeId"));
        int billid = idUpdate.getModeDataNewId("uf_dp", modeid, 1, 1, new SimpleDateFormat("yyyy-MM-dd").format(new Date()), new SimpleDateFormat("HH:mm:ss").format(new Date()));
        RecordSet rs = new RecordSet();
        String sql = "update uf_dp set sqr=?,sqrq=?,ssbm=?,ssgs=?,dpmc=?,dq=?,province=?,city=?,dz=?,jd=?,wd=?,dpbhposbh=?,xszg=? where id=?";
        String posNum = biz.generatePOSNumber();//生成店铺编号
        boolean sqlStatus = rs.executeUpdate(sql,entity.getUser(), biz.calculateAverage(entity.getDateTime()),entity.getDep(),
                entity.getSub(), entity.getNewCus(), entity.getRegion(),entity.getProvince(), entity.getCity(),entity.getAddress(),entity.getLng(),entity.getLat(),posNum,entity.getUser(), billid);
        writeLog("将打卡数据中的店铺信息写入到店铺库："+sqlStatus);
        ModeRightInfo.editModeDataShare(1, modeid, billid);//重置建模权限
        inserKhDpData(entity,posNum,"1");
        return posNum;
    }

    //新增客户&店铺档案
    public void inserKhDpData(FieldSignEntity entity,String num,String lx ) {
        FieldSignBiz biz = new FieldSignBiz();//公共类
        ModeRightInfo ModeRightInfo = new ModeRightInfo();
        ModeRightInfo.setNewRight(true);
        ModeDataIdUpdate idUpdate = new ModeDataIdUpdate();
        //参数:建模表   模块ID 数据创建人   数据所属人员  创建日期YYYY-MM-DD  创建时间HH:mm:ss
        int modeid = Integer.valueOf(new BaseBean().getPropValue("sec_dev_config","khdpModeId"));
        int billid = idUpdate.getModeDataNewId("uf_khdpk", modeid, 1, 1, new SimpleDateFormat("yyyy-MM-dd").format(new Date()), new SimpleDateFormat("HH:mm:ss").format(new Date()));
        RecordSet rs = new RecordSet();
        String sql = "update uf_khdpk set mc=?,lx=?,bh=?,xszg=? where id=?";
        boolean sqlStatus = rs.executeUpdate(sql, entity.getNewCus(), lx, num, entity.getUser(), billid);
        writeLog("将打卡数据中的店铺信息写入到客户&店铺档案库："+sqlStatus);
        ModeRightInfo.editModeDataShare(1, modeid, billid);//重置建模权限
    }
}
