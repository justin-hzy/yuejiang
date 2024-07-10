package com.engine.interfaces.tx.field.service;

import weaver.hrm.User;

import java.util.Map;

/**
 * FileName: FieldSignService.java
 * 打卡处理service
 *
 * @Author tx
 * @Date 2022/12/20
 * @Version 1.00
 **/
public interface FieldSignService {

     /**
      * 获取打卡结果
      * @param params 参数列表
      * @param user 用户
      */
      Map<String, Object> getSignRes(Map<String, Object> params, User user);

    /**
     * 写入客户/店铺库
     * @param params 参数列表
     * @param user 用户
     */
    Map<String, Object> insertCusStore(Map<String, Object> params, User user);

    /**
     * 写入写入拜访报告台账和问题跟踪台账
     * @param params 参数列表
     * @param user 用户
     */
    Map<String, Object> visitReportCmd(Map<String, Object> params, User user);

}
