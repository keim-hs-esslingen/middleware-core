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
import de.hsesslingen.keim.efs.middleware.validation.PositionAsString;
import de.hsesslingen.keim.efs.mobility.config.EfsSwaggerApiResponseSupport;
import static de.hsesslingen.keim.efs.mobility.utils.EfsRequest.CREDENTIALS_HEADER_DESC;
import static de.hsesslingen.keim.efs.mobility.utils.EfsRequest.CREDENTIALS_HEADER_NAME;
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

/**
 *
 * @author keim
 */
@EfsSwaggerApiResponseSupport
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public interface IOptionsApi {

    /**
     * Returns available transport options for given coordinate.Start time can
     * be defined, but is optional.If startTime is not provided, but required by
     * the third party API, a default value of "Date.now()" is used.
     *
     * @param from User's location in comma separated form e.g. 60.123,27.456.
     * @param radius Maximum distance a user wants to travel to reach asset in
     * metres, e.g. 500 metres.
     * @param to A desired destination e.g. 60.123,27.456.
     * @param startTime Start time either in ms since epoch or as a zoned date time in ISO format.
     * @param endTime End time either in ms since epoch or as a zoned date time in ISO format.
     * @param share Defines if user can also share a ride. (Available values :
     * YES, NO)
     * @param credentials Credential data as json content string
     * @return List of {@link Options}
     */
    @GetMapping("/options")
    @ResponseStatus(HttpStatus.OK)
    @EfsSwaggerGetBookingOptions
    public List<Options> getOptions(
            @RequestParam(required = true) @PositionAsString String from,
            @RequestParam(required = false) @PositionAsString String to,
            @RequestParam(required = false) @ApiParam(FLEX_DATETIME_DESC) ZonedDateTime startTime,
            @RequestParam(required = false) @ApiParam(FLEX_DATETIME_DESC) ZonedDateTime endTime,
            @RequestParam(required = false) @ApiParam("Unit: meter") Integer radius,
            @RequestParam(required = false) Boolean share,
            @RequestHeader(name = CREDENTIALS_HEADER_NAME, required = false) @ApiParam(CREDENTIALS_HEADER_DESC) String credentials
    );
}
