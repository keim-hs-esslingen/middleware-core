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

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.hsesslingen.keim.efs.middleware.model.Booking;
import de.hsesslingen.keim.efs.middleware.model.BookingState;
import de.hsesslingen.keim.efs.middleware.model.BookingAction;
import de.hsesslingen.keim.efs.middleware.model.NewBooking;
import static de.hsesslingen.keim.efs.middleware.provider.ICredentialsApi.TOKEN_DESCRIPTION;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.RequestHeader;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import de.hsesslingen.keim.efs.middleware.validation.ConsistentBookingDateParams;
import de.hsesslingen.keim.efs.middleware.validation.OnCreate;
import de.hsesslingen.keim.efs.mobility.config.EfsSwaggerApiResponseSupport;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.utils.EfsRequest;
import static de.hsesslingen.keim.efs.mobility.utils.EfsRequest.TOKEN_HEADER;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;

/**
 * @author k.sivarasah 17 Oct 2019
 */
@EfsSwaggerApiResponseSupport
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public interface IBookingApi {

    public static final String BOOKINGS_PATH = "/bookings";

    /**
     * Returns a list of bookings.
     *
     * @param state The state for which to filter the bookings.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. Can be {@code null}.
     * @return List of {@link Booking}
     */
    @GetMapping(BOOKINGS_PATH)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get Bookings", notes = "Returns a list of Booking optionally filtered by their state.")
    public List<Booking> getBookings(
            @RequestParam(required = false) BookingState state,
            @RequestHeader(name = TOKEN_HEADER, required = false) @ApiParam(value = TOKEN_DESCRIPTION) String token
    );

    /**
     * Gets a {@link Booking} using the booking id
     *
     * @param id The booking id.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. Can be {@code null}.
     * @return the {@link Booking} object
     */
    @GetMapping(BOOKINGS_PATH + "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get Booking by Id", notes = "Returns the Booking with the given unique booking id")
    public Booking getBookingById(
            @PathVariable String id,
            @RequestHeader(name = TOKEN_HEADER, required = false) @ApiParam(value = TOKEN_DESCRIPTION) String token
    );

    /**
     * Creates a new booking and returns it.
     *
     * @param newBooking {@link NewBooking} that should be created
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. Can be {@code null}.
     * @return {@link Booking} that was created
     */
    @PostMapping(BOOKINGS_PATH)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a new Booking", notes = "Creates a new Booking for a service-provider "
            + "in BOOKED or STARTED state using the provided NewBooking object and returns it")
    public Booking createNewBooking(
            @RequestBody @Validated(OnCreate.class) @Valid @ConsistentBookingDateParams NewBooking newBooking,
            @RequestHeader(name = TOKEN_HEADER, required = false) @ApiParam(value = TOKEN_DESCRIPTION) String token
    );

    /**
     * Updates an existing {@link Booking} with new details.
     *
     * @param id the booking id
     * @param booking the {@link Booking} object containing modified data
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. Can be {@code null}.
     * @return the modified {@link Booking} object
     */
    @PutMapping(BOOKINGS_PATH + "/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ApiOperation(value = "Modify a Booking", notes = "Updates an existing Booking with the provided details")
    public Booking modifyBooking(
            @PathVariable String id,
            @RequestBody @Valid @ConsistentBookingDateParams Booking booking,
            @RequestHeader(name = TOKEN_HEADER, required = false) @ApiParam(value = TOKEN_DESCRIPTION) String token
    );

    /**
     * Can be used to perform actions on bookings.
     *
     * @param bookingId The ID of the booking on which to perform the action.
     * @param action The action that should be performed on the booking with the
     * given bookingId.
     * @param secret A secret that might be required by some services to perform
     * this action. (e.g. a PIN)
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. Can be {@code null}.
     */
    @PostMapping(BOOKINGS_PATH + "/{bookingId}/action/{action}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Perform an action on a booking", notes = "Performs the given action on a booking.")
    public void performAction(
            @PathVariable String bookingId,
            @PathVariable BookingAction action,
            @RequestParam(required = false) String secret,
            @RequestHeader(name = TOKEN_HEADER, required = false) @ApiParam(value = TOKEN_DESCRIPTION) String token
    );

    /**
     * Assembles a request, matching the {@code GET /bookings} endpoint, for the
     * service with the given url using the given token.
     * <p>
     * The returned request can be sent using {@code request.go()} which will
     * return a {@link ResponseEntity}.
     *
     * @param serviceUrl The base url of the mobility service who should be
     * queried. Use {@link MobilityService#getServiceUrl()} to get this url.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. Can be {@code null}.
     * @return
     */
    public static EfsRequest<List<Booking>> buildGetBookingsRequest(
            String serviceUrl,
            String token
    ) {
        return EfsRequest
                .get(serviceUrl + BOOKINGS_PATH)
                .token(token)
                .expect(new ParameterizedTypeReference<List<Booking>>() {
                });
    }

    /**
     * Assembles a request, matching the {@code GET /bookings} endpoint, for the
     * service with the given url using the given token.
     * <p>
     * The returned request can be sent using {@code request.go()} which will
     * return a {@link ResponseEntity}.
     *
     * @param serviceUrl The base url of the mobility service who should be
     * queried. Use {@link MobilityService#getServiceUrl()} to get this url.
     * @param state A booking state by which to filter the results.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. Can be {@code null}.
     * @return
     */
    public static EfsRequest<List<Booking>> buildGetBookingsRequest(
            String serviceUrl,
            BookingState state,
            String token
    ) {
        return buildGetBookingsRequest(serviceUrl, token).query("state", state);
    }

    /**
     * Assembles a request, matching the {@code GET /bookings/{bookingId}}
     * endpoint, for the service with the given url using the given token.
     * <p>
     * The returned request can be sent using {@code request.go()} which will
     * return a {@link ResponseEntity}.
     *
     * @param serviceUrl The base url of the mobility service who should be
     * queried. Use {@link MobilityService#getServiceUrl()} to get this url.
     * @param id The ID of the booking which shall be retrieved.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. Can be {@code null}.
     * @return
     */
    public static EfsRequest<Booking> buildGetBookingByIdRequest(
            String serviceUrl,
            String id,
            String token
    ) {
        return EfsRequest
                .get(serviceUrl + BOOKINGS_PATH + "/" + id)
                .token(token)
                .expect(Booking.class);
    }

    /**
     * Assembles a request, matching the {@code POST /bookings} endpoint, for
     * the service with the given url using the given token.
     * <p>
     * The returned request can be sent using {@code request.go()} which will
     * return a {@link ResponseEntity}.
     *
     * @param serviceUrl The base url of the mobility service who should be
     * queried. Use {@link MobilityService#getServiceUrl()} to get this url.
     * @param newBooking
     * @param token JSON-serialized credentials object, specific to each
     * mobility service provider.
     * @return
     */
    public static EfsRequest<Booking> buildCreateNewBookingRequest(
            String serviceUrl,
            NewBooking newBooking,
            String token
    ) {
        return EfsRequest
                .post(serviceUrl + BOOKINGS_PATH)
                .token(token)
                .body(newBooking)
                .expect(Booking.class);
    }

    /**
     * Assembles a request, matching the {@code PUT /bookings/{bookingId}}
     * endpoint, for the service with the given url using the given token.
     * <p>
     * The returned request can be sent using {@code request.go()} which will
     * return a {@link ResponseEntity}.
     *
     * @param serviceUrl The base url of the mobility service who should be
     * queried. Use {@link MobilityService#getServiceUrl()} to get this url.
     * @param booking
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. Can be {@code null}.
     * @return
     */
    public static EfsRequest<Booking> buildModifyBookingRequest(
            String serviceUrl,
            Booking booking,
            String token
    ) {
        return EfsRequest
                .put(serviceUrl + BOOKINGS_PATH + "/" + booking.getId())
                .token(token)
                .body(booking)
                .expect(Booking.class);
    }

    /**
     * Assembles a request, matching the
     * {@code POST /bookings/{bookingId}/action/{action}} endpoint, for the
     * service with the given url using the given token.
     * <p>
     * The returned request can be sent using {@code request.go()} which will
     * return a {@link ResponseEntity}.
     *
     * @param serviceUrl The base url of the mobility service who should be
     * queried. Use {@link MobilityService#getServiceUrl()} to get this url.
     * @param bookingId The id of the booking on which to perform the given
     * action.
     * @param action The action that should be performed.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. Can be {@code null}.
     * @return
     */
    public static EfsRequest<Void> buildPerformActionRequest(
            String serviceUrl,
            String bookingId,
            BookingAction action,
            String token
    ) {
        return EfsRequest
                .post(serviceUrl + BOOKINGS_PATH + "/" + bookingId + "/action/" + action.toString())
                .token(token)
                .expect(Void.class);
    }

    /**
     * Assembles a request, matching the
     * {@code POST /bookings/{bookingId}/action/{action}} endpoint, for the
     * service with the given url using the given token.
     * <p>
     * The returned request can be sent using {@code request.go()} which will
     * return a {@link ResponseEntity}.
     *
     * @param serviceUrl The base url of the mobility service who should be
     * queried. Use {@link MobilityService#getServiceUrl()} to get this url.
     * @param bookingId The id of the booking on which to perform the given
     * action.
     * @param action The action that should be performed.
     * @param secret Some provdiders require an additional secret for performing
     * the given action. (e.g. a PIN)
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. Can be {@code null}.
     * @return
     */
    public static EfsRequest<Void> buildPerformActionRequest(
            String serviceUrl,
            String bookingId,
            BookingAction action,
            String secret,
            String token
    ) {
        return buildPerformActionRequest(serviceUrl, bookingId, action, token)
                .query("secret", secret);
    }
}
