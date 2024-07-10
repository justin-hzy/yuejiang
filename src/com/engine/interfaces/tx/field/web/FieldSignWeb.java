package com.engine.interfaces.tx.field.web;

import com.alibaba.fastjson.JSONObject;
import com.engine.common.util.ParamUtil;
import com.engine.common.util.ServiceUtil;
import com.engine.interfaces.tx.field.entity.FieldSignEntity;
import com.engine.interfaces.tx.field.service.FieldSignService;
import com.engine.interfaces.tx.field.service.impl.FieldSignServiceImpl;
import com.weaverboot.frame.ioc.anno.parameterAnno.WeaParamBean;
import weaver.general.BaseBean;
import weaver.hrm.HrmUserVarify;
import weaver.hrm.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * FileName: FieldSignWeb.java
 * 打卡处理Action
 *
 * @Author tx
 * @Date 2022/12/19
 * @Version 1.00
 **/
public class FieldSignWeb {



    private FieldSignService getService(){
        //实例化Service类
        return ServiceUtil.getService(FieldSignServiceImpl.class);
    }

    private BaseBean bean = new BaseBean();


    /**
     * 获取打卡结果
     * @param request
     * @param response
     * @param entity 签到信息
     * @return 返回打卡结果
     */
    @POST
    @Path("/getSignRes")
    @Produces(MediaType.TEXT_PLAIN)
    public String getSignRes(@Context HttpServletRequest request, @Context HttpServletResponse response, @WeaParamBean FieldSignEntity entity){
        bean.writeLog(this.getClass().getName(),"FieldSignWeb=>getSignRes=>params=>"+entity.toString());
        Map<String,Object> apidatas = new HashMap<String,Object>();
        try{
            User user = HrmUserVarify.getUser(request, response);
            Map<String, Object> params = ParamUtil.request2Map(request);
            params.put("entity",entity);
            //实例化Service 并调用业务类处理
            apidatas = getService().getSignRes(params, user);
            apidatas.put("api_status", true);
        }catch(Exception e){
            //异常处理
            e.printStackTrace();
            bean.writeLog("返回打卡结果错误："+e);
            apidatas.put("api_status", false);
            apidatas.put("api_errormsg", "catch exception : " + e.getMessage());
        }
        //数据转换
        return JSONObject.toJSONString(apidatas);
    }

    /**
     * 写入客户、店铺库中
     * @param request
     * @param response
     * @param entity 签到信息
     * @return 返回写入结果
     */
    @POST
    @Path("/insertCustomerStore")
    @Produces(MediaType.TEXT_PLAIN)
    public String insertCustomerStore(@Context HttpServletRequest request, @Context HttpServletResponse response, @WeaParamBean FieldSignEntity entity){
        bean.writeLog(this.getClass().getName(),"FieldSignWeb=>insertCustomerStore=>params=>"+entity.toString());
        Map<String,Object> apidatas = new HashMap<String,Object>();
        try{
            User user = HrmUserVarify.getUser(request, response);
            Map<String, Object> params = ParamUtil.request2Map(request);
            params.put("entity",entity);
            //实例化Service 并调用业务类处理
            apidatas = getService().insertCusStore(params, user);
            apidatas.put("api_status", true);
        }catch(Exception e){
            //异常处理
            e.printStackTrace();
            bean.writeLog("写入客户/店铺库错误："+e);
            apidatas.put("api_status", false);
            apidatas.put("api_errormsg", "catch exception : " + e.getMessage());
        }
        //数据转换
        return JSONObject.toJSONString(apidatas);
    }

    /**
     * 写入拜访报告台账和问题跟踪台账中
     * @param request
     * @param response
     * @param entity 签到信息
     * @return 返回写入结果
     */
    @POST
    @Path("/visitReport")
    @Produces(MediaType.TEXT_PLAIN)
    public String visitReport(@Context HttpServletRequest request, @Context HttpServletResponse response, @WeaParamBean FieldSignEntity entity){
        bean.writeLog(this.getClass().getName(),"FieldSignWeb=>visitReport=>params=>"+entity.toString());
        Map<String,Object> apidatas = new HashMap<String,Object>();
        try{
            User user = HrmUserVarify.getUser(request, response);
            Map<String, Object> params = ParamUtil.request2Map(request);
            params.put("entity",entity);
            //实例化Service 并调用业务类处理
            apidatas = getService().visitReportCmd(params, user);
            apidatas.put("api_status", true);
        }catch(Exception e){
            //异常处理
            e.printStackTrace();
            bean.writeLog("拜访报告台账和问题跟踪台账错误："+e);
            apidatas.put("api_status", false);
            apidatas.put("api_errormsg", "catch exception : " + e.getMessage());
        }
        //数据转换
        return JSONObject.toJSONString(apidatas);
    }



//    /**
//     * 创建流程
//     * @param request
//     * @param response
//     * @param entity 签到信息
//     * @return 返回流程requestid
//     */
//    @POST
//    @Path("/creatRequest")
//    @Produces(MediaType.TEXT_PLAIN)
//    public String creatRequest(@Context HttpServletRequest request, @Context HttpServletResponse response, @WeaParamBean FieldSignEntity entity){
//        bean.writeLog(this.getClass().getName(),"FieldSignWeb=>creatRequest=>params=>"+entity.toString());
//        Map<String,Object> apidatas = new HashMap<String,Object>();
//        try{
//            User user = HrmUserVarify.getUser(request, response);
//            Map<String, Object> params = ParamUtil.request2Map(request);
//            params.put("entity",entity);
//            //实例化Service 并调用业务类处理
//            apidatas = getService().creatRequest(params, user);
//            apidatas.put("api_status", true);
//        }catch(Exception e){
//            //异常处理
//            e.printStackTrace();
//            bean.writeLog("创建流程结果错误："+e);
//            apidatas.put("api_status", false);
//            apidatas.put("api_errormsg", "catch exception : " + e.getMessage());
//        }
//        //数据转换
//        return JSONObject.toJSONString(apidatas);
//    }
//
//
//    /**
//     * 触发系统提醒
//     * @param request
//     * @param response
//     * @param entity 签到信息
//     * @return 返回触发提醒结果
//     */
//    @POST
//    @Path("/triggerReminder")
//    @Produces(MediaType.TEXT_PLAIN)
//    public String triggerReminder(@Context HttpServletRequest request, @Context HttpServletResponse response, @WeaParamBean FieldSignEntity entity){
//        bean.writeLog(this.getClass().getName(),"FieldSignWeb=>triggerReminder=>params=>"+entity.toString());
//        Map<String,Object> apidatas = new HashMap<String,Object>();
//        try{
//            User user = HrmUserVarify.getUser(request, response);
//            Map<String, Object> params = ParamUtil.request2Map(request);
//            params.put("entity",entity);
//            //实例化Service 并调用业务类处理
//            apidatas = getService().triggerReminder(params, user);
//            apidatas.put("api_status", true);
//        }catch(Exception e){
//            //异常处理
//            e.printStackTrace();
//            bean.writeLog("触发提醒结果错误："+e);
//            apidatas.put("api_status", false);
//            apidatas.put("api_errormsg", "catch exception : " + e.getMessage());
//        }
//        //数据转换
//        return JSONObject.toJSONString(apidatas);
//    }








}
