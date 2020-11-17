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
import de.hsesslingen.keim.efs.middleware.model.Option;
import de.hsesslingen.keim.efs.middleware.model.Place;
import de.hsesslingen.keim.efs.middleware.utils.FlexibleZonedDateTimeParser;
import de.hsesslingen.keim.efs.mobility.service.MobilityType;
import de.hsesslingen.keim.efs.mobility.service.Mode;
import java.time.ZonedDateTime;
import java.util.Set;
import javax.validation.Valid;

/**
 * This interface represents the API, that the options rest controller uses to
 * perform the possible actions provided by the options API. That API will only
 * be available if this interface is implemented and provided as a spring bean.
 *
 * @author boesch, K.Sivarasah
 * @param <C>
 */
public interface IOptionsService<C extends AbstractCredentials> {

    /**
     * Returns available mobility options for the given criteria.
     * <p>
     * Param {@link startTime} can be defined, but is optional. If
     * {@link startTime} is not provided, but required by the remote API of the
     * provider, a sensible default value is used automatically, which is usally
     * the current point in time ("now").
     * <p>
     * Param {@link endTime} must be after param {@link startTime}, if
     * {@link startTime} is given, but it is not dependent on {@link startTime}.
     * Usually <em>either</em> {@link startTime} <em>or</em> {@link endTime} are
     * given, defining one point of reference in time, that should be used for
     * matching options. However, if both params are given, the provider can
     * chose how to interpret this situation and return the best options based
     * on that.
     *
     * @param from The starting place of the travel for which options are
     * requested.
     * @param to An optional end place of the travel for which options are
     * requested.
     * @param startTime Optional desired start time of mobility. Can <b>not</b>
     * be in past. Values up to 10 seconds in past from "now" are tolerated in
     * validation, to respect network and processing delays for HTTP requests.
     * Format is flexible. See {@link FlexibleZonedDateTimeParser} for details
     * on possible formats.
     * @param endTime Optional desired end time of mobility. Can <b>not</b> be
     * in past and must be after {@link startTime}, if {@link startTime} is
     * given. Values up to 10 seconds in past from "now" are tolerated in
     * validation, to respect network and processing delays for HTTP requests.
     * Format is flexible. See {@link FlexibleZonedDateTimeParser} for details
     * on possible formats.
     * @param radiusMeter Maximum distance a user wants to travel to reach the
     * start point of the mobility option in meters. This basically serves as a
     * search radius around the geo-position given in param {@link from}.
     * @param sharingAllowed Defines if user is ok with sharing his mobility
     * option with others, potentially unknown people.
     * @param modesAllowed Allowed modes for legs and potential sub-legs of all
     * options returned.
     * @param mobilityTypesAllowed Allowed mobilityTypes for legs and potential
     * sub-legs of all options returned.
     * @param limitTo An optional upper limit of results for the response.
     * @param includeGeoPaths Whether detailed information about the path of
     * legs or about free floating areas should be included, if available.
     * @param credentials The credentials needed to authenticate and authorize
     * oneself to perform this action.
     * @return List of {@link Option}
     */
    @NonNull
    public List<Option> getOptions(
            @NonNull Place from,
            @Nullable Place to,
            @Nullable ZonedDateTime startTime,
            @Nullable ZonedDateTime endTime,
            @Nullable Integer radiusMeter,
            @Nullable Boolean sharingAllowed,
            @Nullable Set<Mode> modesAllowed,
            @Nullable Set<MobilityType> mobilityTypesAllowed,
            @Nullable Integer limitTo,
            @Nullable Boolean includeGeoPaths,
            @Nullable @Valid C credentials
    );

}
