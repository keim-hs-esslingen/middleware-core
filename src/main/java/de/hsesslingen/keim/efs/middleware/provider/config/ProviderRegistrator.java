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

import de.hsesslingen.keim.efs.middleware.provider.BookingApi;
import de.hsesslingen.keim.efs.middleware.provider.CredentialsApi;
import de.hsesslingen.keim.efs.middleware.provider.OptionsApi;
import de.hsesslingen.keim.efs.middleware.provider.PlacesApi;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.service.MobilityService.API;
import de.hsesslingen.keim.efs.mobility.utils.EfsRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;
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
@ConditionalOnProperty(name = "middleware.provider.api.enabled", havingValue = "true")
public class ProviderRegistrator {

    private static final Logger logger = LoggerFactory.getLogger(ProviderRegistrator.class);

    @Value("${middleware.provider.api.registration.retry-delay:5}")
    private long retryDelay;
    @Value("${middleware.provider.api.registration.disabled:false}")
    private boolean registrationDisabled;
    @Value("${middleware.service-directory-url}")
    public String baseUrl;

    @Autowired(required = false)
    private ProviderProperties properties;

    // Requires the available rest controllers to check whether they exists or not.
    @Autowired(required = false)
    private PlacesApi placesApi;
    @Autowired(required = false)
    private OptionsApi optionsApi;
    @Autowired(required = false)
    private BookingApi bookingApi;
    @Autowired(required = false)
    private CredentialsApi credentialsApi;

    private ScheduledExecutorService executor;
    private TaskScheduler scheduler;
    private ScheduledFuture future;

    /**
     * Gets the scheduler used for provider registration. If none is created
     * yet, it will create a new one.
     *
     * @return
     */
    private TaskScheduler getScheduler() {
        if (scheduler == null) {
            logger.info("Creating task scheduler for provider registration...");

            executor = Executors.newSingleThreadScheduledExecutor();
            scheduler = new ConcurrentTaskScheduler(executor);
        }

        return scheduler;
    }

    /**
     * Shuts down the registration scheduler.
     */
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

    /**
     * Checks which of the rest controllers are actually ready and therefore
     * available.
     *
     * @return
     */
    private Set<API> getAvailableApis() {
        var list = EnumSet.noneOf(API.class);

        if (placesApi != null) {
            list.add(MobilityService.API.PLACES_API);
        }

        if (optionsApi != null) {
            list.add(MobilityService.API.OPTIONS_API);
        }

        if (bookingApi != null) {
            list.add(MobilityService.API.BOOKING_API);
        }

        if (credentialsApi != null) {
            list.add(MobilityService.API.CREDENTIALS_API);
        }

        return list;
    }

    /**
     * Checks which APIs were configured and whether their corresponding rest
     * controller was actually created or not. Mismatches between configuration
     * and reality are logged.
     */
    private void checkAndUpdateConfiguredApis() {
        var availableApis = getAvailableApis();
        var service = properties.getMobilityService();
        var configApis = service.getApis();

        for (var api : configApis) {
            if (!availableApis.contains(api)) {
                logger.warn("The API \"{}\" is configured as API in your mobility service properties, but it's controller was not instantiated. This can be due to some error or misconfiguration. The API will be removed from your available APIs.", api);
            }
        }

        var toRemove = new ArrayList<API>();
        for (var api : availableApis) {
            if (!configApis.contains(api)) {
                logger.warn("A rest controller was created for the API \"{}\" but this API is not configured as an available API in your mobility service properties. The API will not be shown as available to match your configuration.", api);
                toRemove.add(api);
            }
        }

        availableApis.removeAll(toRemove);

        // Update properties with truely available APIs...
        properties.getMobilityService().setApis(availableApis);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void registerInServiceDirectory() {
        if (registrationDisabled) {
            logger.warn("Service registration is disabled.");
            return;
        }

        if (properties == null || properties.getMobilityService() == null) {
            logger.warn("No mobility service properties defined. "
                    + "Nothing will be registered in the service directory and noone will be able to find you. "
                    + "Specify your mobility properties to be able to provide services to consumers.");
            return;
        }

        logger.info("Checking for mismatches between the configured APIs and the actually available ones...");
        checkAndUpdateConfiguredApis();

        logger.info("Trying to register my service at the service-directory...");

        try {
            register(properties.getMobilityService());
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
