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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hsesslingen.keim.efs.middleware.config.swagger.SwaggerAutoConfiguration;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.hsesslingen.keim.efs.middleware.model.Booking;
import de.hsesslingen.keim.efs.middleware.model.BookingAction;
import de.hsesslingen.keim.efs.middleware.model.BookingState;
import de.hsesslingen.keim.efs.middleware.model.Customer;
import de.hsesslingen.keim.efs.middleware.model.NewBooking;
import de.hsesslingen.keim.efs.middleware.model.Options;
import de.hsesslingen.keim.efs.middleware.validation.OnCreate;
import de.hsesslingen.keim.efs.mobility.service.MobilityType;
import de.hsesslingen.keim.efs.mobility.service.Mode;
import io.swagger.annotations.Api;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.hsesslingen.keim.efs.middleware.validation.ConsistentBookingDateParams;
import java.util.Collection;
import static java.util.stream.Collectors.joining;
import org.springframework.beans.factory.annotation.Value;

/**
 * This class receives the consumers requests for searching and booking mobility
 * options and delegates them to the {@link ConsumerService}.
 *
 * @author k.sivarasah 17 Oct 2019
 */
@Validated
@RestController
@Api(tags = {SwaggerAutoConfiguration.CONSUMER_API_TAG})
@ConditionalOnProperty(name = "middleware.consumer.api.enabled", havingValue = "true")
public class ConsumerApi implements IConsumerApi {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerApi.class);

    @Autowired
    public ConsumerService consumerService;

    @Autowired
    private ObjectMapper mapper;

    @Value("${middleware.logging.debug.obfuscate-credentials:true}")
    private boolean obfuscateCredentialsForDebugLogging;

    @Override
    public List<Options> getOptions(
            String from,
            String to,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radius,
            Boolean share,
            Set<MobilityType> mobilityTypes,
            Set<Mode> modes,
            Set<String> serviceIds,
            String credentials
    ) {
        logger.info("Received request to get options from providers.");

        //<editor-fold defaultstate="collapsed" desc="Debug logging input params...">
        logger.debug("Params of this request:\nfrom={}\nfromPlaceId={}\nto={}\ntoPlaceId={}\nstartTime={}\nendTime={}\nradius={}\nshare={}\ncredentials={}",
                from, to, startTime, endTime, radius, share,
                stringifyCollection(mobilityTypes),
                stringifyCollection(modes),
                stringifyCollection(serviceIds),
                obfuscateConditional(credentials)
        );
        //</editor-fold>

        var options = consumerService.getOptions(from, to, startTime, endTime,
                radius, share, mobilityTypes, modes, serviceIds, credentials);

        logger.debug("Responding with {} options.", options.size());

        return options;
    }

    @Override
    public List<Booking> getBookings(Set<String> serviceIds, BookingState state, String credentials) {
        logger.info("Received request to get bookings from providers.");

        //<editor-fold defaultstate="collapsed" desc="Debug logging input params...">
        logger.debug(
                "Params of this request:\nserviceIds={}\nstate={}\ncredentials={}",
                stringifyCollection(serviceIds),
                state,
                obfuscateConditional(credentials)
        );
        //</editor-fold>

        var bookings = consumerService.getBookings(serviceIds, credentials);

        logger.debug("Responding with {} bookings.", bookings.size());

        return bookings;
    }

    @Override
    public Booking getBookingById(@PathVariable String id, @RequestParam String serviceId, String credentials) {
        logger.info("Received request to get a booking by id from a specific provider.");

        //<editor-fold defaultstate="collapsed" desc="Debug logging input params...">
        logger.debug(
                "Params of this request:\nid={}\nserviceId={}\ncredentials={}",
                id,
                serviceId,
                obfuscateConditional(credentials)
        );
        //</editor-fold>

        var booking = consumerService.getBookingById(id, serviceId, credentials);

        if (logger.isTraceEnabled()) {
            logger.trace("Responding with: {}", stringify(booking));
        }

        return booking;
    }

    @Override
    public Booking createNewBooking(
            @RequestBody @Validated({OnCreate.class, IConsumerApi.class}) @Valid @ConsistentBookingDateParams NewBooking newBooking,
            String credentials) {
        logger.info("Received request to create a new booking at a specific provider.");

        //<editor-fold defaultstate="collapsed" desc="Debug logging input params...">
        logger.debug(
                "Params of this request:\ncredentials={}",
                obfuscateConditional(credentials)
        );

        if (logger.isTraceEnabled()) {
            logger.trace("Body of this request:\n{}", stringify(newBooking));
        }
        //</editor-fold>

        var booking = consumerService.createBooking(newBooking, credentials);

        if (logger.isTraceEnabled()) {
            logger.trace("Responding with: {}", stringify(booking));
        }

        return booking;
    }

    @Override
    public Booking modifyBooking(
            @PathVariable String id,
            @RequestBody @Validated(IConsumerApi.class) @Valid @ConsistentBookingDateParams Booking booking,
            String credentials
    ) {
        logger.info("Received request to modify a booking at a specific provider.");

        //<editor-fold defaultstate="collapsed" desc="Debug logging input params...">
        logger.debug(
                "Params of this request:\nid={}\ncredentials={}",
                id,
                obfuscateConditional(credentials)
        );

        if (logger.isTraceEnabled()) {
            logger.trace("Body of this request:\n{}", stringify(booking));
        }
        //</editor-fold>

        var result = consumerService.modifyBooking(id, booking, credentials);

        if (logger.isTraceEnabled()) {
            logger.trace("Responding with: {}", stringify(result));
        }

        return result;
    }

    @Override
    public void performAction(String bookingId, BookingAction action, String serviceId, String assetId, String secret, String more, String credentials) {
        logger.info("Received request to perform an action on a booking");

        //<editor-fold defaultstate="collapsed" desc="Debug logging input params...">
        logger.debug(
                "Params of this request:\bookingId={}\naction={}\nserviceId={}\nassetId={}\nsecret={}\ncredentials={}\nThe value of param \"more\" will be put to the next line:\n{}",
                bookingId, action, serviceId, assetId,
                obfuscateConditional(secret),
                obfuscateConditional(credentials),
                more
        );
        //</editor-fold>

        consumerService.performAction(bookingId, action, serviceId, assetId, secret, more, credentials);
    }

    @Override
    public String createLoginToken(String serviceId, String credentials) {
        logger.info("Received request to create a login token at a specific provider.");

        //<editor-fold defaultstate="collapsed" desc="Debug logging input params...">
        logger.debug(
                "Params of this request:\nserviceId={}\ncredentials={}",
                serviceId, obfuscateConditional(credentials)
        );
        //</editor-fold>

        var token = consumerService.createLoginToken(serviceId, credentials);

        logger.debug("Responding with: {}", obfuscateConditional(token));

        return token;
    }

    @Override
    public String registerUser(String serviceId, String credentials, Customer userData) {
        logger.info("Received request to register a user at a specific provider.");

        //<editor-fold defaultstate="collapsed" desc="Debug logging input params...">
        logger.debug(
                "Params of this request:\nserviceId={}\ncredentials={}\nuserData={}\nuserData.id={}\nuserData.firstName={}\nuserData.lastName={}\nuserData.email={}\nuserData.phone={}",
                serviceId,
                obfuscateConditional(credentials),
                userData != null ? "(non-null value)" : "null",
                userData != null ? obfuscateConditional(userData.getId()) : "(userData is null)",
                userData != null ? obfuscateConditional(userData.getFirstName()) : "(userData is null)",
                userData != null ? obfuscateConditional(userData.getLastName()) : "(userData is null)",
                userData != null ? obfuscateConditional(userData.getEmail()) : "(userData is null)",
                userData != null ? obfuscateConditional(userData.getPhone()) : "(userData is null)"
        );
        //</editor-fold>

        var newUser = consumerService.registerUser(serviceId, credentials, userData);

        logger.debug("Responding with: {}", obfuscateConditional(newUser));

        return newUser;
    }

    @Override
    public Boolean deleteLoginToken(String serviceId, String credentials) {
        logger.info("Received request to delete a token at a specific provider.");

        //<editor-fold defaultstate="collapsed" desc="Debug logging input params...">
        logger.debug(
                "Params of this request:\nserviceId={}\ncredentials={}",
                serviceId, obfuscateConditional(credentials)
        );
        //</editor-fold>

        var wasSuccessful = consumerService.deleteLoginToken(serviceId, credentials);

        logger.debug("Responding with: {}", wasSuccessful);

        return wasSuccessful;
    }

    @Override
    public Boolean checkCredentialsAreValid(String serviceId, String credentials) {
        logger.info("Received request to check the validity of credentials at a specific provider.");

        //<editor-fold defaultstate="collapsed" desc="Debug logging input params...">
        logger.debug(
                "Params of this request:\nserviceId={}\ncredentials={}",
                serviceId, obfuscateConditional(credentials)
        );
        //</editor-fold>

        var areValid = consumerService.checkCredentialsAreValid(serviceId, credentials);

        logger.debug("Responding with: {}", areValid);

        return areValid;
    }

    /**
     * Serializes a collection of objects by calling {@code toString()} on each
     * one and joining their results with commas.
     *
     * @param collection
     * @return
     */
    private String stringifyCollection(Collection<?> collection) {
        return collection != null ? collection.stream().map(t -> t.toString()).collect(joining(",")) : "null";
    }

    /**
     * If the config property "middleware.logging.debug.obfuscate-credentials"
     * is set to true (which is default), this method obfuscates the given
     * input. Otherwise {@code any.toString()} will be returned.
     *
     * @param any
     * @return
     */
    public String obfuscateConditional(Object any) {
        if (this.obfuscateCredentialsForDebugLogging) {
            return obfuscate(any);
        }

        return any.toString();
    }

    /**
     * Can be used to obfuscate the given string value. In contrast to the
     * instance method {@link conditionalObfuscate}, this method always
     * obfuscates the input.
     * <p>
     * <ul>
     * <li>{@code null} is rendered to {@code "null"}</li>
     * <li>Empty string is rendered to {@code "\"\""}</li>
     * <li>Everything else is rendered to {@code "***"}</li>
     * </ul>
     *
     * @param any
     * @return
     */
    public String obfuscate(Object any) {
        if (any == null) {
            return "null";
        }

        if (any.toString().isEmpty()) {
            return "\"\"";
        }

        return "***";
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
