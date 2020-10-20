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
import static de.hsesslingen.keim.efs.middleware.model.ICoordinates.positionIsValid;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import de.hsesslingen.keim.efs.middleware.model.Options;
import de.hsesslingen.keim.efs.middleware.model.Place;
import de.hsesslingen.keim.efs.middleware.provider.credentials.CredentialsUtils;
import io.swagger.annotations.Api;
import java.time.ZonedDateTime;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author boesch, K.Sivarasah
 */
@Validated
@RestController
@ConditionalOnBean(IOptionsService.class)
@Api(tags = {SwaggerAutoConfiguration.OPTIONS_API_TAG})
public class OptionsApi implements IOptionsApi {

    private static final Logger logger = LoggerFactory.getLogger(OptionsApi.class);

    @Autowired
    private IOptionsService optionsService;

    @Autowired
    private CredentialsUtils credentialsUtils;

    @Override
    public List<Options> getOptions(
            String from,
            String fromPlaceId,
            String to,
            String toPlaceId,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radius,
            Boolean share,
            String credentials
    ) {
        logger.info("Received request to get options.");

        //<editor-fold defaultstate="collapsed" desc="Debug logging input params...">
        logger.debug("Params of this request:\nfrom={}\nfromPlaceId={}\nto={}\ntoPlaceId={}\nstartTime={}\nendTime={}\nradius={}\nshare={}\ncredentials={}",
                from, fromPlaceId, to, toPlaceId,
                startTime, endTime, radius, share,
                credentialsUtils.obfuscateConditional(credentials)
        );
        //</editor-fold>

        // Converting input params...
        var placeFrom = new Place(from);

        if (fromPlaceId != null && !fromPlaceId.isBlank()) {
            placeFrom.setStopId(fromPlaceId);
        }

        Place placeTo = null;

        if (positionIsValid(to)) {
            placeTo = new Place(to);

            if (isNotBlank(toPlaceId)) {
                placeTo.setStopId(toPlaceId);
            }
        }

        // Getting options from user implemented OptionsService.
        var options = optionsService.getOptions(
                placeFrom, placeTo, startTime, endTime, radius, share,
                credentialsUtils.fromString(credentials)
        );

        logger.debug("Responding with a list of {} options.", options.size());

        return options;
    }

}
