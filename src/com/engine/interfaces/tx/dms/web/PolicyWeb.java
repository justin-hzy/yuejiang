package com.engine.interfaces.tx.dms.web;

import com.alibaba.fastjson.JSONObject;
import com.engine.common.util.ParamUtil;
import com.engine.common.util.ServiceUtil;
import com.engine.interfaces.tx.dms.service.PolicyService;
import com.engine.interfaces.tx.dms.service.impl.PolicyServiceImpl;
import weaver.general.BaseBean;
import weaver.hrm.HrmUserVarify;
import weaver.hrm.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * FileName: PolicyWeb.java
 * DMS管理促销政策web
 *
 * @Author tx
 * @Date 2023/8/5
 * @Version 1.00
 **/
public class PolicyWeb {

    private PolicyService getService(){
        //实例化Service类
        return ServiceUtil.getService(PolicyServiceImpl.class);
    }

    private BaseBean bean = new BaseBean();

    /**
     * 获取促销政策信息
     * @param request
     * @param response
     * @return 返回写入结果
     */
    @GET
    @Path("/getPolicyData")
    @Produces(MediaType.TEXT_PLAIN)
    public String getPolicyData(@Context HttpServletRequest request, @Context HttpServletResponse response){
        Map<String, Object> params = ParamUtil.request2Map(request);
        bean.writeLog("PolicyWeb=>getPolicyData=>params=>"+params.toString());
        Map<String,Object> apidatas = new HashMap<String,Object>();
        try{
            User user = HrmUserVarify.getUser(request, response);
            //实例化Service 并调用业务类处理
            apidatas = getService().getPolicyData(params, user);
            apidatas.put("api_status", true);
        }catch(Exception e){
            //异常处理
            e.printStackTrace();
            bean.writeLog("获取促销政策信息："+e);
            apidatas.put("api_status", false);
            apidatas.put("api_errormsg", "catch exception : " + e.getMessage());
        }
        //数据转换
        return JSONObject.toJSONString(apidatas);
    }

}
