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

import de.hsesslingen.keim.efs.middleware.model.Place;
import static de.hsesslingen.keim.efs.middleware.provider.ICredentialsApi.TOKEN_DESCRIPTION;
import static de.hsesslingen.keim.efs.middleware.provider.IOptionsApi.OPTIONS_PATH;
import de.hsesslingen.keim.efs.middleware.validation.PositionAsString;
import de.hsesslingen.keim.efs.mobility.config.EfsSwaggerApiResponseSupport;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.utils.EfsRequest;
import io.swagger.annotations.ApiParam;
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
 * This API serves for querying information about locations belonging to a
 * provider. This is intended to be used for fixed and permanent locations, not
 * free-floating or dynamic assets.
 *
 * @author keim
 */
@EfsSwaggerApiResponseSupport
@RequestMapping(value = "/api/places", produces = MediaType.APPLICATION_JSON_VALUE)
public interface IPlacesApi {

    /**
     * API for searching provider specific places.
     *
     * @param query
     * @param areaCenter
     * @param radiusMeter
     * @param limitTo
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity.
     * @return
     */
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<Place> search(
            @RequestParam String query,
            @RequestParam(required = false) @PositionAsString String areaCenter,
            @RequestParam(required = false) Integer radiusMeter,
            @RequestParam(required = false) Integer limitTo,
            @RequestHeader(name = TOKEN_HEADER, required = false) @ApiParam(value = TOKEN_DESCRIPTION) String token
    );

    /**
     * Assembles a get-places request for the service with the given url and the
     * given params.The params are checked for null values and added only if
     * they are present and sensible.<p>
     * Use {@link MobilityService#getServiceUrl()} to get the API url of a
     * mobility service. The returned request can be sent using
     * {@code request.go()} which will return a {@link ResponseEntity}.
     *
     * @param serviceUrl
     * @param query
     * @param areaCenter
     * @param radiusMeter
     * @param limitTo
     * @param token
     * @return
     */
    public static EfsRequest<List<Place>> buildSearchRequest(
            String serviceUrl,
            String query,
            String areaCenter,
            Integer radiusMeter,
            Integer limitTo,
            String token
    ) {
        // Start build the request object...
        var request = EfsRequest
                .get(serviceUrl + OPTIONS_PATH)
                .query("query", query)
                .expect(new ParameterizedTypeReference<List<Place>>() {
                });

        // Building query string by adding existing params...
        if (isNotBlank(areaCenter)) {
            request.query("areaCenter", areaCenter);
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
