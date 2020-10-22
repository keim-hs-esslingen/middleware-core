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
import static de.hsesslingen.keim.efs.middleware.provider.IBookingApi.buildBookingRequest;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.service.MobilityService.API;
import de.hsesslingen.keim.efs.mobility.utils.EfsRequest;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import static de.hsesslingen.keim.efs.middleware.provider.IOptionsApi.buildSearchRequest;

/**
 *
 * @author keim
 */
public class ProviderProxy {

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
     * @param token
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
            String token
    ) {
        return buildSearchRequest(service.getServiceUrl(), from, fromPlaceId, to, toPlaceId, startTime, endTime, radiusMeter, share, token);
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
     * @param token
     * @return
     */
    public EfsRequest<List<Options>> createOptionsRequest(
            String from,
            String to,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            Boolean share,
            String token
    ) {
        return buildSearchRequest(service.getServiceUrl(), from, null, to, null, startTime, endTime, radiusMeter, share, token);
    }

    /**
     * Assembles a get-booking request, specific to the mobility service
     * provider of this proxy, using the given credentials.
     *
     * @param token JSON-serialized credentials object, specific to each
     * mobility service provider.
     * @return
     */
    public EfsRequest<List<Booking>> createGetBookingRequest(
            String token
    ) {
        return buildBookingRequest(service.getServiceUrl(), token);
    }

}
