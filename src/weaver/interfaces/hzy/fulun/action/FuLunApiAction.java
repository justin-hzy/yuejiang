package weaver.interfaces.hzy.fulun.action;


import com.alibaba.fastjson.JSONObject;
import com.icbc.api.internal.apache.http.E;
import com.icbc.api.internal.apache.http.impl.cookie.S;
import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.hzy.fulun.util.FuLunApiUtil;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.Cell;
import weaver.soa.workflow.request.DetailTable;
import weaver.soa.workflow.request.RequestInfo;
import weaver.soa.workflow.request.Row;
import weaver.workflow.request.RequestManager;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FuLunApiAction extends BaseBean implements Action {

    private String apiId;

    @Override
    public String execute(RequestInfo requestInfo) {

        writeLog("开始执行FuLunApiAction！");
        RequestManager requestManager = requestInfo.getRequestManager();
        String requestId = requestInfo.getRequestid();
        FuLunApiUtil apiUtil = new FuLunApiUtil();

        //获取富仑配置表信息
        Map<String,String> apiConfig = apiUtil.getApiConfig(apiId);

        Map<String,String> mainData = WorkflowToolMethods.getMainTableInfo(requestInfo);
        mainData.put("requestid",requestId);

        List<Map<String, String>> detailData = new ArrayList<>();
        int mxindex = Util.getIntValue(apiConfig.get("mxbxh"));

        //writeLog("明细数据="+requestInfo.getDetailTableInfo().getDetailTable().length);

        if(mxindex != -1){
            detailData = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, mxindex);
        }

        writeLog("mainData="+mainData.toString());

        writeLog("detailData="+detailData.toString());

        String processCode = mainData.get("lcbh");

        //构建接口入参
        String params = apiUtil.getParams(apiId, mainData, detailData);

        if("1".equals(apiId)){
            //销售出库
            /*0: 未请求  1:请求成功 2:请求失败 3: 三次执行失败，需要人工干预 (组装拆卸/转码 4:提交流程 5:提交流程失败)*/
            String status = "0";

            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String todayString = today.format(formatter);

            todayString = "'" + todayString + "'";

            params = "'"+params+"'";

            //请求入参
            try{
                RecordSet rs = new RecordSet();
                String sql = "insert into uf_fl_order_request (requestId,apiid,params,status,createTime) values ("
                        +requestId+","+apiId+","+params+","+status+","+todayString+")";
                rs.executeUpdate(sql);
            }catch (Exception e){
                apiUtil.errlogMessage(apiId,requestId,params,e.getMessage());
                writeLog("requestId="+requestId);
                writeLog("apiId="+apiId);
                writeLog("params="+params);
                e.printStackTrace();
            }
        }else if("2".equals(apiId)){
            //销售退货
            /*0: 未请求  1:请求成功 2:请求失败 3: 三次执行失败，需要人工干预 (组装拆卸/转码 4:提交流程 5:提交流程失败)*/
            String status = "0";

            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String todayString = today.format(formatter);

            todayString = "'" + todayString + "'";

            params = "'"+params+"'";

            //请求入参
            try{
                RecordSet rs = new RecordSet();
                String sql = "insert into uf_fl_return_order_request (requestId,apiid,params,status,createTime) values ("
                        +requestId+","+apiId+","+params+","+status+","+todayString+")";
                rs.executeUpdate(sql);
            }catch (Exception e){
                apiUtil.errlogMessage(apiId,requestId,params,e.getMessage());
                writeLog("requestId="+requestId);
                writeLog("apiId="+apiId);
                writeLog("params="+params);
                e.printStackTrace();
            }
        }else if("3".equals(apiId)){
            //调拨单
            /*0: 未请求  1:请求成功 2:请求失败 3: 三次执行失败，需要人工干预 (组装拆卸/转码 4:提交流程 5:提交流程失败)*/
            String status = "0";

            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String todayString = today.format(formatter);

            todayString = "'" + todayString + "'";

            params = "'"+params+"'";

            //请求入参
            try{
                RecordSet rs = new RecordSet();
                String sql = "insert into uf_fl_transfer_order_request (requestId,apiid,process_code,params,status,createTime) values ("
                        +requestId+","+apiId+","+processCode+","+params+","+status+","+todayString+")";
                rs.executeUpdate(sql);
            }catch (Exception e){
                apiUtil.errlogMessage(apiId,requestId,params,e.getMessage());
                writeLog("requestId="+requestId);
                writeLog("apiId="+apiId);
                writeLog("params="+params);
                e.printStackTrace();
            }
        }else if("4".equals(apiId)){
            //寄售调拨出库单
            /*0: 未请求  1:请求成功 2:请求失败 3: 三次执行失败，需要人工干预 (组装拆卸/转码 4:提交流程 5:提交流程失败)*/

            String status = "0";

            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String todayString = today.format(formatter);

            todayString = "'" + todayString + "'";

            params = "'"+params+"'";

            //请求入参
            try{
                RecordSet rs = new RecordSet();
                String sql = "insert into uf_fl_cons_order_request (requestId,apiid,params,status,createTime) values ("
                        +requestId+","+apiId+","+params+","+status+","+todayString+")";
                rs.executeUpdate(sql);
            }catch (Exception e){
                apiUtil.errlogMessage(apiId,requestId,params,e.getMessage());
                writeLog("requestId="+requestId);
                writeLog("apiId="+apiId);
                writeLog("params="+params);
                e.printStackTrace();
            }
        }else if("5".equals(apiId)){
            //寄售调拨退货-进仓单
            /*0: 未请求  1:请求成功 2:请求失败 3: 三次执行失败，需要人工干预 (组装拆卸/转码 4:提交流程 5:提交流程失败)*/
            String status = "0";

            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String todayString = today.format(formatter);

            todayString = "'" + todayString + "'";

            params = "'"+params+"'";

            //请求入参
            try{
                RecordSet rs = new RecordSet();
                String sql = "insert into uf_fl_return_cons_order_request (requestId,apiid,params,status,createTime) values ("
                        +requestId+","+apiId+","+params+","+status+","+todayString+")";
                rs.executeUpdate(sql);
            }catch (Exception e){
                apiUtil.errlogMessage(apiId,requestId,params,e.getMessage());
                writeLog("requestId="+requestId);
                writeLog("apiId="+apiId);
                writeLog("params="+params);
                e.printStackTrace();
            }
        }else if ("6".equals(apiId)){
            //采购进仓单
            /*0: 未请求  1:请求成功 2:请求失败 3: 三次执行失败，需要人工干预 (组装拆卸/转码 4:提交流程 5:提交流程失败)*/
            String status = "0";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String now = dateFormat.format(new Date());

            now = "'"+now+"'";

            params = "'"+params+"'";

            //请求入参
            try{
                RecordSet rs = new RecordSet();
                String sql = "insert into uf_fl_pur_in_order_request (requestId,apiid,params,status,createTime) values ("
                        +requestId+","+apiId+","+params+","+status+","+now+")";

                rs.executeUpdate(sql);
            }catch (Exception e){
                apiUtil.errlogMessage(apiId,requestId,params,e.getMessage());
                writeLog("requestId="+requestId);
                writeLog("apiId="+apiId);
                writeLog("params="+params);
                e.printStackTrace();
            }
        }else if("7".equals(apiId)){
            //采购退单
            /*0: 未请求  1:请求成功 2:请求失败 3: 三次执行失败，需要人工干预 (组装拆卸/转码 4:提交流程 5:提交流程失败)*/
            String status = "0";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String now = dateFormat.format(new Date());

            now = "'"+now+"'";

            params = "'"+params+"'";

            //请求入参
            try{
                RecordSet rs = new RecordSet();
                String sql = "insert into uf_fl_re_pur_order_request (requestId,apiid,params,status,createTime) values ("
                        +requestId+","+apiId+","+params+","+status+","+now+")";

                rs.executeUpdate(sql);
            }catch (Exception e){
                apiUtil.errlogMessage(apiId,requestId,params,e.getMessage());
                writeLog("requestId="+requestId);
                writeLog("apiId="+apiId);
                writeLog("params="+params);
                e.printStackTrace();
            }
        }else if("12".equals(apiId)){

            writeLog("组装参数"+params);

            params = "'"+params+"'";

            //0: 未请求  1:请求成功 2:请求失败 3: 三次执行失败，需要人工干预 (组装拆卸/转码 4:提交流程 5:提交流程失败)
            String status = "0";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String now = dateFormat.format(new Date());

            now = "'"+now+"'";

            try {

                RecordSet rs = new RecordSet();
                String sql = "insert into uf_fl_set_dismantle_order_request (requestId,apiid,params,status,createTime) values ("+
                        requestId+","+apiId+","+params+","+status+","+now+")";
                writeLog("sql="+sql);
                rs.executeUpdate(sql);
            }catch (Exception e){
                apiUtil.errlogMessage(apiId,requestId,params,e.getMessage());
                writeLog("requestId="+requestId);
                writeLog("apiId="+apiId);
                writeLog("params="+params);
                e.printStackTrace();
            }
        } else if("13".equals(apiId)){

            writeLog("拆卸参数"+params);


            /*0: 未请求  1:请求成功 2:请求失败 3: 三次执行失败，需要人工干预 (组装拆卸/转码 4:提交流程 5:提交流程失败)*/
            String status = "0";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String now = dateFormat.format(new Date());

            now = "'"+now+"'";

            params = "'"+params+"'";
            //请求入参
            try{
                RecordSet rs = new RecordSet();
                String sql = "insert into uf_fl_set_dismantle_order_request (requestId,apiid,params,status,createTime) values ("
                        +requestId+","+apiId+","+params+","+status+","+now+")";

                rs.executeUpdate(sql);
            }catch (Exception e){
                apiUtil.errlogMessage(apiId,requestId,params,e.getMessage());
                writeLog("requestId="+requestId);
                writeLog("apiId="+apiId);
                writeLog("params="+params);
                e.printStackTrace();
            }
        }else if("17".equals(apiId)){
            //销售退货2.0
            /*0: 未请求  1:请求成功 2:请求失败 3: 三次执行失败，需要人工干预 (组装拆卸/转码 4:提交流程 5:提交流程失败)*/
            String status = "0";

            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String todayString = today.format(formatter);

            todayString = "'" + todayString + "'";

            params = "'"+params+"'";

            //请求入参
            try{
                RecordSet rs = new RecordSet();
                String sql = "insert into uf_fl_return_order_request (requestId,apiid,params,status,createTime) values ("
                        +requestId+","+apiId+","+params+","+status+","+todayString+")";
                rs.executeUpdate(sql);
            }catch (Exception e){
                apiUtil.errlogMessage(apiId,requestId,params,e.getMessage());
                writeLog("requestId="+requestId);
                writeLog("apiId="+apiId);
                writeLog("params="+params);
                e.printStackTrace();
            }
        } else if("14".equals(apiId)){
            //转码父项
            /*0: 未请求  1:请求成功 2:请求失败 3: 三次执行失败，需要人工干预 (组装拆卸/转码 4:提交流程 5:提交流程失败)*/
            String status = "0";

            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String todayString = today.format(formatter);

            todayString = "'" + todayString + "'";

            params = "'"+params+"'";

            //请求入参
            try{
                RecordSet rs = new RecordSet();
                String sql = "insert into uf_fl_transcode_order_request (requestId,apiid,params,status,createTime) values ("
                        +requestId+","+apiId+","+params+","+status+","+todayString+")";
                rs.executeUpdate(sql);
            }catch (Exception e){
                apiUtil.errlogMessage(apiId,requestId,params,e.getMessage());
                writeLog("requestId="+requestId);
                writeLog("apiId="+apiId);
                writeLog("params="+params);
                e.printStackTrace();
            }
        }else if("16".equals(apiId)){
            writeLog("锁库数据进入中间表");
            /*0: 未请求  1:请求成功 2:请求失败 3: 三次执行失败，需要人工干预 (组装拆卸/转码 4:提交流程 5:提交流程失败)*/
            String status = "0";

            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String todayString = today.format(formatter);

            todayString = "'" + todayString + "'";

            params = "'"+params+"'";

            //请求入参
            try{
                RecordSet rs = new RecordSet();
                String sql = "insert into uf_fl_channel_lock_request (requestId,apiid,params,status,createTime) values ("
                        +requestId+","+apiId+","+params+","+status+","+todayString+")";
                rs.executeUpdate(sql);
            }catch (Exception e){
                apiUtil.errlogMessage(apiId,requestId,params,e.getMessage());
                writeLog("requestId="+requestId);
                writeLog("apiId="+apiId);
                writeLog("params="+params);
                e.printStackTrace();
            }
        }
//        else if("15".equals(apiId)){
//            //转码子项
//            /*0: 未请求  1:请求成功 2:请求失败 3: 三次执行失败，需要人工干预 (组装拆卸/转码 4:提交流程 5:提交流程失败)*/
//
//            String status = "0";
//
//            LocalDate today = LocalDate.now();
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//            String todayString = today.format(formatter);
//
//            todayString = "'" + todayString + "'";
//
//            params = "'"+params+"'";
//
//            //请求入参
//            try{
//                RecordSet rs = new RecordSet();
//                String sql = "insert into uf_fl_transcode_order_request (requestId,apiid,son_params,son_status,createTime) values ("
//                        +requestId+","+apiId+","+params+","+status+","+todayString+")";
//                rs.executeUpdate(sql);
//            }catch (Exception e){
//                apiUtil.errlogMessage(apiId,requestId,params,e.getMessage());
//                writeLog("requestId="+requestId);
//                writeLog("apiId="+apiId);
//                writeLog("params="+params);
//                e.printStackTrace();
//            }
//        }
        return Action.SUCCESS;
    }


}
