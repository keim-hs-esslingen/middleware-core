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
import de.hsesslingen.keim.efs.middleware.model.BookingAction;
import de.hsesslingen.keim.efs.middleware.model.BookingState;
import de.hsesslingen.keim.efs.middleware.model.ICoordinates;
import de.hsesslingen.keim.efs.middleware.model.NewBooking;
import de.hsesslingen.keim.efs.middleware.model.Options;
import de.hsesslingen.keim.efs.middleware.model.Place;
import static de.hsesslingen.keim.efs.middleware.provider.IBookingApi.buildCreateNewBookingRequest;
import static de.hsesslingen.keim.efs.middleware.provider.IBookingApi.buildGetBookingByIdRequest;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.service.MobilityService.API;
import de.hsesslingen.keim.efs.mobility.utils.EfsRequest;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import static de.hsesslingen.keim.efs.middleware.provider.IBookingApi.buildGetBookingsRequest;
import static de.hsesslingen.keim.efs.middleware.provider.IBookingApi.buildModifyBookingRequest;
import static de.hsesslingen.keim.efs.middleware.provider.IBookingApi.buildPerformActionRequest;
import static de.hsesslingen.keim.efs.middleware.provider.ICredentialsApi.buildCreateTokenRequest;
import static de.hsesslingen.keim.efs.middleware.provider.ICredentialsApi.buildDeleteTokenRequest;
import static de.hsesslingen.keim.efs.middleware.provider.ICredentialsApi.buildIsTokenValidRequest;
import static de.hsesslingen.keim.efs.middleware.provider.IOptionsApi.buildGetOptionsRequest;
import de.hsesslingen.keim.efs.middleware.provider.IPlacesApi;
import de.hsesslingen.keim.efs.middleware.provider.credentials.TokenCredentials;

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

    public EfsRequest<List<Place>> createSearchPlacesRequest(
            String query,
            ICoordinates areaCenter,
            Integer radiusMeter,
            Integer limitTo,
            String token
    ) {
        return IPlacesApi.buildSearchRequest(service.getServiceUrl(),
                query, areaCenter, radiusMeter, limitTo, token
        );
    }

    public EfsRequest<List<Options>> createGetOptionsRequest(
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
        return buildGetOptionsRequest(
                service.getServiceUrl(),
                from, fromPlaceId, to, toPlaceId,
                startTime, endTime, radiusMeter, share, token
        );
    }

    public EfsRequest<List<Options>> createGetOptionsRequest(
            String from,
            String to,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            Boolean share,
            String token
    ) {
        return buildGetOptionsRequest(service.getServiceUrl(),
                from, to, startTime, endTime, radiusMeter, share, token
        );
    }

    public EfsRequest<List<Booking>> createGetBookingsRequest(
            String token
    ) {
        return buildGetBookingsRequest(service.getServiceUrl(), token);
    }

    public EfsRequest<List<Booking>> createGetBookingsRequest(
            BookingState state,
            String token
    ) {
        return buildGetBookingsRequest(service.getServiceUrl(), state, token);
    }

    public EfsRequest<Booking> createGetBookingByIdRequest(
            String id,
            String token
    ) {
        return buildGetBookingByIdRequest(service.getServiceUrl(), id, token);
    }

    public EfsRequest<Booking> createCreateBookingRequest(
            NewBooking newBooking,
            String token
    ) {
        return buildCreateNewBookingRequest(service.getServiceUrl(), newBooking, token);
    }

    public EfsRequest<Booking> createModifyBookingRequest(
            Booking booking,
            String token
    ) {
        return buildModifyBookingRequest(service.getServiceUrl(), booking, token);
    }

    public EfsRequest<Void> createPerformActionRequest(
            String bookingId,
            BookingAction action,
            String token
    ) {
        return buildPerformActionRequest(service.getServiceUrl(), bookingId, action, token);
    }

    public EfsRequest<Void> createPerformActionRequest(
            String bookingId,
            BookingAction action,
            String secret,
            String token
    ) {
        return buildPerformActionRequest(service.getServiceUrl(), bookingId, action, secret, token);
    }

    public EfsRequest<TokenCredentials> createCreateTokenRequest(
            String userId,
            String secret
    ) {
        return buildCreateTokenRequest(service.getServiceUrl(), userId, secret);
    }

    public EfsRequest<Void> createDeleteTokenRequest(
            String token
    ) {
        return buildDeleteTokenRequest(service.getServiceUrl(), token);
    }

    public EfsRequest<Boolean> createIsTokenValidRequest(
            String token
    ) {
        return buildIsTokenValidRequest(service.getServiceUrl(), token);
    }

}
