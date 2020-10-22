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
import io.swagger.annotations.Api;
import java.util.List;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
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
public class PlacesApi extends ProviderApiBase implements IPlacesApi {

    private static final Logger logger = getLogger(PlacesApi.class);

    @Autowired
    private IPlacesService service;

    @Override
    public List<Place> search(
            String query,
            String areaCenter,
            Integer radiusMeter,
            Integer limitTo,
            String token
    ) {
        logger.info("Received request for searching places.");

        //<editor-fold defaultstate="collapsed" desc="Debug logging input params...">
        logger.debug("Params of this request:\nquery={}\nareaCenter={}\radiusMeter={}\nlimitTo={}",
                query, areaCenter, radiusMeter, limitTo
        );
        //</editor-fold>

        // Convert input params...
        var coordinates = positionIsValid(areaCenter) ? Coordinates.of(areaCenter) : null;

        // Delegate search to user implemented PlacesService...
        var places = service.search(
                query, coordinates, radiusMeter, limitTo,
                parseCredentials(null, token)
        );

        logger.debug("Responding with a list of {} places.", places.size());

        return places;
    }

}
