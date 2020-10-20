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
package de.hsesslingen.keim.efs.middleware.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.Objects;
import static java.util.stream.Collectors.joining;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Contains some methods that are used by all API controllers in this library.
 *
 * @author keim
 */
public abstract class ApiBase {

    @Autowired
    private ObjectMapper mapper;

    @Value("${middleware.logging.debug.obfuscate-credentials:true}")
    protected boolean obfuscateCredentialsForDebugLogging;

    /**
     * Serializes a collection of objects by calling {@code toString()} on each
     * one and joining their results with commas.
     *
     * @param collection
     * @return
     */
    protected String stringifyCollection(Collection<?> collection) {
        return collection != null
                ? collection.stream()
                        .map(Objects::toString)
                        .collect(joining(","))
                : "null";
    }

    /**
     * If the config property "middleware.logging.debug.obfuscate-credentials"
     * is set to true (which is default), this method obfuscates the given
     * input. Otherwise {@code any.toString()} will be returned.
     *
     * @param any
     * @return
     */
    protected String obfuscateConditional(Object any) {
        if (this.obfuscateCredentialsForDebugLogging) {
            return obfuscate(any);
        }

        if (any == null) {
            return "null";
        }

        return any.toString();
    }

    /**
     * Can be used to obfuscate the given string value. In contrast to the
     * method {@link conditionalObfuscate}, this method always obfuscates the
     * input.
     * <p>
     * <ul>
     * <li>{@code null} is rendered to {@code "null"}</li>
     * <li>Empty string is rendered to {@code "\"\""}</li>
     * <li>Everything else is rendered to {@code "***"}</li>
     * </ul>
     *
     * @param any
     * @return
     */
    protected String obfuscate(Object any) {
        if (any == null) {
            return "null";
        }

        if (any.toString().isEmpty()) {
            return "\"\"";
        }

        return "***";
    }

    /**
     * Stringifies the given object. If serialization fails, a message string is
     * returned. This method is inteded to be used for logging.
     *
     * @param o
     * @return
     */
    protected String stringify(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException ex) {
            return "Could not serialize object for logging. Exception occured.";
        }
    }
}
