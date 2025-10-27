package org.egov.dx.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EPramaanTokenRes {

    private String sub;
    private Boolean pwdAuthStatus;
    private String gender;
    private String epramaanId;
    private String aadhaarHash;
    private String iss;
    private String sessionId;
    private Boolean drivingLicenceVerified;
    private String ssoId;
    private String loginMode;
    private Boolean aadhaarVerified;
    private String aud;
    private String uniqueUserId;
    private String dob;
    private String service;
    private String name;
    private String mobileNumber;
    private String exp;
    private String iat;
    private Boolean panVerified;
    private String jti;
    private String username;

}
