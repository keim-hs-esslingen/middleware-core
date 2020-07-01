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
package de.hsesslingen.keim.efs.middleware.booking;

import lombok.Data;

/**
 * The validity token (such as booking ID, travel ticket etc.) that MaaS clients
 * will display to validate the trip when starting the leg.
 *
 * @author boesch
 */
@Data
public class Token {

    /**
     * The rules that MaaS will interpret to schedule, -validate or cancel the
     * booking.
     */
    private ValidityDuration validityDuration;

    /**
     * Arbitrary metadata the TO may pass along the ticket to the client (e.g. a
     * booking code, base64 encoded binary)
     */
    private Object meta;

    
    
    @Data
    public static class ValidityDuration {

        /**
         * The starting time from which the ticket is valid
         */
        private long from;

        /**
         * The finishing time the ticket is valid for
         */
        private long to;

    }
}
