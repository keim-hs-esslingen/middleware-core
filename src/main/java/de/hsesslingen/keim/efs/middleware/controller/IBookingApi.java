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
package de.hsesslingen.keim.efs.middleware.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.hsesslingen.keim.efs.middleware.booking.Booking;
import de.hsesslingen.keim.efs.middleware.booking.BookingState;
import de.hsesslingen.keim.efs.middleware.common.Options;
import de.hsesslingen.keim.efs.middleware.config.ApiConstants;
import de.hsesslingen.keim.efs.middleware.config.swagger.EfsSwaggerGetBookingOptions;
import de.hsesslingen.keim.efs.middleware.validation.PositionAsString;
import de.hsesslingen.keim.efs.middleware.validation.TimeIsInFuture;
import de.hsesslingen.keim.efs.mobility.config.EfsSwaggerApiResponseSupport;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * @author k.sivarasah 17 Oct 2019
 */
@EfsSwaggerApiResponseSupport
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public interface IBookingApi extends IBilateralBookingApi {

    /**
     * Returns available transport options for given coordinate.Start time can
     * be defined, but is optional.If startTime is not provided, but required by
     * the third party API, a default value of "Date.now()" is used.
     *
     * @param from User's location in comma separated form e.g. 60.123,27.456.
     * @param radius Maximum distance a user wants to travel to reach asset in
     * metres, e.g. 500 metres.
     * @param to A desired destination e.g. 60.123,27.456.
     * @param startTime Starttime in ms since epoch
     * @param endTime Endtime in ms since epoch
     * @param share Defines if user can also share a ride. (Available values :
     * YES, NO)
     * @param credentials Credential data as json content string
     * @return List of {@link Options}
     */
    @GetMapping(value = "/bookings/options")
    @ResponseStatus(HttpStatus.OK)
    @EfsSwaggerGetBookingOptions
    public List<Options> getBookingOptions(
            @RequestParam(required = true) @PositionAsString String from,
            @RequestParam(required = false) @PositionAsString String to,
            @RequestParam(required = false) @TimeIsInFuture Long startTime,
            @RequestParam(required = false) @TimeIsInFuture Long endTime,
            @RequestParam(required = false) Integer radius,
            @RequestParam(required = false) Boolean share,
            @RequestHeader(name = ApiConstants.CREDENTIALS_HEADER_NAME, required = false) @ApiParam(ApiConstants.CREDENTIALS_DESC) String credentials
    );

    /**
     * Returns a list of bookings.
     *
     * @param state The state for which to filter the bookings.
     * @param credentials Credential data as json content string
     * @return List of {@link Booking}
     */
    @GetMapping(value = "/bookings")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get Bookings", notes = "Returns a list of Booking optionally filtered by their state.")
    public List<Booking> getBookings(
            @RequestParam(required = false) BookingState state,
            @RequestHeader(name = ApiConstants.CREDENTIALS_HEADER_NAME, required = false) @ApiParam(ApiConstants.CREDENTIALS_DESC) String credentials
    );

    /**
     * Gets a {@link Booking} using the booking id
     *
     *
     * @param id the booking id
     * @param credentials Credential data as json content string
     * @return the {@link Booking} object
     */
    @GetMapping(value = "/bookings/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Get Booking by Id", notes = "Returns the Booking with the given unique booking id")
    public Booking getBookingById(@PathVariable String id,
            @RequestHeader(name = ApiConstants.CREDENTIALS_HEADER_NAME, required = false) @ApiParam(ApiConstants.CREDENTIALS_DESC) String credentials);

}
