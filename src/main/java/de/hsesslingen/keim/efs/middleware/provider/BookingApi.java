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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.hsesslingen.keim.efs.middleware.model.Booking;
import de.hsesslingen.keim.efs.middleware.model.BookingAction;
import de.hsesslingen.keim.efs.middleware.model.BookingState;
import de.hsesslingen.keim.efs.middleware.model.NewBooking;
import de.hsesslingen.keim.efs.middleware.provider.credentials.CredentialsUtils;
import de.hsesslingen.keim.efs.middleware.validation.OnCreate;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.hsesslingen.keim.efs.middleware.validation.ConsistentBookingDateParams;

/**
 * @author boesch, K.Sivarasah
 */
@Validated
@RestController
@ConditionalOnBean({IBookingService.class})
@Api(tags = {SwaggerAutoConfiguration.BOOKING_API_TAG})
public class BookingApi implements IBookingApi {

    private static final Logger logger = LoggerFactory.getLogger(BookingApi.class);

    @Autowired
    private IBookingService bookingService;

    @Autowired
    private CredentialsUtils credentialsUtils;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public List<Booking> getBookings(BookingState state, String credentials) {
        logger.info("Received request to get bookings.");

        //<editor-fold defaultstate="collapsed" desc="Debug logging input params...">
        logger.debug(
                "Params of this request:\nstate={}\ncredentials={}",
                state, credentialsUtils.obfuscateConditional(credentials)
        );
        //</editor-fold>

        var bookings = bookingService.getBookings(state, credentialsUtils.fromString(credentials));

        logger.debug("Responding with a list of {} bookings.", bookings.size());

        return bookings;
    }

    @Override
    public Booking getBookingById(@PathVariable String id, String credentials) {
        logger.info("Received request to get a booking by id.");

        //<editor-fold defaultstate="collapsed" desc="Debug logging input params...">
        logger.debug(
                "Params of this request:\nid={}\ncredentials={}",
                id, credentialsUtils.obfuscateConditional(credentials)
        );
        //</editor-fold>

        var result = bookingService.getBookingById(id, credentialsUtils.fromString(credentials));

        if (logger.isTraceEnabled()) {
            logger.trace("Responding with: {}", stringify(result));
        }

        return result;
    }

    @Override
    public Booking createNewBooking(
            @RequestBody @Validated(OnCreate.class) @Valid @ConsistentBookingDateParams NewBooking newBooking,
            String credentials
    ) {
        logger.info("Received request to create a new booking.");

        //<editor-fold defaultstate="collapsed" desc="Debug logging input params...">
        logger.debug(
                "Params of this request:\ncredentials={}",
                credentialsUtils.obfuscateConditional(credentials)
        );

        if (logger.isTraceEnabled()) {
            logger.trace("Body of this request:\n{}", stringify(newBooking));
        }
        //</editor-fold>

        var creds = credentialsUtils.fromString(credentials);

        var result = bookingService.createNewBooking(newBooking, creds);

        if (logger.isTraceEnabled()) {
            logger.trace("Responding with: {}", stringify(result));
        }

        return result;
    }

    @Override
    public Booking modifyBooking(
            @PathVariable String id,
            @RequestBody
            @Valid
            @ConsistentBookingDateParams Booking booking,
            String credentials
    ) {
        logger.info("Received request to modify a booking.");

        //<editor-fold defaultstate="collapsed" desc="Debug logging input params...">
        logger.debug(
                "Params of this request:\nid={}\ncredentials={}",
                id, credentialsUtils.obfuscateConditional(credentials)
        );

        if (logger.isTraceEnabled()) {
            logger.trace("Body of this request:\n{}", stringify(booking));
        }
        //</editor-fold>

        var creds = credentialsUtils.fromString(credentials);

        var result = bookingService.modifyBooking(id, booking, creds);

        if (logger.isTraceEnabled()) {
            logger.trace("Responding with: {}", stringify(result));
        }

        return result;
    }

    @Override
    public void performAction(
            String bookingId,
            BookingAction action,
            String assetId,
            String secret,
            String more,
            String credentials
    ) {
        logger.info("Received request to perform an action on a booking.");

        //<editor-fold defaultstate="collapsed" desc="Debug logging input params...">
        logger.debug(
                "Params of this request:\nbookingId={}\naction={}\nassetId={}\nsecret={}\ncredentials={}\nThe value of field \"more\" is logged in the next line:\n{}",
                action, bookingId, assetId,
                credentialsUtils.obfuscateConditional(secret),
                credentialsUtils.obfuscateConditional(credentials),
                more
        );
        //</editor-fold>

        bookingService.performAction(
                bookingId, action, assetId, secret, more,
                credentialsUtils.fromString(credentials)
        );
    }

    /**
     * Stringifies the given object. If serialization fails, a message string is
     * returned. This method is inteded to be used for logging.
     *
     * @param o
     * @return
     */
    private String stringify(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException ex) {
            return "Could not serialize object for logging. Exception occured.";
        }
    }
}
