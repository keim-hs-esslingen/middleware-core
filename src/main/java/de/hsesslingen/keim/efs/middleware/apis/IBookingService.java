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
package de.hsesslingen.keim.efs.middleware.apis;

import java.time.Instant;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import de.hsesslingen.keim.efs.middleware.apis.security.AbstractCredentials;
import de.hsesslingen.keim.efs.middleware.model.Booking;
import de.hsesslingen.keim.efs.middleware.model.BookingAction;
import de.hsesslingen.keim.efs.middleware.model.BookingState;
import de.hsesslingen.keim.efs.middleware.model.NewBooking;
import de.hsesslingen.keim.efs.middleware.model.Options;
import de.hsesslingen.keim.efs.middleware.model.Place;
import de.hsesslingen.keim.efs.mobility.exception.AbstractEfsException;
import javax.validation.Valid;

/**
 *
 * @author boesch, K.Sivarasah
 * @param <C>
 */
public interface IBookingService<C extends AbstractCredentials> {

    /**
     * Returns available transport options for given coordinate.Start time can
     * be defined, but is optional. If startTime is not provided, but required
     * by the third party API, a default value of "Date.now()" is used.
     *
     * @param from User's location
     * @param radiusMeter Maximum distance a user wants to travel to reach asset
     * @param to A desired destination
     * @param sharingAllowed Defines if user can also share a ride. (Null
     * allowed)
     * @param startTime Planned start-time of the trip
     * @param endTime Planned end-time of the trip
     * @param credentials Credential data
     * @return List of {@link Options}
     */
    public @NonNull
    List<Options> getBookingOptions(
            @NonNull Place from,
            @Nullable Place to,
            @Nullable Instant startTime,
            @Nullable Instant endTime,
            @Nullable Integer radiusMeter,
            @Nullable Boolean sharingAllowed,
            @Nullable @Valid C credentials
    ) throws AbstractEfsException;

    /**
     * Returns available transport options for given coordinate.Start time can
     * be defined, but is optional. If startTime is not provided, but required
     * by the third party API, a default value of "Date.now()" is used.
     *
     * @param from User's location
     * @param startTime Planned start-time of the trip
     * @param credentials Credential data
     * @return List of {@link Options}
     */
    public @NonNull
    List<Options> getBookingOptions(@NonNull Place from, @Nullable Instant startTime,
            @Nullable @Valid C credentials) throws AbstractEfsException;

    /**
     * Returns all Bookings, optionally filtered by the specified state.
     *
     * @param state The state for which to filter the bookings.
     * @param credentials Credential data
     * @return List of {@link Options}
     */
    public @NonNull
    List<Booking> getBookings(BookingState state, @NonNull @Valid C credentials) throws AbstractEfsException;

    /**
     * Creates a new Booking in BOOKED state.
     *
     * @param newBooking NewBooking data
     * @param credentials Credential data
     * @return The newly created Booking
     */
    public Booking createNewBooking(NewBooking newBooking, @NonNull @Valid C credentials) throws AbstractEfsException;

    /**
     * Returns the Booking for the given id.
     *
     * @param id The unique identifier of a Booking
     * @param credentials Credential data
     * @return Booking with the given id
     */
    public Booking getBookingById(String id, @NonNull @Valid C credentials) throws AbstractEfsException;

    /**
     * Modifies a Booking using the given Booking object
     *
     * @param id The unique identifier of a Booking
     * @param booking Booking object containing the update
     * @param credentials Credential data
     * @return The current Booking after update
     */
    public Booking modifyBooking(String id, Booking booking, @NonNull @Valid C credentials) throws AbstractEfsException;

    /**
     * Can be used to perform actions on bookings. This can be used to e.g.
     * unlock the door of rented vehicles, or stamp tickets...
     *
     * @param bookingId The ID of the booking on which to perform the action.
     * @param action The action that should be performed on the booking with the
     * given bookingId.
     * @param assetId The ID of the asset on which to perform this action. If
     * none specified, the service can choose how to handle this situation.
     * @param secret A secret that might be required by some services to perform
     * this action. (e.g. a PIN)
     * @param more Additional information that might be required by some
     * services in order to perform this action.
     * @param credentials The credentials needed to authorize oneself to perform
     * this action.
     */
    public void performAction(
            @NonNull String bookingId,
            @NonNull BookingAction action,
            @Nullable String assetId,
            @Nullable String secret,
            @Nullable String more,
            @NonNull @Valid C credentials
    );
}
