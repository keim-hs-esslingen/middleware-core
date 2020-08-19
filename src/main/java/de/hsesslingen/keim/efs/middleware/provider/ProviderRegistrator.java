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
package de.hsesslingen.keim.efs.middleware.provider;

import de.hsesslingen.keim.efs.middleware.common.ServiceDirectoryProxy;
import de.hsesslingen.keim.efs.middleware.config.IMobilityServiceConfigurationProperties;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private static final Log log = LogFactory.getLog(ProviderRegistrator.class);

    @Autowired
    private ServiceDirectoryProxy proxy;

    @Autowired
    private IMobilityServiceConfigurationProperties serviceConfig;

    @Value("${efs.middleware.provider-api.registration.retry-delay:5}")
    private long retryDelay;

    @Value("${efs.middleware.provider-api.registration.disabled:false}")
    private boolean registrationDisabled;

    private ScheduledExecutorService executor;
    private TaskScheduler scheduler;
    private ScheduledFuture future;

    private TaskScheduler getScheduler() {
        if (scheduler == null) {
            log.info("Creating task scheduler for provider registration...");

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
            log.warn("Service registration is disabled.");
            return;
        }

        log.info("Trying to register my service at the service-directory...");

        try {
            proxy.register(serviceConfig.getMobilityService());
        } catch (Exception ex) {
            log.trace(ex);
            log.info("Registration failed. Retrying after " + retryDelay + " seconds.");
            future = getScheduler().schedule(this::registerInServiceDirectory, Instant.now().plusSeconds(retryDelay));
            return;
        }

        if (scheduler != null) {
            log.info("Registration successful. Shutting down registration-retry-scheduler...");
            shutdownScheduler();
        } else {
            log.info("Registration successful.");
        }
    }

}
