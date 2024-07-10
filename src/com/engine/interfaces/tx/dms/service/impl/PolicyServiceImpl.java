package com.engine.interfaces.tx.dms.service.impl;

import com.engine.interfaces.tx.dms.cmd.PromotionPolicyCmd;
import com.engine.interfaces.tx.dms.service.PolicyService;

import com.engine.core.impl.Service;
import weaver.hrm.User;

import java.util.Map;

/**
 * FileName: PolicyServiceImpl.java
 * DMS管理促销政策service实现类
 *
 * @Author tx
 * @Date 2023/8/5
 * @Version 1.00
 **/
public class PolicyServiceImpl extends Service implements PolicyService {

    /**
     * 获取促销政策信息
     * @param params 参数列表
     * @param user 用户
     */
    @Override
    public Map<String, Object> getPolicyData(Map<String, Object> params, User user) {
        return commandExecutor.execute(new PromotionPolicyCmd(params,user));
    }
}
