package org.egov.dx.repository;

import org.egov.dx.web.models.EPramaanTokenRes;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EPramaanMapper {
    public EPramaanTokenRes mapClaimsToResponse(Map<String, Object> claims) {
        EPramaanTokenRes response = new EPramaanTokenRes();

        response.setSub((String) claims.get("sub"));
        response.setPwdAuthStatus(Boolean.valueOf(String.valueOf(claims.get("pwd_auth_status"))));
        response.setGender((String) claims.get("gender"));
        response.setEpramaanId((String) claims.get("epramaanId"));
        response.setAadhaarHash((String) claims.get("aadhaar_hash"));
        response.setIss((String) claims.get("iss"));
        response.setSessionId((String) claims.get("session_id"));
        response.setDrivingLicenceVerified(Boolean.valueOf(String.valueOf(claims.get("driving_licence_verified"))));
        response.setSsoId((String) claims.get("sso_id"));
        response.setLoginMode((String) claims.get("loginMode"));
        response.setAadhaarVerified(Boolean.valueOf(String.valueOf(claims.get("aadhaar_verified"))));
        response.setAud((String) claims.get("aud"));
        response.setUniqueUserId((String) claims.get("unique_user_id"));
        response.setDob((String) claims.get("dob"));
        response.setService((String) claims.get("service"));
        response.setName((String) claims.get("name"));
        response.setMobileNumber((String) claims.get("mobile_number"));
        response.setExp(String.valueOf(claims.get("exp")));
        response.setIat(String.valueOf(claims.get("iat")));
        response.setPanVerified(Boolean.valueOf(String.valueOf(claims.get("pan_verified"))));
        response.setJti((String) claims.get("jti"));
        response.setUsername((String) claims.get("username"));

        return response;
    }
}
