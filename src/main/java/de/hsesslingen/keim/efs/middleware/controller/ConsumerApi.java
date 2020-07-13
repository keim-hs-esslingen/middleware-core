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

import java.time.Instant;
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

import de.hsesslingen.keim.efs.middleware.booking.Booking;
import de.hsesslingen.keim.efs.middleware.booking.BookingAction;
import de.hsesslingen.keim.efs.middleware.booking.BookingState;
import de.hsesslingen.keim.efs.middleware.booking.Customer;
import de.hsesslingen.keim.efs.middleware.booking.NewBooking;
import de.hsesslingen.keim.efs.middleware.common.Options;
import de.hsesslingen.keim.efs.middleware.consumer.ConsumerService;
import de.hsesslingen.keim.efs.middleware.consumer.OptionsRequest;
import de.hsesslingen.keim.efs.middleware.validation.ConsistentBookingDateParameters;
import de.hsesslingen.keim.efs.middleware.validation.OnCreate;
import de.hsesslingen.keim.efs.mobility.service.MobilityType;
import de.hsesslingen.keim.efs.mobility.service.Mode;
import io.swagger.annotations.Api;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class receives the consumers requests for searching and booking mobility
 * options and delegates them to the {@link ConsumerService}.
 *
 * @author k.sivarasah 17 Oct 2019
 */
@Validated
@RestController
@Api(tags = {"Consumer Api"})
@ConditionalOnProperty(name = "efs.middleware.consumer-api.enabled", havingValue = "true")
public class ConsumerApi implements IConsumerApi {

    private static final Log log = LogFactory.getLog(ConsumerApi.class);

    @Autowired
    public ConsumerService consumerService;

    @Override
    public List<Options> getBookingOptions(String from, String to, Long startTime, Long endTime, Integer radius,
            Boolean share, Set<MobilityType> mobilityTypes, Set<Mode> modes, Set<String> serviceIds, String credentials) {
        log.info("Received request to get options from providers.");

        return consumerService.getOptions(new OptionsRequest()
                .setFrom(from).setTo(to)
                .setStartTime(startTime == null ? null : Instant.ofEpochMilli(startTime))
                .setEndTime(endTime == null ? null : Instant.ofEpochMilli(endTime))
                .setRadius(radius).setShare(share)
                .setModes(modes).setMobilityTypes(mobilityTypes)
                .setServiceIds(serviceIds),
                credentials);
    }

    @Override
    public List<Booking> getBookings(Set<String> serviceIds, BookingState state, String credentials) {
        log.info("Received request to get bookings from providers.");
        return consumerService.getBookings(serviceIds, credentials);
    }

    @Override
    public Booking getBookingById(@PathVariable String id, @RequestParam String serviceId, String credentials) {
        log.info("Received request to get a booking by id from a specific provider.");
        return consumerService.getBookingById(id, serviceId, credentials);
    }

    @Override
    public Booking createNewBooking(@RequestBody @Validated(OnCreate.class) @Valid @ConsistentBookingDateParameters NewBooking newBooking,
            String credentials) {
        log.info("Received request to create a new booking at a specific provider.");
        return consumerService.createBooking(newBooking, credentials);
    }

    @Override
    public Booking modifyBooking(@PathVariable String id, @RequestBody @Valid @ConsistentBookingDateParameters Booking booking,
            String credentials) {
        log.info("Received request to modify a booking at a specific provider.");
        return consumerService.modifyBooking(id, booking, credentials);
    }

    @Override
    public void performAction(String bookingId, BookingAction action, String serviceId, String assetId, String secret, String more, String credentials) {
        log.info("Received request to perform an action on a booking");
        consumerService.performAction(bookingId, action, serviceId, assetId, secret, more, credentials);
    }

    @Override
    public String createLoginToken(String serviceId, String credentials) {
        log.info("Received request to create a login token at a specific provider.");
        return consumerService.createLoginToken(serviceId, credentials);
    }

    @Override
    public String registerUser(String serviceId, String credentials, Customer userData) {
        log.info("Received request to register a user at a specific provider.");
        return consumerService.registerUser(serviceId, credentials, userData);
    }

    @Override
    public Boolean deleteLoginToken(String serviceId, String credentials) {
        log.info("Received request to delete a token at a specific provider.");
        return consumerService.deleteLoginToken(serviceId, credentials);
    }

    @Override
    public Boolean checkCredentialsAreValid(String serviceId, String credentials) {
        log.info("Received request to check the validity of credentials at a specific provider.");
        return consumerService.checkCredentialsAreValid(serviceId, credentials);
    }
}
