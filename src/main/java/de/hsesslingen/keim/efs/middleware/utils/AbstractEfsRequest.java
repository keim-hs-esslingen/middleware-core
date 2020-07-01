/*
 * MIT License
 * 
 * Copyright (c) 2020 Hochschule Esslingen
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */
package de.hsesslingen.keim.efs.middleware.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hsesslingen.keim.efs.middleware.config.ApiConstants;
import de.hsesslingen.keim.restutils.AbstractRequest;
import de.hsesslingen.keim.restutils.UrlUtils;
import java.net.URI;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author ben
 * @param <T>
 */
public abstract class AbstractEfsRequest<T> extends AbstractRequest<T> {

    //<editor-fold defaultstate="collapsed" desc="Configuration code">
    private static final Logger log = LoggerFactory.getLogger(AbstractEfsRequest.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String LOCALHOST = "localhost";

    // These values must be configured once statically using AbstractEfsRequest.configure(...).
    private static RestTemplate restTemplate;
    private static RestTemplate restTemplateLoadBalanced;
    private static String apiKey;

    public static void configure(RestTemplate loadBalanced, RestTemplate normal, String apiKey) {
        AbstractEfsRequest.restTemplate = normal;
        AbstractEfsRequest.restTemplateLoadBalanced = loadBalanced;
        AbstractEfsRequest.apiKey = apiKey;
    }
    //</editor-fold>

    private Object credentials;

    protected AbstractEfsRequest(HttpMethod method, String uri) {
        super(method, uri);
    }

    protected AbstractEfsRequest(HttpMethod method, URI uri) {
        super(method, uri);
    }

    protected AbstractEfsRequest(HttpMethod method) {
        super(method);
    }

    protected AbstractEfsRequest(String uri) {
        super(uri);
    }

    protected AbstractEfsRequest(URI uri) {
        super(uri);
    }

    protected AbstractEfsRequest() {
        super();
    }

    /**
     * This method will set the credentials that shall be used in this request.
     *
     * @param credentials
     * @return
     */
    protected AbstractEfsRequest<T> credentials(Object credentials) {
        this.credentials = credentials;
        return this;
    }

    @Override
    protected ResponseEntity<T> go() {
        // Before we send the request, lets add our credentials...
        addCredentialsToHeader();
        addApiKeyToHeader();
        return super.go();
    }

    private void addApiKeyToHeader() {
        if (apiKey != null) {
            super.header(ApiConstants.API_KEY_HEADER_NAME, apiKey);
        }
    }

    private void addCredentialsToHeader() {
        if (credentials == null) {
            return;
        }

        try {
            if (credentials instanceof Map) {
                super.headers((Map) credentials);
            } else if (credentials instanceof String) {
                super.header(ApiConstants.CREDENTIALS_HEADER_NAME, (String) credentials);
            } else {
                super.header(ApiConstants.CREDENTIALS_HEADER_NAME, mapper.writeValueAsString(credentials));
            }
        } catch (JsonProcessingException e) {
            log.error("Credential information could not be added to HttpHeader", e);
        }
    }

    @Override
    protected RestTemplate getRestTemplate() {
        // This method is overridden from super class, because we need to provide a different rest template.

        String host = uriBuilder().build().getHost();

        if (LOCALHOST.equals(host) || UrlUtils.isValidDomainName(host) || UrlUtils.isValidIpAddress(host)) {
            return restTemplate;
        } else {
            return restTemplateLoadBalanced;
        }
    }
}
