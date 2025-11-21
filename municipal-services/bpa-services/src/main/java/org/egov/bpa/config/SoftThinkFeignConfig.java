package org.egov.bpa.config;

import feign.codec.ErrorDecoder;
import org.egov.bpa.service.property.PropertyServiceErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SoftThinkFeignConfig {
    @Value("${softthink.supported.cities}")
    public String supportedCities;

    @Bean("softThinkErrorDecoder")
    public ErrorDecoder softThinkErrorDecoder() {
        return new PropertyServiceErrorDecoder();
    }
}
