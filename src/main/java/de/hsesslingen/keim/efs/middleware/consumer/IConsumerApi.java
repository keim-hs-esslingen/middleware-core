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

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.hsesslingen.keim.efs.middleware.model.Booking;
import de.hsesslingen.keim.efs.middleware.model.BookingState;
import de.hsesslingen.keim.efs.middleware.model.Customer;
import de.hsesslingen.keim.efs.middleware.model.Options;
import de.hsesslingen.keim.efs.middleware.config.swagger.EfsSwaggerGetBookingOptions;
import static de.hsesslingen.keim.efs.middleware.config.swagger.SwaggerAutoConfiguration.FLEX_DATETIME_DESC;
import de.hsesslingen.keim.efs.middleware.model.BookingAction;
import de.hsesslingen.keim.efs.middleware.model.NewBooking;
import de.hsesslingen.keim.efs.middleware.validation.OnCreate;
import de.hsesslingen.keim.efs.middleware.validation.PositionAsString;
import de.hsesslingen.keim.efs.mobility.config.EfsSwaggerApiResponseSupport;
import de.hsesslingen.keim.efs.mobility.service.MobilityType;
import de.hsesslingen.keim.efs.mobility.service.Mode;
import static de.hsesslingen.keim.efs.mobility.utils.EfsRequest.CREDENTIALS_HEADER_DESC;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import static de.hsesslingen.keim.efs.mobility.utils.EfsRequest.CREDENTIALS_HEADER_NAME;
import java.time.ZonedDateTime;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import de.hsesslingen.keim.efs.middleware.validation.ConsistentBookingDateParams;

/**
 * This interface contains the method declarations, including all swagger-ui
 * annotations for the {@link ConsumerApi}
 *
 * @author k.sivarasah 17 Oct 2019
 */
@EfsSwaggerApiResponseSupport
@RequestMapping(value = "/consumer/api", produces = MediaType.APPLICATION_JSON_VALUE)
public interface IConsumerApi {

    /**
     * Returns available transport options for given coordinate. Start time can
     * be defined, but is optional. If startTime is not provided, but required
     * by the third party API, a default value of "Date.now()" is used.
     *
     * @param from User's location in comma separated form e.g. 60.123,27.456
     * @param to Desired destination location in comma separated form e.g.
     * 60.123,27.456
     * @param startTime Starttime in ms since epoch
     * @param endTime Endtime in ms since epoch
     * @param radius Maximum distance a user wants to travel to reach asset in
     * metres, e.g. 500 metres
     * @param share Defines if user can also share a ride.
     * @param mobilityTypes Desired {@link MobilityType}s
     * @param modes Desired {@link Mode}s
     * @param serviceIds Ids of preferred services to filter by
     * @param credentials Credential data as json content string
     * @return List of {@link Options}
     */
    @GetMapping({"/bookings/options", "/options"})
    @ResponseStatus(HttpStatus.OK)
    @EfsSwaggerGetBookingOptions
    public List<Options> getOptions(
            @RequestParam @PositionAsString String from,
            @RequestParam(required = false) @PositionAsString String to,
            @RequestParam(required = false) @ApiParam(FLEX_DATETIME_DESC) ZonedDateTime startTime,
            @RequestParam(required = false) @ApiParam(FLEX_DATETIME_DESC) ZonedDateTime endTime,
            @RequestParam(required = false) @ApiParam("Unit: meter") Integer radius,
            @RequestParam(required = false) Boolean share,
            @RequestParam(required = false, defaultValue = "") @ApiParam("Desired mobility types") Set<MobilityType> mobilityTypes,
            @RequestParam(required = false, defaultValue = "") @ApiParam("Desired modes") Set<Mode> modes,
            @RequestParam(required = false, defaultValue = "") @ApiParam("Ids of preferred services to filter by") Set<String> serviceIds,
            @RequestHeader(name = CREDENTIALS_HEADER_NAME, required = false) @ApiParam(CREDENTIALS_HEADER_DESC) String credentials
    );

    /**
     * Gets a {@link Booking} from the service using its id
     *
     * @param id the booking id
     * @param serviceId the service id
     * @param credentials Credential data as json content string
     * @return the {@link Booking} object
     */
    @GetMapping("/bookings/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get Booking by Id", notes = "Returns the Booking with the given unique booking id")
    public Booking getBookingById(
            @PathVariable String id,
            @RequestParam String serviceId,
            @RequestHeader(name = CREDENTIALS_HEADER_NAME, required = false) @ApiParam(CREDENTIALS_HEADER_DESC) String credentials
    );

    /**
     * Gets a {@link List<Booking>} from the service using its id
     *
     * @param serviceIds List of the services from which to get the bookings.
     * @param state Optionally a state for which to filter the bookings.
     * @param credentials Credential data as json content string
     * @return the {@link List<Booking>} object
     */
    @GetMapping("/bookings")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get bookings from agencies.")
    public List<Booking> getBookings(
            @RequestParam(defaultValue = "") @ApiParam("List of the service providers from which to get the bookings.") Set<String> serviceIds,
            @RequestParam(required = false) @ApiParam("Optionally a state for which to filter the bookings.") BookingState state,
            @RequestHeader(name = CREDENTIALS_HEADER_NAME, required = false) @ApiParam(CREDENTIALS_HEADER_DESC) String credentials
    );

    /**
     * Creates a new booking and returns it.
     *
     * @param newBooking {@link NewBooking} that should be created
     * @param credentials Credential data as json content string
     * @return {@link Booking} that was created
     */
    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a new Booking", notes = "Creates a new Booking for a service-provider "
            + "in BOOKED or STARTED state using the provided NewBooking object and returns it")
    public Booking createNewBooking(
            @RequestBody @Validated({OnCreate.class, ConsumerApi.class}) @Valid @ConsistentBookingDateParams NewBooking newBooking,
            @RequestHeader(name = CREDENTIALS_HEADER_NAME, required = false) @ApiParam(CREDENTIALS_HEADER_DESC) String credentials
    );

    /**
     * Updates an existing {@link Booking} with new details.
     *
     * @param id the booking id
     * @param booking the {@link Booking} object containing modified data
     * @param credentials Credential data as json content string
     * @return the modified {@link Booking} object
     */
    @PutMapping("/bookings/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ApiOperation(value = "Modify a Booking", notes = "Updates an existing Booking with the provided details")
    public Booking modifyBooking(
            @PathVariable String id,
            @RequestBody @Validated(ConsumerApi.class) @Valid @ConsistentBookingDateParams Booking booking,
            @RequestHeader(name = CREDENTIALS_HEADER_NAME, required = false) @ApiParam(CREDENTIALS_HEADER_DESC) String credentials
    );

    /**
     * Can be used to perform actions on bookings. This can be used to e.g.
     * unlock the door of rented vehicles, or stamp tickets...
     *
     * @param bookingId The ID of the booking on which to perform the action.
     * @param action The action that should be performed on the booking with the
     * given bookingId.
     * @param serviceId The ID of the service to which this booking belongs to.
     * This is only required if calling the consumer API of a middleware
     * instance.
     * @param assetId The ID of the asset on which to perform this action. If
     * none specified, the service can choose how to handle this situation.
     * @param secret A secret that might be required by some services to perform
     * this action. (e.g. a PIN)
     * @param more Additional information that might be required by some
     * services in order to perform this action.
     * @param credentials The credentials needed to authorize oneself to perform
     * this action.
     */
    @PostMapping("/bookings/{bookingId}/action/{action}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Perform an action on a booking", notes = "Performs the given action on a booking.")
    public void performAction(
            @PathVariable String bookingId,
            @PathVariable BookingAction action,
            @RequestParam String serviceId,
            @RequestParam(required = false) String assetId,
            @RequestParam(required = false) String secret,
            @RequestBody(required = false) String more,
            @RequestHeader(name = CREDENTIALS_HEADER_NAME, required = false) @ApiParam(CREDENTIALS_HEADER_DESC) String credentials
    );

    /**
     * Allows to create a login token associated to the provided
     * credentials.This might be necessary for some providers, but not for all.
     *
     * @param serviceId The service at which to create a login-token.
     * @param credentials Credential data as json content string
     * @return Login token data in a provider specific format.
     */
    @PostMapping("/credentials/login-token")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Create login token", notes = "Allows to create a login-token associated to the provided credentials. This might be necessary for some providers, but not for all.")
    public String createLoginToken(
            @RequestParam @ApiParam("The service of which to get a login token.") String serviceId,
            @RequestHeader(name = CREDENTIALS_HEADER_NAME, required = true) @ApiParam(CREDENTIALS_HEADER_DESC) String credentials
    );

    /**
     * Logs out (invalidates) the given login-token.
     *
     * @param serviceId The service at which to invalidate the given
     * login-token.
     * @param credentials Provider specific credentials.
     * @return true if the logout was successful. false if some error occured.
     */
    @DeleteMapping("/credentials/login-token")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Log out user", notes = "Logs out (invalidates) the given login-token.")
    public Boolean deleteLoginToken(
            @RequestParam @ApiParam("The service of which to get a login token.") String serviceId,
            @RequestHeader(name = CREDENTIALS_HEADER_NAME, required = true) @ApiParam(CREDENTIALS_HEADER_DESC) String credentials
    );

    /**
     * Registers a new user at this service.
     *
     * @param serviceId The service at which to register the given user.
     * @param credentials Credential data as json content string
     * @param userData Data about the user that might be required by some
     * providers.
     * @return Credentials data in a provider specific format.
     */
    @PostMapping("/credentials/user")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Register a new user", notes = "Registers a new user at this service.")
    public String registerUser(
            @RequestParam @ApiParam("The service of which to get a login token.") String serviceId,
            @RequestHeader(name = CREDENTIALS_HEADER_NAME, required = true) @ApiParam(CREDENTIALS_HEADER_DESC) String credentials,
            @RequestBody(required = false) @ApiParam("Possibly required extra data about the new user.") Customer userData
    );

    /**
     * Checks whether the given credentials are still valid and can be used for
     * booking purposes etc.
     *
     * @param serviceId The service at which to check the credentials validity.
     * @param credentials Provider specific credentials.
     * @return true if valid, false if not.
     */
    @GetMapping("/credentials/check")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Check validity of credentials.", notes = "Checks whether the given credentials are still valid and can be used for booking purposes etc.")
    public Boolean checkCredentialsAreValid(
            @RequestParam @ApiParam("The service of which to get a login token.") String serviceId,
            @RequestHeader(name = CREDENTIALS_HEADER_NAME, required = true) @ApiParam(CREDENTIALS_HEADER_DESC) String credentials
    );
}
