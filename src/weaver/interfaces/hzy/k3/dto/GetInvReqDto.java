package weaver.interfaces.hzy.k3.dto;

import lombok.Data;

@Data
public class GetInvReqDto {

    private String sku;

    private String stockNumber;

    private String storeType;
}
