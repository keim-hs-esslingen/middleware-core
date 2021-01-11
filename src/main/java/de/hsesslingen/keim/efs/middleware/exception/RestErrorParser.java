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
package de.hsesslingen.keim.efs.middleware.exception;

import de.hsesslingen.keim.efs.mobility.exception.MiddlewareError;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

/**
 * This class is a response error handler that can be extended by simply
 * implementing the {@link parseError()} method. This allows to more
 * conveniently parse errors returned from provider APIs.
 *
 * @author boesch
 */
public abstract class RestErrorParser implements ResponseErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestErrorParser.class);

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR
                || response.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatus httpStatus = response.getStatusCode();

        String responseBody;

        try ( Scanner scanner = new Scanner(response.getBody(), Charset.forName("UTF-8").name())) {
            responseBody = scanner.useDelimiter("\\A").next();
        }

        MiddlewareError error = parseError(responseBody, httpStatus, response);

        if (error == null) {
            // Fallback.
            error = new MiddlewareError(httpStatus.value(), responseBody);
        }

        logger.error("Error Response: {}", responseBody);

        throw error.toException();
    }

    public abstract MiddlewareError parseError(String responseBody, HttpStatus statusCode, ClientHttpResponse response);

}
