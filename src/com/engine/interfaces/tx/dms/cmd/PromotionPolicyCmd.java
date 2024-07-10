package com.engine.interfaces.tx.dms.cmd;

import com.engine.common.biz.AbstractCommonCommand;
import com.engine.common.entity.BizLogContext;
import com.engine.core.interceptor.CommandContext;
import com.engine.interfaces.tx.dms.constant.PolicyConstant;
import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.hrm.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FileName: PromotionPolicyCmd.java
 * 促销政策cmd
 *
 * @Author tx
 * @Date 2023/8/5
 * @Version 1.00
 **/
public class PromotionPolicyCmd extends AbstractCommonCommand<Map<String, Object>> {

    public PromotionPolicyCmd(Map<String, Object> params, User user) {
        writeLog("PromotionPolicyCmd==>params=>"+params.toString());
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
        String policyId = (String) params.get("policyId");//获取促销政策id参数

        //根据促销政策id查询促销政策信息
        RecordSet rs = new RecordSet();
        apidatas.put("fullGiftInProportion",fullGiftInProportion(rs, policyId));
        apidatas.put("fullGiftLimitProducts",fullGiftLimitProducts(rs, policyId));
        apidatas.put("discount",discount(rs, policyId));
        apidatas.put("adjustDirectSupplyPrice",adjustDirectSupplyPrice(rs, policyId));
        return apidatas;
    }

    //查询满赠按比例政策信息
    public List<Map<String,String>> fullGiftInProportion(RecordSet rs, String policyId){
        List<Map<String,String>> listData = new ArrayList<>();
        String sql = "select dt.spbm,dt.spmc,dt.zp1bm,dt.zp2bm,dt.qdl,dt.zpsl,dt.zpsl1,dt.zp2sl,dt.zk from uf_cxzc t , uf_cxzc_dt1 dt  where  t.id = dt.mainid and t.id in ("+policyId+") and t.cxlx = ?";
        rs.executeQuery(sql, PolicyConstant.FGIP);
        while (rs.next()){
            Map<String,String> mapData = new HashMap<>();
            mapData.put("spbm", Util.null2String(rs.getString("spbm")));
            mapData.put("spmc", Util.null2String(rs.getString("spmc")));
            mapData.put("zp1bm", Util.null2String(rs.getString("zp1bm")));
            mapData.put("zp2bm", Util.null2String(rs.getString("zp2bm")));
            mapData.put("qdl",Util.null2String(rs.getString("qdl")));
            mapData.put("zpsl",Util.null2String(rs.getString("zpsl")));
            mapData.put("zpsl1",Util.null2String(rs.getString("zpsl1")));
            mapData.put("zp2sl",Util.null2String(rs.getString("zp2sl")));
            mapData.put("zk",Util.null2String(rs.getString("zk")));
            listData.add(mapData);
        }
        return listData;
    }

    //查询满赠送额度产品
    public List<Map<String,String>> fullGiftLimitProducts(RecordSet rs, String policyId){
        List<Map<String,String>> listData = new ArrayList<>();
        String sql = "select dt.spbm,dt.spmc,dt.sl,dt.mzje from uf_cxzc t , uf_cxzc_dt2 dt where  t.id = dt.mainid  and  t.id in ("+policyId+") and t.cxlx = ? ";
        rs.executeQuery(sql, PolicyConstant.FGLP);
        while (rs.next()){
            Map<String,String> mapData = new HashMap<>();
            mapData.put("spbm", Util.null2String(rs.getString("spbm")));
            mapData.put("spmc", Util.null2String(rs.getString("spmc")));
            mapData.put("sl",Util.null2String(rs.getString("sl")));
            mapData.put("mzje",Util.null2String(rs.getString("mzje")));
            listData.add(mapData);
        }
        return listData;
    }

    //查询折扣
    public List<Map<String,String>> discount(RecordSet rs, String policyId){
        List<Map<String,String>> listData = new ArrayList<>();
        String sql = "select dt.spbm,dt.spmc,dt.zk,dt.qdl from uf_cxzc t , uf_cxzc_dt3 dt  where  t.id = dt.mainid and  t.id in ("+policyId+") and t.cxlx = ? ";
        rs.executeQuery(sql, PolicyConstant.DISCOUNT);
        while (rs.next()){
            Map<String,String> mapData = new HashMap<>();
            mapData.put("spbm", Util.null2String(rs.getString("spbm")));
            mapData.put("spmc", Util.null2String(rs.getString("spmc")));
            mapData.put("zk",Util.null2String(rs.getString("zk")));
            mapData.put("qdl",Util.null2String(rs.getString("qdl")));
            listData.add(mapData);
        }
        return listData;
    }

    //查询直供价格调整
    public List<Map<String,String>> adjustDirectSupplyPrice(RecordSet rs, String policyId){
        List<Map<String,String>> listData = new ArrayList<>();
        String sql = "select dzqzgjg,dzhzgjg from uf_cxzc where id in ("+policyId+") and cxlx = ? ";
        rs.executeQuery(sql, PolicyConstant.ADSP);
        while (rs.next()){
            Map<String,String> mapData = new HashMap<>();
            mapData.put("dzqzgjg", Util.null2String(rs.getString("dzqzgjg")));
            mapData.put("dzhzgjg", Util.null2String(rs.getString("dzhzgjg")));
            listData.add(mapData);
        }
        return listData;
    }



}
