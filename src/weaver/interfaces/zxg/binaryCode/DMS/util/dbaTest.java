package weaver.interfaces.zxg.binaryCode.DMS.util;

import weaver.conn.RecordSetDataSource;
import weaver.general.BaseBean;
import weaver.interfaces.schedule.BaseCronJob;

public class dbaTest extends BaseCronJob {


    @Override
    public void execute() {
        new BaseBean().writeLog("哈哈哈");
        RecordSetDataSource rsd = new RecordSetDataSource("bojun");
        rsd.execute(" select DIMNAME from DMS_M_DIM ");
        int i = 1;
        while (rsd.next()){
            new BaseBean().writeLog("第+"+i+"条："+rsd.getString(1));
            i++;
        }
        new BaseBean().writeLog("总条："+i);
    }
}
