package weaver.interfaces.hzy.k3.service;

import com.wbi.util.Util;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.dto.GetInvReqDto;
import weaver.interfaces.hzy.k3.pojo.SaleDt1;
import weaver.interfaces.hzy.k3.pojo.SaleDt2;
import weaver.interfaces.hzy.me.util.MeApiUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProDtService extends BaseBean {

    public Map<String,List<SaleDt1>> getProDt(String requestid){

        RecordSet dtRs1 = new RecordSet();

        String dt1Sql = "select dt1.khddh,dt1.hptxm,dt1.ddje,dt1.gg,sum(dt1.fhl) fhl from formtable_main_226_dt1 as dt1,formtable_main_226 main where dt1.fhl is not null and dt1.mainid = main.id and main.requestid = ? group by dt1.khddh, dt1.hptxm,dt1.lsj,dt1.gg";

        writeLog("dt1Sql="+dt1Sql);

        dtRs1.executeQuery(dt1Sql,requestid);

        Map<String,List<SaleDt1>> dt1Map = new HashMap<>();

        while (dtRs1.next()){
            //客户订单号
            String khddh = Util.null2String(dtRs1.getString("khddh"));
            //sku
            String hptxm = Util.null2String(dtRs1.getString("hptxm"));

            //税率
            String gg = Util.null2String(dtRs1.getString("gg"));
            //发货量
            Integer fhl = dtRs1.getInt("fhl");
            //含税单价
            String ddje = Util.null2String(dtRs1.getString("ddje"));

            if(dt1Map.containsKey(khddh)){
                List<SaleDt1> dt1List = dt1Map.get(khddh);
                SaleDt1 saleDt1 = new SaleDt1();

                saleDt1.setFhl(fhl);
                saleDt1.setHptxm(hptxm);
                saleDt1.setSellPrice(ddje);
                saleDt1.setGg(gg);
                dt1List.add(saleDt1);
            }else {
                List<SaleDt1> dt1List = new ArrayList<>();
                SaleDt1 saleDt1 = new SaleDt1();

                saleDt1.setFhl(fhl);
                saleDt1.setHptxm(hptxm);
                saleDt1.setSellPrice(ddje);
                saleDt1.setGg(gg);
                dt1List.add(saleDt1);
                dt1Map.put(khddh,dt1List);
            }
        }
        return dt1Map;
    }


    public Map<String,List<SaleDt2>> getProDt2(String requestid){

        RecordSet dtRs2 = new RecordSet();

        String dt2Sql = "select dt2.khddh,dt2.hptxm,dt2.xsj,sum(dt2.fhsl) fhsl from formtable_main_226_dt2 as dt2,formtable_main_226 main where dt2.mainid = main.id and main.requestid = ? group by dt2.khddh, dt2.hptxm,dt2.xsj";

        writeLog("dt2Sql="+dt2Sql);

        dtRs2.executeQuery(dt2Sql,requestid);

        Map<String,List<SaleDt2>> dt2Map = new HashMap<>();

        while (dtRs2.next()){

            //客户订单号
            String khddh = Util.null2String(dtRs2.getString("khddh"));

            String hptxm = Util.null2String(dtRs2.getString("hptxm"));

            //发货量
            Integer fhsl = dtRs2.getInt("fhsl");

            //销售价
            String xsj = Util.null2String(dtRs2.getString("xsj"));

            if(dt2Map.containsKey(khddh)){
                List<SaleDt2> dt2List = dt2Map.get(khddh);
                SaleDt2 saleDt2 = new SaleDt2();
                saleDt2.setFhsl(fhsl);
                saleDt2.setHptxm(hptxm);
                saleDt2.setSellPrice(xsj);
                dt2List.add(saleDt2);
            }else {
                List<SaleDt2> dt2List = new ArrayList<>();
                SaleDt2 saleDt2 = new SaleDt2();
                saleDt2.setFhsl(fhsl);
                saleDt2.setHptxm(hptxm);
                saleDt2.setSellPrice(xsj);
                dt2List.add(saleDt2);
                dt2Map.put(khddh,dt2List);
            }
        }

        return dt2Map;
    }
}
