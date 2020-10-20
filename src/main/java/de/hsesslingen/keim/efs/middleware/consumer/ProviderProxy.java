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

import de.hsesslingen.keim.efs.middleware.model.Booking;
import de.hsesslingen.keim.efs.middleware.model.Options;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.service.MobilityService.API;
import de.hsesslingen.keim.efs.mobility.utils.EfsRequest;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import org.springframework.core.ParameterizedTypeReference;

/**
 *
 * @author keim
 */
public class ProviderProxy {

    private static final String CREDENTIALS_PATH = "/credentials";
    private static final String BOOKINGS_PATH = "/bookings";
    private static final String OPTIONS_PATH = "/options";

    private final MobilityService service;

    public ProviderProxy(MobilityService service) {
        this.service = service;
    }

    public Set<API> getSupportedApis() {
        return service.getApis();
    }

    public boolean supportsApi(API api) {
        return service.getApis().contains(api);
    }

    /**
     * Assembles a get-options request, specific to the mobility service
     * provider of this proxy, using the given params.The params are checked for
     * null values and added only if they are present and sensible.
     *
     * @param from
     * @param fromPlaceId
     * @param to
     * @param toPlaceId
     * @param startTime
     * @param endTime
     * @param radiusMeter
     * @param share
     * @param credentials
     * @return
     */
    public EfsRequest<List<Options>> createOptionsRequest(
            String from,
            String fromPlaceId,
            String to,
            String toPlaceId,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            Boolean share,
            String credentials
    ) {
        return buildOptionsRequest(service.getServiceUrl(), from, fromPlaceId, to, toPlaceId, startTime, endTime, radiusMeter, share, credentials);
    }

    /**
     * Assembles a get-options request, specific to the mobility service
     * provider of this proxy, using the given params. The params are checked
     * for null values and added only if they are present and sensible.
     *
     * @param from
     * @param to
     * @param startTime
     * @param endTime
     * @param radiusMeter
     * @param share
     * @param credentials
     * @return
     */
    public EfsRequest<List<Options>> createOptionsRequest(
            String from,
            String to,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            Boolean share,
            String credentials
    ) {
        return buildOptionsRequest(service.getServiceUrl(), from, null, to, null, startTime, endTime, radiusMeter, share, credentials);
    }

    /**
     * Assembles a get-booking request, specific to the mobility service
     * provider of this proxy, using the given credentials.
     *
     * @param credentials JSON-serialized credentials object, specific to each
     * mobility service provider.
     * @return
     */
    public EfsRequest<List<Booking>> createGetBookingRequest(
            String credentials
    ) {
        return buildBookingRequest(service, credentials);
    }

    /**
     * Assembles a booking request for the given mobility service using the
     * given credentials.
     *
     * @param service
     * @param credentials JSON-serialized credentials object, specific to each
     * mobility service provider.
     * @return
     */
    public static EfsRequest<List<Booking>> buildBookingRequest(
            MobilityService service,
            String credentials
    ) {
        return EfsRequest
                .get(service.getServiceUrl() + BOOKINGS_PATH)
                .credentials(credentials)
                .expect(new ParameterizedTypeReference<List<Booking>>() {
                });
    }

    /**
     * Assembles a get-options request for the given provider service url with
     * the given params. The params are checked for null values and added only
     * if they are present and sensible.
     *
     * @param serviceUrl
     * @param from
     * @param to
     * @param startTime
     * @param endTime
     * @param radiusMeter
     * @param share
     * @param credentials
     * @return
     */
    public static EfsRequest<List<Options>> buildOptionsRequest(
            String serviceUrl,
            String from,
            String to,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            Boolean share,
            String credentials
    ) {
        return buildOptionsRequest(serviceUrl, from, null, to, null, startTime, endTime, radiusMeter, share, credentials);
    }

    /**
     * Assembles a get-options request for the given provider service url with
     * the given params. The params are checked for null values and added only
     * if they are present and sensible.
     *
     * @param serviceUrl
     * @param from
     * @param fromPlaceId
     * @param to
     * @param toPlaceId
     * @param startTime
     * @param endTime
     * @param radiusMeter
     * @param share
     * @param credentials
     * @return
     */
    public static EfsRequest<List<Options>> buildOptionsRequest(
            String serviceUrl,
            String from,
            String fromPlaceId,
            String to,
            String toPlaceId,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            Boolean share,
            String credentials
    ) {
        // Start build the request object...
        var request = EfsRequest
                .get(serviceUrl + OPTIONS_PATH)
                .query("from", from)
                .expect(new ParameterizedTypeReference<List<Options>>() {
                });

        // Building query string by adding existing params...
        if (isNotBlank(fromPlaceId)) {
            request.query("fromPlaceId", fromPlaceId);
        }
        if (isNotBlank(to)) {
            request.query("to", to);
        }
        if (isNotBlank(toPlaceId)) {
            request.query("toPlaceId", toPlaceId);
        }
        if (startTime != null) {
            request.query("startTime", startTime.toInstant().toEpochMilli());
        }
        if (endTime != null) {
            request.query("endTime", endTime.toInstant().toEpochMilli());
        }
        if (radiusMeter != null && radiusMeter >= 0) {
            request.query("radius", radiusMeter);
        }
        if (share != null) {
            request.query("share", share);
        }
        if (isNotBlank(credentials)) {
            request.credentials(credentials);
        }

        return request;
    }

}
