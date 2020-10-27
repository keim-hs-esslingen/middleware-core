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

import de.hsesslingen.keim.efs.middleware.config.swagger.EfsSwaggerGetBookingOptions;
import static de.hsesslingen.keim.efs.middleware.config.swagger.SwaggerAutoConfiguration.FLEX_DATETIME_DESC;
import de.hsesslingen.keim.efs.middleware.model.Options;
import static de.hsesslingen.keim.efs.middleware.provider.ICredentialsApi.TOKEN_DESCRIPTION;
import de.hsesslingen.keim.efs.middleware.validation.PositionAsString;
import de.hsesslingen.keim.efs.mobility.config.EfsSwaggerApiResponseSupport;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.utils.EfsRequest;
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
import static de.hsesslingen.keim.efs.mobility.utils.EfsRequest.TOKEN_HEADER;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author keim
 */
@EfsSwaggerApiResponseSupport
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public interface IOptionsApi {

    public static final String OPTIONS_PATH = "/options";

    /**
     * Returns available transport options for given coordinate.Start time can
     * be defined, but is optional.If startTime is not provided, but required by
     * the third party API, a default value of "Date.now()" is used.
     *
     * @param from User's location in comma separated form e.g. 60.123,27.456.
     * @param fromPlaceId An optional place ID that represents the entity at
     * position {@link from}. This place ID is provider specific and can be
     * obtained using the places API.
     * @param radius Maximum distance a user wants to travel to reach asset in
     * metres, e.g. 500 metres.
     * @param toPlaceId An optional place ID that represents the entity at
     * position {@link to}. This place ID is provider specific and can be
     * obtained using the places API.
     * @param to A desired destination e.g. 60.123,27.456.
     * @param startTime Start time either in ms since epoch or as a zoned date
     * time in ISO format.
     * @param endTime End time either in ms since epoch or as a zoned date time
     * in ISO format.
     * @param share Defines if user can also share a ride. (Available values :
     * YES, NO)
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity.
     * @return List of {@link Options}
     */
    @GetMapping(OPTIONS_PATH)
    @ResponseStatus(HttpStatus.OK)
    @EfsSwaggerGetBookingOptions
    public List<Options> getOptions(
            @RequestParam(required = true) @PositionAsString String from,
            @RequestParam(required = false) String fromPlaceId,
            @RequestParam(required = false) @PositionAsString String to,
            @RequestParam(required = false) String toPlaceId,
            @RequestParam(required = false) @ApiParam(FLEX_DATETIME_DESC) ZonedDateTime startTime,
            @RequestParam(required = false) @ApiParam(FLEX_DATETIME_DESC) ZonedDateTime endTime,
            @RequestParam(required = false) @ApiParam("Unit: meter") Integer radius,
            @RequestParam(required = false) Boolean share,
            @RequestHeader(name = TOKEN_HEADER, required = false) @ApiParam(value = TOKEN_DESCRIPTION) String token
    );

    /**
     * Assembles a request, matching the {@code GET /options} endpoint, for the
     * service with the given url using the given token.
     * <p>
     * The params are checked for null values and added only if they are present
     * and sensible.
     * <p>
     * The returned request can be send using {@code request.go()} which will
     * return a {@link ResponseEntity}.
     *
     * @param serviceUrl The base url of the mobility service who should be
     * queried. Use {@link MobilityService#getServiceUrl()} to get this url.
     * @param from
     * @param to
     * @param startTime
     * @param endTime
     * @param radiusMeter
     * @param share
     * @param token
     * @return
     */
    public static EfsRequest<List<Options>> buildSearchRequest(
            String serviceUrl,
            String from,
            String to,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            Boolean share,
            String token
    ) {
        return buildSearchRequest(serviceUrl, from, null, to, null, startTime, endTime, radiusMeter, share, token);
    }

    /**
     * Assembles a request, matching the {@code GET /options} endpoint, for the
     * service with the given url using the given token.
     * <p>
     * The params are checked for null values and added only if they are present
     * and sensible.
     * <p>
     * The returned request can be send using {@code request.go()} which will
     * return a {@link ResponseEntity}.
     *
     * @param serviceUrl The base url of the mobility service who should be
     * queried. Use {@link MobilityService#getServiceUrl()} to get this url.
     * @param from
     * @param fromPlaceId
     * @param to
     * @param toPlaceId
     * @param startTime
     * @param endTime
     * @param radiusMeter
     * @param share
     * @param token
     * @return
     */
    public static EfsRequest<List<Options>> buildSearchRequest(
            String serviceUrl,
            String from,
            String fromPlaceId,
            String to,
            String toPlaceId,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            Boolean share,
            String token
    ) {
        // Start build the request object...
        var request = EfsRequest
                .get(serviceUrl + OPTIONS_PATH)
                .query("from", from)
                .expect(new ParameterizedTypeReference<List<Options>>() {
                });

        // Building query string by adding existing params...
        if (isNotBlank(fromPlaceId)) {
            request.query("fromPlaceId", fromPlaceId);
        }
        if (isNotBlank(to)) {
            request.query("to", to);
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
            request.query("radius", radiusMeter);
        }
        if (share != null) {
            request.query("share", share);
        }
        if (isNotBlank(token)) {
            request.token(token);
        }

        return request;
    }

}
