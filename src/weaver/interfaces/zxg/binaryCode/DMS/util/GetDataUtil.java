package weaver.interfaces.zxg.binaryCode.DMS.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class GetDataUtil {
    //获取当前日期
    public String getData(){
    Date date=new Date();
    SimpleDateFormat format  = new SimpleDateFormat("yyyy-MM-dd");
    //创建Calendar实例
    Calendar cal = Calendar.getInstance();
    //设置当前时间
    cal.setTime(date);
    return format.format(cal.getTime());
    }
    //获取前一个月
    public  String accMm(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date); // 设置为当前时间
        calendar.add(Calendar.MONTH,-1);
        date = calendar.getTime();
        String accDate = format.format(date);
        return accDate;
    }
    //获取当前日期
    public String getYr(){
        Date date=new Date();
        SimpleDateFormat format  = new SimpleDateFormat("MM-dd");
        //创建Calendar实例
        Calendar cal = Calendar.getInstance();
        //设置当前时间
        cal.setTime(date);
        return format.format(cal.getTime());
    }
    //获取当前年份
    public String getYear(){
        Date date=new Date();
        SimpleDateFormat format  = new SimpleDateFormat("yyyy");
        //创建Calendar实例
        Calendar cal = Calendar.getInstance();
        //设置当前时间
        cal.setTime(date);
        return format.format(cal.getTime());
    }
    //获取去年
    public String getaccYear(){
        Date date=new Date();
        SimpleDateFormat format  = new SimpleDateFormat("yyyy");
        //创建Calendar实例
        Calendar cal = Calendar.getInstance();
        //设置当前时间
        cal.setTime(date);
        cal.add(Calendar.YEAR,-1);
        return  format.format(cal.getTime());
    }
    //获取日
    public String getDay(){
        Date date=new Date();
        SimpleDateFormat format  = new SimpleDateFormat("dd");
        //创建Calendar实例
        Calendar cal = Calendar.getInstance();
        //设置当前时间
        cal.setTime(date);
        return format.format(cal.getTime());
    }

}
