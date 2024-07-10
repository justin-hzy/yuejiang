package com.engine.interfaces.tx.field.biz;


import com.engine.interfaces.tx.field.constant.FieldSignConstant;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.impl.PointImpl;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * FileName: FieldSignBiz.java
 * 外勤打卡公共类
 *
 * @Author tx
 * @Date 2022/12/22
 * @Version 1.00
 **/
public class FieldSignBiz extends BaseBean{


    /**
     * 根据两地直线距离和打卡有效范围，判断本次打卡是否有效
     *
     * @param positionMap 两地经纬度map
     * @return 返回是否有效
     */
    public boolean getSignRes(Map<String,Double> positionMap){
        if(getDistance(positionMap)<=getValidDistance()){
            return true;
        }
        return false;
    }


    /**
     * 根据两地经纬度获取两地之间的直线距离
     *
     * @param positionMap 打卡地址和客户地址map
     * @return 返回两地之间的直线距离
     */
    public double getDistance(Map<String,Double> positionMap){
        SpatialContext geo = SpatialContext.GEO;
        //签到地址经纬度
        Point signPoint = new PointImpl(positionMap.get("signLng"),positionMap.get("signLat"),geo);
        //客户地址经纬度
        Point cusPoint = new PointImpl(positionMap.get("cusLng"),positionMap.get("cusLat"),geo);
        //计算两地距离 单位km
        double distance = geo.calcDistance(signPoint,cusPoint) * DistanceUtils.DEG_TO_KM;
        writeLog("签到地址与客户地址距离 "+distance+" 千米");
        return distance;
    }

    /**
     * 从外勤打卡有效范围配置表uf_wqdkyxfwpzb中获取打卡有效范围
     *
     * @return 返回打卡有效范围 单位km
     */
    public double getValidDistance(){
        double validDistance=0.5;//默认0.5km
        //从外勤打卡有效范围配置表中获取打卡有效范围
        RecordSet rs = new RecordSet();
        rs.executeQuery("select wqdkyxfwm from uf_wqdkyxfwpzb ");
        while (rs.next()){
            validDistance = rs.getDouble("wqdkyxfwm")/1000;
        }
        writeLog("打卡有效范围 "+validDistance+" km");
        return validDistance;
    }


    /**
     * 根据打卡人、打卡日期和打卡客户从拜访计划台账表uf_bfjh中获取个人计划拜访客户地址经纬度
     *
     * @param date 打卡日期
     * @param cus 打卡客户
     * @return 返回计划拜访客户地址经纬度
     */
    public Map<String,Double> planVisit(String user,String date,String cus)  {
        Map<String,Double> positionMap = new HashMap<>();
        //从拜访计划台账表uf_bfjh中获取计划拜访客户地址经纬度
        RecordSet rs = new RecordSet();
        rs.executeQuery("select kh,khdz,jd,wd from uf_bfjh where lx= ? and sqr=?  and kh=? " +
                "and CONVERT(?, DATETIME) BETWEEN CONVERT(kssj, DATETIME) AND CONVERT(jssj, DATETIME)"
                ,FieldSignConstant.PLAN_VISIT,user,cus,date);
        while (rs.next()){
            positionMap.put("cusLng",rs.getDouble("jd"));
            positionMap.put("cusLat",rs.getDouble("wd"));
        }
        writeLog("拜访客户地址经纬度 "+positionMap.toString());
        return positionMap;
    }

    /**
     * 根据打卡人、打卡日期和打卡客户从拜访计划台账表uf_bfjh中获取个人拜访计划
     *
     * @param user 打卡人
     * @param date 打卡日期
     * @param cus 打卡客户
     * @return 返回个人拜访计划信息
     */
    public Map<String,String> getPlanVisit(String user,String date,String cus)  {
        Map<String,String> planMap = new HashMap<>();
        //从拜访计划台账表uf_bfjh中获取个人拜访计划、开始时间、结束时间、拜访事由
        RecordSet rs = new RecordSet();
        rs.executeQuery("select id,kssj,jssj,bfsy from uf_bfjh where lx= ? and sqr=?  and kh=? " +
                        "and CONVERT(?, DATETIME) BETWEEN CONVERT(kssj, DATETIME) AND CONVERT(jssj, DATETIME)"
                ,FieldSignConstant.PLAN_VISIT,user,cus,date);
        while (rs.next()){
            planMap.put("id",rs.getString("id"));
            planMap.put("kssj",rs.getString("kssj"));
            planMap.put("jssj",rs.getString("jssj"));
            planMap.put("bfsy",rs.getString("bfsy"));
        }
        //若无计划则为临时拜访否则为计划拜访
        if (Util.null2String(planMap.get("id")).equals("")){
            planMap.put("bflb",FieldSignConstant.PLAN_TYPE1);
        }else{
            planMap.put("bflb",FieldSignConstant.PLAN_TYPE0);
        }
        writeLog("个人拜访计划信息"+planMap.toString());
        return planMap;
    }

    /**
     * 根据打卡人、打卡日期和打卡客户从原始打卡记录表uf_wqysdk中获取签到打卡信息
     * 若有多条打卡数据则取正常的打卡数据，若无正常数据则取异常最新的数据
     *
     * @param user 打卡人
     * @param date 打卡日期
     * @param cus 打卡客户
     * @return 返回计划拜访客户地址经纬度
     */
    public Map<String,String> getSignData(String user,String date,String cus)  {
        Map<String,String> visitMap = new HashMap<>();
        RecordSet rs = new RecordSet();
        rs.executeQuery("select dkzp,dksj from uf_wqysdk where zt = '0' and dklx='0' and xm=? and kh=?  and CONVERT(dksj, date) = ? ORDER BY id DESC "
                ,user,cus,calculateAverage(date));
        if (rs.next()){
            visitMap.put("dkzp",rs.getString("dkzp"));
            visitMap.put("dksj",rs.getString("dksj"));
        }else {
            rs.executeQuery("select dkzp,dksj from uf_wqysdk where zt != '0' and dklx='0' and xm=? and kh=? and CONVERT(dksj, date) = ? ORDER BY id DESC "
                    ,user,cus,calculateAverage(date));
            rs.next();
            visitMap.put("dkzp",rs.getString("dkzp"));
            visitMap.put("dksj",rs.getString("dksj"));
        }

        writeLog("打卡信息 "+ visitMap.toString());
        return visitMap;
    }

    /**
     * 根据打卡信息从客户&店铺地址簿address_book中获取客户所有信息并轮番判断该笔打卡信息是否有效
     *
     * @param signCus 客户
     * @param signLng 经度
     * @param signLat 维度
     * @return 返回是否有效
     */
    public boolean validSign(String signCus,double signLng,double signLat){
        Map<String,Double> positionMap = new HashMap<>();
        positionMap.put("signLng",signLng);
        positionMap.put("signLat",signLat);
        RecordSet rs = new RecordSet();
        rs.executeQuery("select dz,jd,wd from address_book where khbh=?",signCus);
        while (rs.next()){
            positionMap.put("cusLng",rs.getDouble("jd"));
            positionMap.put("cusLat",rs.getDouble("wd"));
            if(getDistance(positionMap)<getValidDistance()){
                writeLog(getClass().getName(),"打卡地址与该客户其他的地址比对有效，地址"+positionMap);
                return true;
            }
        }
        writeLog(getClass().getName(),"打卡地址与该客户其他的地址比对无效！");
        return false;
    }



    /**
     * 修改原始打卡记录报表的打卡状态
     *
     * @param id 数据id
     * @param status 修改状态
     */
    public void upSignData(String id,String status){
        RecordSet rs = new RecordSet();
        rs.executeUpdate("update uf_wqysdk set zt=? where id=?",status,id);
    }

    /**
     * 修改拜访计划台账uf_bfjh的完成状态
     *
     * @param user 打卡人
     * @param date 打卡日期
     * @param cus 打卡客户
     * @param status 修改状态
     */
    public void upPlanVisitData(String user,String cus,String date,String status){
        RecordSet rs = new RecordSet();
        rs.executeUpdate("update uf_bfjh set zt=? where lx= ? and sqr=?  and kh=? " +
                        " and CONVERT(?, DATETIME) BETWEEN CONVERT(kssj, DATETIME) AND CONVERT(jssj, DATETIME)"
                ,status,FieldSignConstant.PLAN_VISIT,user,cus,date);
    }


    /**
     * 日期字符串格式转换 例如：2023-06-14 22:28转2023-06-14
     *
     * @param dataTime 日期时间
     * @return 日期
     */
    public String calculateAverage(String dataTime){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateTime = null;
        try {
            dateTime = dateFormat.parse(dataTime);
        } catch (ParseException e) {
            writeLog("日期格式转化错误",e);
        }
        String dateStr = dateFormat.format(dateTime);
        return dateStr;
    }


    /**
     * 根据CUST0000规则生成客户编号
     *
     * @return 客户编号
     */
    public String generateCUSTNumber(){
        writeLog("生成客户编号！");
        int processNum = 0;
        RecordSet rs = new RecordSet();
        rs.executeQuery("SELECT REPLACE(khbh, 'CUST', '') num FROM uf_kh where khbh like 'CUST%'  ORDER BY LPAD(REPLACE(khbh, 'CUST', ''), 4, '0') DESC LIMIT 1" );
        while (rs.next()){
            processNum = Util.getIntValue(rs.getString("num"),-1);
            if(processNum == -1){
                writeLog("生成客户编号失败！");
                return "";
            }
        }
        processNum++;
        String cust = "CUST";
        String format = "%s%04d";
        String number = String.format(format, cust, processNum);
        writeLog("生成客户编号成功："+number);
        return number;
    }

    /**
     * 根据POS000规则生成店铺编号
     *
     * @return 店铺编号
     */
    public  String generatePOSNumber(){
        writeLog("生成店铺编号！");
        int processNum = 0;
        RecordSet rs = new RecordSet();
        rs.executeQuery("SELECT  REPLACE(dpbhposbh, 'POS', '') num FROM uf_dp where dpbhposbh like 'POS%' ORDER BY LPAD(REPLACE(dpbhposbh, 'POS', ''), 3, '0') DESC LIMIT 1 " );
        while (rs.next()){
            processNum = Util.getIntValue(rs.getString("num"),0);
            if(processNum == 0){
                writeLog("生成店铺编号失败！");
                return "";
            }
        }
        processNum++;
        String cust = "POS";
        String format = "%s%03d";
        String number = String.format(format, cust, processNum);
        writeLog("生成店铺编号成功："+number);
        return number;
    }






}
