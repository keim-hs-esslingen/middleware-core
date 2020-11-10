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

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import de.hsesslingen.keim.efs.middleware.provider.credentials.AbstractCredentials;
import de.hsesslingen.keim.efs.middleware.model.Booking;
import de.hsesslingen.keim.efs.middleware.model.BookingAction;
import de.hsesslingen.keim.efs.middleware.model.BookingState;
import de.hsesslingen.keim.efs.middleware.model.NewBooking;
import javax.validation.Valid;

/**
 * This interface represents the API, that the booking rest controller uses to
 * perform the possible actions provided by the booking API. That API will only
 * be available if this interface is implemented and provided as a spring bean.
 *
 * @author boesch, K.Sivarasah
 * @param <C>
 */
public interface IBookingService<C extends AbstractCredentials> {

    /**
     * Returns a list of bookings associated with the account that is
     * represented by the given credentials.
     *
     * @param state An optional booking state by which to filter the results.
     * @param credentials The credentials needed to authenticate and authorize
     * oneself to perform this action.
     * @return
     */
    public @NonNull
    List<Booking> getBookings(BookingState state, @NonNull @Valid C credentials);

    /**
     * Gets a particular {@link Booking} using the booking id.
     *
     * @param id The ID of the booking that is to be returned.
     * @param credentials The credentials needed to authenticate and authorize
     * oneself to perform this action.
     * @return
     */
    public Booking getBookingById(String id, @NonNull @Valid C credentials);

    /**
     * Creates a new booking and returns it.
     *
     * @param newBooking The {@link NewBooking} that should be created.
     * @param optionReference An optional reference to an {@link Option} that
     * unambiguously references this option for booking. This reference is
     * sometimes given in instances of {@link Option}.
     * @param credentials The credentials needed to authenticate and authorize
     * oneself to perform this action.
     * @return The newly created Booking
     */
    public Booking createNewBooking(NewBooking newBooking, @Nullable String optionReference, @NonNull @Valid C credentials);

    /**
     * Updates an existing {@link Booking} with new details.
     *
     * @param id The booking id.
     * @param booking The {@link Booking} object containing modified data.
     * @param credentials The credentials needed to authenticate and authorize
     * oneself to perform this action.
     * @return
     */
    public Booking modifyBooking(String id, Booking booking, @NonNull @Valid C credentials);

    /**
     * Can be used to perform actions on bookings.
     *
     * @param bookingId The ID of the booking on which to perform the action.
     * @param action The action that should be performed on the booking with the
     * given {@link bookingId}.
     * @param secret An optional secret that might be required by some mobility
     * service providers to perform this action. (e.g. a PIN)
     * @param credentials The credentials needed to authenticate and authorize
     * oneself to perform this action.
     */
    public void performAction(
            @NonNull String bookingId,
            @NonNull BookingAction action,
            @Nullable String secret,
            @NonNull @Valid C credentials
    );
}
