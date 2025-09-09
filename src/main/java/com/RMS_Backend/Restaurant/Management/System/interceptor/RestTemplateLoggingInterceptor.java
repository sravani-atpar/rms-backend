package com.RMS_Backend.Restaurant.Management.System.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Interceptor for logging outgoing REST calls made with RestTemplate
 */
@Component
public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RestTemplateLoggingInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        logRequest(request, body);
        long startTime = System.currentTimeMillis();
        ClientHttpResponse response = execution.execute(request, body);
        long duration = System.currentTimeMillis() - startTime;
        
        // Create a wrapper to log the response details
        ClientHttpResponse responseWrapper = new BufferingClientHttpResponseWrapper(response);
        logResponse(responseWrapper, duration);
        return responseWrapper;
    }

    private void logRequest(HttpRequest request, byte[] body) {
        if (log.isDebugEnabled()) {
            String requestBody = new String(body, StandardCharsets.UTF_8);
            String headers = request.getHeaders().entrySet().stream()
                    .map(entry -> entry.getKey() + ": " + entry.getValue())
                    .collect(Collectors.joining(", "));
            
            log.debug("OUTGOING REQUEST: {} {}\nHeaders: [{}]\nBody: {}",
                    request.getMethod(), request.getURI(),
                    headers, requestBody);
        }
    }

    private void logResponse(ClientHttpResponse response, long duration) throws IOException {
        if (log.isDebugEnabled()) {
            String responseBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
            String headers = response.getHeaders().entrySet().stream()
                    .map(entry -> entry.getKey() + ": " + entry.getValue())
                    .collect(Collectors.joining(", "));
            
            log.debug("OUTGOING RESPONSE: {} ({}ms)\nHeaders: [{}]\nBody: {}",
                    response.getStatusCode(), duration,
                    headers, responseBody);
        }
    }

    /**
     * Wrapper class to buffer the response body so it can be read multiple times
     */
    private static class BufferingClientHttpResponseWrapper implements ClientHttpResponse {
        private final ClientHttpResponse response;
        private byte[] body;

        public BufferingClientHttpResponseWrapper(ClientHttpResponse response) {
            this.response = response;
        }

        @Override
        public org.springframework.http.HttpStatusCode getStatusCode() throws IOException {
            return response.getStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return response.getStatusText();
        }

        @Override
        public void close() {
            response.close();
        }

        @Override
        public java.io.InputStream getBody() throws IOException {
            if (body == null) {
                body = StreamUtils.copyToByteArray(response.getBody());
            }
            return new java.io.ByteArrayInputStream(body);
        }

        @Override
        public org.springframework.http.HttpHeaders getHeaders() {
            return response.getHeaders();
        }
    }
}