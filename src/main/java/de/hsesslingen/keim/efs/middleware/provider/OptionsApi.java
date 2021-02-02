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

import de.hsesslingen.keim.efs.middleware.config.SwaggerAutoConfiguration;
import static de.hsesslingen.keim.efs.middleware.model.ICoordinates.parseAndValidate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import de.hsesslingen.keim.efs.middleware.model.Option;
import de.hsesslingen.keim.efs.middleware.model.Place;
import de.hsesslingen.keim.efs.middleware.provider.config.ProviderProperties;
import de.hsesslingen.keim.efs.mobility.service.Mode;
import io.swagger.annotations.Api;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import static java.util.Collections.disjoint;
import java.util.Set;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;

/**
 * @author boesch, K.Sivarasah
 */
@Validated
@RestController
@ConditionalOnBean({IOptionsService.class, ProviderProperties.class})
@Api(tags = {SwaggerAutoConfiguration.OPTIONS_API_TAG})
@AutoConfigureAfter(ProviderProperties.class)
public class OptionsApi extends ApiBase implements IOptionsApi {

    private static final String RETURN_ZERO_UPON_MODES_MISMATCH_KEY = "middleware.provider.options-api.return-zero-upon-modes-mismatch";

    @Autowired
    private IOptionsService optionsService;

    @Autowired
    private ProviderProperties properties;

    @Value("${" + RETURN_ZERO_UPON_MODES_MISMATCH_KEY + ":true}")
    private boolean returnZeroUponModesMismatch;

    @Override
    public List<Option> getOptions(
            String from,
            String fromPlaceId,
            String to,
            String toPlaceId,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            Boolean sharingAllowed,
            Set<Mode> modesAllowed,
            Integer limitTo,
            Boolean includeGeoPaths,
            String token
    ) {
        logParams("getOptions", () -> array(
                "from", from,
                "fromPlaceId", fromPlaceId,
                "to", to,
                "toPlaceId", toPlaceId,
                "startTime", startTime,
                "endTime", endTime,
                "radiusMeter", radiusMeter,
                "sharingAllowed", sharingAllowed,
                "modesAllowed", stringifyCollection(modesAllowed, m -> m.name()),
                "limitTo", limitTo,
                "includeGeoPaths", includeGeoPaths
        ));

        if (returnZeroUponModesMismatch
                && modesAllowed != null && !modesAllowed.isEmpty()
                && disjoint(properties.getMobilityService().getModes(), modesAllowed)) {
            logger.info("Returning 0 options because the requested set of allowed modes has none in common with our provided ones. If you want to change this behavior, set property \"{}\" to \"false\".", RETURN_ZERO_UPON_MODES_MISMATCH_KEY);
            var result = new ArrayList<Option>();
            logResult(result);
            return result;
        }

        // Converting input params...
        var placeFrom = Place.fromCoordinates(from);

        if (fromPlaceId != null && !fromPlaceId.isBlank()) {
            placeFrom.setId(fromPlaceId);
        }

        Place placeTo = parseAndValidate(to, Place::fromCoordinates, () -> null);

        if (placeTo != null && isNotBlank(toPlaceId)) {
            placeTo.setId(toPlaceId);
        }

        // Getting options from user implemented OptionsService.
        var result = optionsService.getOptions(
                placeFrom, placeTo, startTime, endTime, radiusMeter,
                sharingAllowed, modesAllowed, limitTo, includeGeoPaths,
                parseToken(token)
        );

        logResult(result);

        return result;
    }

}
