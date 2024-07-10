package com.engine.interfaces.tx.field.service.impl;

import com.engine.core.impl.Service;
import com.engine.interfaces.tx.field.cmd.InsertCusStoreCmd;
import com.engine.interfaces.tx.field.cmd.SignResultCmd;
import com.engine.interfaces.tx.field.cmd.VisitReportCmd;
import com.engine.interfaces.tx.field.service.FieldSignService;
import com.engine.license.cmd.InLicenseCmd;
import weaver.hrm.User;

import java.util.Map;

/**
 * FileName: FieldSignServiceImpl.java
 * 打卡处理service实现类
 *
 * @Author tx
 * @Date 2022/12/20
 * @Version 1.00
 **/
public class FieldSignServiceImpl extends Service implements FieldSignService {

    /**
     * 获取打卡结果
     * @param params 参数列表
     * @param user 用户
     */
    @Override
    public Map<String, Object> getSignRes(Map<String, Object> params, User user) {
        return commandExecutor.execute(new SignResultCmd(params,user));
    }

    /**
     * 写入客户/店铺库
     * @param params 参数列表
     * @param user 用户
     */
    @Override
    public Map<String, Object> insertCusStore(Map<String, Object> params, User user) {
        return commandExecutor.execute(new InsertCusStoreCmd(params,user));
    }

    /**
     * 写入拜访报告台账和问题跟踪台账
     * @param params 参数列表
     * @param user 用户
     */
    @Override
    public Map<String, Object> visitReportCmd(Map<String, Object> params, User user) {
        return commandExecutor.execute(new VisitReportCmd(params,user));
    }


}
