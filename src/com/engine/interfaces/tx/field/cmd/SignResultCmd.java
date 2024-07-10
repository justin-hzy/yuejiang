package com.engine.interfaces.tx.field.cmd;

import com.engine.common.biz.AbstractCommonCommand;
import com.engine.common.entity.BizLogContext;
import com.engine.core.interceptor.CommandContext;
import com.engine.interfaces.tx.field.biz.FieldSignBiz;
import com.engine.interfaces.tx.field.constant.FieldSignConstant;
import com.engine.interfaces.tx.field.entity.FieldSignEntity;
import weaver.hrm.User;

import java.util.HashMap;
import java.util.Map;

/**
 * FileName: SignResultCmd.java
 * 打卡结果Command
 * 根据拜访计划和客户地址判断该笔打卡是否有误
 *
 * @Author tx
 * @Date 2022/12/20
 * @Version 1.00
 **/
public class SignResultCmd extends AbstractCommonCommand<Map<String, Object>> {

    public SignResultCmd(Map<String, Object> params, User user) {
        writeLog("SignResultCmd==>params=>"+params.toString());
        this.user = user;
        this.params = params;
    }

    @Override
    public BizLogContext getLogContext() {
        return null;
    }

    @Override
    public Map<String, Object> execute(CommandContext commandContext) {
        FieldSignBiz biz = new FieldSignBiz();//公共类
        Map<String, Object> apidatas = new HashMap<>();
        //获取打卡参数信息
        FieldSignEntity entity = (FieldSignEntity) params.get("entity");
        //获取计划拜访客户地址经纬度
        Map<String,Double> positionMap = biz.planVisit(entity.getUser(),entity.getDateTime(),entity.getCus());
        //判断打卡客户是否在计划拜访客户中
        if(!positionMap.containsKey("cusLng")){
            //判断打卡客户和地址是否在客户档案库中且打卡地址是否有效
            if(biz.validSign(entity.getCus(),entity.getLng(),entity.getLat())){
                apidatas.put("sign_status", FieldSignConstant.SIGN_FAIL2);
                writeLog("打卡客户不在计划内但是打卡地址在客户档案库中");
            }else{
                apidatas.put("sign_status", FieldSignConstant.SIGN_FAIL3);
                writeLog("打卡客户不在计划内且打卡地址不在客户档案库中");
            }
        }else{
            positionMap.put("signLng",entity.getLng());
            positionMap.put("signLat",entity.getLat());
            //获取打卡地址与客户地址的直线距离
            double distance = biz.getDistance(positionMap);
            //如果打卡地址与客户地址的直线距离超过了打卡有效范围则视为打卡异常
            if(distance>biz.getValidDistance()){
                //判断打卡地址与该客户其他的地址比对是否有效，若有效也算打卡成功
                if(biz.validSign(entity.getCus(),entity.getLng(),entity.getLat())){
                    apidatas.put("sign_status", FieldSignConstant.SIGN_SUCCESS);
                    writeLog("打卡成功");
                }else{
                    apidatas.put("sign_status", FieldSignConstant.SIGN_FAIL0);
                    writeLog("打卡地址与客户地址的直线距离超过了打卡有效范围");
                }
            }else {
                apidatas.put("sign_status", FieldSignConstant.SIGN_SUCCESS);
                writeLog("打卡成功");
            }
        }
        return apidatas;
    }



}
