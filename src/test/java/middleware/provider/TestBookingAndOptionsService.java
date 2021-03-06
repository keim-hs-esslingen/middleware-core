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
package middleware.provider;

import java.util.List;

import de.hsesslingen.keim.efs.middleware.provider.IBookingService;
import de.hsesslingen.keim.efs.middleware.provider.credentials.AbstractCredentials;
import de.hsesslingen.keim.efs.middleware.model.Booking;
import de.hsesslingen.keim.efs.middleware.model.BookingAction;
import de.hsesslingen.keim.efs.middleware.model.BookingState;
import de.hsesslingen.keim.efs.middleware.model.NewBooking;
import de.hsesslingen.keim.efs.middleware.model.Option;
import de.hsesslingen.keim.efs.middleware.model.Place;
import de.hsesslingen.keim.efs.middleware.provider.IOptionsService;
import java.time.ZonedDateTime;
import java.util.Set;

/**
 * @author k.sivarasah 6 Oct 2019
 */
public class TestBookingAndOptionsService implements IBookingService, IOptionsService {

    /* (non-Javadoc)
	 * @see de.hsesslingen.keim.efs.middleware.apis.IBookingService#getBookingsByState(de.hsesslingen.keim.efs.middleware.booking.BookingState)
     */
    @Override
    public List<Booking> getBookings(BookingState state, AbstractCredentials credentials) {
        return List.of();
    }

    /* (non-Javadoc)
	 * @see de.hsesslingen.keim.efs.middleware.apis.IBookingService#createNewBooking(de.hsesslingen.keim.efs.middleware.booking.NewBooking)
     */
    @Override
    public Booking createNewBooking(NewBooking newBooking, String optionReference, AbstractCredentials credentials) {
        return null;
    }

    /* (non-Javadoc)
	 * @see de.hsesslingen.keim.efs.middleware.apis.IBookingService#getBookingById(java.lang.String)
     */
    @Override
    public Booking getBookingById(String id, AbstractCredentials credentials) {
        return null;
    }

    /* (non-Javadoc)
	 * @see de.hsesslingen.keim.efs.middleware.apis.IBookingService#modifyBooking(java.lang.String, de.hsesslingen.keim.efs.middleware.booking.Booking)
     */
    @Override
    public Booking modifyBooking(String id, Booking booking, AbstractCredentials credentials) {
        return null;
    }

    @Override
    public Booking performAction(String bookingId, BookingAction action, String secret, AbstractCredentials credentials) {
        return null;
    }

    @Override
    public List<Option> getOptions(Place from, Place to, ZonedDateTime startTime, ZonedDateTime endTime, Integer radiusMeter, Boolean sharingAllowed, Set modesAllowed, Integer limitTo, Boolean includeGeoPaths, AbstractCredentials credentials) {
        return List.of();
    }

}
