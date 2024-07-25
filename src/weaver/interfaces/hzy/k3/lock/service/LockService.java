package weaver.interfaces.hzy.k3.lock.service;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class LockService extends BaseBean {

    public boolean getLock(String billNo){

        writeLog("执行getLock");

        String querySql = "select version from k3_order_lock where is_avl = 0";

        writeLog("querySql="+querySql);

        RecordSet queryRs = new RecordSet();

        queryRs.executeQuery(querySql);

        if(queryRs.next()){

            writeLog("1111111111111111111");


            //获取锁
            Integer version1 = queryRs.getInt("version");

            Integer version2 = version1 + 1;

            String updateSql = "update k3_order_lock set billno = ?,is_avl = ?,version = ? ,time = ? where version = ? and is_avl = 0 and name = ?";

            writeLog("updateSql="+updateSql);

            RecordSet updateRs = new RecordSet();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = dateFormat.format(new Date());

            writeLog("now="+now);

            boolean result = updateRs.executeUpdate(updateSql,billNo,"1",version2,now,version1,"order");


            writeLog("result="+result);

            writeLog("22222222222222222222222");

            if (result == true){
                writeLog(billNo+"已抢到锁，将进行库存匹配和同步金蝶单据");
                return true;
            }else {
                writeLog(billNo+"未能抢到锁，将进行库存匹配和同步金蝶单据");
                return false;
            }
        }else {
            writeLog(billNo+"未能抢到锁，将进行库存匹配和同步金蝶单据");
            return false;
        }
    }
}
