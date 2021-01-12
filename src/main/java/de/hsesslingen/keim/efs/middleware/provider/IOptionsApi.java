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

import static de.hsesslingen.keim.efs.middleware.config.SwaggerAutoConfiguration.FLEX_DATETIME_DESC;
import de.hsesslingen.keim.efs.middleware.model.ICoordinates;
import static de.hsesslingen.keim.efs.middleware.model.ICoordinates.isValidAndNotNull;
import static de.hsesslingen.keim.efs.middleware.model.ICoordinates.toLatLonString;
import de.hsesslingen.keim.efs.middleware.model.Option;
import static de.hsesslingen.keim.efs.middleware.provider.ICredentialsApi.TOKEN_DESCRIPTION;
import de.hsesslingen.keim.efs.middleware.utils.FlexibleZonedDateTimeParser;
import de.hsesslingen.keim.efs.middleware.validation.PositionAsString;
import de.hsesslingen.keim.efs.mobility.config.EfsSwaggerApiResponseSupport;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.service.Mode;
import de.hsesslingen.keim.efs.mobility.requests.MiddlewareRequest;
import io.swagger.annotations.ApiParam;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import static de.hsesslingen.keim.efs.mobility.requests.MiddlewareRequest.TOKEN_HEADER;
import de.hsesslingen.keim.efs.mobility.requests.MiddlewareRequestTemplate;
import java.util.Set;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;

/**
 * This API serves for querying a mobility service provider for mobility
 * {@link Option}. These options can be understood as possibilities for future
 * bookings.
 * <p>
 * <h3>Additional note:</h3>
 * This interface also provides static methods for building HTTP requests, that
 * match the endpoints defined in it. They are build upon the
 * {@link MiddlewareRequest} class.
 *
 * @author keim
 */
@EfsSwaggerApiResponseSupport
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public interface IOptionsApi {

    public static final String PATH = "/options";

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
     * @param from The desired starting location (coordinates) in
     * comma-separated form, e.g. 60.123,27.456.
     * @param fromPlaceId An optional place ID that represents the entity at
     * position {@link from}. This place ID is provider specific and can be
     * obtained using the places API. (See {@link IPlacesApi})
     * @param to A desired destination location (coordinates) in comma-separated
     * form, e.g. 60.123,27.456.
     * @param toPlaceId An optional place ID that represents the entity at
     * position {@link to}. This place ID is provider specific and can be
     * obtained using the places API. (See {@link IPlacesApi})
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
     * @param limitTo An optional upper limit of results for the response.
     * @param includeGeoPaths Whether detailed information about the path of
     * legs or about free floating areas should be included, if available.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. Most providers do not require a token for querying
     * options using the {@link IOptionsApi}.
     * @return List of {@link Option}
     */
    @GetMapping(PATH)
    @ResponseStatus(HttpStatus.OK)
    public List<Option> getOptions(
            @ApiParam("The desired starting location (coordinates) in comma-separated form, e.g. 60.123,27.456.")
            @RequestParam @PositionAsString String from,
            //
            @ApiParam("An optional place ID that represents the entity at position \"from\". This place ID is provider specific and can be obtained using the Places-API.")
            @RequestParam(required = false) String fromPlaceId,
            //
            @ApiParam("A desired destination location (coordinates) in comma-separated form, e.g. 60.123,27.456.")
            @RequestParam(required = false) @PositionAsString String to,
            //
            @ApiParam("An optional place ID that represents the entity at position \"to\". This place ID is provider specific and can be obtained using the Places-API.")
            @RequestParam(required = false) String toPlaceId,
            //
            @ApiParam("Desired departure time of mobility. Format: " + FLEX_DATETIME_DESC)
            @RequestParam(required = false) ZonedDateTime startTime,
            //
            @ApiParam("Desired arrival time of mobility. Format: " + FLEX_DATETIME_DESC)
            @RequestParam(required = false) ZonedDateTime endTime,
            //
            @ApiParam("Allowed search radius around \"from\" in meter.")
            @RequestParam(required = false) Integer radiusMeter,
            //
            @ApiParam("Whether the assets used can be shared with other people. (Potentially unknown to the user)")
            @RequestParam(required = false, defaultValue = "true") Boolean sharingAllowed,
            //
            @ApiParam("Allowed modes for the legs in the returned options.")
            @RequestParam(required = false, defaultValue = "") Set<Mode> modesAllowed,
            //
            @ApiParam("Limit number of results to this value.")
            @RequestParam(required = false) Integer limitTo,
            //            
            @ApiParam("Whether detailed information about the path of legs or about free floating areas should be included.")
            @RequestParam(required = false, defaultValue = "false") Boolean includeGeoPaths,
            //
            @ApiParam(value = TOKEN_DESCRIPTION)
            @RequestHeader(name = TOKEN_HEADER, required = false) String token
    );

    /**
     * Assembles a request, matching the {@code GET /options} endpoint, for the
     * service with the given url using the given token.See
     * {@link IOptionsApi#getOptions(String, String, String, String, ZonedDateTime, ZonedDateTime, Integer, Boolean, Set, Set, Integer, String)}
     * for JavaDoc on that endpoint.<p>
     * The params are checked for null values and added only if they are present
     * and sensible.
     * <p>
     * The returned request can be send using {@code request.go()} which will
     * return a {@link ResponseEntity}.
     *
     * @param serviceUrl The base url of the mobility service that should be
     * queried. Use {@link MobilityService#getServiceUrl()} to get this url.
     * @param from The desired starting location (coordinates) in
     * comma-separated form, e.g. 60.123,27.456.
     * @param to A desired destination location (coordinates) in comma-separated
     * form, e.g. 60.123,27.456.
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
     * @param limitTo An optional upper limit of results for the response.
     * @param includeGeoPaths Whether detailed information about the path of
     * legs or about free floating areas should be included, if available.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. Most providers do not require a token for querying
     * options using the {@link IOptionsApi}.
     * @param requestTemplate The template that should be used as foundation
     * for building the request.
     * @return
     */
    public static MiddlewareRequest<List<Option>> buildGetOptionsRequest(
            String serviceUrl,
            ICoordinates from,
            ICoordinates to,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            Boolean sharingAllowed,
            Set<Mode> modesAllowed,
            Integer limitTo,
            Boolean includeGeoPaths,
            String token,
            MiddlewareRequestTemplate requestTemplate
    ) {
        return buildGetOptionsRequest(
                serviceUrl, from, null, to, null, startTime, endTime,
                radiusMeter, sharingAllowed, modesAllowed,
                limitTo, includeGeoPaths, token, requestTemplate
        );
    }

    /**
     * Assembles a request, matching the {@code GET /options} endpoint, for the
     * service with the given url using the given token.See
     * {@link IOptionsApi#getOptions(String, String, String, String, ZonedDateTime, ZonedDateTime, Integer, Boolean, Set, Set, Integer, String)}
     * for JavaDoc on that endpoint.<p>
     * The params are checked for null values and added only if they are present
     * and sensible.
     * <p>
     * The returned request can be send using {@code request.go()} which will
     * return a {@link ResponseEntity}.
     *
     * @param serviceUrl The base url of the mobility service that should be
     * queried. Use {@link MobilityService#getServiceUrl()} to get this url.
     * @param from The desired starting location (coordinates) in
     * comma-separated form, e.g. 60.123,27.456.
     * @param fromPlaceId An optional place ID that represents the entity at
     * position {@link from}. This place ID is provider specific and can be
     * obtained using the places API. (See {@link IPlacesApi})
     * @param to A desired destination location (coordinates) in comma-separated
     * form, e.g. 60.123,27.456.
     * @param toPlaceId An optional place ID that represents the entity at
     * position {@link to}. This place ID is provider specific and can be
     * obtained using the places API. (See {@link IPlacesApi})
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
     * @param limitTo An optional upper limit of results for the response.
     * @param includeGeoPaths Whether detailed information about the path of
     * legs or about free floating areas should be included, if available.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. Most providers do not require a token for querying
     * options using the {@link IOptionsApi}.
     * @param requestTemplate The template that should be used as foundation
     * for building the request.
     * @return
     */
    public static MiddlewareRequest<List<Option>> buildGetOptionsRequest(
            String serviceUrl,
            ICoordinates from,
            String fromPlaceId,
            ICoordinates to,
            String toPlaceId,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            Boolean sharingAllowed,
            Set<Mode> modesAllowed,
            Integer limitTo,
            Boolean includeGeoPaths,
            String token,
            MiddlewareRequestTemplate requestTemplate
    ) {
        // Start build the request object...
        var request = requestTemplate
                .get(serviceUrl + PATH)
                .query("from", toLatLonString(from))
                .expect(new ParameterizedTypeReference<List<Option>>() {
                });

        // Building query string by adding existing params...
        if (isNotBlank(fromPlaceId)) {
            request.query("fromPlaceId", fromPlaceId);
        }
        if (isValidAndNotNull(to)) {
            request.query("to", toLatLonString(to));
        }
        if (isNotBlank(toPlaceId)) {
            request.query("toPlaceId", toPlaceId);
        }
        if (startTime != null) {
            request.query("startTime", startTime.toInstant().toEpochMilli());
        }
        if (endTime != null) {
            request.query("endTime", endTime.toInstant().toEpochMilli());
        }
        if (radiusMeter != null && radiusMeter >= 0) {
            request.query("radiusMeter", radiusMeter);
        }
        if (sharingAllowed != null) {
            request.query("sharingAllowed", sharingAllowed);
        }
        if (modesAllowed != null && !modesAllowed.isEmpty()) {
            var queryValue = modesAllowed.stream().map(Object::toString).collect(joining(","));
            request.query("modesAllowed", queryValue);
        }
        if (limitTo != null) {
            request.query("limitTo", limitTo);
        }
        if (includeGeoPaths != null) {
            request.query("includeGeoPaths", includeGeoPaths);
        }
        if (isNotBlank(token)) {
            request.token(token);
        }

        return request;
    }

}
