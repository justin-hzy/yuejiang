package weaver.interfaces.tx.dms.crojob;

import weaver.conn.RecordSet;
import weaver.formmode.data.ModeDataIdUpdate;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;
import weaver.interfaces.tx.util.LocalDateUtil;
import weaver.interfaces.tx.util.ToolsFunction;

import java.util.HashMap;
import java.util.Map;

/**
 * FileName: RebateCalcJob.java
 * 定时计算月返，季返，年返
 *
 * @Author tx
 * @Date 2024/7/25
 * @Version 1.00
 **/
public class RebateCalcJob extends BaseCronJob {

    private String fldbjl = "uf_fldbjl"; //返利达标记录
    private String fthjil = "uf_fthjil"; //发退货记录
    private String flmbk = "uf_flmbk"; //返利目标
    private String flk = "uf_flk";//返利库
    private String YF; //月返计算日期 dd
    private String Q1; //一季度季返计算日期 MM-dd
    private String Q2; //二季度季返计算日期 MM-dd
    private String Q3; //三季度季返计算日期 MM-dd
    private String Q4; //四季度季返计算日期 MM-dd
    private String NF; //年返计算日期 MM-dd
    private BaseBean bean = new BaseBean();
    private int fldbjlModeId = Util.getIntValue(bean.getPropValue("sec_dev_config", "fldbjlModeId")); //返利达标记录库模块id

    public static final String MR = "0"; //月返类型
    public static final String QR = "1"; //季返类型
    public static final String YR = "2"; //年返类型


    @Override
    public void execute() {
        monRebateCalc(); //月返
        quarterRebateCalc(); //季返
        yearRebateCalc(); //年返
    }

    //根据发退货记录表计算每个客户的月返并生成返利记录
    public void monRebateCalc(){
        if (!YF.equals(LocalDateUtil.getLocaDayStr())){ //判断当前日期是否进行月返计算
            return;
        }
        bean.writeLog("开始计算月返！");

        RecordSet queRs = new RecordSet();
        RecordSet calcRs = new RecordSet();
        RecordSet exeRs = new RecordSet();
        ModeDataIdUpdate idUpdate = new ModeDataIdUpdate();
        ModeRightInfo modeRightInfo = new ModeRightInfo();

        String sql = " select distinct kh,pp from " + fthjil + " where fhrq >= '"
                + LocalDateUtil.getFirstDayOfLastMonthStr() + "' and fhrq <= '" + LocalDateUtil.getLastDayOfLastMonthStr() + "'";
        queRs.executeQuery(sql);
        while (queRs.next()){
            //月返及计算金额详情
            Map<String, Object> monRebatInfo = getMonRebatInfo(calcRs, queRs.getString("kh"), queRs.getString("pp"));
            //月返金额
            double monRebatVal = (double) monRebatInfo.get("monRebatVal");
            //生成返利记录
            Map<String, String> sqlMap = new HashMap<>();
            sqlMap.put("khmc", queRs.getString("kh"));
            sqlMap.put("pp", queRs.getString("pp"));
            sqlMap.put("ny", LocalDateUtil.getLastYearMonthStr());
            sqlMap.put("fllx", "0"); //0月返
            sqlMap.put("je", Util.null2String(monRebatInfo.get("monOrderVal")));
            sqlMap.put("flje", Util.null2String(monRebatVal));
            sqlMap.put("bz", Util.null2String(monRebatInfo.get("rebatInfo")));
            int billid = ToolsFunction.insertMode(fldbjlModeId, fldbjl, sqlMap, 1, idUpdate, modeRightInfo, exeRs);
//            //更新返利库   20240827取消月返更新返利库，拆分成其它接口单独更新
//            boolean sqlState = exeRs.executeUpdate("update " + flk + " set ydflje = ifnull(ydflje,0) + " + monRebatVal
//                + " , kyyfje = ifnull(kyyfje,0) + " + monRebatVal + " where khmc = '" + queRs.getString("kh")
//                + "' and pp = '" + queRs.getString("pp") + "'");
//            //更新返利达标记录“是否已计算返利”
//            int state = sqlState? 0:1;
//            exeRs.executeUpdate("update " + fldbjl + " set sfyjsfl = ? where id = ?", state, billid);
        }

    }

    //根据发退货记录表计算每个客户的季返并生成返利记录
    public void quarterRebateCalc(){
        int quarter = 0; //当前季度
        String mdStr = LocalDateUtil.getLocaMonDayStr(); //当前月日
        //用日期判断当前属于哪个季度
        if (Q1.equals(mdStr)) {
            quarter = 1;
        } else if (Q2.equals(mdStr)) {
            quarter = 2;
        } else if (Q3.equals(mdStr)) {
            quarter = 3;
        } else if (Q4.equals(mdStr)) {
            quarter = 4;
        }
        if (quarter ==0 ){
            return;
        }
        bean.writeLog("开始计算季返！");

        RecordSet queRs = new RecordSet();
        RecordSet calcRs = new RecordSet();
        RecordSet exeRs = new RecordSet();
        ModeDataIdUpdate idUpdate = new ModeDataIdUpdate();
        ModeRightInfo modeRightInfo = new ModeRightInfo();

        int year = Util.getIntValue(LocalDateUtil.getLocalYearStr()); //当前年
        //如果是第四季度，年返需减1，因为第四季度是跨年计算返利的
        if(quarter == 4){
            year = year - 1;
        }

        //订单发货日期范围
        String startDate = LocalDateUtil.getQuarterStartDate(year, quarter);
        String endDate = LocalDateUtil.getQuarterEndDate(year, quarter);

        String sql = " select distinct kh,pp from " + fthjil + " where fhrq >= '"
                + startDate + "' and fhrq <= '" + endDate + "'";
        queRs.executeQuery(sql);
        while (queRs.next()){
            String kh = queRs.getString("kh");
            String pp = queRs.getString("pp");
            //季返达标及计算金额详情
            Map<String, Object> quarterTargetInfo = getQuarterTargetInfo(calcRs, kh, pp, quarter);
            //季返达标金额
            double quarterTargetVal = (double) quarterTargetInfo.get("quarterTargetVal");
            //季返及计算金额详情
            Map<String, Object> quarterRebatInfo = getQuarterRebatInfo(calcRs, kh, pp, quarter);
            //季返订单金额
            double quarterOrderVal = (double) quarterRebatInfo.get("quarterOrderVal");
            //需判断季度发货订单金额是否达标
            String flag = "1"; //默认否
            double quarterRebatVal = 0;
            if(quarterTargetVal >= getTargetVal(exeRs, kh, pp, QR)){
                flag = "0";
                quarterRebatVal = (double) quarterRebatInfo.get("quarterRebatVal");
            }
            //生成返利记录
            Map<String, String> sqlMap = new HashMap<>();
            sqlMap.put("khmc", queRs.getString("kh"));
            sqlMap.put("pp", queRs.getString("pp"));
            sqlMap.put("ny", LocalDateUtil.getLastYearMonthStr());
            sqlMap.put("fllx", "1"); //1季返
            sqlMap.put("sfdb", flag);
            sqlMap.put("fldbje", Util.null2String(quarterTargetVal));
            sqlMap.put("je", Util.null2String(quarterRebatInfo.get("quarterOrderVal")));
            sqlMap.put("flje", Util.null2String(quarterRebatVal));
            sqlMap.put("bz", "季返达标计算详情：" + Util.null2String(quarterTargetInfo.get("rebatInfo"))
                    + "; 季返计算详情：" + Util.null2String(quarterRebatInfo.get("rebatInfo")));
            int billid = ToolsFunction.insertMode(fldbjlModeId, fldbjl, sqlMap, 1, idUpdate, modeRightInfo, exeRs);
            //更新返利库
            boolean sqlState = exeRs.executeUpdate("update " + flk + " set jdflje = ifnull(jdflje,0) + " + quarterRebatVal
                    + " , kyjfje = ifnull(kyjfje,0) + " + quarterRebatVal + " where khmc = '" + queRs.getString("kh")
                    + "' and pp = '" + queRs.getString("pp") + "'");
            //更新返利达标记录“是否已计算返利”
            int state = sqlState? 0:1;
            exeRs.executeUpdate("update " + fldbjl + " set sfyjsfl = ? where id = ?", state, billid);
        }

    }

    //根据发退货记录表计算每个客户的年返并生成返利记录
    public void yearRebateCalc(){
        if (!NF.equals(LocalDateUtil.getLocaMonDayStr())){ //判断当前日期是否进行年返计算
            return;
        }
        bean.writeLog("开始计算年返！");

        RecordSet queRs = new RecordSet();
        RecordSet calcRs = new RecordSet();
        RecordSet exeRs = new RecordSet();
        ModeDataIdUpdate idUpdate = new ModeDataIdUpdate();
        ModeRightInfo modeRightInfo = new ModeRightInfo();

        //订单发货日期范围
        String startDate = LocalDateUtil.getFirstDayOfLastYear();
        String endDate = LocalDateUtil.getLastDayOfLastYear();

        String sql = " select distinct kh,pp from " + fthjil + " where fhrq >= '"
                + startDate + "' and fhrq <= '" + endDate + "'";
        queRs.executeQuery(sql);
        while (queRs.next()){
            String kh = queRs.getString("kh");
            String pp = queRs.getString("pp");
            //年返达标及计算金额详情
            Map<String, Object> yearTargetInfo = getYearTargetInfo(calcRs, kh, pp);
            //年返达标金额
            double yearTargetVal = (double) yearTargetInfo.get("yearTargetVal");
            //年返及计算金额详情
            Map<String, Object> yearRebatInfo = getYearRebatInfo(calcRs, kh, pp);
            //年返订单金额
            double yearOrderVal = (double) yearRebatInfo.get("yearOrderVal");
            //需判断年度发货订单金额是否达标
            String flag = "1"; //默认否
            double yearRebatVal = 0;
            if(yearTargetVal >= getTargetVal(exeRs, kh, pp, YR)){
                if("0".equals(isAddRebate(exeRs, kh, pp))){ //判断是否支持追加季返
                    addQuarterRebate(exeRs, kh, pp, idUpdate, modeRightInfo); //追加季返
                }
                flag = "0";
                yearRebatVal = (double) yearRebatInfo.get("yearRebatVal");
            }
            //生成返利记录
            Map<String, String> sqlMap = new HashMap<>();
            sqlMap.put("khmc", queRs.getString("kh"));
            sqlMap.put("pp", queRs.getString("pp"));
            sqlMap.put("ny", LocalDateUtil.getLastYearMonthStr());
            sqlMap.put("fllx", "2"); //2年返
            sqlMap.put("sfdb", flag);
            sqlMap.put("fldbje", Util.null2String(yearTargetVal));
            sqlMap.put("je", Util.null2String(yearRebatInfo.get("yearOrderVal")));
            sqlMap.put("flje", Util.null2String(yearRebatVal));
            sqlMap.put("bz", "年返达标计算详情：" + Util.null2String(yearTargetInfo.get("rebatInfo"))
                    + "; 年返计算详情：" + Util.null2String(yearRebatInfo.get("rebatInfo")));
            int billid = ToolsFunction.insertMode(fldbjlModeId, fldbjl, sqlMap, 1, idUpdate, modeRightInfo, exeRs);
            //更新返利库
            boolean sqlState = exeRs.executeUpdate("update " + flk + " set ndflje = ifnull(ndflje,0) + " + yearRebatVal
                    + " , kynfje = ifnull(kynfje,0) + " + yearRebatVal + " where khmc = '" + queRs.getString("kh")
                    + "' and pp = '" + queRs.getString("pp") + "'");
            //更新返利达标记录“是否已计算返利”
            int state = sqlState? 0:1;
            exeRs.executeUpdate("update " + fldbjl + " set sfyjsfl = ? where id = ?", state, billid);
        }
        
    }


    //获取月返及计算金额详情
    public Map<String, Object> getMonRebatInfo(RecordSet rs, String kh, String pp){
        Map<String, Object> monRebatInfo = new HashMap<>();

        //订单发货日期范围
        String startDate = LocalDateUtil.getFirstDayOfLastMonthStr();
        String endDate = LocalDateUtil.getLastDayOfLastMonthStr();

        double orderShipVal = getOrderShipVal(rs, kh, pp, startDate, endDate, MR); //享受月返发货订单金额
        double orderReturnVal = getOrderReturnVal(rs, kh, pp, startDate, endDate); //退货订单金额
        double orderExchangeVal = getOrderExchangeVal(rs, kh, pp, startDate, endDate); //换货订单金额
        double orderDecalVal = getOrderDecalVal(rs, kh, pp, startDate, endDate); //贴花使用金额
        double quarterUsageVal = getQuarterUsageVal(rs, kh, pp, startDate, endDate); //季返使用金额
        double annualUsageVal = getAnnualUsageVal(rs, kh, pp, startDate, endDate); //年返使用金额
        double rate = getRate(rs, kh, pp, MR); //返利比例

        //月返计算返利=（正常发货订单-退货订单+换货订单+贴花使用金额+季返使用金额+年返使用金额（上年）-不享受月返订单金额）*比例
        double monRebatVal = (orderShipVal - orderReturnVal + orderExchangeVal + orderDecalVal +
                quarterUsageVal + annualUsageVal) * rate;
        //订单计算总金额
        double monOrderVal = orderShipVal - orderReturnVal + orderExchangeVal + orderDecalVal +
                quarterUsageVal + annualUsageVal;
        //月返计算详情金额组成
        String rebatInfo = "享受月返的发货订单金额：" + orderShipVal + "，退货订单金额：" + orderReturnVal
                + "，换货订单金额：" + orderExchangeVal + "，贴花使用金额：" + orderDecalVal
                + "，季返使用金额：" + quarterUsageVal + "，年返使用金额：" + annualUsageVal
                + "，月返比例：" + rate;

        monRebatInfo.put("monRebatVal", monRebatVal);
        monRebatInfo.put("monOrderVal", monOrderVal);
        monRebatInfo.put("rebatInfo", rebatInfo);

        return monRebatInfo;
    }

    //获取季返及计算金额详情
    public Map<String, Object> getQuarterRebatInfo(RecordSet rs, String kh, String pp, int quarter) {
        Map<String, Object> quarterRebatInfo = new HashMap<>();

        int year = Util.getIntValue(LocalDateUtil.getLocalYearStr()); //当前年
        //如果是第四季度，年返需减1，因为第四季度是跨年计算返利的
        if(quarter == 4){
            year = year - 1;
        }

        //订单发货日期范围
        String startDate = LocalDateUtil.getQuarterStartDate(year, quarter);
        String endDate = LocalDateUtil.getQuarterEndDate(year, quarter);

        double orderShipVal = getOrderShipVal(rs, kh, pp, startDate, endDate, QR); //享受季返发货订单金额
        double orderReturnVal = getOrderReturnVal(rs, kh, pp, startDate, endDate); //退货订单金额
        double orderExchangeVal = getOrderExchangeVal(rs, kh, pp, startDate, endDate); //换货订单金额
        double rate = getRate(rs, kh, pp, QR); //返利比例

        //季返计算返利金额=（正常发货订单-退货订单+换货订单-不享受季返年返订单金额）*比例
        double quarterRebatVal = (orderShipVal - orderReturnVal + orderExchangeVal) * rate;
        //订单计算总金额
        double quarterOrderVal = orderShipVal - orderReturnVal + orderExchangeVal;
        //季返计算详情金额组成
        String rebatInfo = "享受季返的发货订单金额：" + orderShipVal + "，退货订单金额：" + orderReturnVal
                + "，换货订单金额：" + orderExchangeVal + "，季返比例：" + rate;

        quarterRebatInfo.put("quarterRebatVal", quarterRebatVal);
        quarterRebatInfo.put("quarterOrderVal", quarterOrderVal);
        quarterRebatInfo.put("rebatInfo", rebatInfo);

        return quarterRebatInfo;
    }

    //获取季返达标金额及计算详情
    public Map<String, Object> getQuarterTargetInfo(RecordSet rs, String kh, String pp, int quarter) {
        Map<String, Object> quarterTargetInfo = new HashMap<>();

        int year = Util.getIntValue(LocalDateUtil.getLocalYearStr()); //当前年
        //如果是第四季度，年返需减1，因为第四季度是跨年计算返利的
        if(quarter == 4){
            year = year - 1;
        }

        //订单发货日期范围
        String startDate = LocalDateUtil.getQuarterStartDate(year, quarter);
        String endDate = LocalDateUtil.getQuarterEndDate(year, quarter);

        double normalOrderVal = getNormalOrderVal(rs, kh, pp, startDate, endDate); //正常发货订单金额
        double orderReturnVal = getOrderReturnVal(rs, kh, pp, startDate, endDate); //退货订单金额
        double orderExchangeVal = getOrderExchangeVal(rs, kh, pp, startDate, endDate); //换货订单金额
        double orderDecalVal = getOrderDecalVal(rs, kh, pp, startDate, endDate); //贴花使用金额
        double quarterUsageVal = getQuarterUsageVal(rs, kh, pp, startDate, endDate); //季返使用金额
        double annualUsageVal = getAnnualUsageVal(rs, kh, pp, startDate, endDate); //年返使用金额
        double nonReturnVal = getNonReturnVal(rs, kh, pp, startDate, endDate); //1%无退换发货金额

        //季返年返计算返利达标金额=正常发货订单-退货订单+换货订单+贴花使用金额+季返使用金额+年返使用金额（上年）+1%无退换发货金额
        /*double quarterTargetVal = normalOrderVal - orderReturnVal + orderExchangeVal + orderDecalVal
                + quarterUsageVal + annualUsageVal + nonReturnVal;*/

        //25年新政策  季返年返计算返利达标金额=正常发货订单-退货订单+换货订单+贴花使用金额+1%无退换发货金额
        double quarterTargetVal = normalOrderVal - orderReturnVal + orderExchangeVal + orderDecalVal
                 + nonReturnVal;

        //季返计算详情金额组成
        String rebatInfo = "正常发货订单金额：" + normalOrderVal + "，退货订单金额：" + orderReturnVal
                + "，换货订单金额：" + orderExchangeVal + "，贴花使用金额：" + orderDecalVal
                + "，季返使用金额：" + quarterUsageVal + "，年返使用金额：" + annualUsageVal
                + "，1%无退换发货金额：" + nonReturnVal;

        quarterTargetInfo.put("quarterTargetVal", quarterTargetVal);
        quarterTargetInfo.put("rebatInfo", rebatInfo);

        return quarterTargetInfo;
    }

    //获取年返及计算金额详情
    public Map<String, Object> getYearRebatInfo(RecordSet rs, String kh, String pp) {
        Map<String, Object> yearRebatInfo = new HashMap<>();

        //订单发货日期范围
        String startDate = LocalDateUtil.getFirstDayOfLastYear();
        String endDate = LocalDateUtil.getLastDayOfLastYear();

        double orderShipVal = getOrderShipVal(rs, kh, pp, startDate, endDate, YR); //享受年返发货订单金额
        double orderReturnVal = getOrderReturnVal(rs, kh, pp, startDate, endDate); //退货订单金额
        double orderExchangeVal = getOrderExchangeVal(rs, kh, pp, startDate, endDate); //换货订单金额
        double rate = getRate(rs, kh, pp, YR); //返利比例

        //年返计算返利金额=（正常发货订单-退货订单+换货订单-不享受季返年返订单金额）*比例
        double yearRebatVal = (orderShipVal - orderReturnVal + orderExchangeVal) * rate;
        //订单计算总金额
        double yearOrderVal = orderShipVal - orderReturnVal + orderExchangeVal;
        //年返计算详情金额组成
        String rebatInfo = "享受季返的发货订单金额：" + orderShipVal + "，退货订单金额：" + orderReturnVal
                + "，换货订单金额：" + orderExchangeVal + "，年返比例：" + rate;

        yearRebatInfo.put("yearRebatVal", yearRebatVal);
        yearRebatInfo.put("yearOrderVal", yearOrderVal);
        yearRebatInfo.put("rebatInfo", rebatInfo);

        return yearRebatInfo;
    }



    //获取年返达标金额及计算详情
    public Map<String, Object> getYearTargetInfo(RecordSet rs, String kh, String pp) {
        Map<String, Object> yearargetInfo = new HashMap<>();

        //订单发货日期范围
        String startDate = LocalDateUtil.getFirstDayOfLastYear();
        String endDate = LocalDateUtil.getLastDayOfLastYear();

        double normalOrderVal = getNormalOrderVal(rs, kh, pp, startDate, endDate); //正常发货订单金额
        double orderReturnVal = getOrderReturnVal(rs, kh, pp, startDate, endDate); //退货订单金额
        double orderExchangeVal = getOrderExchangeVal(rs, kh, pp, startDate, endDate); //换货订单金额
        double orderDecalVal = getOrderDecalVal(rs, kh, pp, startDate, endDate); //贴花使用金额
        double quarterUsageVal = getQuarterUsageVal(rs, kh, pp, startDate, endDate); //季返使用金额
        double annualUsageVal = getAnnualUsageVal(rs, kh, pp, startDate, endDate); //年返使用金额
        double nonReturnVal = getNonReturnVal(rs, kh, pp, startDate, endDate); //1%无退换发货金额

        //季返年返计算返利达标金额=正常发货订单-退货订单+换货订单+贴花使用金额+季返使用金额+年返使用金额（上年）+1%无退换发货金额
        /*double yearTargetVal = normalOrderVal - orderReturnVal + orderExchangeVal + orderDecalVal
                + quarterUsageVal + annualUsageVal + nonReturnVal;*/


        //25年新政策  季返年返计算返利达标金额=正常发货订单-退货订单+换货订单+贴花使用金额+1%无退换发货金额
        double yearTargetVal = normalOrderVal - orderReturnVal + orderExchangeVal + orderDecalVal
                + nonReturnVal;

        //年返计算详情金额组成
        String rebatInfo = "正常发货订单金额：" + normalOrderVal + "，退货订单金额：" + orderReturnVal
                + "，换货订单金额：" + orderExchangeVal + "，贴花使用金额：" + orderDecalVal
                + "，季返使用金额：" + quarterUsageVal + "，年返使用金额：" + annualUsageVal
                + "，1%无退换发货金额：" + nonReturnVal;

        yearargetInfo.put("yearTargetVal", yearTargetVal);
        yearargetInfo.put("rebatInfo", rebatInfo);

        return yearargetInfo;
    }


    //根据客户、品牌、日期范围、返利类型获取享受返利的发货订单金额，享受返利的发货订单=正常发货订单-不享受月返订单金额
    public double getOrderShipVal(RecordSet rs, String kh, String pp, String startDate, String endDate, String rebateType){
        double orderShipVal = 0;
        String sql = " select sum(dpzfje * hpsl) as je from " + fthjil + " where fhrq >= '"
                + startDate + "' and fhrq <= '" + endDate
                + "' and kh= '" + kh + "' and pp = '" + pp + "' and lx = 0 and ddfllx = 0 ";
        if(MR.equals(rebateType)){
            sql = sql + " and sfcyyf = 0 ";
        } else if (QR.equals(rebateType)) {
            sql = sql + " and sfcyjf = 0 ";
        } else if (YR.equals(rebateType)) {
            sql = sql + " and sfcynf = 0 ";
        }
        bean.writeLog("获取享受月返的发货订单金额sql:" + sql);
        rs.executeQuery(sql);
        if (rs.next()){
            orderShipVal = Util.getDoubleValue(rs.getString("je"), 0);
        }
        return orderShipVal;
    }

    //根据客户、品牌、日期范围获取退货订单金额
    public double getOrderReturnVal(RecordSet rs, String kh, String pp, String startDate, String endDate){
        double orderReturnVal = 0;
        String sql = " select sum(dpzfje * hpsl) as je from " + fthjil + " where fhrq >= '"
                + startDate + "' and fhrq <= '" + endDate
                + "' and kh= '" + kh + "' and pp = '" + pp + "' and lx = 1  ";
        bean.writeLog("获取退货订单金额sql:" + sql);
        rs.executeQuery(sql);
        if (rs.next()){
            orderReturnVal = Util.getDoubleValue(rs.getString("je"), 0);
        }
        return orderReturnVal;
    }

    //根据客户、品牌、日期范围换货订单金额 换货订单=流程退换货类型+发货类型
    public double getOrderExchangeVal(RecordSet rs, String kh, String pp, String startDate, String endDate){
        double orderExchangeVal = 0;
        String sql = " select sum(dpzfje * hpsl) as je from " + fthjil + " where fhrq >= '"
                + startDate + "' and fhrq <= '" + endDate
                + "' and kh= '" + kh + "' and pp = '" + pp + "' and lx = 0 and lclx = 3";
        bean.writeLog("获取换货订单金额sql:" + sql);
        rs.executeQuery(sql);
        if (rs.next()){
            orderExchangeVal = Util.getDoubleValue(rs.getString("je"), 0);
        }
        return orderExchangeVal;
    }

    //根据客户、品牌、日期范围获取贴花使用金额
    public double getOrderDecalVal(RecordSet rs, String kh, String pp, String startDate, String endDate){
        double orderDecalVal = 0;
        String sql = " select sum(dpzfje * hpsl) as je from " + fthjil + " where fhrq >= '"
                + startDate + "' and fhrq <= '" + endDate
                + "' and kh= '" + kh + "' and pp = '" + pp + "' and lx = 0 and ddfllx = 3";
        bean.writeLog("获取贴花使用金额sql:" + sql);
        rs.executeQuery(sql);
        if (rs.next()){
            orderDecalVal = Util.getDoubleValue(rs.getString("je"), 0);
        }
        return orderDecalVal;
    }

    //根据客户、品牌、日期范围获取季返使用金额
    public double getQuarterUsageVal(RecordSet rs, String kh, String pp, String startDate, String endDate){
        double quarterUsageVal = 0;
        String sql = " select sum(dpzfje * hpsl) as je from " + fthjil + " where fhrq >= '"
                + startDate + "' and fhrq <= '" + endDate
                + "' and kh= '" + kh + "' and pp = '" + pp + "' and lx = 0 and ddfllx = 1 and syfllx = 1";
        bean.writeLog("获取季返使用金额sql:" + sql);
        rs.executeQuery(sql);
        if (rs.next()){
            quarterUsageVal = Util.getDoubleValue(rs.getString("je"), 0);
        }
        return quarterUsageVal;
    }

    //根据客户、品牌、日期范围获取年返使用金额
    public double getAnnualUsageVal(RecordSet rs, String kh, String pp, String startDate, String endDate){
        double annualUsageVal = 0;
        String sql = " select sum(dpzfje * hpsl) as je from " + fthjil + " where fhrq >= '"
                + startDate + "' and fhrq <= '" + endDate
                + "' and kh= '" + kh + "' and pp = '" + pp + "' and lx = 0 and ddfllx = 1 and syfllx = 2";
        bean.writeLog("获取年返使用金额sql:" + sql);
        rs.executeQuery(sql);
        if (rs.next()){
            annualUsageVal = Util.getDoubleValue(rs.getString("je"), 0);
        }
        return annualUsageVal;
    }

    //根据客户、品牌、返利类型获取返利比例
    public double getRate(RecordSet rs, String kh, String pp, String rebateType){
        double rate = 0;
        String year = LocalDateUtil.getLocalYearStr();
        String sel = "";
        if(MR.equals(rebateType)){
            sel = "yfbl";
            if("01".equals(LocalDateUtil.getLocaMonthStr())){ //如果当前是1月则需考虑跨年情况 获取前一年
                year = LocalDateUtil.getLastYearStr();
            }
        } else if (QR.equals(rebateType)) {
            sel = "jfbl";
            if(Q4.equals(LocalDateUtil.getLocaMonDayStr())){ //如果当前是计算第4季度返利则需考虑跨年情况 获取前一年
                year = LocalDateUtil.getLastYearStr();
            }
        } else if (YR.equals(rebateType)) {
            sel = "nfbl";
            year = LocalDateUtil.getLastYearStr();
        }
        String sql = "select " + sel + " bl from " + flmbk + " where khmc = '" + kh + "' and pp = '" + pp + "' " +
                " and nf = '" + year + "'";
        bean.writeLog("获取返利比例sql:" + sql);
        rs.executeQuery(sql);
        if (rs.next()){
            rate = Util.getDoubleValue(rs.getString("bl"), 0) / 100; //数据为整数需除以100
        }
        return rate;
    }

    //根据客户、品牌、年份、返利类型获取返利比例
    public double getRate(RecordSet rs, String kh, String pp, String year, String rebateType){
        double rate = 0;
        String sel = "";
        if(MR.equals(rebateType)){
            sel = "yfbl";
        } else if (QR.equals(rebateType)) {
            sel = "jfbl";
        } else if (YR.equals(rebateType)) {
            sel = "nfbl";
        }
        String sql = "select " + sel + " bl from " + flmbk + " where khmc = '" + kh + "' and pp = '" + pp + "' " +
                " and nf = '" + year + "'";
        bean.writeLog("获取返利比例sql：" + sql);
        rs.executeQuery(sql);
        if (rs.next()){
            rate = Util.getDoubleValue(rs.getString("bl"), 0) / 100; //数据为整数需除以100
        }
        return rate;
    }

    //根据客户、品牌、日期范围获取1%无退换发货金额，无退货发货=货补类型+无退货返利额度类型
    public double getNonReturnVal(RecordSet rs, String kh, String pp, String startDate, String endDate){
        double nonReturnVal = 0;
        String sql = " select sum(hbsjddj * hpsl) as je from " + fthjil + " where fhrq >= '"
                + startDate + "' and fhrq <= '" + endDate
                + "' and kh= '" + kh + "' and pp = '" + pp + "' and lx = 4 and hbedlx = 2 ";
        bean.writeLog("获取1%无退换发货金额sql:" + sql);
        rs.executeQuery(sql);
        if (rs.next()){
            nonReturnVal = Util.getDoubleValue(rs.getString("je"), 0);
        }
        return nonReturnVal;
    }

    //根据客户、品牌、日期范围获取正常订单金额
    public double getNormalOrderVal(RecordSet rs, String kh, String pp, String startDate, String endDate){
        double normalOrderVal = 0;
        String sql = " select sum(dpzfje * hpsl) as je from " + fthjil + " where fhrq >= '"
                + startDate + "' and fhrq <= '" + endDate
                + "' and kh= '" + kh + "' and pp = '" + pp + "' and lx = 0 and ddfllx = 0 ";
        bean.writeLog("获取正常订单金额sql:" + sql);
        rs.executeQuery(sql);
        if (rs.next()){
            normalOrderVal = Util.getDoubleValue(rs.getString("je"), 0);
        }
        return normalOrderVal;
    }


    //根据客户、品牌、年份、返利类型获取季返年返达标金额
    public double getTargetVal(RecordSet rs, String kh, String pp, String rebateType){
        double targetVal = 0;
        String sel = "";
        String year = LocalDateUtil.getLocalYearStr();
        if (QR.equals(rebateType)) {
            String mdStr = LocalDateUtil.getLocaMonDayStr(); //当前月日
            //用日期判断当前取哪个季度返利
            if (Q1.equals(mdStr)) {
                sel = "q1mb";
            } else if (Q2.equals(mdStr)) {
                sel = "q2mb";
            } else if (Q3.equals(mdStr)) {
                sel = "q3mb";
            } else if (Q4.equals(mdStr)) {
                sel = "q4mb";
                year = LocalDateUtil.getLastYearStr(); //跨年计算
            }
        } else if (YR.equals(rebateType)) {
            sel = "ndflmb";
            year = LocalDateUtil.getLastYearStr(); //跨年计算
        }
        String sql = "select " + sel + " je from " + flmbk + " where khmc = '" + kh + "' and pp = '" + pp + "' " +
                " and nf = '" + year + "'";
        bean.writeLog("获取季返年返达标金额sql:" + sql);
        rs.executeQuery(sql);
        if (rs.next()){
            targetVal = Util.getDoubleValue(rs.getString("je"), 0);
        }
        return targetVal;
    }

    //根据客户、品牌判断是否追加季返
    public String isAddRebate(RecordSet rs, String kh, String pp){
        String bool = "1";
        String sql = "select zjfl bool from " + flmbk + " where khmc = '" + kh + "' and pp = '" + pp + "' " +
                " and nf = '" + LocalDateUtil.getLastYearStr() + "'";
        rs.executeQuery(sql);
        if (rs.next()){
            bool = Util.null2String(rs.getString("bool"));
        }
        return bool;
    }

    //追加季返并生成返利记录
    public void addQuarterRebate(RecordSet rs, String kh, String pp, ModeDataIdUpdate idUpdate, ModeRightInfo modeRightInfo){
        double sum = 0;
        String info = "达到年度目标追加季度返利详情：";
        String year = LocalDateUtil.getLastYearStr();
        double rate = getRate(rs, kh, pp, year, QR); //获取比例
        String sqlIn = "'" + year + "-03'," + "'" + year + "-06'," + "'" + year + "-09'," + "'" + year + "-12'";
        String sql = "select ny,je from " + fldbjl + " where khmc = '" + kh + "' and pp = '" + pp + "' " +
                " and fllx = 1 and sfdb = 1 and ny in (" + sqlIn + ") order by ny";
        rs.executeQuery(sql);
        while (rs.next()){
            String je = rs.getString("je");
            sum = sum + Util.getDoubleValue(je,0);
            String mon = rs.getString("ny").split("-")[1];
            if (mon.equals("03")){
                info = info + "第一季度返利计算金额：" + je + ",";
            } else if (mon.equals("06")) {
                info = info + "第二季度返利计算金额：" + je + ",";
            }else if (mon.equals("09")) {
                info = info + "第三季度返利计算金额：" + je + ",";
            }else if (mon.equals("12")) {
                info = info + "第四季度返利计算金额：" + je + ",";
            }
        }
        double rebatVal = sum * rate;
        info = info + "返利比例：" + rate;

        //生成返利记录
        Map<String, String> sqlMap = new HashMap<>();
        sqlMap.put("khmc", kh);
        sqlMap.put("pp", pp);
        sqlMap.put("ny", LocalDateUtil.getLastYearMonthStr());
        sqlMap.put("fllx", "1"); //1季返
        sqlMap.put("sfdb", "0");
        sqlMap.put("je", Util.null2String(sum));
        sqlMap.put("flje", Util.null2String(rebatVal));
        sqlMap.put("bz", info);
        int billid = ToolsFunction.insertMode(fldbjlModeId, fldbjl, sqlMap, 1, idUpdate, modeRightInfo, rs);
        //更新返利库
        boolean sqlState = rs.executeUpdate("update " + flk + " set jdflje = ifnull(jdflje,0) + " + rebatVal
                + " , kyjfje = ifnull(kyjfje,0) + " + rebatVal + " where khmc = '" + kh
                + "' and pp = '" + pp + "'");
        //更新返利达标记录“是否已计算返利”
        int state = sqlState? 0:1;
        rs.executeUpdate("update " + fldbjl + " set sfyjsfl = ? where id = ?", state, billid);

    }


    


















}
