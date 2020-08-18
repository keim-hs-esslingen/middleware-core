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

import de.hsesslingen.keim.efs.middleware.model.Booking;
import de.hsesslingen.keim.efs.middleware.model.BookingAction;
import de.hsesslingen.keim.efs.middleware.model.NewBooking;
import de.hsesslingen.keim.efs.middleware.config.ApiConstants;
import de.hsesslingen.keim.efs.middleware.validation.ConsistentBookingDateParameters;
import de.hsesslingen.keim.efs.middleware.validation.OnCreate;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * An interface describing booking endpoints that are part of bilateral
 * communication. This interface is extended by interfaces used both on consumer
 * and provider side.
 *
 * @author ben
 */
public interface IBilateralBookingApi {

    /**
     * Creates a new booking and returns it.
     *
     * @param newBooking {@link NewBooking} that should be created
     * @param credentials Credential data as json content string
     * @return {@link Booking} that was created
     */
    @PostMapping(value = "/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a new Booking", notes = "Creates a new Booking for a service-provider "
            + "in BOOKED or STARTED state using the provided NewBooking object and returns it")
    public Booking createNewBooking(
            @RequestBody @Validated(OnCreate.class) @Valid @ConsistentBookingDateParameters NewBooking newBooking,
            @RequestHeader(name = ApiConstants.CREDENTIALS_HEADER_NAME, required = false) @ApiParam(ApiConstants.CREDENTIALS_DESC) String credentials);

    /**
     * Updates an existing {@link Booking} with new details.
     *
     * @param id the booking id
     * @param booking the {@link Booking} object containing modified data
     * @param credentials Credential data as json content string
     * @return the modified {@link Booking} object
     */
    @PutMapping(value = "/bookings/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ApiOperation(value = "Modify a Booking", notes = "Updates an existing Booking with the provided details")
    public Booking modifyBooking(
            @PathVariable String id,
            @RequestBody @Valid @ConsistentBookingDateParameters Booking booking,
            @RequestHeader(name = ApiConstants.CREDENTIALS_HEADER_NAME, required = false) @ApiParam(ApiConstants.CREDENTIALS_DESC) String credentials);

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
    @PostMapping(value = "/bookings/{bookingId}/action/{action}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Perform an action on a booking", notes = "Performs the given action on a booking.")
    public void performAction(
            @PathVariable String bookingId,
            @PathVariable BookingAction action,
            @RequestParam(required = false) String serviceId,
            @RequestParam(required = false) String assetId,
            @RequestParam(required = false) String secret,
            @RequestBody(required = false) String more,
            @RequestHeader(name = ApiConstants.CREDENTIALS_HEADER_NAME, required = false) @ApiParam(ApiConstants.CREDENTIALS_DESC) String credentials
    );

}
