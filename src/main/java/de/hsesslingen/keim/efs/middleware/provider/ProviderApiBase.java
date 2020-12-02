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
import java.util.Collection;
import java.util.function.Supplier;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Used as a base class for provider APIs providing some commonly used methods.
 *
 * @author keim
 * @param <C> The type of credentials, this API expects.
 */
public abstract class ProviderApiBase<C extends AbstractCredentials> extends ApiBase {

    protected final Logger logger = getLogger(getClass());

    @Autowired(required = false)
    private ICredentialsDeserializer<C> deserializer;

    /**
     * Attempts to deserialize the given token into a credentials object.If the
     * given string value is {@code null} or empty, {@code null} is returned.<p>
     * Otherwise, if an implementation of the {@link ICredentialsDeserializer}
     * interface available as a spring bean, that one will be autowired and used
     * for deserialization. Using this mechanism, the particular type of the
     * credentials object can be controlled.
     * <p>
     * Last but not least, if such a bean is not implemented, the string is
     * tried to be deserialized without knowledge of the underlying structure.
     *
     * @param token
     * @return
     */
    protected C parseToken(String token) {
        C result = null;

        if (deserializer != null) {
            result = deserializer.parseToken(token);
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

    /**
     * Generic method to log input params. Suppliers for the second arg can be
     * conveniently created using {@link array(Object[])}.
     * <p>
     * Use as follows:
     * {@code logIncoming("doing something", () -> array("argA", argA, "argB", argB, ...))}.
     * <p>
     * Make sure to obfuscate sensitive values using {@link obfuscate(Object)}
     * or {@link obfuscateConditional(Object)}.
     * <p>
     * The method name is always logged in the following schema: "Received
     * ${methodName}-request."
     * <p>
     * The variables are only logged, and the supplier therefore only called, if
     * log level is set to DEBUG.
     *
     * @param methodName The name of the method whose params should be logged.
     * @param variablesAndValuesSupplier A function returning an array of
     * objects that contains the variable names and values of the logged method
     * pairwise.
     */
    protected void logParams(String methodName, Supplier<Object[]> variablesAndValuesSupplier) {
        logger.info("Received " + methodName + "-request.");

        if (logger.isDebugEnabled() && variablesAndValuesSupplier != null) {

            var variablesAndValues = variablesAndValuesSupplier.get();

            if (variablesAndValues != null) {
                var sb = new StringBuilder("Params of this request:\n");

                boolean isVariable = true;

                for (var v : variablesAndValues) {
                    sb.append(v != null ? v : "null");

                    if (isVariable) {
                        sb.append("=");
                    } else {
                        sb.append("\n");
                    }

                    isVariable = !isVariable;
                }

                if (!isVariable) {
                    // This means we had an uneven number of objects in variablesAndValues
                    logger.warn("Provided uneven number of variables and values. Please contact the developers of this library and tell them that this warning occured in {} while logging params of method {}.", getClass().getName(), methodName);
                }

                logger.debug(sb.toString());
            }
        }
    }

    /**
     * Generic method to log input params.Suppliers for the thrid arg can be
     * conveniently created using {@link #array(Object[])}
     * .<p>
     * Use as follows:
     * {@code logIncoming("doing something", bodyObj, () -> array("argA", argA, "argB", argB, ...))}.
     * <p>
     * Make sure to obfuscate sensitive values using {@link obfuscate(Object)}
     * or {@link obfuscateConditional(Object)}.
     * <p>
     * Request bodies are only logged if log level is TRACE.
     *
     * @param methodName The name of the method whose params should be logged.
     * @param body The parsed body object of the request.
     * @param variablesAndValuesSupplier A function returning an array of
     * objects that contains the variable names and values of the logged method
     * pairwise.
     */
    protected void logParamsWithBody(String methodName, Object body, Supplier<Object[]> variablesAndValuesSupplier) {
        logParams(methodName, variablesAndValuesSupplier);

        if (logger.isTraceEnabled()) {
            logger.trace("Body of this request:\n{}", stringify(body));
        }
    }

    /**
     * Logs the given result object as JSON string if TRACE logging is enabled.
     * <p>
     * Oowever, if the given result object is an instance of {@link Collection},
     * the size of the collection will be logged in level DEBUG.
     *
     * @param result
     */
    protected void logResult(Object result) {
        if (logger.isDebugEnabled() && result instanceof Collection) {
            logger.debug("Responding with a list of size {}.", ((Collection) result).size());
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Responding with following value:\n{}", stringify(result));
        }
    }

    /**
     * Simply returns the given varrgs array. Intended to be used together with
     * {@link #logParams(String, Supplier)}
     *
     *
     * @param variablesAndValues
     * @return
     */
    protected static Object[] array(Object... variablesAndValues) {
        return variablesAndValues;
    }

}
