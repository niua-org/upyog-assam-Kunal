package org.egov.dx.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EparmaanReponse {

    @JsonProperty("code")
    private String code;

    @JsonProperty("authToken")
    private String authToken;
}
