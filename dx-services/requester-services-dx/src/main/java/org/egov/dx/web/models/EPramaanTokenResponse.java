package org.egov.dx.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;

import javax.validation.Valid;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EPramaanTokenResponse {

	@JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo=null;

    @Valid
    private EPramaanTokenRes tokenRes=null;

    @Valid
    private UserRes userRes=null;

    }
