package org.egov.bpa.service.property.softthink;

import lombok.extern.slf4j.Slf4j;
import org.egov.bpa.web.model.property.softthink.SoftThinkPropertyRequest;
import org.egov.bpa.web.model.property.softthink.SoftThinkPropertyResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class SoftThinkRestClient {

    @Value("${softthink.property.service.url}")
    private String baseUrl;
    
    @Value("${softthink.property.service.endpoint}")
    private String endpoint;

    @Value("${softthink.property.service.auth.token}")
    private String authToken;

    private final RestTemplate restTemplate = new RestTemplate();

    public SoftThinkPropertyResponse fetchHoldingDetails(SoftThinkPropertyRequest request) {
        String url = baseUrl + endpoint;
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", authToken);
        
        String jsonBody = "{\"HoldingNumber\": \"" + request.getHoldingNumber() + "\"}";
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        
        ResponseEntity<SoftThinkPropertyResponse> response = restTemplate.exchange(
            url, HttpMethod.POST, entity, SoftThinkPropertyResponse.class);
            
        return response.getBody();
    }
}