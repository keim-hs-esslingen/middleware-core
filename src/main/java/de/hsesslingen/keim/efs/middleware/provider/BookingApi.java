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
import de.hsesslingen.keim.efs.middleware.apis.security.ICredentialsFactory;
import java.time.Instant;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.hsesslingen.keim.efs.middleware.apis.IBookingService;
import de.hsesslingen.keim.efs.middleware.apis.security.AbstractCredentials;
import de.hsesslingen.keim.efs.middleware.model.Booking;
import de.hsesslingen.keim.efs.middleware.model.BookingAction;
import de.hsesslingen.keim.efs.middleware.model.BookingState;
import de.hsesslingen.keim.efs.middleware.model.NewBooking;
import de.hsesslingen.keim.efs.middleware.model.Options;
import de.hsesslingen.keim.efs.middleware.model.Place;
import de.hsesslingen.keim.efs.middleware.validation.ConsistentBookingDateParameters;
import de.hsesslingen.keim.efs.middleware.validation.OnCreate;
import io.swagger.annotations.Api;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author boesch, K.Sivarasah
 */
@Validated
@RestController
@ConditionalOnBean(IBookingService.class)
@Api(tags = {"Booking Api"})
public class BookingApi implements IBookingApi {

    private static final Log log = LogFactory.getLog(BookingApi.class);

    @Autowired
    private IBookingService bookingService;

    @Autowired
    private ICredentialsFactory credentialsFactory;

    @Value("${efs.middleware.debug-credentials:false}")
    private boolean debugCredentials;
    @Value("${efs.middleware.debug-input-objects:false}")
    private boolean debugInputObjects;

    @Override
    public List<Options> getBookingOptions(
            String from, String to,
            Long startTime, Long endTime,
            ZonedDateTime startTimeIso, ZonedDateTime endTimeIso,
            Integer radius, Boolean share,
            String credentials) {
        log.info("Received request to get options.");

        Place placeTo = StringUtils.isEmpty(to) ? null : new Place(to);

        // Choose option for start time: 1. startTime, 2. startTimeIso, 3. Instant.now()
        var startTimeInstant = (startTime != null) ? Instant.ofEpochMilli(startTime)
                : (startTimeIso != null) ? startTimeIso.toInstant()
                        : Instant.now();

        // Choose option for end time: 1. endTime, 2. endTimeIso, 3. null
        var endTimeInstant = (endTime != null) ? Instant.ofEpochMilli(endTime)
                : (endTimeIso != null) ? endTimeIso.toInstant()
                        : null;

        var creds = credentialsFactory.fromString(credentials);
        debugOutputCredentials(creds);

        return bookingService.getBookingOptions(new Place(from), placeTo, startTimeInstant, endTimeInstant, radius, share, creds);
    }

    @Override
    public List<Booking> getBookings(BookingState state, String credentials) {
        log.info("Received request to get bookings.");

        var creds = credentialsFactory.fromString(credentials);
        debugOutputCredentials(creds);

        return bookingService.getBookings(state, creds);
    }

    @Override
    public Booking getBookingById(@PathVariable String id, String credentials) {
        if (!debugInputObjects) {
            log.info("Received request to get a booking by id.");
        } else {
            log.info("Received request to get booking with id \"" + id + "\".");
        }

        var creds = credentialsFactory.fromString(credentials);
        debugOutputCredentials(creds);

        return bookingService.getBookingById(id, creds);
    }

    @Override
    public Booking createNewBooking(@RequestBody @Validated(OnCreate.class) @Valid @ConsistentBookingDateParameters NewBooking newBooking,
            String credentials) {
        log.info("Received request to create a new booking.");

        if (debugInputObjects) {
            debugLogAsJson(newBooking);
        }

        if (newBooking.getState() != BookingState.NEW) {
            log.warn("Received a NewBooking with booking state set to \"" + newBooking.getState() + "\". Setting it manually to \"NEW\".");
            newBooking.setState(BookingState.NEW);
        }

        var creds = credentialsFactory.fromString(credentials);
        debugOutputCredentials(creds);

        return bookingService.createNewBooking(newBooking, creds);
    }

    @Override
    public Booking modifyBooking(@PathVariable String id,
            @RequestBody @Valid @ConsistentBookingDateParameters Booking booking,
            String credentials) {

        if (!debugInputObjects) {
            log.info("Received request to modify a booking.");
        } else {
            log.info("Received request to modify booking with id \"" + id + "\".");
            debugLogAsJson(booking);
        }

        var creds = credentialsFactory.fromString(credentials);
        debugOutputCredentials(creds);

        return bookingService.modifyBooking(id, booking, creds);
    }

    @Override
    public void performAction(String bookingId, BookingAction action, String serviceId, String assetId, String secret, String more, String credentials) {

        if (!debugInputObjects) {
            log.info("Received request to perform an action on a booking.");
        } else {
            log.info(String.format("Received request to perform action %s on booking %s. (assetId=%s, (obfuscated) secret=%s)", action, bookingId, assetId, obfuscate(secret)));
            log.debug(more);
        }

        var creds = credentialsFactory.fromString(credentials);
        debugOutputCredentials(creds);

        bookingService.performAction(bookingId, action, assetId, secret, more, creds);
    }

    /**
     * This method outputs the (obfuscated) credentials sent with a request. It
     * is intended to be used for debugging purposes.
     * <p>
     * To activate this function, set {@code efs.middleware.debug-credentials}
     * (default=false) in application properties to true. Additionally the log
     * level must be set to {@code DEBUG}, otherwise the output will not be
     * logged.
     *
     * @param creds
     */
    private void debugOutputCredentials(AbstractCredentials creds) {
        if (debugCredentials) {
            if (creds == null) {
                log.debug("Credentials are null.");
                return;
            }

            StringBuilder sb = new StringBuilder();

            sb.append("Parsed credentials with following values: ");

            var cl = creds.getClass();
            var fields = cl.getDeclaredFields();

            int count = 0;
            for (Field field : fields) {
                field.setAccessible(true);

                sb.append(field.getName());

                try {
                    sb.append("=").append(obfuscate(field.get(creds)));
                } catch (IllegalAccessException ex) {
                    sb.append("->IllegalAccessException");
                } catch (IllegalArgumentException ex) {
                    sb.append("->IllegalArgumentException");
                }

                sb.append(", ");

                field.setAccessible(false);

                ++count;
            }

            String output;

            if (count == 0) {
                sb.append("(no values parsed)");
                output = sb.toString();
            } else {
                output = sb.toString();

                if (output.endsWith(", ")) {
                    output = output.substring(0, output.lastIndexOf(","));
                }
            }

            log.debug(output);
        }
    }

    private String obfuscate(Object any) {
        if (any == null) {
            return "null";
        }

        if (any.toString().isEmpty()) {
            return "\"\"";
        }

        return "***";
    }

    /**
     * Used in {@link debugLogAsJson}, lazily loaded.
     */
    private ObjectMapper debugMapper;

    /**
     * Log objects on debug level for debugging purposes.
     *
     * @param o
     */
    private void debugLogAsJson(Object o) {
        if (debugMapper == null) {
            debugMapper = new ObjectMapper();
        }

        try {
            log.debug(debugMapper.writeValueAsString(o));
        } catch (JsonProcessingException ex) {
            log.debug("Could not serialize object for debug logging. Exception occured.");
            log.debug(ex);
        }
    }
}
