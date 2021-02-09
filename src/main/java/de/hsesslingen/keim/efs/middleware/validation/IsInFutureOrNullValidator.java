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

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Checks if the given time value is a valid point of time in the future Null
 * values are considered as valid!
 *
 * @author k.sivarasah 4 Oct 2019
 */
public class IsInFutureOrNullValidator implements ConstraintValidator<IsInFutureOrNull, Object> {

    public static final Duration THRESHOLD = Duration.ofSeconds(10);

    @Override
    public boolean isValid(Object time, ConstraintValidatorContext context) {
        if (time == null) {
            return true;
        }

        boolean valid = false;

        if (time instanceof Number) {
            valid = isValid((Long) time, context);
        } else if (time instanceof Temporal) {
            valid = isValid((Temporal) time, context);
        }

        return valid;
    }

    private boolean isValid(Long time, ConstraintValidatorContext context) {
        try {
            // Get a value that is close in the past (now - THRESHOLD) and compare to that value.
            var nowMinus10s = Instant.now().minus(THRESHOLD).getLong(ChronoField.INSTANT_SECONDS);

            return time - nowMinus10s >= 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValid(Temporal time, ConstraintValidatorContext context) {
        return isValid(time.getLong(ChronoField.INSTANT_SECONDS), context);
    }
}
