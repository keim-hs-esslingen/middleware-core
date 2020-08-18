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

import java.io.IOException;
import java.time.Instant;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.regex.Pattern;

/**
 * Deserializer for {@link Instant}. Makes an {@link Instant} out of a
 * milliseconds since epoch.
 *
 * @author k.sivarasah 3 Oct 2019
 */
public class FlexibleZonedDateTimeDeserializer extends JsonDeserializer<ZonedDateTime> {

    private static Pattern MILLIS_PATTERN = Pattern.compile("^[0-9]+$");

    @Override
    public ZonedDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        var value = p.getText();

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

        throw new IOException("Unable to parse zoned date time in any known format.");
    }

}
