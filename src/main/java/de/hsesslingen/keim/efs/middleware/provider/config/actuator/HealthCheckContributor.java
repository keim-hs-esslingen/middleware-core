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
package de.hsesslingen.keim.efs.middleware.provider.config.actuator;

import de.hsesslingen.keim.efs.middleware.provider.config.ProviderProperties;
import de.hsesslingen.keim.efs.mobility.utils.EfsRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Custom HealthCheck Implementation. A list of URLs will be load through
 * ConfigurationProperties. The Healt Status is set to UP or DOWN according to
 * the reachability of the provided HealthCheck-URLs
 *
 * @author k.sivarasah 3 Oct 2019
 * @author b.oesch
 */
@Component
@ConditionalOnBean(ProviderProperties.class)
@ConditionalOnMissingBean
public class HealthCheckContributor implements HealthIndicator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProviderProperties properties;

    @Override
    public Health health() {
        if (logger.isDebugEnabled()) {
            var joinedUrls = properties.getHealthCheckUrls().stream().collect(Collectors.joining());
            logger.debug("Checking service availability using Health-Check-Urls: " + joinedUrls);
        }

        Map<String, String> healthStatus = new HashMap<>();

        Builder healthBuilder = Health.up();

        for (String url : properties.getHealthCheckUrls()) {
            try {
                EfsRequest request = EfsRequest.get(url);
                ResponseEntity<?> response = request.go();
                HttpStatus responseStatus = response.getStatusCode();

                Assert.isTrue(responseStatus.is2xxSuccessful(), String.format("%s returned HttpStatus %s", url, responseStatus));
                healthStatus.put(url, responseStatus.toString());
            } catch (Exception e) {
                logger.warn("Health check failed. See exception for details.", e);
                healthBuilder = Health.down();
                healthStatus.put(url, "Error");
            }
        }

        return healthBuilder.withDetails(healthStatus).build();

    }

}
