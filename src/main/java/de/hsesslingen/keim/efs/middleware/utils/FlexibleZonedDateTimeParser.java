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
 * This is a flexible parser for ZonedDateTime objects that adds missing default
 * information as necessary. It makes use of the static parse methods provided
 * by many of the classes in the {@code java.time} package and therefore
 * supports all the formats that those methods support.
 * <p>
 * You can enter a millisecond value since 01. Jan. 1970, a fully qualified
 * zoned ISO-timestamp but also many parts of such a timestamp soley by
 * themselves as well.
 * <p>
 * For example using the value "15:00" will cause the parser to parse this value
 * as 15 o'clock on the current date of the parsing server. The server will add
 * default date information as necessary.
 * <p>
 * If entering only a date, e.g. 2020-03-11, the parser will use the point in
 * time at the start of this local date and convert it to a
 * {@link ZonedDateTime} using his default {@link ZoneId}.
 * <p>
 * Of course this method leaves space for ambiguity, but it can make testing and
 * querying the API easier. For production usage, using a millisecond value or a
 * fully qualified ISO-timestamp including timezone should be used. This
 * resolves any ambiguity.
 * <p>
 * The following methods for parsing a temporal object are used in the described
 * order. If one succeeds, the result is returned immediately without trying the
 * remaining methods:
 * <ol>
 * <li>Regex-Parsing a milliseconds value describing the milliseconds since 01.
 * Jan. 1970.</li>
 * <li>{@link ZonedDateTime#parse(CharSequence)}</li>
 * <li>{@link OffsetDateTime#parse(CharSequence)} and if successful,
 * {@link OffsetDateTime#toZonedDateTime()} on the returned object.</li>
 * <li>{@link LocalDateTime#parse(CharSequence)} and if successful,
 * {@link LocalDateTime#atZone(ZoneId)} on the returned object with
 * {@link ZoneId#systemDefault()} as value.</li>
 * <li>{@link LocalDate#parse(CharSequence)} and if successful,
 * {@link LocalDate#atStartOfDay(ZoneId)} on the returned object with
 * {@link ZoneId#systemDefault()} as value.</li>
 * <li>{@link OffsetTime#parse(CharSequence)} and if successful,
 * {@link OffsetTime#atDate(LocalDate)} on the returned object with
 * {@link LocalDate#now()} as value, and then using
 * {@link OffsetDateTime#toZonedDateTime()} to convert it to a
 * {@link ZonedDateTime}.</li>
 * <li>{@link LocalTime#parse(CharSequence)} and if successful,
 * {@link LocalTime#atDate(LocalDate)} on the returned object with
 * {@link LocalDate#now()} as value, and then using
 * {@link LocalDateTime#atZone(ZoneId)} with {@link ZoneId#systemDefault()} to
 * convert it to a {@link ZonedDateTime}.</li>
 * </ol>
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
