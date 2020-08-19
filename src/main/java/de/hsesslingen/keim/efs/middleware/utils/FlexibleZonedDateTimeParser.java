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
package de.hsesslingen.keim.efs.middleware.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.regex.Pattern;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * This converter is a flexible converter that adds missing default information
 * in order to be able to create a zoned date time.
 *
 * @author boesch
 */
@Component
public class FlexibleZonedDateTimeParser implements Converter<String, ZonedDateTime> {

    private static Pattern MILLIS_PATTERN = Pattern.compile("^[0-9]+$");

    public static ZonedDateTime tryParseZonedDateTime(String value) {
        if (MILLIS_PATTERN.matcher(value).matches()) {
            return Instant.ofEpochMilli(Long.parseLong(value)).atZone(ZoneId.systemDefault());
        }

        try {
            return ZonedDateTime.parse(value);
        } catch (Exception ex) {
        }

        try {
            return OffsetDateTime.parse(value).toZonedDateTime();
        } catch (Exception ex) {
        }

        try {
            return LocalDateTime.parse(value).atZone(ZoneId.systemDefault());
        } catch (Exception ex) {
        }

        try {
            return LocalDate.parse(value).atStartOfDay(ZoneId.systemDefault());
        } catch (Exception ex) {
        }

        try {
            return OffsetTime.parse(value).atDate(LocalDate.now()).toZonedDateTime();
        } catch (Exception ex) {
        }

        try {
            return LocalTime.parse(value).atDate(LocalDate.now()).atZone(ZoneId.systemDefault());
        } catch (Exception ex) {
        }

        throw new RuntimeException("Unable to parse ZonedDateTime using any known format.");
    }

    @Override
    public ZonedDateTime convert(String s) {
        return tryParseZonedDateTime(s);
    }

}
