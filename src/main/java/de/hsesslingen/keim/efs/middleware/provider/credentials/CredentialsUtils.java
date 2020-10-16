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
package de.hsesslingen.keim.efs.middleware.provider.credentials;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hsesslingen.keim.efs.mobility.utils.EfsRequest;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * This class serves as a utility component that can be used to deserialize
 * credential data for remote APIs. It has several static utility functions that
 * can be used anywhere but also some member functions that are only available
 * if this component is autowired into another component.
 * <p>
 * These member function make use of a potential bean that implements the
 * {@link ICredentialsDeserializer} interface, which can be provided by the
 * users of this library to deserizlie credentials into objects that fit their
 * use case.
 * <p>
 * Such a bean however is not required and if it doesn't exists, this component
 * will instead try to deserialize the credentials without knowledge of the
 * class blueprint.
 *
 * @author k.sivarasah 12 Nov 2019
 * @author b.oesch
 */
@Service
@Lazy
public class CredentialsUtils {

    private final static Logger logger = LoggerFactory.getLogger(CredentialsUtils.class);
    private static ObjectMapper mapper; // Do not use directly. Use getMapper() to be safe from null pointers.

    public CredentialsUtils() {
        logger.debug("Instantiating CredentialsUtils...");
    }

    @Autowired(required = false)
    private ICredentialsDeserializer deserializer;

    @Value("${middleware.logging.debug.obfuscate-credentials:true}")
    private boolean obfuscateCredentialsForDebugLogging;

    /**
     * Attempts to deserialize the given String into a credentials object. If
     * the given string value is {@code null} or empty, {@code null} is
     * returned.
     * <p>
     * Otherwise, if an implementation of the ICredentialsService interface is
     * given and discerevd as a spring bean, that one will be autowired and used
     * for deserialization. Using this mechanism, the particular type of the
     * credentials object can be controlled.
     * <p>
     * Last but not least, if such a bean is not implemented, the string is
     * tried to be deserialized without knowledge of the underlying structure.
     *
     * @param <C>
     * @param credentials
     * @return
     */
    public <C extends AbstractCredentials> C fromString(String credentials) {
        if (credentials == null || credentials.isEmpty()) {
            return null;
        }

        C creds;

        if (deserializer != null) {
            creds = (C) deserializer.fromString(credentials);
        } else {
            creds = (C) toCredentials(credentials, AbstractCredentials.class);
        }

        if (logger.isDebugEnabled()) {
            debugOutputCredentials(creds);
        }

        return creds;
    }

    /**
     * This method outputs the (obfuscated) credentials sent with a request. It
     * is intended to be used for debugging purposes.
     * <p>
     * To activate this function, set {@code efs.middleware.debug-credentials}
     * (default=false) in application properties to true. Additionally the log
     * level must be set to {@code DEBUG}, otherwise the output will not be
     * logged.
     *
     * @param creds
     */
    private void debugOutputCredentials(AbstractCredentials creds) {

        if (creds == null) {
            logger.debug("Credentials are null.");
            return;
        }

        StringBuilder sb = new StringBuilder();

        sb.append("Parsed credentials with following values: ");

        var cl = creds.getClass();
        var fields = cl.getDeclaredFields();

        int count = 0;
        for (Field field : fields) {
            field.setAccessible(true);

            sb.append(field.getName());

            try {
                var value = field.get(creds);

                if (obfuscateCredentialsForDebugLogging) {
                    value = obfuscate(value);
                }

                sb.append("=").append(value);
            } catch (IllegalAccessException ex) {
                sb.append("->IllegalAccessException");
            } catch (IllegalArgumentException ex) {
                sb.append("->IllegalArgumentException");
            }

            sb.append(", ");

            field.setAccessible(false);

            ++count;
        }

        String output;

        if (count == 0) {
            sb.append("(no values parsed)");
            output = sb.toString();
        } else {
            output = sb.toString();

            if (output.endsWith(", ")) {
                output = output.substring(0, output.lastIndexOf(","));
            }
        }

        logger.debug(output);
    }

    /**
     * Can be used to obfuscate the given string value.
     * <ul>
     * <li>{@code null} is rendered to {@code "null"}</li>
     * <li>Empty string is rendered to {@code "\"\""}</li>
     * <li>Everything else is rendered to {@code "***"}</li>
     * </ul>
     *
     * @param any
     * @return
     */
    public static String obfuscate(Object any) {
        if (any == null) {
            return "null";
        }

        if (any.toString().isEmpty()) {
            return "\"\"";
        }

        return "***";
    }

    /**
     * Allows setting a custom ObjectMapper.
     *
     * @param mapper
     */
    public static void configure(ObjectMapper mapper) {
        CredentialsUtils.mapper = mapper;
    }

    private static ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }

        return mapper;
    }

    /**
     * Converts the given map to a credential-object of wished type.
     *
     * @param credentials credential information as Map
     * @param credentialType return type
     * @param <T> The type for the credentials object
     * @return credential-object as wished or null
     */
    public static <T> T toCredentials(Map<String, String> credentials, Class<T> credentialType) {
        if (credentials == null || credentialType == null) {
            return null;
        }

        try {
            return getMapper().convertValue(credentials, credentialType);
        } catch (IllegalArgumentException e) {
            logger.error("Credential-Map could not be converted to Type {}", credentialType, e);
        }
        return null;
    }

    /**
     * Converts the given string to a credential-object of wished type.
     *
     * @param <T>
     * @param credentials credential information as JSON content String
     * @param credentialType return type
     * @return credential-object as wished or null
     */
    public static <T> T toCredentials(String credentials, Class<T> credentialType) {
        if (!StringUtils.hasText(credentials) || credentialType == null) {
            return null;
        }

        try {
            return getMapper().readValue(credentials, credentialType);
        } catch (JsonProcessingException e) {
            logger.error("Credential-String could not be converted to Type {}", credentialType, e);
        }
        return null;
    }

    /**
     * Converts the given credential-object to a JSON content String
     *
     * @param credentialObject The credential-object
     * @return JSON content String or null
     */
    public static String toJsonString(Object credentialObject) {
        if (credentialObject == null) {
            return null;
        }

        try {
            return getMapper().writeValueAsString(credentialObject);
        } catch (JsonProcessingException e) {
            logger.error("Credential Object could not be converted to String", e);
        }
        return null;
    }

    /**
     * Converts the given string to a Map
     *
     * @param credentials credential information as JSON content String
     * @return credential information as Map or null
     */
    public static Map<String, String> toMap(String credentials) {
        if (!StringUtils.hasText(credentials)) {
            return null;
        }

        try {
            return getMapper().readValue(credentials, new TypeReference<Map<String, String>>() {
            });
        } catch (JsonProcessingException e) {
            logger.error("Credential-String could not be converted to Map", e);
        }
        return null;
    }

    /**
     * Converts the given credential-object to a Map that can be used as header
     * parameter
     *
     * @param credentialObject The credential-object
     * @return Map with "x-credentials" as key and JSON content String of
     * credential-object as value
     */
    public static Map<String, String> toHttpHeaderElement(Object credentialObject) {
        if (credentialObject == null) {
            return null;
        }

        Map<String, String> headerElement = new HashMap<>();
        headerElement.put(EfsRequest.CREDENTIALS_HEADER_NAME, toJsonString(credentialObject));
        return headerElement;
    }
}
