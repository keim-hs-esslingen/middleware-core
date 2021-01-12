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

import de.hsesslingen.keim.efs.middleware.model.ICoordinates;
import de.hsesslingen.keim.efs.middleware.model.Place;
import static de.hsesslingen.keim.efs.middleware.provider.ICredentialsApi.TOKEN_DESCRIPTION;
import de.hsesslingen.keim.efs.middleware.validation.PositionAsString;
import de.hsesslingen.keim.efs.mobility.config.EfsSwaggerApiResponseSupport;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.utils.MiddlewareRequest;
import io.swagger.annotations.ApiParam;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import static de.hsesslingen.keim.efs.mobility.utils.MiddlewareRequest.TOKEN_HEADER;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;

/**
 * This API serves for querying information about locations (places) belonging
 * to a provider. This is intended to be used for fixed and permanent locations,
 * not free-floating or dynamic asset locations.
 * <p>
 * <h3>Additional note:</h3>
 * This interface also provides static methods for building HTTP requests, that
 * match the endpoints defined in it. They are build upon the {@link MiddlewareRequest}
 * class.
 *
 * @author keim
 */
@EfsSwaggerApiResponseSupport
@RequestMapping(value = "/api/places", produces = MediaType.APPLICATION_JSON_VALUE)
public interface IPlacesApi {

    public static final String PATH = "/places";

    /**
     * API for searching provider specific places by text. The text is used as a
     * query to find places, whose properties match this text at least
     * partially. This can be understood as a way to find places by arbitrary
     * text searches, such as names of places, or addresses or even coordinates
     * or ids.
     *
     * @param query The text that is to be used as query for searching places.
     * @param areaCenter An optional geo-location that defines the center of a
     * circular search area contrained by param {@link radiusMeter}. If no
     * radius is given, a default radius is chosen by the provider.
     * @param radiusMeter A radius in unit meter, that serves as a constraint
     * for param {@link areaCenter}. Only applied together with
     * {@link areaCenter}.
     * @param limitTo An optional upper limit of results for the response.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. Most providers do not require a token for querying
     * options using the {@link IPlacesApi}.
     * @return
     */
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<Place> searchPlaces(
            @ApiParam("The text that is to be used as query for searching places.")
            @RequestParam String query,
            //
            @ApiParam("An optional geo-location that defines the center of a circular search area contrained by param \"radiusMeter\". If no radius is given, a default radius is chosen by the provider.")
            @RequestParam(required = false) @PositionAsString String areaCenter,
            //
            @ApiParam("A radius in unit meter, that serves as a constraint for param \"areaCenter\". Only applied together with \"areaCenter\".")
            @RequestParam(required = false) Integer radiusMeter,
            //
            @ApiParam(" An optional upper limit of results for the response.")
            @RequestParam(required = false) Integer limitTo,
            //
            @ApiParam(value = TOKEN_DESCRIPTION)
            @RequestHeader(name = TOKEN_HEADER, required = false) String token
    );

    /**
     * Assembles a request, matching the {@code GET /places/search} endpoint,
     * for the service with the given url using the given token. See
     * {@link IPlacesApi#search(String, String, Integer, Integer, String)} for
     * JavaDoc on that endpoint.
     * <p>
     * The params are checked for null values and added only if they are present
     * (i.e. not {@code null} and not blank) and sensible.
     * <p>
     * The returned request can be sent using {@code request.go()} which will
     * return a {@link ResponseEntity}.
     *
     * @param serviceUrl The base url of the mobility service that should be
     * queried. Use {@link MobilityService#getServiceUrl()} to get this url.
     * @param query The text that is to be used as query for searching places.
     * @param areaCenter An optional geo-location that defines the center of a
     * circular search area contrained by param {@link radiusMeter}. If no
     * radius is given, a default radius is chosen by the provider.
     * @param radiusMeter A radius in unit meter, that serves as a constraint
     * for param {@link areaCenter}. Only applied together with areaCenter.
     * @param limitTo An optional upper limit of results for the response.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ICredentialsApi} for more
     * details on tokens. Most providers do not require a token for querying
     * locations using the {@link IPlacesApi}.
     * @return
     */
    public static MiddlewareRequest<List<Place>> buildSearchRequest(
            String serviceUrl,
            String query,
            ICoordinates areaCenter,
            Integer radiusMeter,
            Integer limitTo,
            String token
    ) {
        // Start build the request object...
        var request = MiddlewareRequest
                .get(serviceUrl + PATH + "/search")
                .query("query", query)
                .expect(new ParameterizedTypeReference<List<Place>>() {
                });

        // Building query string by adding existing params...
        if (areaCenter != null) {
            request.query("areaCenter", areaCenter.getLat() + "," + areaCenter.getLon());
        }
        if (radiusMeter != null) {
            request.query("radiusMeter", radiusMeter);
        }
        if (limitTo != null) {
            request.query("limitTo", limitTo);
        }
        if (isNotBlank(token)) {
            request.token(token);
        }

        return request;
    }

}
