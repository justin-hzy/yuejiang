package com.engine.interfaces.tx.dms.service;

import weaver.hrm.User;

import java.util.Map;

/**
 * FileName: PolicyService.java
 * DMS管理促销政策service
 *
 * @Author tx
 * @Date 2023/8/5
 * @Version 1.00
 **/
public interface PolicyService {

    /**
     * 获取促销政策信息
     * @param params 参数列表
     * @param user 用户
     */
    Map<String, Object> getPolicyData(Map<String, Object> params, User user);


}
