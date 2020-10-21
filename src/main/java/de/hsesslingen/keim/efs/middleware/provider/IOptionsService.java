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
import de.hsesslingen.keim.efs.middleware.model.Options;
import de.hsesslingen.keim.efs.middleware.model.Place;
import de.hsesslingen.keim.efs.mobility.exception.AbstractEfsException;
import java.time.ZonedDateTime;
import javax.validation.Valid;

/**
 *
 * @author boesch, K.Sivarasah
 * @param <C>
 */
public interface IOptionsService<C extends AbstractCredentials> {

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
    @NonNull
    public List<Options> getOptions(
            @NonNull Place from,
            @Nullable Place to,
            @Nullable ZonedDateTime startTime,
            @Nullable ZonedDateTime endTime,
            @Nullable Integer radiusMeter,
            @Nullable Boolean sharingAllowed,
            @Nullable @Valid C credentials
    ) throws AbstractEfsException;

}
