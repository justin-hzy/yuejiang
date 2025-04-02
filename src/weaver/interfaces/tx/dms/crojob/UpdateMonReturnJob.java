package weaver.interfaces.tx.dms.crojob;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;
import weaver.interfaces.tx.util.LocalDateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * FileName: UpdateMonReturnJob.java
 * 更新月返
 *
 * @Author tx
 * @Date 2024/10/24
 * @Version 1.0
 **/
public class UpdateMonReturnJob extends BaseCronJob {

    private String flcfjl = "uf_flcfjl"; //返利处罚记录
    private String fldbjl = "uf_fldbjl"; //返利达标记录
    private String flk = "uf_flk";//返利库
    private String kh = "uf_kh";//客户库
    private String YF; //月返更新日期 dd
    private BaseBean bean = new BaseBean();

    @Override
    public void execute() {
        if (YF.equals(LocalDateUtil.getLocaDayStr())) { //判断当前日期是否进行月返更新
            RecordSet queRs = new RecordSet();
            RecordSet exeRs = new RecordSet();
            updateOnlineAgent(queRs);
            updateOfflineAgent(queRs, exeRs);
        }
    }

    //更新线上代理商月返，每月定时将上个月返金额更新至返利库中
    public void updateOnlineAgent(RecordSet rs){
        bean.writeLog("开始更新线上代理商月返！");

        String ny = LocalDateUtil.getLastYearMonthStr();
        String sql = "update " + flk + " t1 " +
                " join " + kh + " t2 on t1.khmc = t2.khbh " +
                " join (select pp,khmc,sum( flje ) fljes from " + fldbjl + " where ny = '" + ny + "' " +
                " and fllx = 0 and ( sfyjsfl != 0 or sfyjsfl is null ) group by pp,khmc ) t3 on t1.pp = t3.pp and t1.khmc = t3.khmc " +
                " set t1.ydflje = ifnull(t1.ydflje, 0) + ifnull(t3.fljes, 0), " +
                "     t1.kyyfje = ifnull(t1.kyyfje, 0) + ifnull(t3.fljes, 0) " +
                " where t2.khlx = 1 ";
        boolean sqlState = rs.executeUpdate(sql);

        bean.writeLog("更新线上代理商月返sql：" + sql);

        //更新返利达标记录表线上代理商月返记录“是否已计算返利”状态
        int state = sqlState? 0:1;
        rs.executeUpdate("update " + fldbjl + " t1 join " + kh + " t2 on t1.khmc = t2.khbh set sfyjsfl = " + state +
                " where t1.ny = '" + ny + "' and t1.fllx = 0 and t2.khlx = 1 and ( t1.sfyjsfl != 0 or t1.sfyjsfl is null ) ");

    }

    //更新线下代理商月返，每月定时将前三个月返金额更新至返利库中
    public void updateOfflineAgent(RecordSet queRs, RecordSet exeRs){
        bean.writeLog("开始更新线下代理商月返！");

        //年月范围
        String startNy = LocalDateUtil.getPreviousYearMonthStr(3);
        String endNy = LocalDateUtil.getLastYearMonthStr();

        //查询哪些客户及品牌存在违规情况
        List<String> violateList = new ArrayList<>(); //违规客户及品牌集合
        queRs.executeQuery("select khmc,pp from  " + flcfjl + " where  " +
                " ny >= '" + startNy + "' and ny <= '" + endNy + "'" +
                " group by khmc,pp  having sum(wgcs) > 0 ");

        while (queRs.next()){
            violateList.add(queRs.getString("khmc") + "_" + queRs.getString("pp"));
        }
        bean.writeLog("违规客户及品牌：" + violateList);

        //更新线下代理商、线下直营月返
        queRs.executeQuery("select t1.khmc,t1.pp,sum(t1.flje) fljez from " + fldbjl + " t1 " +
                " join " + kh + " t2 on t1.khmc = t2.khbh " +
                " where ny='" + startNy + "' and t1.fllx = 0 " +
                " and ( t1.sfyjsfl != 0 or t1.sfyjsfl is null ) " +
                " and t2.khlx in (0,4) group by t1.khmc,t1.pp ");
        while (queRs.next()){
            String khmc = queRs.getString("khmc");
            String pp = queRs.getString("pp");
            double fljez = Util.getDoubleValue(queRs.getString("fljez"), 0);

            //判断是否有违规情况
            if(!violateList.contains(khmc + "_" + pp)){
                //更新返利库
                boolean sqlState = exeRs.executeUpdate("update " + flk +
                        " set ydflje = ifnull(ydflje, 0) + " + fljez +
                        ", kyyfje = ifnull(kyyfje, 0) + " + fljez +
                        " where khmc = '" + khmc  + "' and pp = '" + pp + "'");

                //更新返利达标记录“是否已计算返利”状态
                int state = sqlState? 0:1;
                exeRs.executeUpdate("update " + fldbjl + " set sfyjsfl = " + state +
                        " where ny = '" + startNy + "' and fllx = 0 and khmc = '" + khmc + "' and pp = '" + pp + "'" +
                        " and ( sfyjsfl != 0 or sfyjsfl is null )");
            }else {
                //更新返利达标记录“是否已计算返利”状态
                exeRs.executeUpdate("update " + fldbjl + " set sfyjsfl = 1 , " +
                        " bz = concat(bz, '（因" + startNy + "至" + endNy + "期间存在违规情况，故不发放此笔月返金额）')" +
                        " where ny = '" + startNy + "' and fllx = 0 and khmc = '" + khmc + "' and pp = '" + pp + "'" +
                        " and ( sfyjsfl != 0 or sfyjsfl is null )");
            }

        }

    }

}
