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

import de.hsesslingen.keim.efs.middleware.config.swagger.SwaggerAutoConfiguration;
import de.hsesslingen.keim.efs.middleware.model.Coordinates;
import static de.hsesslingen.keim.efs.middleware.model.ICoordinates.positionIsValid;
import de.hsesslingen.keim.efs.middleware.model.Place;
import de.hsesslingen.keim.efs.middleware.provider.credentials.CredentialsUtils;
import io.swagger.annotations.Api;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author keim
 */
@Validated
@RestController
@ConditionalOnBean(IPlacesService.class)
@Api(tags = {SwaggerAutoConfiguration.PLACES_API_TAG})
public class PlacesApi implements IPlacesApi {

    private static final Logger logger = LoggerFactory.getLogger(PlacesApi.class);

    public PlacesApi() {
        logger.debug("Instantiating PlacesApi...");
    }

    @Autowired
    private IPlacesService service;

    @Autowired
    private CredentialsUtils credentialsUtils;

    @Override
    public List<Place> search(
            String query,
            String areaCenter,
            Integer radiusMeter,
            Integer limitTo,
            String credentials
    ) {
        logger.info("Received search request for places.");
        logger.debug("Search params: query={}, areaCenter={}, radiusMeter={}, limitTo={}", query, areaCenter, radiusMeter, limitTo);

        var creds = credentialsUtils.fromString(credentials);

        var coordinates = positionIsValid(areaCenter) ? Coordinates.of(areaCenter) : null;

        return service.search(query, coordinates, radiusMeter, limitTo, creds);
    }

}
