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
package de.hsesslingen.keim.efs.middleware.provider;

import de.hsesslingen.keim.efs.middleware.common.ApiBase;
import de.hsesslingen.keim.efs.middleware.provider.credentials.AbstractCredentials;
import de.hsesslingen.keim.efs.middleware.provider.credentials.ICredentialsDeserializer;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Used as a base class for provider APIs providing some commonly used methods.
 *
 * @author keim
 * @param <C> The type of credentials, this API expects.
 */
public abstract class ProviderApiBase<C extends AbstractCredentials> extends ApiBase {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired(required = false)
    private ICredentialsDeserializer<C> deserializer;

    /**
     * Attempts to deserialize the given String into a credentials object.If the
     * given string value is {@code null} or empty, {@code null} is returned.<p>
     * Otherwise, if an implementation of the ICredentialsService interface is
     * given and discerevd as a spring bean, that one will be autowired and used
     * for deserialization. Using this mechanism, the particular type of the
     * credentials object can be controlled.
     * <p>
     * Last but not least, if such a bean is not implemented, the string is
     * tried to be deserialized without knowledge of the underlying structure.
     *
     * @param credentials
     * @param token
     * @return
     */
    protected C parseCredentials(String credentials, String token) {
        C result = null;

        if (deserializer != null) {
            if (isNotEmpty(token)) {
                result = deserializer.parseToken(token);
            } else if (isNotEmpty(credentials)) {
                result = deserializer.parseCredentials(credentials);
            }
        }

        if (result == null) {
            logger.debug("No credentials provided by client.");
        } else if (logger.isDebugEnabled()) {
            debugOutputCredentials(result);
        }

        return result;
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
            logger.debug("Credentials object is null.");
            return;
        }

        var sb = new StringBuilder();

        sb.append("Parsed credentials with following values: ");

        var cl = creds.getClass();
        var fields = cl.getDeclaredFields();

        int count = 0;

        for (var field : fields) {
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

}
