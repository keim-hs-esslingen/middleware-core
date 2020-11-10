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
package de.hsesslingen.keim.efs.middleware.consumer;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;

import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.service.MobilityService.API;
import de.hsesslingen.keim.efs.mobility.service.MobilityType;
import de.hsesslingen.keim.efs.mobility.utils.EfsRequest;
import de.hsesslingen.keim.efs.mobility.service.Mode;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

/**
 * Helper Class used to make rest calls to service-directory
 *
 * @author k.sivarasah 26 Sep 2019
 */
@Service
@Lazy
@ConditionalOnProperty(name = "middleware.consumer.api.enabled", havingValue = "true")
public class ServiceDirectoryProxy {

    private static final Logger logger = getLogger(ServiceDirectoryProxy.class);

    @Value("${middleware.service-directory-url}")
    public String baseUrl;

    public ServiceDirectoryProxy() {
        // Used for tracing lazy loading of beans.
        logger.debug("Initializing ServiceDirectoryProxy.");
    }

    /**
     * Gets all available services from the Service-Directory.
     *
     * @return List of {@link MobilityService}
     */
    public List<MobilityService> getAll() {
        logger.info("Querying service directory for all available mobility services...");
        return buildGetAllRequest(baseUrl).go().getBody();
    }

    /**
     * Searches for available services in the Service-Directory that match the
     * given criteria.
     *
     * @param anyOfTheseMobilityTypesSupported Providers support one of the
     * given mobility types.
     * @param anyOfTheseModesSupported Providers support one of the given modes.
     * @param allOfTheseApisSupported Provders support ALL of the given apis.
     * @return List of {@link MobilityService}
     */
    public List<MobilityService> search(
            Set<MobilityType> anyOfTheseMobilityTypesSupported,
            Set<Mode> anyOfTheseModesSupported,
            API... allOfTheseApisSupported
    ) {
        Set<API> apiSet = null;

        if (allOfTheseApisSupported != null) {
            apiSet = Set.of(allOfTheseApisSupported);
        }

        return search(anyOfTheseMobilityTypesSupported, anyOfTheseModesSupported, apiSet);
    }

    /**
     * Searches for available services in the Service-Directory that match the
     * given criteria.
     *
     * @param anyOfTheseMobilityTypesSupported Providers support one of the
     * given mobility types.
     * @param anyOfTheseModesSupported Providers support one of the given modes.
     * @param allOfTheseApisSupported Provders support ALL of the given apis.
     * @return List of {@link MobilityService}
     */
    public List<MobilityService> search(
            Set<MobilityType> anyOfTheseMobilityTypesSupported,
            Set<Mode> anyOfTheseModesSupported,
            Set<API> allOfTheseApisSupported
    ) {
        logger.info("Querying service directory for specific set of available mobility services...");
        return buildSearchRequest(baseUrl, anyOfTheseMobilityTypesSupported, anyOfTheseModesSupported, allOfTheseApisSupported, true)
                .go()
                .getBody();
    }

    /**
     * Builds a request to get a filtered list of mobility services from the
     * service directory at the given URL.
     *
     * @param serviceDirectoryUrl
     * @param anyOfTheseMobilityTypesSupported
     * @param anyOfTheseModesSupported
     * @param allOfTheseApisSupported
     * @param excludeInactive
     * @return
     */
    public static EfsRequest<List<MobilityService>> buildSearchRequest(
            String serviceDirectoryUrl,
            Set<MobilityType> anyOfTheseMobilityTypesSupported,
            Set<Mode> anyOfTheseModesSupported,
            Set<API> allOfTheseApisSupported,
            boolean excludeInactive
    ) {
        var request = EfsRequest.get(serviceDirectoryUrl + "/search")
                .expect(new ParameterizedTypeReference<List<MobilityService>>() {
                })
                .query("excludeInactive", excludeInactive);

        if (anyOfTheseMobilityTypesSupported != null && !anyOfTheseMobilityTypesSupported.isEmpty()) {
            request.query("mobilityTypes", anyOfTheseMobilityTypesSupported.toArray());
        }
        if (anyOfTheseModesSupported != null && !anyOfTheseModesSupported.isEmpty()) {
            request.query("modes", anyOfTheseModesSupported.toArray());
        }
        if (allOfTheseApisSupported != null && !allOfTheseApisSupported.isEmpty()) {
            request.query("apis", allOfTheseApisSupported.toArray());
        }

        return request;
    }

    /**
     * Builds a request to get all registered mobility services from the service
     * directory at the given URL.
     *
     * @param serviceDirectoryUrl
     * @return
     */
    public static EfsRequest<List<MobilityService>> buildGetAllRequest(
            String serviceDirectoryUrl
    ) {
        return buildSearchRequest(serviceDirectoryUrl, null, null, null, true);
    }
}
