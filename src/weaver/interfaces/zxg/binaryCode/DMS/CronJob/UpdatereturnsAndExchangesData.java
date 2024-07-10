package weaver.interfaces.zxg.binaryCode.DMS.CronJob;

import com.api.integration.Base;
import weaver.conn.RecordSet;
import weaver.formmode.data.ModeDataIdUpdate;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.interfaces.schedule.BaseCronJob;

import java.text.SimpleDateFormat;
import java.util.*;

public class UpdatereturnsAndExchangesData extends BaseCronJob {


    /**
     * 更新退货货额度表
     */
    private static String table = "uf_thhrdk";
    private static Integer modeid_thh = 55;
    private static Integer  modeid_hbmx = 64 ;
    //prd
//    private static Integer modeid_thh = 65;
//    private static Integer  modeid_hbmx = 63 ;

    @Override
    public void execute() {
        new BaseBean().writeLog("更新退换货");
        method1();
        updateHbtz();

    }
    //头一年的退换货额度库记录的可退货金额清零，转入已失效金额
    public void method1(){
        new BaseBean().writeLog("更新收付款台账信息");
        Date date=new Date();
        SimpleDateFormat format  = new SimpleDateFormat("yyyy");
        //创建Calendar实例
        Calendar cal = Calendar.getInstance();
        //设置当前时间
        cal.setTime(date);
        //在当前时间基础上减一年
        cal.add(Calendar.YEAR, -1);
        String nf = format.format(cal.getTime());
        RecordSet rs = new RecordSet();
        String v_sql = " select id,kthje from "+table+" where nf = '"+nf+"' ";
        new BaseBean().writeLog("method1"+v_sql);
        rs.execute(v_sql);
        while (rs.next()){
            try {
                rs.execute(" update "+table+" set ysxje = ifnull(replace(kthje,',',''),'0'),ysxhhje = ifnull(replace(khhje,',',''),'0')  where id = '"+rs.getString(1)+"' ");
                rs.execute(" update "+table+" set kthje ='0',khhje = '0'  where id = '"+rs.getString(1)+"' ");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        method2();
    }

    //生成当年的退换货额度记录，所有金额均为0
    public void method2(){
        new BaseBean().writeLog("更新当年信息");
        Date date=new Date();
        SimpleDateFormat format  = new SimpleDateFormat("yyyy");
        //创建Calendar实例
        Calendar cal = Calendar.getInstance();
        //设置当前时间
        cal.setTime(date);
        //在当前时间基础上减一年
        cal.add(Calendar.YEAR, -1);
        String nf = format.format(cal.getTime());
        RecordSet rs = new RecordSet();
        String v_sql = " select DISTINCT kh,khbh,pp from "+table+" where nf = '"+nf+"' ";
        new BaseBean().writeLog("method2"+v_sql);
        rs.execute(v_sql);
        while (rs.next()){
            try {
                ModeDataIdUpdate idUpdate = new ModeDataIdUpdate();
                //参数:建模表   模块ID 数据创建人   数据所属人员  创建日期YYYY-MM-DD  创建时间HH:mm:ss
                int billid = idUpdate.getModeDataNewId(table, modeid_thh, 1, 1, new SimpleDateFormat("yyyy-MM-dd").format(new Date()), new SimpleDateFormat("HH:mm:ss").format(new Date()));
                rs.executeUpdate(" update "+table+" set khbh=?,kh=?,pp=?,nf=?,ythje=?,ysxje=?,je=?,kthje=?  where id='" + billid + "'",rs.getString("khbh"),rs.getString("kh"),rs.getString("pp"),newnf(),'0','0','0','0');
                new ModeRightInfo().editModeDataShare(1, modeid_thh, billid);//重置建模权限
                } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //发货金额
    public double funutil2(String kh,String pp) {
        double je =0;
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy");
        //创建Calendar实例
        Calendar cal = Calendar.getInstance();
        //设置当前时间
        cal.setTime(date);
        //在当前时间基础上减一年
        cal.add(Calendar.YEAR, -1);
        String nf = format.format(cal.getTime());
        RecordSet rs = new RecordSet();
        String v_sql = " select sum((replace(dpzfje,',','') * hpsl)) as je from uf_fthjil where kh = '" + kh + "' and fhrq<='" + nf + "-12-30' and fhrq>='" + nf + "-01-01' and lx = '0' and pp = '"+pp+"' and ddfllx= '0' ";
        new BaseBean().writeLog("funutil2" + v_sql);
        rs.execute(v_sql);
        if (rs.next()) {
            je=rs.getDouble(1);
        }
        return je;
    }
    public String newnf() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy");
        //创建Calendar实例
        Calendar cal = Calendar.getInstance();
        //设置当前时间
        cal.setTime(date);
        //在当前时间基础上减一年
        cal.add(Calendar.YEAR,0);
        String nf = format.format(cal.getTime());
        return nf;
    }
    //找退换货台账，该客户头一年没有退换货类型=5%退换货的客户，将发退货记录该客户、该品牌下，发货记录金额汇总*1%的值加到该客户、该品牌，类型=1%年度发货金额转换的货补额度额度记录的金额和可使用金额
    public void updateHbtz(){
        new BaseBean().writeLog("更新货补额度");
        Date date=new Date();
        SimpleDateFormat format  = new SimpleDateFormat("yyyy");
        //创建Calendar实例
        Calendar cal = Calendar.getInstance();
        //设置当前时间
        cal.setTime(date);
        //在当前时间基础上减一年
        cal.add(Calendar.YEAR, -1);
        String nf = format.format(cal.getTime());
        RecordSet rs = new RecordSet();
        String v_sql = "select DISTINCT kh,pp from uf_hbedk where kh not in (select kh from uf_thhtz  where thlx = '3') and kh is not null and pp is not null   ";
        //更新表单建模 权限
        rs.execute(v_sql);
        while (rs.next()) {
            if (funutil1(rs.getString(1),rs.getString(2))) {
                //则将发货记录金额汇总*1%加到当年客户的货补额度额度记录的金额和可使用金额
                ModeDataIdUpdate idUpdate = new ModeDataIdUpdate();
                double je = funutil2(rs.getString(1), rs.getString(2)) * 0.01;
                //增加货补额度
                rs.execute(" update uf_hbedk set je = ifnull(replace(je,',',''),'0') + " + je + ", kyhbje =ifnull(replace(kyhbje,',',''),'0') + " + je + "  where kh = '" + rs.getString(1) + "' and pp = '" + rs.getString(2) + "' and hbedlx = '2' ");
                new BaseBean().writeLog("货补：update uf_hbedk set je = ifnull(replace(je,',',''),'0') + " + je + ", kyhbje =ifnull(replace(kyhbje,',',''),'0') + " + je + "  where kh = '" + rs.getString(1) + "' and pp = '" + rs.getString(2) + "' and hbedlx = '2' ");
                //增加货补明细
                int billid1 = idUpdate.getModeDataNewId("uf_hbedmx", modeid_hbmx, 1, 1, new SimpleDateFormat("yyyy-MM-dd").format(new Date()), new SimpleDateFormat("HH:mm:ss").format(new Date()));
                rs.executeUpdate(" update uf_hbedmx  set kh=?,je=?,pp=?,lx=?,rq=?,hbedlx=? where id='" + billid1 + "'", rs.getString(1), je, rs.getString(2), 6, new SimpleDateFormat("yyyy-MM-dd").format(new Date()),2);
                new ModeRightInfo().editModeDataShare(1, modeid_thh, billid1);//重置建模权限
            }
        }

    }
    //判断有无退货
    public boolean funutil1(String kh,String pp){
        Date date=new Date();
        SimpleDateFormat format  = new SimpleDateFormat("yyyy");
        //创建Calendar实例
        Calendar cal = Calendar.getInstance();
        //设置当前时间
        cal.setTime(date);
        //在当前时间基础上减一年
        cal.add(Calendar.YEAR, -1);
        String nf = format.format(cal.getTime());
        RecordSet rs = new RecordSet();
        String v_sql = " select id from uf_fthjil where kh = '"+kh+"' and pp = '"+pp+"' and (fhrq<="+nf+"-12-30 or fhrq>="+nf+"-01-01) and lx = '1' ";
        new BaseBean().writeLog("funutil11"+v_sql);
        rs.execute(v_sql);
        if(rs.next()){
            return false;

        }else{
            return true;
        }
    }

}
