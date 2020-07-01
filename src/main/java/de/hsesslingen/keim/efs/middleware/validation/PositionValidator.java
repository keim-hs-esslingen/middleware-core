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
package de.hsesslingen.keim.efs.middleware.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.util.StringUtils;

/**
 * Validates Position given as String in format latitude,longitude. Example {@code 49.123,17.234}
 * Null values are considered as valid!
 * 
 * @author k.sivarasah
 * 1 Oct 2019
 */

public class PositionValidator implements ConstraintValidator<PositionAsString, String> {

	@Override
	public boolean isValid(String pos, ConstraintValidatorContext context) {
		if(pos == null) {
			return true;
		}
		
		Double lat;
		Double lon;
		try {
			lat = Double.valueOf(pos.split(",")[0]);
			lon = Double.valueOf(pos.split(",")[1]);
		} catch (Exception e ) {
			return false;
		}
			
		return !StringUtils.isEmpty(pos) && StringUtils.countOccurrencesOf(pos, ",") == 1 &&
				 lat >= -90 && lat<= 90 && lon >= -180 && lon <= 180;
	}

}
