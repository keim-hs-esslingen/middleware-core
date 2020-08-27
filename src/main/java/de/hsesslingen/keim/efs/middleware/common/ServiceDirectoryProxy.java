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
package de.hsesslingen.keim.efs.middleware.common;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;

import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.service.MobilityType;
import de.hsesslingen.keim.efs.mobility.utils.EfsRequest;
import de.hsesslingen.keim.efs.mobility.service.Mode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.ParameterizedTypeReference;

/**
 * Helper Class used to make rest calls to service-directory
 *
 * @author k.sivarasah 26 Sep 2019
 */
public class ServiceDirectoryProxy {

    private static final Log log = LogFactory.getLog(ServiceDirectoryProxy.class);

    @Value("${efs.services.url.service-directory:http://service-directory/api}")
    public String baseUrl;

    /**
     * Searches for available services in Service-Directory
     *
     * @return List of {@link MobilityService}
     */
    public List<MobilityService> search() {
        log.info("Querying service directory for available mobility services...");
        return EfsRequest.get(buildUri()).expect(new ParameterizedTypeReference<List<MobilityService>>() {
        }).go().getBody();
    }

    /**
     * Searches for available services in Service-Directory using
     * {@link MobilityType} and {@link Mode}
     *
     * @param mobilityTypes Set of {@link MobilityType}
     * @param modes Set of {@link Mode}
     * @param serviceIds Ids of preferred services
     * @return List of {@link MobilityService}
     */
    public List<MobilityService> search(Set<MobilityType> mobilityTypes, Set<Mode> modes, Set<String> serviceIds) {
        log.info("Querying service directory for specific set of available mobility services...");
        return EfsRequest.get(buildUri(mobilityTypes, modes, serviceIds, true)).expect(new ParameterizedTypeReference<List<MobilityService>>() {
        }).go().getBody();
    }

    private String buildUri() {
        return buildUri(null, null, null, true);
    }

    private String buildUri(Set<MobilityType> mobilityTypes, Set<Mode> modes, Set<String> serviceIds, boolean activeOnly) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search")
                .queryParam("active", activeOnly);

        if (mobilityTypes != null) {
            uriBuilder.queryParam("mobilityTypes", mobilityTypes.toArray());
        }
        if (modes != null) {
            uriBuilder.queryParam("modes", modes.toArray());
        }
        if (serviceIds != null) {
            uriBuilder.queryParam("serviceIds", serviceIds.toArray());
        }

        String uri = uriBuilder.toUriString();

        log.trace("Using \"" + uri + "\" for querying the service directory.");

        return uri;
    }

    public void register(MobilityService service) {
        EfsRequest.post(baseUrl + "/services").body(service).go();
    }
}
