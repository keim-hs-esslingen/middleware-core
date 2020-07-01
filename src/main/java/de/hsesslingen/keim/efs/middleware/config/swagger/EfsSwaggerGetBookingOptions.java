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
package de.hsesslingen.keim.efs.middleware.config.swagger;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * Custom annotation containing Swagger's parameter-descriptions for the endpoint GET '/bookings/options'.
 * Using this annotation documentation code can be separated from program-code, which increases the
 * readability of our code.
 * 
 * @author k.sivarasah
 * 8 Oct 2019
 */

@ApiOperation(value = "Get Transport Options", notes = "Returns available transport options for given coordinate. Start time can " + 
		"be defined, but is optional. If startTime is not provided, but required " + 
		"by the third party API, a default value of \"Date.now()\" is used.")
@ApiImplicitParams(value = { 
		@ApiImplicitParam(name = "from", value = "User’s location in comma separated form e.g. 49.123,17.234", required = true, dataType="string"),
		@ApiImplicitParam(name = "to", value = "Destination location in comma separated form e.g. 49.123,17.234", dataType="string"),
		@ApiImplicitParam(name = "startTime", value = "A UTC timestamp in the future (number of ms since epoch)", dataType="long"),
		@ApiImplicitParam(name = "endTime", value = "A UTC timestamp in the future (number of ms since epoch)", dataType="long"),
		@ApiImplicitParam(name = "radius", value = "Search radius in meters", dataType = "integer"),
		@ApiImplicitParam(name = "share", value = "Defines if user can also share a ride", dataType = "boolean")
})

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EfsSwaggerGetBookingOptions {

}
