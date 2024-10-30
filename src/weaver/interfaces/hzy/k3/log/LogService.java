package weaver.interfaces.hzy.k3.log;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LogService extends BaseBean {

    public void addLog(String lcbh,String status){
        String insertSql = "insert into k3_tran_log (lcbh,status,createTime) values (?,?,?)";
        RecordSet insertRs = new RecordSet();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String todayString = today.format(formatter);

        todayString = "'" + todayString + "'";
        insertRs.executeUpdate(insertSql,lcbh,status,todayString);
    }

}
