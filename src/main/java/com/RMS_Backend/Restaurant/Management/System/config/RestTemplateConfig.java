package com.RMS_Backend.Restaurant.Management.System.config;


import com.RMS_Backend.Restaurant.Management.System.interceptor.RestTemplateLoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * Configuration for RestTemplate with logging capabilities
 */
@Configuration
public class RestTemplateConfig {

    private final RestTemplateLoggingInterceptor loggingInterceptor;

    public RestTemplateConfig(RestTemplateLoggingInterceptor loggingInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
    }

    @Bean
    public RestTemplate restTemplate() {
        // Use BufferingClientHttpRequestFactory to allow reading the response multiple times
        RestTemplate restTemplate = new RestTemplate(
                new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));

        // Add the logging interceptor
        restTemplate.setInterceptors(Collections.singletonList(loggingInterceptor));

        return restTemplate;
    }
}
