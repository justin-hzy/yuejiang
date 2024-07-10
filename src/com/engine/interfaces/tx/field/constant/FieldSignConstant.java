package com.engine.interfaces.tx.field.constant;

/**
 * FileName: FieldSignConstant.java
 * 外勤打卡状态常量类
 *
 * @Author tx
 * @Date 2022/12/20
 * @Version 1.00
 **/
public class FieldSignConstant {
    /*
     * 打卡状态码：
     * 1 打卡成功
     * 0 打卡地址与客户地址的直线距离超过了打卡有效范围
     * -1 打卡客户在计划内但是打卡地址不在客户档案库中
     * -2 打卡客户不在计划内但是打卡地址在客户档案库中
     * -3 打卡客户不在计划内且打卡地址不在客户档案库中
     */
    public static final String SIGN_SUCCESS = "1";
    public static final String SIGN_FAIL0 = "0";
    public static final String SIGN_FAIL1 = "-1";
    public static final String SIGN_FAIL2 = "-2";
    public static final String SIGN_FAIL3 = "-3";

    /*
     * 拜访方式：
     * 0 计划拜访
     * 1 拜访新客户
     * 2 拜访新店铺
     */
    public static final String VISIT_TYPE0 = "0";
    public static final String VISIT_TYPE1 = "1";
    public static final String VISIT_TYPE2 = "2";


    //计划拜访类型
    public static final String PLAN_VISIT = "0";//拜访

    /*
     * 拜访报告拜访类别
     * 0 计划拜访
     * 1 临时拜访
     */
    public static final String PLAN_TYPE0 = "0";
    public static final String PLAN_TYPE1 = "1";

    /*
     * 问题跟进记录处理状态
     * 0 未解决
     * 1 已解决
     */
    public static final String PROBLEM_STATUS0  = "0";
    public static final String PROBLEM_STATUS1 = "1";

}
