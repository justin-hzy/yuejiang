package weaver.interfaces.hzy.k3.pur.action;

import com.weaver.general.BaseBean;
import weaver.interfaces.hzy.k3.service.K3Service;
import weaver.interfaces.workflow.action.Action;
import weaver.interfaces.zxg.binaryCode.DMS.util.WorkflowToolMethods;
import weaver.soa.workflow.request.RequestInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PriceCheckOutAction extends BaseBean implements Action {



    @Override
    public String execute(RequestInfo requestInfo) {

        K3Service k3Service = new K3Service();

        List<Map<String, String>> detailDatas1 = weaver.interfaces.tx.util.WorkflowToolMethods.getDetailTableInfo(requestInfo, 1);

        writeLog("detailDatas1=" + detailDatas1.toString());

        List<String>  deailyNecPriceMatchList = new ArrayList<>();
        for (Map<String, String> detailData : detailDatas1){
            String sku = detailData.get("wlbm");
            String fTaxPrice = k3Service.getDailyNecPrice(sku);
            if(fTaxPrice == null){
                deailyNecPriceMatchList.add(sku);
            }
        }

        List<String>  priceMatchList = new ArrayList<>();
        for (Map<String, String> detailData : detailDatas1){
            String sku = detailData.get("wlbm");
            String fTaxPrice = k3Service.getPrice(sku);
            if(fTaxPrice == null){
                priceMatchList.add(sku);
            }
        }




        return SUCCESS;
    }
}
