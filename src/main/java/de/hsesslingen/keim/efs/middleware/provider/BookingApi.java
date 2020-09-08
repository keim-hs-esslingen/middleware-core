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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hsesslingen.keim.efs.middleware.config.swagger.SwaggerAutoConfiguration;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.hsesslingen.keim.efs.middleware.model.Booking;
import de.hsesslingen.keim.efs.middleware.model.BookingAction;
import de.hsesslingen.keim.efs.middleware.model.BookingState;
import de.hsesslingen.keim.efs.middleware.model.NewBooking;
import de.hsesslingen.keim.efs.middleware.model.Options;
import de.hsesslingen.keim.efs.middleware.model.Place;
import de.hsesslingen.keim.efs.middleware.provider.credentials.CredentialsUtils;
import de.hsesslingen.keim.efs.middleware.validation.OnCreate;
import io.swagger.annotations.Api;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import de.hsesslingen.keim.efs.middleware.validation.ConsistentBookingDateParams;

/**
 * @author boesch, K.Sivarasah
 */
@Validated
@RestController
@ConditionalOnBean({IBookingService.class, IOptionsService.class})
@Api(tags = {SwaggerAutoConfiguration.BOOKING_API_TAG})
public class BookingApi implements IBookingApi {

    private static final Logger logger = LoggerFactory.getLogger(BookingApi.class);

    @Autowired
    private IBookingService bookingService;

    @Autowired
    private IOptionsService optionsService;

    @Autowired
    private CredentialsUtils credentialsUtils;

    @Autowired
    private ObjectMapper mapper;

    @Value("${efs.middleware.debug-input-objects:false}")
    private boolean debugInputObjects;

    @Override
    public List<Options> getBookingOptions(
            String from, String to,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radius,
            Boolean share,
            String credentials
    ) {
        logger.info("Received request to get options.");

        Place placeTo = StringUtils.isEmpty(to) ? null : new Place(to);

        var creds = credentialsUtils.fromString(credentials);

        return optionsService.getOptions(new Place(from), placeTo, startTime, endTime, radius, share, creds);
    }

    @Override
    public List<Booking> getBookings(BookingState state, String credentials) {
        logger.info("Received request to get bookings.");

        var creds = credentialsUtils.fromString(credentials);

        return bookingService.getBookings(state, creds);
    }

    @Override
    public Booking getBookingById(@PathVariable String id, String credentials) {
        if (!debugInputObjects) {
            logger.info("Received request to get a booking by id.");
        } else {
            logger.info("Received request to get booking with id \"" + id + "\".");
        }

        var creds = credentialsUtils.fromString(credentials);

        return bookingService.getBookingById(id, creds);
    }

    @Override
    public Booking createNewBooking(@RequestBody @Validated(OnCreate.class) @Valid @ConsistentBookingDateParams NewBooking newBooking,
            String credentials) {
        logger.info("Received request to create a new booking.");

        if (debugInputObjects) {
            debugLogAsJson(newBooking);
        }

        var creds = credentialsUtils.fromString(credentials);

        return bookingService.createNewBooking(newBooking, creds);
    }

    @Override
    public Booking modifyBooking(@PathVariable String id,
            @RequestBody @Valid @ConsistentBookingDateParams Booking booking,
            String credentials) {

        if (!debugInputObjects) {
            logger.info("Received request to modify a booking.");
        } else {
            logger.info("Received request to modify booking with id \"" + id + "\".");
            debugLogAsJson(booking);
        }

        var creds = credentialsUtils.fromString(credentials);

        return bookingService.modifyBooking(id, booking, creds);
    }

    @Override
    public void performAction(String bookingId, BookingAction action, String assetId, String secret, String more, String credentials) {

        if (!debugInputObjects) {
            logger.info("Received request to perform an action on a booking.");
        } else {
            logger.info(String.format("Received request to perform action %s on booking %s. (assetId=%s, (obfuscated) secret=%s)", action, bookingId, assetId, CredentialsUtils.obfuscate(secret)));
            logger.debug(more);
        }

        var creds = credentialsUtils.fromString(credentials);

        bookingService.performAction(bookingId, action, assetId, secret, more, creds);
    }

    /**
     * Log objects on debug level for debugging purposes.
     *
     * @param o
     */
    private void debugLogAsJson(Object o) {
        try {
            logger.debug(mapper.writeValueAsString(o));
        } catch (JsonProcessingException ex) {
            logger.debug("Could not serialize object for debug logging. Exception occured.", ex);
        }
    }
}
