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
package de.hsesslingen.keim.efs.middleware.provider.config;

import de.hsesslingen.keim.efs.middleware.common.ServiceDirectoryProxy;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.utils.EfsRequest;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Component;

/**
 *
 * @author keim
 */
@Component
@ConditionalOnProperty(name = "efs.middleware.provider-api.enabled", havingValue = "true")
public class ProviderRegistrator {

    private static final Logger logger = LoggerFactory.getLogger(ProviderRegistrator.class);

    @Autowired
    private ServiceDirectoryProxy proxy;

    @Autowired
    private IMobilityServiceConfigurationProperties serviceConfig;

    @Value("${efs.middleware.provider-api.registration.retry-delay:5}")
    private long retryDelay;

    @Value("${efs.middleware.provider-api.registration.disabled:false}")
    private boolean registrationDisabled;

    @Value("${efs.services.url.service-directory:http://service-directory/api}")
    public String baseUrl;

    private ScheduledExecutorService executor;
    private TaskScheduler scheduler;
    private ScheduledFuture future;

    private TaskScheduler getScheduler() {
        if (scheduler == null) {
            logger.info("Creating task scheduler for provider registration...");

            executor = Executors.newSingleThreadScheduledExecutor();
            scheduler = new ConcurrentTaskScheduler(executor);
        }

        return scheduler;
    }

    private void shutdownScheduler() {
        if (scheduler != null) {
            scheduler = null;
            executor.shutdown();
            executor = null;

            if (future != null) {
                future.cancel(false);
                future = null;
            }
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerInServiceDirectory() {
        if (registrationDisabled) {
            logger.warn("Service registration is disabled.");
            return;
        }

        logger.info("Trying to register my service at the service-directory...");

        try {
            register(serviceConfig.getMobilityService());
        } catch (Exception ex) {
            var message = "Registration failed. Retrying after " + retryDelay + " seconds.";

            if (logger.isTraceEnabled()) {
                logger.trace(message + " {}", ex);
            } else {
                logger.info(message);
            }

            future = getScheduler().schedule(this::registerInServiceDirectory, Instant.now().plusSeconds(retryDelay));
            
            return;
        }

        if (scheduler != null) {
            logger.info("Registration successful. Shutting down registration-retry-scheduler...");
            shutdownScheduler();
        } else {
            logger.info("Registration successful.");
        }
    }

    private void register(MobilityService service) {
        EfsRequest.post(baseUrl + "/services").toInternal().body(service).go();
    }

}
