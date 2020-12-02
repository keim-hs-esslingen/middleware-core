
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

import de.hsesslingen.keim.efs.middleware.config.SwaggerAutoConfiguration;
import de.hsesslingen.keim.efs.middleware.provider.credentials.TokenCredentials;
import static de.hsesslingen.keim.efs.mobility.exception.HttpException.internalServerError;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author ben
 */
@Validated
@RestController
@ConditionalOnBean(ICredentialsService.class)
@Api(tags = {SwaggerAutoConfiguration.CREDENTIALS_API_TAG})
public class CredentialsApi extends ApiBase implements ICredentialsApi {

    @Autowired
    private ICredentialsService service;

    @Override
    public TokenCredentials createToken(String userId, String secret) {
        logParams("createToken", () -> array(
                "userId", obfuscateConditional(userId),
                "secret", obfuscateConditional(secret)
        ));

        var token = service.createToken(userId, secret);

        //<editor-fold defaultstate="collapsed" desc="Checking output and doing debug-logging.">
        if (token == null) {
            logger.error(
                    "Your implementation of ICredentialsService (class \"{}\") returned null upon calling the \"createToken\" method. This is a violation of the ICredentialsService interface contract. If you cannot create a token, throw a meaningful exception instead, but be aware to not reveal too much information about the underlying credentials.",
                    service.getClass()
            );

            throw internalServerError("An error occured when trying to create a token with the given userId and secret.");
        } else {
            logger.debug(
                    "Responding with the following token:\ntoken={}\nvalidUntil={}",
                    obfuscateConditional(token.getToken()),
                    token.getValidUntil()
            );
        }
        //</editor-fold>

        return token;
    }

    @Override
    public void deleteToken(String token) {
        logParams("deleteToken", () -> array(
                "token", obfuscateConditional(token))
        );

        service.deleteToken(token);
    }

    @Override
    public boolean isTokenValid(String token) {
        logParams("isTokenValid", () -> array(
                "token", obfuscateConditional(token))
        );

        var result = service.isTokenValid(token);

        logResult(result);

        return result;
    }

}
