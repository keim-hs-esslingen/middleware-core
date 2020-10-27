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
import de.hsesslingen.keim.efs.mobility.service.MobilityType;
import de.hsesslingen.keim.efs.mobility.service.Mode;

/**
 *
 * @author keim
 */
public class ProviderProxy {

    private final MobilityService service;

    public ProviderProxy(MobilityService service) {
        this.service = service;
    }

    public String getServiceId() {
        return service.getId();
    }

    public MobilityService getService() {
        return service;
    }

    public boolean supportsApi(API api) {
        return service.getApis().contains(api);
    }

    public boolean supportsMode(Mode mode) {
        return service.getModes().contains(mode);
    }

    public boolean supportsMobilityType(MobilityType mt) {
        return service.getMobilityTypes().contains(mt);
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

    public List<Place> searchPlaces(
            String query,
            ICoordinates areaCenter,
            Integer radiusMeter,
            Integer limitTo,
            String token
    ) {
        return createSearchPlacesRequest(query, areaCenter, radiusMeter, limitTo, token)
                .go()
                .getBody();
    }

    public EfsRequest<List<Options>> createGetOptionsRequest(
            String from,
            String fromPlaceId,
            String to,
            String toPlaceId,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            Boolean sharingAllowed,
            Set<Mode> modesAllowed,
            Set<MobilityType> mobilityTypesAllowed,
            Integer limitTo,
            String token
    ) {
        return buildGetOptionsRequest(service.getServiceUrl(),
                from, fromPlaceId, to, toPlaceId, startTime, endTime,
                radiusMeter, sharingAllowed, modesAllowed, mobilityTypesAllowed,
                limitTo, token
        );
    }

    public List<Options> getOptions(
            String from,
            String fromPlaceId,
            String to,
            String toPlaceId,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            Boolean sharingAllowed,
            Set<Mode> modesAllowed,
            Set<MobilityType> mobilityTypesAllowed,
            Integer limitTo,
            String token
    ) {
        return createGetOptionsRequest(from, fromPlaceId, to, toPlaceId, startTime, endTime, radiusMeter, sharingAllowed, modesAllowed, mobilityTypesAllowed, limitTo, token)
                .go()
                .getBody();
    }

    public EfsRequest<List<Options>> createGetOptionsRequest(
            String from,
            String to,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            Boolean sharingAllowed,
            Set<Mode> modesAllowed,
            Set<MobilityType> mobilityTypesAllowed,
            Integer limitTo,
            String token
    ) {
        return buildGetOptionsRequest(service.getServiceUrl(),
                from, to, startTime, endTime, radiusMeter, sharingAllowed,
                modesAllowed, mobilityTypesAllowed, limitTo, token
        );
    }

    public List<Options> getOptions(
            String from,
            String to,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            Boolean sharingAllowed,
            Set<Mode> modesAllowed,
            Set<MobilityType> mobilityTypesAllowed,
            Integer limitTo,
            String token
    ) {
        return createGetOptionsRequest(from, to, startTime, endTime, radiusMeter, sharingAllowed, modesAllowed, mobilityTypesAllowed, limitTo, token)
                .go()
                .getBody();
    }

    public EfsRequest<List<Booking>> createGetBookingsRequest(
            String token
    ) {
        return buildGetBookingsRequest(service.getServiceUrl(), token);
    }

    public List<Booking> getBookings(
            String token
    ) {
        return createGetBookingsRequest(token).go().getBody();
    }

    public EfsRequest<List<Booking>> createGetBookingsRequest(
            BookingState state,
            String token
    ) {
        return buildGetBookingsRequest(service.getServiceUrl(), state, token);
    }

    public List<Booking> getBookings(
            BookingState state,
            String token
    ) {
        return createGetBookingsRequest(state, token).go().getBody();
    }

    public EfsRequest<Booking> createGetBookingByIdRequest(
            String id,
            String token
    ) {
        return buildGetBookingByIdRequest(service.getServiceUrl(), id, token);
    }

    public Booking getBookingById(
            String id,
            String token
    ) {
        return createGetBookingByIdRequest(id, token).go().getBody();
    }

    public EfsRequest<Booking> createCreateBookingRequest(
            NewBooking newBooking,
            String token
    ) {
        return buildCreateNewBookingRequest(service.getServiceUrl(), newBooking, token);
    }

    public Booking createBooking(
            NewBooking newBooking,
            String token
    ) {
        return createCreateBookingRequest(newBooking, token).go().getBody();
    }

    public EfsRequest<Booking> createModifyBookingRequest(
            Booking booking,
            String token
    ) {
        return buildModifyBookingRequest(service.getServiceUrl(), booking, token);
    }

    public Booking modifyBooking(
            Booking booking,
            String token
    ) {
        return createModifyBookingRequest(booking, token).go().getBody();
    }

    public EfsRequest<Void> createPerformActionRequest(
            String bookingId,
            BookingAction action,
            String token
    ) {
        return buildPerformActionRequest(service.getServiceUrl(), bookingId, action, token);
    }

    public void performAction(
            String bookingId,
            BookingAction action,
            String token
    ) {
        createPerformActionRequest(bookingId, action, token).go();
    }

    public EfsRequest<Void> createPerformActionRequest(
            String bookingId,
            BookingAction action,
            String secret,
            String token
    ) {
        return buildPerformActionRequest(service.getServiceUrl(), bookingId, action, secret, token);
    }

    public void performAction(
            String bookingId,
            BookingAction action,
            String secret,
            String token
    ) {
        createPerformActionRequest(bookingId, action, secret, token).go();
    }

    public EfsRequest<TokenCredentials> createCreateTokenRequest(
            String userId,
            String secret
    ) {
        return buildCreateTokenRequest(service.getServiceUrl(), userId, secret);
    }

    public TokenCredentials createToken(
            String userId,
            String secret
    ) {
        return createCreateTokenRequest(userId, secret).go().getBody();
    }

    public EfsRequest<Void> createDeleteTokenRequest(String token) {
        return buildDeleteTokenRequest(service.getServiceUrl(), token);
    }

    public void deleteToken(String token) {
        createDeleteTokenRequest(token).go();
    }

    public EfsRequest<Boolean> createIsTokenValidRequest(String token) {
        return buildIsTokenValidRequest(service.getServiceUrl(), token);
    }

    public Boolean isTokenValid(String token) {
        return createIsTokenValidRequest(token).go().getBody();
    }
}
