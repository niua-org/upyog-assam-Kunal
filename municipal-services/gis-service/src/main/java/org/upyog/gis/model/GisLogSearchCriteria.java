package org.upyog.gis.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GisLogSearchCriteria {

    private String applicationNo;
    private String rtpId;
    private String status;
    private String tenantId;
    private Integer offset;
    private Integer limit;

    public boolean isEmpty(){
        return (this.tenantId == null && this.applicationNo == null && this.rtpId == null && this.status == null);
    }

}
