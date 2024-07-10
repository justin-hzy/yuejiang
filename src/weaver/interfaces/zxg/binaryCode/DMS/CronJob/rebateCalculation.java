package weaver.interfaces.zxg.binaryCode.DMS.CronJob;

import com.api.integration.Base;
import weaver.conn.RecordSet;
import weaver.formmode.data.ModeDataIdUpdate;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;
import weaver.interfaces.zxg.binaryCode.DMS.util.GetDataUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class rebateCalculation extends BaseCronJob {

    private String uf_fldbjl = "uf_fldbjl"; //返利达标记录
    //private Integer mode__fldbjl = 59;
    private Integer mode__fldbjl = 70;
    private String uf_fthjil = "uf_fthjil"; //发退货记录
    private String uf_flmbk = "uf_flmbk"; //返利目标
    private String flk = "uf_flk";//返利库11111
    GetDataUtil getDataUtil = new GetDataUtil();

    private String yfscrq;//月份生成日期
    private String varQ1;
    private String varQ2;
    private String varQ3;
    private String varQ4;
    private String ndscrq;//年度生成日期
    //private Integer mode__fldbjl = Integer.valueOf(modeid);


    @Override
    public void execute() {
        method1();
        method2();
        method3();
    }
    //遍历发退货生成月度达标记录
    public void method1() {
        RecordSet rs = new RecordSet();
        //月返 每月1号生成
        if(yfscrq.equals(getDataUtil.getDay())){
            String v_sql = " select distinct kh,pp from "+uf_fthjil+" where fhrq >= '"+getDataUtil.accMm()+"-01' and fhrq <= '"+getDataUtil.accMm()+"-31'";
            new BaseBean().writeLog("月返:"+ v_sql);
            rs.execute(v_sql);
            while (rs.next()){
                try {
                    ModeDataIdUpdate idUpdate = new ModeDataIdUpdate();
                    int billid = idUpdate.getModeDataNewId(uf_fldbjl, mode__fldbjl, 1, 1, new SimpleDateFormat("yyyy-MM-dd").format(new Date()), new SimpleDateFormat("HH:mm:ss").format(new Date()));
                    rs.executeUpdate(" update "+uf_fldbjl+" set khmc=?,pp=?,ny=?,fllx=?,je=?,flje=?,sfyjsfl=?  where id='" + billid + "'",rs.getString(1),rs.getString(2),getDataUtil.accMm(),0,0,0,0);
                    //更新表单建模 权限
                    //计算金额
                    new BaseBean().writeLog("金额"+method1_je(rs.getString(1),rs.getString(2)));
                    rs.executeUpdate(" update "+uf_fldbjl+" set je=? where id='" + billid + "'",method1_je(rs.getString(1),rs.getString(2)));
                    //计算返利金额
                    rs.executeUpdate(" update "+uf_fldbjl+" set flje=? where id='" + billid + "'",rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getYear(),"0","",method1_je(rs.getString(1),rs.getString(2))));
                    //更新返利库
                    rs.execute( " update "+flk+" set ydflje = ifnull(ydflje,'0')+ "+rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getYear(),"0","",method1_je(rs.getString(1),rs.getString(2)))+",kyyfje = ifnull(kyyfje,'0')+ "+rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getYear(),"0","",method1_je(rs.getString(1),rs.getString(2)))+"  where khmc = '"+rs.getString(1)+"'and pp = '"+rs.getString(2)+"' ");
                    //更新是否计算返利
                    rs.executeUpdate(" update "+uf_fldbjl+" set sfyjsfl=? where id='" + billid + "'",0);
                    new ModeRightInfo().editModeDataShare(1, mode__fldbjl, billid);//重置建模权限
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }
    //计算出返利达标记录的金额字段 月度
    public double method1_je(String kh,String pp) {
        double fhje = 0;
        double thje = 0;
        RecordSet rs = new RecordSet();
        String v_sql_fh = " select sum(dpzfje * hpsl) as je from "+uf_fthjil+" where fhrq >= '"+getDataUtil.accMm()+"-01' and fhrq <= '"+getDataUtil.accMm()+"-31' and lx = '0' and kh= '"+kh+"' and pp = '"+pp+"' and sfcyyf = '0' and ddfllx = '0' ";
        new BaseBean().writeLog("v_sql_fh"+v_sql_fh);
        rs.execute(v_sql_fh);
        if(rs.next()){
            new BaseBean().writeLog("fhje"+rs.getDouble(1));
            if(rs.getDouble(1)==-1.0||"-1.0".equals(rs.getDouble(1))){
                fhje = 0;
            }else{
                fhje = rs.getDouble(1);
            }

        }

        String v_sql_th = " select sum(dpzfje * hpsl) as je from "+uf_fthjil+" where fhrq >= '"+getDataUtil.accMm()+"-01' and fhrq <= '"+getDataUtil.accMm()+"-31' and lx = '1' and kh= '"+kh+"' and pp = '"+pp+"' ";
        new BaseBean().writeLog("v_sql_th"+v_sql_th);
        rs.execute(v_sql_th);
        if(rs.next()){
            if(rs.getDouble(1)==-1.0||"-1.0".equals(rs.getDouble(1))){
                thje = 0;
            }else{
                thje = rs.getDouble(1);
            }
        }
        new BaseBean().writeLog("fhje"+fhje+"thje"+thje);
        return fhje - thje;
        }

    //遍历发退货生成季度达标记录
    public void method2() {
        RecordSet rs = new RecordSet();
        //季度 Q4:01-01 Q1:04-01 Q2:07-01 Q3:10-01
        //一季度
        if(varQ1.equals(getDataUtil.getYr())){
            String v_sql = " select distinct kh,pp from "+uf_fthjil+" where fhrq >= '"+getDataUtil.getYear()+"-01-01' and fhrq <= '"+getDataUtil.getYear()+"-03-31'";
            new BaseBean().writeLog("Q1季返:"+ v_sql);
            rs.execute(v_sql);
            while (rs.next()){
                    try {
                        ModeDataIdUpdate idUpdate = new ModeDataIdUpdate();
                        int billid = idUpdate.getModeDataNewId(uf_fldbjl, mode__fldbjl, 1, 1, new SimpleDateFormat("yyyy-MM-dd").format(new Date()), new SimpleDateFormat("HH:mm:ss").format(new Date()));
                        rs.executeUpdate(" update "+uf_fldbjl+" set khmc=?,pp=?,ny=?,fllx=?,je=?,flje=?,sfyjsfl=?  where id='" + billid + "'",rs.getString(1),rs.getString(2),getDataUtil.accMm(),1,0,0,0);
                        //更新表单建模 权限
                        //计算金额
                        new BaseBean().writeLog("返利金额Q1"+method2_je(rs.getString(1),rs.getString(2),"1"));
                        rs.executeUpdate(" update "+uf_fldbjl+" set je=? where id='" + billid + "'",method2_je(rs.getString(1),rs.getString(2),"1"));
                        //计算返利金额
                        rs.executeUpdate(" update "+uf_fldbjl+" set flje=? where id='" + billid + "'",rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getYear(),"1","1",method2_je(rs.getString(1),rs.getString(2),"1")));
                        //计算达标金额
                        double dbje = method2_je(rs.getString(1),rs.getString(2),"1")+method2_dbje(rs.getString(1),rs.getString(2),"1");
                        rs.executeUpdate(" update "+uf_fldbjl+" set fldbje=? where id='" + billid + "'",dbje);
                        //是否达标字段赋值
                        if(rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getYear(),"1","1",method2_je(rs.getString(1),rs.getString(2),"1"))==0){
                            rs.executeUpdate(" update "+uf_fldbjl+" set sfdb=? where id='" + billid + "'",1);
                        }else{
                            rs.executeUpdate(" update "+uf_fldbjl+" set sfdb=? where id='" + billid + "'",0);
                        }
                        //更新返利库
                        rs.execute( " update "+flk+" set jdflje = ifnull(jdflje,'0')+ "+rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getYear(),"1","1",method2_je(rs.getString(1),rs.getString(2),"1"))+",kyjfje = ifnull(kyjfje,'0')+ "+rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getYear(),"1","1",method2_je(rs.getString(1),rs.getString(2),"1"))+"  where khmc = '"+rs.getString(1)+"'and pp = '"+rs.getString(2)+"'");

                        //更新是否计算返利
                        rs.executeUpdate(" update "+uf_fldbjl+" set sfyjsfl=? where id='" + billid + "'",0);

                        new ModeRightInfo().editModeDataShare(1, mode__fldbjl, billid);//重置建模权限
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }else  if(varQ2.equals(getDataUtil.getYr())){
            String v_sql = " select distinct kh,pp from "+uf_fthjil+" where fhrq >= '"+getDataUtil.getYear()+"-04-01' and fhrq <= '"+getDataUtil.getYear()+"-06-31'";
            new BaseBean().writeLog("Q2季返:"+ v_sql);
            rs.execute(v_sql);
            while (rs.next()){
                try {
                    ModeDataIdUpdate idUpdate = new ModeDataIdUpdate();
                    int billid = idUpdate.getModeDataNewId(uf_fldbjl, mode__fldbjl, 1, 1, new SimpleDateFormat("yyyy-MM-dd").format(new Date()), new SimpleDateFormat("HH:mm:ss").format(new Date()));
                    rs.executeUpdate(" update "+uf_fldbjl+" set khmc=?,pp=?,ny=?,fllx=?,je=?,flje=?,sfyjsfl=?  where id='" + billid + "'",rs.getString(1),rs.getString(2),getDataUtil.accMm(),1,0,0,0);
                    //更新表单建模 权限
                    //计算返利
                    new BaseBean().writeLog("返利金额Q2"+method2_je(rs.getString(1),rs.getString(2),"2"));
                    rs.executeUpdate(" update "+uf_fldbjl+" set je=? where id='" + billid + "'",method2_je(rs.getString(1),rs.getString(2),"2"));
                    //计算返利金额
                    rs.executeUpdate(" update "+uf_fldbjl+" set flje=? where id='" + billid + "'",rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getYear(),"1","2",method2_je(rs.getString(1),rs.getString(2),"2")));
                    //计算达标金额
                    double dbje = method2_je(rs.getString(1),rs.getString(2),"2")+method2_dbje(rs.getString(1),rs.getString(2),"2");
                    rs.executeUpdate(" update "+uf_fldbjl+" set fldbje=? where id='" + billid + "'",dbje);
                    //是否达标字段赋值
                    if(rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getYear(),"1","2",method2_je(rs.getString(1),rs.getString(2),"2"))==0){
                        rs.executeUpdate(" update "+uf_fldbjl+" set sfdb=? where id='" + billid + "'",1);
                    }else{
                        rs.executeUpdate(" update "+uf_fldbjl+" set sfdb=? where id='" + billid + "'",0);
                    }
                    //更新返利库
                    rs.execute( " update "+flk+" set jdflje = ifnull(jdflje,'0')+ "+rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getYear(),"1","2",method2_je(rs.getString(1),rs.getString(2),"2"))+",kyjfje = ifnull(kyjfje,'0')+ "+rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getYear(),"1","2",method2_je(rs.getString(1),rs.getString(2),"2"))+" where khmc = '"+rs.getString(1)+"'and pp = '"+rs.getString(2)+"'");

                    //更新是否计算返利
                    rs.executeUpdate(" update "+uf_fldbjl+" set sfyjsfl=? where id='" + billid + "'",0);

                    new ModeRightInfo().editModeDataShare(1, mode__fldbjl, billid);//重置建模权限
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }else  if(varQ3.equals(getDataUtil.getYr())){
            String v_sql = " select distinct kh,pp from "+uf_fthjil+" where fhrq >= '"+getDataUtil.getYear()+"-07-01' and fhrq <= '"+getDataUtil.getYear()+"-09-31'";
            new BaseBean().writeLog("Q3季返:"+ v_sql);
            rs.execute(v_sql);
            while (rs.next()){
                try {
                    ModeDataIdUpdate idUpdate = new ModeDataIdUpdate();
                    int billid = idUpdate.getModeDataNewId(uf_fldbjl, mode__fldbjl, 1, 1, new SimpleDateFormat("yyyy-MM-dd").format(new Date()), new SimpleDateFormat("HH:mm:ss").format(new Date()));
                    rs.executeUpdate(" update "+uf_fldbjl+" set khmc=?,pp=?,ny=?,fllx=?,je=?,flje=?,sfyjsfl=?  where id='" + billid + "'",rs.getString(1),rs.getString(2),getDataUtil.accMm(),1,0,0,0);
                    //更新表单建模 权限
                    //计算返利
                    new BaseBean().writeLog("返利Q3"+method2_je(rs.getString(1),rs.getString(2),"3"));
                    rs.executeUpdate(" update "+uf_fldbjl+" set je=? where id='" + billid + "'",method2_je(rs.getString(1),rs.getString(2),"3"));
                    //计算返利金额
                    new BaseBean().writeLog("Q3返利金额"+rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getYear(),"1","3",method2_je(rs.getString(1),rs.getString(2),"3")));
                    rs.executeUpdate(" update "+uf_fldbjl+" set flje=? where id='" + billid + "'",rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getYear(),"1","3",method2_je(rs.getString(1),rs.getString(2),"3")));
                    //计算达标金额
                    double dbje = method2_je(rs.getString(1),rs.getString(2),"3")+method2_dbje(rs.getString(1),rs.getString(2),"3");
                    rs.executeUpdate(" update "+uf_fldbjl+" set fldbje=? where id='" + billid + "'",dbje);
                    //是否达标字段赋值
                    if(rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getYear(),"1","3",method2_je(rs.getString(1),rs.getString(2),"3"))==0){
                        rs.executeUpdate(" update "+uf_fldbjl+" set sfdb=? where id='" + billid + "'",1);
                    }else{
                        rs.executeUpdate(" update "+uf_fldbjl+" set sfdb=? where id='" + billid + "'",0);
                    }
                    //更新返利库
                    rs.execute( " update "+flk+" set jdflje = ifnull(jdflje,'0')+ "+rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getYear(),"1","3",method2_je(rs.getString(1),rs.getString(2),"3"))+",kyjfje = ifnull(kyjfje,'0')+ "+rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getYear(),"1","3",method2_je(rs.getString(1),rs.getString(2),"3"))+"  where khmc = '"+rs.getString(1)+"'and pp = '"+rs.getString(2)+"'");

                    //更新是否计算返利
                    rs.executeUpdate(" update "+uf_fldbjl+" set sfyjsfl=? where id='" + billid + "'",0);

                    new ModeRightInfo().editModeDataShare(1, mode__fldbjl, billid);//重置建模权限
                } catch (Exception e) {
                    new BaseBean().writeLog("错误"+e);
                    e.printStackTrace();
                }
            }
        }else  if(varQ4.equals(getDataUtil.getYr())){
            String v_sql = " select distinct kh,pp from "+uf_fthjil+" where fhrq >= '"+getDataUtil.getaccYear()+"-10-01' and fhrq <= '"+getDataUtil.getaccYear()+"-12-31'";
            new BaseBean().writeLog("Q4季返:"+ v_sql);
            rs.execute(v_sql);
            while (rs.next()){
                try {
                    ModeDataIdUpdate idUpdate = new ModeDataIdUpdate();
                    int billid = idUpdate.getModeDataNewId(uf_fldbjl, mode__fldbjl, 1, 1, new SimpleDateFormat("yyyy-MM-dd").format(new Date()), new SimpleDateFormat("HH:mm:ss").format(new Date()));
                    rs.executeUpdate(" update "+uf_fldbjl+" set khmc=?,pp=?,ny=?,fllx=?,je=?,flje=?,sfyjsfl=?  where id='" + billid + "'",rs.getString(1),rs.getString(2),getDataUtil.accMm(),1,0,0,0);
                    //更新表单建模 权限
                    //计算返利
                    new BaseBean().writeLog("返利金额Q4"+method2_je(rs.getString(1),rs.getString(2),"4"));
                    rs.executeUpdate(" update "+uf_fldbjl+" set je=? where id='" + billid + "'",method2_je(rs.getString(1),rs.getString(2),"4"));
                    //计算返利金额
                    rs.executeUpdate(" update "+uf_fldbjl+" set flje=? where id='" + billid + "'",rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getaccYear(),"1","4",method2_je(rs.getString(1),rs.getString(2),"4")));
                    //计算达标金额
                    double dbje = method2_je(rs.getString(1),rs.getString(2),"4")+method2_dbje(rs.getString(1),rs.getString(2),"4");
                    rs.executeUpdate(" update "+uf_fldbjl+" set fldbje=? where id='" + billid + "'",dbje);
                    //是否达标字段赋值
                    if(rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getaccYear(),"1","4",method2_je(rs.getString(1),rs.getString(2),"4"))==0){
                        rs.executeUpdate(" update "+uf_fldbjl+" set sfdb=? where id='" + billid + "'",1);
                    }else{
                        rs.executeUpdate(" update "+uf_fldbjl+" set sfdb=? where id='" + billid + "'",0);
                    }
                    //更新返利库
                    rs.execute( " update "+flk+" set jdflje = ifnull(jdflje,'0')+ "+rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getaccYear(),"1","4",method2_je(rs.getString(1),rs.getString(2),"4"))+",kyjfje = ifnull(kyjfje,'0')+ "+rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getaccYear(),"1","4",method2_je(rs.getString(1),rs.getString(2),"4"))+" where khmc = '"+rs.getString(1)+"'and pp = '"+rs.getString(2)+"' ");

                    //更新是否计算返利
                    rs.executeUpdate(" update "+uf_fldbjl+" set sfyjsfl=? where id='" + billid + "'",0);

                    new ModeRightInfo().editModeDataShare(1, mode__fldbjl, billid);//重置建模权限
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
          }
        }
    //计算出返利达标记录的金额字段 季返
    public double method2_je(String kh,String pp,String jd) {
        double fhje = 0;
        double thje = 0;
        double hbje = 0;
        double je = 0;
        RecordSet rs = new RecordSet();
        if("1".equals(jd)) {
            String v_sql_fh = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getYear()+"-01-01' and fhrq <= '"+getDataUtil.getYear()+"-03-31' and lx = '0' and kh= '"+kh+"' and pp = '"+pp+"' and sfcyjf ='0' and ddfllx = '0'  ";
            rs.execute(v_sql_fh);
            if (rs.next()) {
                if(rs.getDouble(1)==-1.0||"-1.0".equals(rs.getDouble(1))){
                    fhje = 0;
                }else{
                    fhje = rs.getDouble(1);
                }

            }

            String v_sql_th = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getYear()+"-01-01' and fhrq <= '"+getDataUtil.getYear()+"-03-31' and lx = '1' and kh= '"+kh+"' and pp = '"+pp+"' ";
            rs.execute(v_sql_th);
            if (rs.next()) {
                if(rs.getDouble(1)==-1.0||"-1.0".equals(rs.getDouble(1))){
                    thje = 0;
                }else{
                    thje = rs.getDouble(1);
                }
            }

//            String v_sql_hb = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getYear()+"-01-01' and fhrq <= '"+getDataUtil.getYear()+"-03-31' and lx = '4' and kh= '"+kh+"' and pp = '"+pp+"' ";
//            rs.execute(v_sql_hb);
//            if (rs.next()) {
//                hbje = rs.getDouble(1);
//            }
            je =  fhje - thje;
        }else if("2".equals(jd)){
            String v_sql_fh = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getYear()+"-04-01' and fhrq <= '"+getDataUtil.getYear()+"-06-31' and lx = '0' and kh= '"+kh+"' and pp = '"+pp+"' and sfcyjf ='0' and ddfllx = '0' ";
            rs.execute(v_sql_fh);
            if (rs.next()) {
                if(rs.getDouble(1)==-1.0||"-1.0".equals(rs.getDouble(1))){
                    fhje = 0;
                }else{
                    fhje = rs.getDouble(1);
                }
            }

            String v_sql_th = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getYear()+"-04-01' and fhrq <= '"+getDataUtil.getYear()+"-06-31' and lx = '1' and kh= '"+kh+"' and pp = '"+pp+"' ";
            rs.execute(v_sql_th);
            if (rs.next()) {
                if(rs.getDouble(1)==-1.0||"-1.0".equals(rs.getDouble(1))){
                    thje = 0;
                }else{
                    thje = rs.getDouble(1);
                }
            }
//            String v_sql_hb = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getYear()+"-04-01' and fhrq <= '"+getDataUtil.getYear()+"-06-31' and lx = '4' and kh= '"+kh+"' and pp = '"+pp+"' ";
//            rs.execute(v_sql_hb);
//            if (rs.next()) {
//                hbje = rs.getDouble(1);
//            }
            je =  fhje - thje ;

        }else if("3".equals(jd)){
            String v_sql_fh = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getYear()+"-07-01' and fhrq <= '"+getDataUtil.getYear()+"-09-31' and lx = '0' and kh= '"+kh+"' and pp = '"+pp+"' and sfcyjf ='0' and ddfllx = '0' ";
            rs.execute(v_sql_fh);
            if (rs.next()) {
                if(rs.getDouble(1)==-1.0||"-1.0".equals(rs.getDouble(1))){
                    fhje = 0;
                }else{
                    fhje = rs.getDouble(1);
                }
            }

            String v_sql_th = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getYear()+"-07-01' and fhrq <= '"+getDataUtil.getYear()+"-09-31' and lx = '1' and kh= '"+kh+"' and pp = '"+pp+"' ";
            rs.execute(v_sql_th);
            if (rs.next()) {
                if(rs.getDouble(1)==-1.0||"-1.0".equals(rs.getDouble(1))){
                    thje = 0;
                }else{
                    thje = rs.getDouble(1);
                }
            }
//            String v_sql_hb = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getYear()+"-07-01' and fhrq <= '"+getDataUtil.getYear()+"-09-31' and lx = '4' and kh= '"+kh+"' and pp = '"+pp+"' ";
//            rs.execute(v_sql_hb);
//            if (rs.next()) {
//                hbje = rs.getDouble(1);
//            }
            je =  fhje - thje ;
        }else if ("4".equals(jd)){
            String v_sql_fh = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getYear()+"-10-01' and fhrq <= '"+getDataUtil.getYear()+"-12-31' and lx = '0' and kh= '"+kh+"' and pp = '"+pp+"' and sfcyjf ='0'  and ddfllx = '0'";
            rs.execute(v_sql_fh);
            if (rs.next()) {
                if(rs.getDouble(1)==-1.0||"-1.0".equals(rs.getDouble(1))){
                    fhje = 0;
                }else{
                    fhje = rs.getDouble(1);
                }
            }

            String v_sql_th = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getYear()+"-10-01' and fhrq <= '"+getDataUtil.getYear()+"-12-31' and lx = '1' and kh= '"+kh+"' and pp = '"+pp+"' ";
            rs.execute(v_sql_th);
            if (rs.next()) {
                if(rs.getDouble(1)==-1.0||"-1.0".equals(rs.getDouble(1))){
                    thje = 0;
                }else{
                    thje = rs.getDouble(1);
                }
            }
//            String v_sql_hb = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getYear()+"-10-01' and fhrq <= '"+getDataUtil.getYear()+"-12-31' and lx = '4' and kh= '"+kh+"' and pp = '"+pp+"' ";
//            rs.execute(v_sql_hb);
//            if (rs.next()) {
//                hbje = rs.getDouble(1);
//            }
            je =  fhje - thje ;
        }
        return  je;
    }
    //计算出返利达标记录的达标金额字段 季返
    public double method2_dbje(String kh,String pp,String jd) {
        double fhje = 0;
        double thje = 0;
        double je = 0;
        RecordSet rs = new RecordSet();
        if("1".equals(jd)) {
            String v_sql_fh = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getYear()+"-01-01' and fhrq <= '"+getDataUtil.getYear()+"-03-31' and lx = '0' and kh= '"+kh+"' and pp = '"+pp+"' and sfcyjf ='0' and syfllx in('1','2') ";
            rs.execute(v_sql_fh);
            if (rs.next()) {
                if(rs.getDouble(1)==-1.0||"-1.0".equals(rs.getDouble(1))){
                    fhje = 0;
                }else{
                    fhje = rs.getDouble(1);
                }
            }

            String v_sql_th = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getYear()+"-01-01' and fhrq <= '"+getDataUtil.getYear()+"-03-31' and lx = '1' and kh= '"+kh+"' and pp = '"+pp+"' and syfllx in('1','2') ";
            rs.execute(v_sql_th);
            if (rs.next()) {
                if(rs.getDouble(1)==-1.0||"-1.0".equals(rs.getDouble(1))){
                    thje = 0;
                }else{
                    thje = rs.getDouble(1);
                }
            }
            je =  fhje - thje;
        }else if("2".equals(jd)){
            String v_sql_fh = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getYear()+"-04-01' and fhrq <= '"+getDataUtil.getYear()+"-06-31' and lx = '0' and kh= '"+kh+"' and pp = '"+pp+"' and sfcyjf ='0' and syfllx in('1','2') ";
            rs.execute(v_sql_fh);
            if (rs.next()) {
                if(rs.getDouble(1)==-1.0||"-1.0".equals(rs.getDouble(1))){
                    fhje = 0;
                }else{
                    fhje = rs.getDouble(1);
                }
            }

            String v_sql_th = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getYear()+"-04-01' and fhrq <= '"+getDataUtil.getYear()+"-06-31' and lx = '1' and kh= '"+kh+"' and pp = '"+pp+"' and syfllx in('1','2') ";
            rs.execute(v_sql_th);
            if (rs.next()) {
                if(rs.getDouble(1)==-1.0||"-1.0".equals(rs.getDouble(1))){
                    thje = 0;
                }else{
                    thje = rs.getDouble(1);
                }
            }
            je =  fhje - thje ;

        }else if("3".equals(jd)){
            String v_sql_fh = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getYear()+"-07-01' and fhrq <= '"+getDataUtil.getYear()+"-09-31' and lx = '0' and kh= '"+kh+"' and pp = '"+pp+"' and sfcyjf ='0' and syfllx in('1','2') ";
            rs.execute(v_sql_fh);
            if (rs.next()) {
                if(rs.getDouble(1)==-1.0||"-1.0".equals(rs.getDouble(1))){
                    fhje = 0;
                }else{
                    fhje = rs.getDouble(1);
                }
            }

            String v_sql_th = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getYear()+"-07-01' and fhrq <= '"+getDataUtil.getYear()+"-09-31' and lx = '1' and kh= '"+kh+"' and pp = '"+pp+"' and syfllx in('1','2') ";
            rs.execute(v_sql_th);
            if (rs.next()) {
                if(rs.getDouble(1)==-1.0||"-1.0".equals(rs.getDouble(1))){
                    thje = 0;
                }else{
                    thje = rs.getDouble(1);
                }
            }

            je =  fhje - thje;
        }else if ("4".equals(jd)){
            String v_sql_fh = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getYear()+"-10-01' and fhrq <= '"+getDataUtil.getYear()+"-12-31' and lx = '0' and kh= '"+kh+"' and pp = '"+pp+"' and sfcyjf ='0' and syfllx in('1','2') ";
            rs.execute(v_sql_fh);
            if (rs.next()) {
                if(rs.getDouble(1)==-1.0||"-1.0".equals(rs.getDouble(1))){
                    fhje = 0;
                }else{
                    fhje = rs.getDouble(1);
                }
            }

            String v_sql_th = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getYear()+"-10-01' and fhrq <= '"+getDataUtil.getYear()+"-12-31' and lx = '1' and kh= '"+kh+"' and pp = '"+pp+"' and syfllx in('1','2') ";
            rs.execute(v_sql_th);
            if (rs.next()) {
                if(rs.getDouble(1)==-1.0||"-1.0".equals(rs.getDouble(1))){
                    thje = 0;
                }else{
                    thje = rs.getDouble(1);
                }
            }
            je =  fhje - thje;
        }
        return  je;
    }
    //遍历发退货生成年度度达标记录
    public void method3() {
        RecordSet rs = new RecordSet();
        //月返 每月1号生成
        if(ndscrq.equals(getDataUtil.getYr())){
            String v_sql = " select distinct kh,pp from "+uf_fthjil+" where fhrq >= '"+getDataUtil.getaccYear()+"-01-01' and fhrq <= '"+getDataUtil.getaccYear()+"-12-31' ";
            new BaseBean().writeLog("年返:"+ v_sql);
            rs.execute(v_sql);
            while (rs.next()){
                try {
                    ModeDataIdUpdate idUpdate = new ModeDataIdUpdate();
                    int billid = idUpdate.getModeDataNewId(uf_fldbjl, mode__fldbjl, 1, 1, new SimpleDateFormat("yyyy-MM-dd").format(new Date()), new SimpleDateFormat("HH:mm:ss").format(new Date()));
                    rs.executeUpdate(" update "+uf_fldbjl+" set khmc=?,pp=?,ny=?,fllx=?,je=?,flje=?,sfyjsfl=?  where id='" + billid + "'",rs.getString(1),rs.getString(2),getDataUtil.accMm(),2,0,0,0);
                    //更新表单建模 权限
                    //计算返利
                    new BaseBean().writeLog("返利金额"+method3_je(rs.getString(1),rs.getString(2)));
                    rs.executeUpdate(" update "+uf_fldbjl+" set je=? where id='" + billid + "'",method3_je(rs.getString(1),rs.getString(2)));
                    //计算返利金额
                    rs.executeUpdate(" update "+uf_fldbjl+" set flje=? where id='" + billid + "'",rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getaccYear(),"2","",method3_je(rs.getString(1),rs.getString(2))));
                    //计算达标金额
                    double dbje = method3_je(rs.getString(1),rs.getString(2))+method3_dbje(rs.getString(1),rs.getString(2));
                    rs.executeUpdate(" update "+uf_fldbjl+" set fldbje=? where id='" + billid + "'",dbje);
                    //是否达标字段赋值
                    if(rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getaccYear(),"2","",method3_je(rs.getString(1),rs.getString(2)))==0){
                        rs.executeUpdate(" update "+uf_fldbjl+" set sfdb=? where id='" + billid + "'",1);
                    }else{
                        rs.executeUpdate(" update "+uf_fldbjl+" set sfdb=? where id='" + billid + "'",0);
                    }
                    //更新返利库
                    rs.execute( " update "+flk+" set ndflje = ifnull(ndflje,'0')+ "+rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getaccYear(),"2","",method3_je(rs.getString(1),rs.getString(2)))+",kynfje = ifnull(kynfje,'0')+ "+rebateTarget(rs.getString(1),rs.getString(2),getDataUtil.getaccYear(),"2","",method3_je(rs.getString(1),rs.getString(2)))+" where khmc = '"+rs.getString(1)+"'and pp = '"+rs.getString(2)+"' ");

                    //更新是否计算返利
                    rs.executeUpdate(" update "+uf_fldbjl+" set sfyjsfl=? where id='" + billid + "'",0);

                    new ModeRightInfo().editModeDataShare(1, mode__fldbjl, billid);//重置建模权限

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    //计算出返利达标记录的金额字段 年度
    public double method3_je(String kh,String pp) {
        double fhje = 0;
        double thje = 0;
        double hbje = 0;
        double je = 0;
        RecordSet rs = new RecordSet();
            String v_sql_fh = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getaccYear()+"-01-01' and fhrq <= '"+getDataUtil.getaccYear()+"-12-31' and lx = '0' and kh= '"+kh+"' and pp = '"+pp+"'  and  sfcynf = '0' and ddfllx = '0' ";
            rs.execute(v_sql_fh);
            if (rs.next()) {
                if(rs.getDouble(1)==-1.0||"-1.0".equals(rs.getDouble(1))){
                    fhje = 0;
                }else{
                    fhje = rs.getDouble(1);
                }
            }
            String v_sql_th = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getaccYear()+"-01-01' and fhrq <= '"+getDataUtil.getaccYear()+"-12-31' and lx = '1' and kh= '"+kh+"' and pp = '"+pp+"' ";
            rs.execute(v_sql_th);
            if (rs.next()) {
                if(rs.getDouble(1)==-1.0||"-1.0".equals(rs.getDouble(1))){
                    thje = 0;
                }else{
                    thje = rs.getDouble(1);
                }
            }
//            String v_sql_hb = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getaccYear()+"-01-01' and fhrq <= '"+getDataUtil.getaccYear()+"-12-31' and lx = '4' and kh= '"+kh+"' and pp = '"+pp+"' ";
//            rs.execute(v_sql_hb);
//            if (rs.next()) {
//                hbje = rs.getDouble(1);
//            }
            je =  fhje - thje ;
        return  je;
    }
    //计算出返利达标记录的达标金额字段 年度
    public double method3_dbje(String kh,String pp) {
        double fhje = 0;
        double thje = 0;
        double je = 0;
        RecordSet rs = new RecordSet();
        String v_sql_fh = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getaccYear()+"-01-01' and fhrq <= '"+getDataUtil.getaccYear()+"-12-31' and lx = '0' and kh= '"+kh+"' and pp = '"+pp+"' and syfllx in('1','2') ";
        rs.execute(v_sql_fh);
        if (rs.next()) {
            if(rs.getDouble(1)==-1.0||"-1.0".equals(rs.getDouble(1))){
                fhje = 0;
            }else{
                fhje = rs.getDouble(1);
            }
        }
        String v_sql_th = " select sum(dpzfje * hpsl) as je from " + uf_fthjil + " where fhrq >= '"+getDataUtil.getaccYear()+"-01-01' and fhrq <= '"+getDataUtil.getaccYear()+"-12-31' and lx = '1' and kh= '"+kh+"' and pp = '"+pp+"' and syfllx in('1','2') ";
        rs.execute(v_sql_th);
        if (rs.next()) {
            if(rs.getDouble(1)==-1.0||"-1.0".equals(rs.getDouble(1))){
                thje = 0;
            }else{
                thje = rs.getDouble(1);
            }
        }
        je =  fhje - thje;
        return  je;
    }


    //判断客户是否参与返利并返回返利
    public double rebateTarget(String kh,String pp,String nf,String lx,String jd,Double je) {
        new BaseBean().writeLog("返利金额计算"+kh+"-"+pp+"-"+nf+"-"+lx+"-"+jd+"-"+je);
        double flje = 0;//返利金额
        RecordSet rs = new RecordSet();
        //0月份
        if("0".equals(lx)){
            String v_sql =  " select sfcyyf,yfbl from "+uf_flmbk+" where khmc = '"+kh+"' and pp = '"+pp+"' and nf = '"+nf+"' ";
            rs.execute(v_sql);
            if(rs.next()){
                //flje = je *(rs.getDouble(2) * 0.01);
                flje = je *(rs.getDouble(2) * 0.01);
            }
        }
        //1季返
        if("1".equals(lx)){
            String v_sql;
            if("1".equals(jd)){
                v_sql =" select sfcyjf,jfbl,q1mb from "+uf_flmbk+" where khmc = '"+kh+"' and pp = '"+pp+"' and nf = '"+nf+"' ";
                rs.execute(v_sql);
                if(rs.next()){
                    if(rs.getDouble(3)<=je){
                        //flje = je/1.13 *(rs.getDouble(2) * 0.01);
                        flje = je*(rs.getDouble(2) * 0.01)/1.13;
                    }
                }
            }else if ("2".equals(jd)){
                v_sql =" select sfcyjf,jfbl,q2mb from "+uf_flmbk+" where khmc = '"+kh+"' and pp = '"+pp+"' and nf = '"+nf+"' ";
                rs.execute(v_sql);
                if(rs.next()){
                    if(rs.getDouble(3)<=je){
                        //flje = je/1.13 *(rs.getDouble(2) * 0.01);
                        flje = je*(rs.getDouble(2) * 0.01)/1.13;
                    }
                }

            }else if ("3".equals(jd)){
                v_sql =" select sfcyjf,jfbl,q3mb from "+uf_flmbk+" where khmc = '"+kh+"' and pp = '"+pp+"' and nf = '"+nf+"' ";
                rs.execute(v_sql);
                if(rs.next()){
                    if(rs.getDouble(3)<=je){
                       // flje = je/1.13 *(rs.getDouble(2) * 0.01);
                        flje = je*(rs.getDouble(2) * 0.01)/1.13;
                    }
                }

            }else if ("4".equals(jd)){
                v_sql =" select sfcyjf,jfbl,q4mb from "+uf_flmbk+" where khmc = '"+kh+"' and pp = '"+pp+"' and nf = '"+nf+"' ";
                rs.execute(v_sql);
                if(rs.next()){
                    if(rs.getDouble(3)<=je){
                        //flje = je/1.13 *(rs.getDouble(2) * 0.01);
                        flje = je*(rs.getDouble(2) * 0.01)/1.13;
                    }
                }
            }

        }
        //2年返
        if("2".equals(lx)){
            String v_sql =  " select sfcynf,nfbl,ndflmb from "+uf_flmbk+" where khmc = '"+kh+"' and pp = '"+pp+"' and nf = '"+nf+"' ";
            rs.execute(v_sql);
            if(rs.next()){
                if(rs.getDouble(3)<=je){
                    //flje = je/1.13 *(rs.getDouble(2) * 0.01);
                    flje = je*(rs.getDouble(2) * 0.01)/1.13;
                }
            }
        }
        new BaseBean().writeLog("flje"+flje);
        return flje;
    }


    public String getYfscrq() {
        return yfscrq;
    }

    public void setYfscrq(String yfscrq) {
        this.yfscrq = yfscrq;
    }
    public String getVarQ1() {
        return varQ1;
    }

    public void setVarQ1(String varQ1) {
        this.varQ1 = varQ1;
    }

    public String getVarQ2() {
        return varQ2;
    }

    public void setVarQ2(String varQ2) {
        this.varQ2 = varQ2;
    }
    public String getVarQ4() {
        return varQ4;
    }

    public void setVarQ4(String varQ4) {
        this.varQ4 = varQ4;
    }

    public String getVarQ3() {
        return varQ3;
    }

    public void setVarQ3(String varQ3) {
        this.varQ3 = varQ3;
    }


    public String getNdscrq() {
        return ndscrq;
    }

}
