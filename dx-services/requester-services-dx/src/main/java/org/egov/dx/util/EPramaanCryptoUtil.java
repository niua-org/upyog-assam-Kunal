package org.egov.dx.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.AESDecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCScopeValue;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.dx.repository.EPramaanMapper;
import org.egov.dx.util.Configurations;
import org.egov.dx.util.EPramaanCryptoUtil;
import org.egov.dx.web.models.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.*;



public class EPramaanCryptoUtil {
  /*  public Map<String, Object> decryptAndVerifyJweToken(String jweToken, String nonce) {
        try {
            SecretKeySpec aesKeySpec = (SecretKeySpec) generateAES256Key(nonce);
            JWEObject jweObject = JWEObject.parse(jweToken);
            jweObject.decrypt(new AESDecrypter(aesKeySpec));

            SignedJWT signedJwt = jweObject.getPayload().toSignedJWT();
            JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) getPublicKey());

            if (!signedJwt.verify(verifier)) {
                throw new RuntimeException("JWT signature verification failed");
            }

            return signedJwt.getPayload().toJSONObject();
        } catch (Exception e) {
            log.error("Error decrypting or verifying JWE token: ", e);
            throw new RuntimeException("Invalid ePramaan token", e);
        }
    }

    private SecretKey generateAES256Key(String nonce) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] key = digest.digest(nonce.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(key, "AES");
    }

    private PublicKey getPublicKey() throws Exception {
        try (InputStream inputStream = getClass().getResourceAsStream("/certificates/epramaan_staging.crt")) {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(inputStream);
            return certificate.getPublicKey();
        }
    }*/

}
