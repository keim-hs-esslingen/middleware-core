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
package de.hsesslingen.keim.efs.middleware.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * This converter is a flexible converter that adds missing default information
 * in order to be able to create a zoned date time.
 *
 * @author boesch
 */
@Component
public class ZonedDateTimeConverter implements Converter<String, ZonedDateTime> {

    @Override
    public ZonedDateTime convert(String s) {
        try {
            return ZonedDateTime.parse(s);
        } catch (Exception ex) {
        }

        try {
            return OffsetDateTime.parse(s).toZonedDateTime();
        } catch (Exception ex) {
        }

        try {
            return LocalDateTime.parse(s).atZone(ZoneId.systemDefault());
        } catch (Exception ex) {
        }

        try {
            return OffsetTime.parse(s).atDate(LocalDate.now()).toZonedDateTime();
        } catch (Exception ex) {

        }
        try {
            return LocalTime.parse(s).atDate(LocalDate.now()).atZone(ZoneId.systemDefault());
        } catch (Exception ex) {

        }

        return LocalDate.parse(s).atStartOfDay(ZoneId.systemDefault());
    }

}
