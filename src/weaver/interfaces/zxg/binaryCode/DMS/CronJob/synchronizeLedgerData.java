package weaver.interfaces.zxg.binaryCode.DMS.CronJob;

import weaver.conn.RecordSet;
import weaver.conn.RecordSetDataSource;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;

public class synchronizeLedgerData extends BaseCronJob {
    /**
     * 更新付款台账数据
     */
    private String sktz = "uf_sktz";//收款台账表名
    private String fktz = "uf_fktz";//付款台账表名

    private String khtz = "uf_kh";//客户库表名

    @Override
    public void execute() {
        new BaseBean().writeLog("更新收付款台账信息");
        RecordSet rs = new RecordSet();
        //收款
        String v_sql = " select fkdw,fkyh,fkzh,id,skgs01 from "+sktz+" where zt not in ('1','2') or zt is null ";
        rs.execute(v_sql);
        while (rs.next()){
            String rt = fun1(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(5));
            new BaseBean().writeLog("返回值"+rt);
            if(!"".equals(rt)){
                rs.execute(" update "+sktz+" set zt = '1' ,szkh = '"+rt+"',szzt = '"+fun3(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(5))+"',dqjl = '"+fun4(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(5))+"',xszg = '"+fun5(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(5))+"'  where id = '"+rs.getString(4)+"'");
            }else {
                rs.execute("update "+sktz+" set zt = '2'  where id = '"+rs.getString(4)+"'");
            }
        }
        rs.execute(" update uf_sktz set khjsfs = (select jsfs from uf_kh where khbh = szkh) ");
        //付款
        String v_sql1 = " select skgs,skyh,skzh,id,fkdw from "+fktz+" where zt not in ('1','2') or zt is null ";
        rs.execute(v_sql1);
        while (rs.next()){
            String rt = fun1(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(5));
            new BaseBean().writeLog("返回值"+rt);
            if(!"".equals(rt)){
                rs.execute(" update "+fktz+" set zt = '1', szkh = '"+rt+"',szzt = '"+fun3(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(5))+"',dqjl = '"+fun4(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(5))+"',xszg = '"+fun5(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(5))+"' where id = '"+rs.getString(4)+"'");
            }else {
                rs.execute("update "+fktz+" set zt = '2'  where id = '"+rs.getString(4)+"'");
            }
        }
        rs.execute(" update uf_fktz set khjsfs = (select jsfs from uf_kh where khbh = szkh) ");

    }

//    public String fun1(String mc,String yh ,String zh){
//        RecordSet rs = new RecordSet();
//        String khmc = "";
//        String v_sql = " select khmcxz from "+khtz+" where khmcst = '"+mc+"'and khh = '"+yh+"' and yhzh = '"+zh+"'";
//        rs.execute(v_sql);
//        new BaseBean().writeLog("sql1"+v_sql);
//        if(rs.next()) {
//            khmc = rs.getString(1);
//        }
//    return khmc;
//    }
    public String fun1(String mc,String yh ,String zh,String sszt){
        RecordSet rs = new RecordSet();
        String khmc = "";
        String v_sql = " select khmcxz from "+khtz+" where khmcst = '"+mc+"' and  szzt  =  '"+zt(sszt)+"' ";
        rs.execute(v_sql);
        new BaseBean().writeLog("sql1"+v_sql);
        if(rs.next()) {
            khmc = rs.getString(1);
        }
        return khmc;
    }
//    public String fun3(String mc,String yh ,String zh){
//        RecordSet rs = new RecordSet();
//        String szzt = "";
//        String v_sql = " select szzt from "+khtz+" where khmcst = '"+mc+"'and khh = '"+yh+"' and yhzh = '"+zh+"'";
//        rs.execute(v_sql);
//        new BaseBean().writeLog("sql2"+v_sql);
//        if(rs.next()) {
//            szzt = rs.getString(1);
//        }
//        return szzt;
//    }
    public String fun3(String mc,String yh ,String zh,String sszt){
        RecordSet rs = new RecordSet();
        String szzt = "";
        String v_sql = " select szzt from "+khtz+" where khmcst = '"+mc+"' and  szzt  =  '"+zt(sszt)+"' ";
        rs.execute(v_sql);
        new BaseBean().writeLog("sql2"+v_sql);
        if(rs.next()) {
            szzt = rs.getString(1);
        }
        return szzt;
    }
    //
//    public String fun4(String mc,String yh ,String zh){
//        RecordSet rs = new RecordSet();
//        String szzt = "";
//        String v_sql = " select dqjl from "+khtz+" where khmcst = '"+mc+"'and khh = '"+yh+"' and yhzh = '"+zh+"'";
//        rs.execute(v_sql);
//        new BaseBean().writeLog("sql2"+v_sql);
//        if(rs.next()) {
//            szzt = rs.getString(1);
//        }
//        return szzt;
//    }
    public String fun4(String mc,String yh ,String zh,String sszt){
        RecordSet rs = new RecordSet();
        String szzt = "";
        String v_sql = " select dqjl from "+khtz+" where khmcst = '"+mc+"' and  szzt  =  '"+zt(sszt)+"' ";
        rs.execute(v_sql);
        new BaseBean().writeLog("sql2"+v_sql);
        if(rs.next()) {
            szzt = rs.getString(1);
        }
        return szzt;
    }
//    public String fun5(String mc,String yh ,String zh){
//        RecordSet rs = new RecordSet();
//        String szzt = "";
//        String v_sql = " select xszg from "+khtz+" where khmcst = '"+mc+"'and khh = '"+yh+"' and yhzh = '"+zh+"'";
//        rs.execute(v_sql);
//        new BaseBean().writeLog("sql2"+v_sql);
//        if(rs.next()) {
//            szzt = rs.getString(1);
//        }
//        return szzt;
//    }
public String fun5(String mc,String yh ,String zh,String sszt){
    RecordSet rs = new RecordSet();
    String szzt = "";
    String v_sql = " select xszg from "+khtz+" where khmcst = '"+mc+"' and  szzt  =  '"+zt(sszt)+"' ";
    rs.execute(v_sql);
    new BaseBean().writeLog("sql2"+v_sql);
    if(rs.next()) {
        szzt = rs.getString(1);
    }
    return szzt;
}

    //结算方式
    public static String fun2(String str) {
        RecordSet rs = new RecordSet();
        rs.execute(" select jsfs  from uf_kh where khbh =  '"+str+"'  ");
        return rs.next()? Util.null2String(rs.getString(1)) : "";
    }
    //主体
    public static String zt (String str) {
        new BaseBean().writeLog("主体"+str);
        RecordSetDataSource rsd = new RecordSetDataSource("kingdee");
        String rt = "";
        rsd.execute(" SELECT A.fnumber \n" +
                " FROM  T_ORG_ORGANIZATIONS A left join dbo.T_ORG_ORGANIZATIONS_L  B ON A.FORGID = B.FORGID \n" +
                " WHERE   B.FLOCALEID = '2052' AND B.FNAME = '"+str+"'  ");
        if(rsd.next()){
            rt = rsd.getString(1);
        }
        return rt;
    }
}
