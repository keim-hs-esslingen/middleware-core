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

import de.hsesslingen.keim.efs.middleware.model.Asset;
import de.hsesslingen.keim.efs.middleware.model.Booking;
import de.hsesslingen.keim.efs.middleware.model.BookingAction;
import de.hsesslingen.keim.efs.middleware.model.BookingState;
import de.hsesslingen.keim.efs.middleware.model.ICoordinates;
import de.hsesslingen.keim.efs.middleware.model.Leg;
import de.hsesslingen.keim.efs.middleware.model.NewBooking;
import de.hsesslingen.keim.efs.middleware.model.Option;
import de.hsesslingen.keim.efs.middleware.model.Place;
import de.hsesslingen.keim.efs.middleware.provider.IAssetsApi;
import de.hsesslingen.keim.efs.middleware.provider.IBookingApi;
import static de.hsesslingen.keim.efs.middleware.provider.IBookingApi.buildCreateNewBookingRequest;
import static de.hsesslingen.keim.efs.middleware.provider.IBookingApi.buildGetBookingByIdRequest;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.service.MobilityService.API;
import de.hsesslingen.keim.efs.mobility.utils.EfsRequest;
import static de.hsesslingen.keim.efs.middleware.provider.IBookingApi.buildGetBookingsRequest;
import static de.hsesslingen.keim.efs.middleware.provider.IBookingApi.buildModifyBookingRequest;
import static de.hsesslingen.keim.efs.middleware.provider.IBookingApi.buildPerformActionRequest;
import de.hsesslingen.keim.efs.middleware.provider.ICredentialsApi;
import static de.hsesslingen.keim.efs.middleware.provider.ICredentialsApi.buildCreateTokenRequest;
import static de.hsesslingen.keim.efs.middleware.provider.ICredentialsApi.buildDeleteTokenRequest;
import static de.hsesslingen.keim.efs.middleware.provider.ICredentialsApi.buildIsTokenValidRequest;
import de.hsesslingen.keim.efs.middleware.provider.IOptionsApi;
import static de.hsesslingen.keim.efs.middleware.provider.IOptionsApi.buildGetOptionsRequest;
import de.hsesslingen.keim.efs.middleware.provider.IPlacesApi;
import de.hsesslingen.keim.efs.middleware.provider.credentials.TokenCredentials;
import de.hsesslingen.keim.efs.middleware.utils.FlexibleZonedDateTimeParser;
import de.hsesslingen.keim.efs.mobility.service.MobilityType;
import de.hsesslingen.keim.efs.mobility.service.Mode;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

/**
 *
 * @author keim
 */
public class ProviderProxy {

    private final MobilityService service;

    public ProviderProxy(MobilityService service) {
        this.service = service;
    }

    /**
     * Get the service ID of the mobility service that this
     * {@link ProviderProxy} is associated with. Identical to calling
     * {@link getService().getId()}.
     *
     * @return
     */
    public String getServiceId() {
        return service.getId();
    }

    /**
     * Get the mobility service that this {@link ProviderProxy} is associated
     * with.
     *
     * @return
     */
    public MobilityService getService() {
        return service;
    }

    /**
     * Checks whether the mobility service associated with this
     * {@link ProviderProxy} supports the given {@link API}.
     *
     * @param api
     * @return
     */
    public boolean supportsApi(API api) {
        return service.getApis().contains(api);
    }

    /**
     * Checks whether the mobility service associated with this
     * {@link ProviderProxy} supports the given {@link Mode}.
     *
     * @param mode
     * @return
     */
    public boolean supportsMode(Mode mode) {
        return service.getModes().contains(mode);
    }

    /**
     * Checks whether the mobility service associated with this
     * {@link ProviderProxy} supports the given {@link MobilityType}.
     *
     * @param mobilityType
     * @return
     */
    public boolean supportsMobilityType(MobilityType mobilityType) {
        return service.getMobilityTypes().contains(mobilityType);
    }

    /**
     * Assembles a request for searching places at this provider using the given
     * arguments. For more information see:
     * {@link IPlacesApi#buildSearchRequest(String, String, ICoordinates, Integer, Integer, String)}
     *
     *
     * @param query The text that is to be used as query for searching places.
     * @param areaCenter An optional geo-location that defines the center of a
     * circular search area contrained by param {@link radiusMeter}. If no
     * radius is given, a default radius is chosen by the provider.
     * @param radiusMeter A radius in unit meter, that serves as a constraint
     * for param {@link areaCenter}. Only applied together with
     * {@link areaCenter}.
     * @param limitTo An optional upper limit of results for the response.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. Most providers do not require a token for querying
     * options using the {@link IPlacesApi}.
     * @return
     */
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

    /**
     * Sends a search-places request to this provider using the given arguments.
     * For more information see:
     * {@link IPlacesApi#buildSearchRequest(String, String, ICoordinates, Integer, Integer, String)}
     *
     * @param query The text that is to be used as query for searching places.
     * @param areaCenter An optional geo-location that defines the center of a
     * circular search area contrained by param {@link radiusMeter}. If no
     * radius is given, a default radius is chosen by the provider.
     * @param radiusMeter A radius in unit meter, that serves as a constraint
     * for param {@link areaCenter}. Only applied together with
     * {@link areaCenter}.
     * @param limitTo An optional upper limit of results for the response.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. Most providers do not require a token for querying
     * options using the {@link IPlacesApi}.
     * @return
     */
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

    /**
     * Assembles a request for getting information about an {@link Asset} at
     * this provider using the given arguments.For more information see:
     * {@link IAssetsApi#getAssetById(java.lang.String, java.lang.String)}
     *
     * @param assetId The ID of the asset which shall be retrieved, which can be
     * found in other objects e.g. of type {@link Leg}.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. Most providers do not require a token for querying
     * options using the {@link IAssetApi}.
     * @return
     */
    public EfsRequest<Asset> createGetAssetByIdRequest(
            String assetId,
            String token
    ) {
        return IAssetsApi.buildGetAssetByIdRequest(service.getServiceUrl(), assetId, token);
    }

    /**
     * Sends a get-asset request to this provider using the given arguments. For
     * more information see:
     * {@link IAssetsApi#getAssetById(java.lang.String, java.lang.String)}
     *
     * @param assetId The ID of the asset which shall be retrieved, which can be
     * found in other objects e.g. of type {@link Leg}.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. Most providers do not require a token for querying
     * options using the {@link IAssetApi}.
     * @return
     */
    public Asset getAssetById(
            String assetId,
            String token
    ) {
        return createGetAssetByIdRequest(assetId, token).go().getBody();
    }

    /**
     * Assembles a request for getting options at this provider using the given
     * arguments.For more information see:
     * {@link IOptionsApi#buildGetOptionsRequest(String, ICoordinates, String, ICoordinates, String, ZonedDateTime, ZonedDateTime, Integer, Boolean, Set, Set, Integer, String)}
     *
     *
     * @param from The desired starting location (coordinates) in
     * comma-separated form, e.g. 60.123,27.456.
     * @param fromPlaceId An optional place ID that represents the entity at
     * position {@link from}. This place ID is provider specific and can be
     * obtained using the places API. (See {@link IPlacesApi})
     * @param to A desired destination location (coordinates) in comma-separated
     * form, e.g. 60.123,27.456.
     * @param toPlaceId An optional place ID that represents the entity at
     * position {@link to}. This place ID is provider specific and can be
     * obtained using the places API. (See {@link IPlacesApi})
     * @param startTime Optional desired start time of mobility. Can <b>not</b>
     * be in past. Values up to 10 seconds in past from "now" are tolerated in
     * validation, to respect network and processing delays for HTTP requests.
     * Format is flexible. See {@link FlexibleZonedDateTimeParser} for details
     * on possible formats.
     * @param endTime Optional desired end time of mobility. Can <b>not</b> be
     * in past and must be after {@link startTime}, if {@link startTime} is
     * given. Values up to 10 seconds in past from "now" are tolerated in
     * validation, to respect network and processing delays for HTTP requests.
     * Format is flexible. See {@link FlexibleZonedDateTimeParser} for details
     * on possible formats.
     * @param radiusMeter Maximum distance a user wants to travel to reach the
     * start point of the mobility option in meters. This basically serves as a
     * search radius around the geo-position given in param {@link from}.
     * @param sharingAllowed Defines if user is ok with sharing his mobility
     * option with others, potentially unknown people.
     * @param modesAllowed Allowed modes for legs and potential sub-legs of all
     * options returned.
     * @param mobilityTypesAllowed Allowed mobilityTypes for legs and potential
     * sub-legs of all options returned.
     * @param limitTo An optional upper limit of results for the response.
     * @param includeGeoPaths Whether detailed information about the path of
     * legs or about free floating areas should be included, if available.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. Most providers do not require a token for querying
     * options using the {@link IOptionsApi}.
     * @return
     */
    public EfsRequest<List<Option>> createGetOptionsRequest(
            ICoordinates from,
            String fromPlaceId,
            ICoordinates to,
            String toPlaceId,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            Boolean sharingAllowed,
            Set<Mode> modesAllowed,
            Set<MobilityType> mobilityTypesAllowed,
            Integer limitTo,
            Boolean includeGeoPaths,
            String token
    ) {
        return buildGetOptionsRequest(service.getServiceUrl(),
                from, fromPlaceId, to, toPlaceId, startTime, endTime,
                radiusMeter, sharingAllowed, modesAllowed, mobilityTypesAllowed,
                limitTo, includeGeoPaths, token
        );
    }

    /**
     * Sends a get-options request to this provider using the given
     * arguments.For more information see:
     * {@link IOptionsApi#buildGetOptionsRequest(String, ICoordinates, String, ICoordinates, String, ZonedDateTime, ZonedDateTime, Integer, Boolean, Set, Set, Integer, String)}
     *
     * @param from The desired starting location (coordinates) in
     * comma-separated form, e.g. 60.123,27.456.
     * @param fromPlaceId An optional place ID that represents the entity at
     * position {@link from}. This place ID is provider specific and can be
     * obtained using the places API. (See {@link IPlacesApi})
     * @param to A desired destination location (coordinates) in comma-separated
     * form, e.g. 60.123,27.456.
     * @param toPlaceId An optional place ID that represents the entity at
     * position {@link to}. This place ID is provider specific and can be
     * obtained using the places API. (See {@link IPlacesApi})
     * @param startTime Optional desired start time of mobility. Can <b>not</b>
     * be in past. Values up to 10 seconds in past from "now" are tolerated in
     * validation, to respect network and processing delays for HTTP requests.
     * Format is flexible. See {@link FlexibleZonedDateTimeParser} for details
     * on possible formats.
     * @param endTime Optional desired end time of mobility. Can <b>not</b> be
     * in past and must be after {@link startTime}, if {@link startTime} is
     * given. Values up to 10 seconds in past from "now" are tolerated in
     * validation, to respect network and processing delays for HTTP requests.
     * Format is flexible. See {@link FlexibleZonedDateTimeParser} for details
     * on possible formats.
     * @param radiusMeter Maximum distance a user wants to travel to reach the
     * start point of the mobility option in meters. This basically serves as a
     * search radius around the geo-position given in param {@link from}.
     * @param sharingAllowed Defines if user is ok with sharing his mobility
     * option with others, potentially unknown people.
     * @param modesAllowed Allowed modes for legs and potential sub-legs of all
     * options returned.
     * @param mobilityTypesAllowed Allowed mobilityTypes for legs and potential
     * sub-legs of all options returned.
     * @param limitTo An optional upper limit of results for the response.
     * @param includeGeoPaths Whether detailed information about the path of
     * legs or about free floating areas should be included, if available.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. Most providers do not require a token for querying
     * options using the {@link IOptionsApi}.
     * @return
     */
    public List<Option> getOptions(
            ICoordinates from,
            String fromPlaceId,
            ICoordinates to,
            String toPlaceId,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            Boolean sharingAllowed,
            Set<Mode> modesAllowed,
            Set<MobilityType> mobilityTypesAllowed,
            Integer limitTo,
            Boolean includeGeoPaths,
            String token
    ) {
        return createGetOptionsRequest(from, fromPlaceId, to, toPlaceId, startTime, endTime, radiusMeter, sharingAllowed, modesAllowed, mobilityTypesAllowed, limitTo, includeGeoPaths, token)
                .go()
                .getBody();
    }

    /**
     * Assembles a request for getting options at this provider using the given
     * arguments.For more information see:
     * {@link IOptionsApi#buildGetOptionsRequest(String, de.hsesslingen.keim.efs.middleware.model.ICoordinates, de.hsesslingen.keim.efs.middleware.model.ICoordinates, ZonedDateTime, ZonedDateTime, Integer, Boolean, Set, Set, Integer, String)}
     *
     * @param from The desired starting location (coordinates) in
     * comma-separated form, e.g. 60.123,27.456.
     * @param to A desired destination as geo-location in comma-separated form,
     * e.g. 60.123,27.456.
     * @param startTime Optional desired start time of mobility. Can <b>not</b>
     * be in past. Values up to 10 seconds in past from "now" are tolerated in
     * validation, to respect network and processing delays for HTTP requests.
     * Format is flexible. See {@link FlexibleZonedDateTimeParser} for details
     * on possible formats.
     * @param endTime Optional desired end time of mobility. Can <b>not</b> be
     * in past and must be after {@link startTime}, if {@link startTime} is
     * given. Values up to 10 seconds in past from "now" are tolerated in
     * validation, to respect network and processing delays for HTTP requests.
     * Format is flexible. See {@link FlexibleZonedDateTimeParser} for details
     * on possible formats.
     * @param radiusMeter Maximum distance a user wants to travel to reach the
     * start point of the mobility option in meters. This basically serves as a
     * search radius around the geo-position given in param {@link from}.
     * @param sharingAllowed Defines if user is ok with sharing his mobility
     * option with others, potentially unknown people.
     * @param modesAllowed Allowed modes for legs and potential sub-legs of all
     * options returned.
     * @param mobilityTypesAllowed Allowed mobilityTypes for legs and potential
     * sub-legs of all options returned.
     * @param limitTo An optional upper limit of results for the response.
     * @param includeGeoPaths Whether detailed information about the path of
     * legs or about free floating areas should be included, if available.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. Most providers do not require a token for querying
     * options using the {@link IOptionsApi}.
     * @return
     */
    public EfsRequest<List<Option>> createGetOptionsRequest(
            ICoordinates from,
            ICoordinates to,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            boolean sharingAllowed,
            Set<Mode> modesAllowed,
            Set<MobilityType> mobilityTypesAllowed,
            Integer limitTo,
            boolean includeGeoPaths,
            String token
    ) {
        return buildGetOptionsRequest(service.getServiceUrl(),
                from, to, startTime, endTime, radiusMeter, sharingAllowed,
                modesAllowed, mobilityTypesAllowed, limitTo, includeGeoPaths, token
        );
    }

    /**
     * Sends a get-options request to this provider using the given
     * arguments.For more information see:
     * {@link IOptionsApi#buildGetOptionsRequest(String, ICoordinates, ICoordinates, ZonedDateTime, ZonedDateTime, Integer, Boolean, Set, Set, Integer, String)}
     *
     * @param from The desired starting location (coordinates) in
     * comma-separated form, e.g. 60.123,27.456.
     * @param to A desired destination as geo-location in comma-separated form,
     * e.g. 60.123,27.456.
     * @param startTime Optional desired start time of mobility. Can <b>not</b>
     * be in past. Values up to 10 seconds in past from "now" are tolerated in
     * validation, to respect network and processing delays for HTTP requests.
     * Format is flexible. See {@link FlexibleZonedDateTimeParser} for details
     * on possible formats.
     * @param endTime Optional desired end time of mobility. Can <b>not</b> be
     * in past and must be after {@link startTime}, if {@link startTime} is
     * given. Values up to 10 seconds in past from "now" are tolerated in
     * validation, to respect network and processing delays for HTTP requests.
     * Format is flexible. See {@link FlexibleZonedDateTimeParser} for details
     * on possible formats.
     * @param radiusMeter Maximum distance a user wants to travel to reach the
     * start point of the mobility option in meters. This basically serves as a
     * search radius around the geo-position given in param {@link from}.
     * @param sharingAllowed Defines if user is ok with sharing his mobility
     * option with others, potentially unknown people.
     * @param modesAllowed Allowed modes for legs and potential sub-legs of all
     * options returned.
     * @param mobilityTypesAllowed Allowed mobilityTypes for legs and potential
     * sub-legs of all options returned.
     * @param limitTo An optional upper limit of results for the response.
     * @param includeGeoPaths Whether detailed information about the path of
     * legs or about free floating areas should be included, if available.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. Most providers do not require a token for querying
     * options using the {@link IOptionsApi}.
     * @return
     */
    public List<Option> getOptions(
            ICoordinates from,
            ICoordinates to,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            Boolean sharingAllowed,
            Set<Mode> modesAllowed,
            Set<MobilityType> mobilityTypesAllowed,
            Integer limitTo,
            Boolean includeGeoPaths,
            String token
    ) {
        return createGetOptionsRequest(from, to, startTime, endTime, radiusMeter, sharingAllowed, modesAllowed, mobilityTypesAllowed, limitTo, includeGeoPaths, token)
                .go()
                .getBody();
    }

    /**
     * Assembles a request for getting bookings at this provider using the given
     * arguments. For more information see:
     * {@link IBookingApi#getBookings(de.hsesslingen.keim.efs.middleware.model.BookingState, java.lang.String)}
     *
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @return
     */
    public EfsRequest<List<Booking>> createGetBookingsRequest(
            String token
    ) {
        return buildGetBookingsRequest(service.getServiceUrl(), token);
    }

    /**
     * Sends a get-bookings request to this provider using the given arguments.
     * For more information see:
     * {@link IBookingApi#getBookings(de.hsesslingen.keim.efs.middleware.model.BookingState, java.lang.String)}
     *
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @return
     */
    public List<Booking> getBookings(
            String token
    ) {
        return createGetBookingsRequest(token).go().getBody();
    }

    /**
     * Assembles a request for getting bookings at this provider using the given
     * arguments. For more information see:
     * {@link IBookingApi#getBookings(de.hsesslingen.keim.efs.middleware.model.BookingState, java.lang.String)}
     *
     * @param state An optional state by which to filter the bookings.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @return
     */
    public EfsRequest<List<Booking>> createGetBookingsRequest(
            BookingState state,
            String token
    ) {
        return buildGetBookingsRequest(service.getServiceUrl(), state, token);
    }

    /**
     * Sends a get-bookings request to this provider using the given arguments.
     * For more information see:
     * {@link IBookingApi#getBookings(de.hsesslingen.keim.efs.middleware.model.BookingState, java.lang.String)}
     *
     * @param state An optional state by which to filter the bookings.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @return
     */
    public List<Booking> getBookings(
            BookingState state,
            String token
    ) {
        return createGetBookingsRequest(state, token).go().getBody();
    }

    /**
     * Assembles a request for getting a specific {@link Booking} object at this
     * provider using the given arguments.For more information see:
     * {@link IBookingApi#getBookingById(java.lang.String, java.lang.String)}
     *
     * @param id The ID of the booking which shall be retrieved.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @return The {@link Booking} object
     */
    public EfsRequest<Booking> createGetBookingByIdRequest(
            String id,
            String token
    ) {
        return buildGetBookingByIdRequest(service.getServiceUrl(), id, token);
    }

    /**
     * Sends a get-booking request to this provider using the given
     * arguments.For more information see:
     * {@link IBookingApi#getBookingById(java.lang.String, java.lang.String)}
     *
     * @param id The ID of the booking which shall be retrieved.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @return The {@link Booking} object
     */
    public Booking getBookingById(
            String id,
            String token
    ) {
        return createGetBookingByIdRequest(id, token).go().getBody();
    }

    /**
     * Assembles a request for creating a new booking at this provider using the
     * given arguments.For more information see:
     * {@link IBookingApi#createNewBooking(de.hsesslingen.keim.efs.middleware.model.NewBooking, java.lang.String, java.lang.String)}
     *
     * @param newBooking The {@link NewBooking} that should be created.
     * @param optionReference An optional reference to an {@link Option} that
     * unambiguously references this option for booking. This reference is
     * sometimes given in instances of {@link Option}.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @return {@link Booking} that was created
     */
    public EfsRequest<Booking> createCreateBookingRequest(
            NewBooking newBooking,
            String optionReference,
            String token
    ) {
        return buildCreateNewBookingRequest(service.getServiceUrl(), newBooking, optionReference, token);
    }

    /**
     * Sends a create-booking request to this provider using the given
     * arguments. For more information see:
     * {@link IBookingApi#createNewBooking(de.hsesslingen.keim.efs.middleware.model.NewBooking, java.lang.String, java.lang.String)}
     *
     * @param newBooking The {@link NewBooking} that should be created.
     * @param optionReference An optional reference to an {@link Option} that
     * unambiguously references this option for booking. This reference is
     * sometimes given in instances of {@link Option}.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @return {@link Booking} that was created
     */
    public Booking createBooking(
            NewBooking newBooking,
            String optionReference,
            String token
    ) {
        return createCreateBookingRequest(newBooking, optionReference, token).go().getBody();
    }

    /**
     * Assembles a request for modifying an existing booking at this provider
     * using the given arguments. For more information see:
     * {@link IBookingApi#modifyBooking(java.lang.String, de.hsesslingen.keim.efs.middleware.model.Booking, java.lang.String)}
     *
     * @param booking The {@link Booking} object containing modified data
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @return the modified {@link Booking} object
     */
    public EfsRequest<Booking> createModifyBookingRequest(
            Booking booking,
            String token
    ) {
        return buildModifyBookingRequest(service.getServiceUrl(), booking, token);
    }

    /**
     * Sends a modify-booking request to this provider using the given
     * arguments.For more information see:
     * {@link IBookingApi#modifyBooking(java.lang.String, de.hsesslingen.keim.efs.middleware.model.Booking, java.lang.String)}
     *
     * @param booking The {@link Booking} object containing modified data
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @return the modified {@link Booking} object
     */
    public Booking modifyBooking(
            Booking booking,
            String token
    ) {
        return createModifyBookingRequest(booking, token).go().getBody();
    }

    /**
     * Assembles a request for performing a {@link BookingAction} on an existing
     * booking at this provider using the given arguments.For more information
     * see:
     * {@link IBookingApi#performAction(java.lang.String, de.hsesslingen.keim.efs.middleware.model.BookingAction, java.lang.String, java.lang.String)}
     *
     * @param bookingId The ID of the booking on which to perform the action.
     * @param action The action that should be performed on the booking with the
     * given {@link bookingId}.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @return
     */
    public EfsRequest<Void> createPerformActionRequest(
            String bookingId,
            BookingAction action,
            String token
    ) {
        return buildPerformActionRequest(service.getServiceUrl(), bookingId, action, token);
    }

    /**
     * Sends a perform-booking-action request to this provider using the given
     * arguments.For more information see:
     * {@link IBookingApi#performAction(java.lang.String, de.hsesslingen.keim.efs.middleware.model.BookingAction, java.lang.String, java.lang.String)}
     *
     * @param bookingId The ID of the booking on which to perform the action.
     * @param action The action that should be performed on the booking with the
     * given {@link bookingId}.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     */
    public void performAction(
            String bookingId,
            BookingAction action,
            String token
    ) {
        createPerformActionRequest(bookingId, action, token).go();
    }

    /**
     * Assembles a request for performing a {@link BookingAction} on an existing
     * booking at this provider using the given arguments. For more information
     * see:
     * {@link IBookingApi#performAction(java.lang.String, de.hsesslingen.keim.efs.middleware.model.BookingAction, java.lang.String, java.lang.String)}
     *
     * @param bookingId The ID of the booking on which to perform the action.
     * @param action The action that should be performed on the booking with the
     * given {@link bookingId}.
     * @param secret An optional secret that might be required by some mobility
     * service providers to perform this action. (e.g. a PIN)
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     * @return
     */
    public EfsRequest<Void> createPerformActionRequest(
            String bookingId,
            BookingAction action,
            String secret,
            String token
    ) {
        return buildPerformActionRequest(service.getServiceUrl(), bookingId, action, secret, token);
    }

    /**
     * Sends a perform-booking-action request to this provider using the given
     * arguments. For more information see:
     * {@link IBookingApi#performAction(java.lang.String, de.hsesslingen.keim.efs.middleware.model.BookingAction, java.lang.String, java.lang.String)}
     *
     * @param bookingId The ID of the booking on which to perform the action.
     * @param action The action that should be performed on the booking with the
     * given {@link bookingId}.
     * @param secret An optional secret that might be required by some mobility
     * service providers to perform this action. (e.g. a PIN)
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. This value is almost certainly required by all
     * mobility service providers for querying the {@link IBookingApi}.
     */
    public void performAction(
            String bookingId,
            BookingAction action,
            String secret,
            String token
    ) {
        createPerformActionRequest(bookingId, action, secret, token).go();
    }

    /**
     * Assembles a request for creating a new token at this provider using the
     * given arguments. For more information see:
     * {@link ICredentialsApi#createToken(java.lang.String, java.lang.String)}
     *
     * @param userId A string value that uniquely identifies a user. (e.g. an
     * email adress, a username, ...). This value can be left out if applicable
     * at the particular mobility service provider.
     * @param secret The secret that authenticates the user with the given
     * {@link userId} or identifies <em>and</em> authenticates the user at the
     * same time, if not {@link userId} is applicable for this mobility service
     * provider. In every case this value is the authenticating piece of
     * information and therefore it is required.
     * @return An instance of TokenCredentials that contains a provider specific
     * token, which can be used as is.
     */
    public EfsRequest<TokenCredentials> createCreateTokenRequest(
            String userId,
            String secret
    ) {
        return buildCreateTokenRequest(service.getServiceUrl(), userId, secret);
    }

    /**
     * Sends a create-token request to this provider using the given arguments.
     * For more information see:
     * {@link ICredentialsApi#createToken(java.lang.String, java.lang.String)}
     *
     * @param userId A string value that uniquely identifies a user. (e.g. an
     * email adress, a username, ...). This value can be left out if applicable
     * at the particular mobility service provider.
     * @param secret The secret that authenticates the user with the given
     * {@link userId} or identifies <em>and</em> authenticates the user at the
     * same time, if not {@link userId} is applicable for this mobility service
     * provider. In every case this value is the authenticating piece of
     * information and therefore it is required.
     * @return An instance of TokenCredentials that contains a provider specific
     * token, which can be used as is.
     */
    public TokenCredentials createToken(
            String userId,
            String secret
    ) {
        return createCreateTokenRequest(userId, secret).go().getBody();
    }

    /**
     * Assembles a request for deleting (invalidating) an existing token this
     * provider using the given arguments. For more information see:
     * {@link ICredentialsApi#deleteToken(java.lang.String)}
     *
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity.
     * @return
     */
    public EfsRequest<Void> createDeleteTokenRequest(String token) {
        return buildDeleteTokenRequest(service.getServiceUrl(), token);
    }

    /**
     * Sends a delete-token (invalidate-token) request to this provider using
     * the given arguments. For more information see:
     * {@link ICredentialsApi#deleteToken(java.lang.String)}
     *
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity.
     */
    public void deleteToken(String token) {
        createDeleteTokenRequest(token).go();
    }

    /**
     * Assembles a request for checking the validity of an existing token at
     * this provider using the given arguments. For more information see:
     * {@link ICredentialsApi#isTokenValid(java.lang.String)}
     *
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity.
     * @return true if valid, false if not.
     */
    public EfsRequest<Boolean> createIsTokenValidRequest(String token) {
        return buildIsTokenValidRequest(service.getServiceUrl(), token);
    }

    /**
     * Sends a is-token-valid request to this provider using the given
     * arguments. For more information see:
     * {@link ICredentialsApi#isTokenValid(java.lang.String)}
     *
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity.
     * @return true if valid, false if not.
     */
    public Boolean isTokenValid(String token) {
        return createIsTokenValidRequest(token).go().getBody();
    }
}
