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
import de.hsesslingen.keim.efs.middleware.model.Customer;
import de.hsesslingen.keim.efs.middleware.provider.credentials.UserDetails;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
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
@ConditionalOnBean(IUsersService.class)
@Api(tags = {SwaggerAutoConfiguration.USERS_API_TAG})
public class UsersApi extends ProviderApiBase implements IUsersApi {

    private static final Logger logger = getLogger(UsersApi.class);

    @Autowired
    private IUsersService usersService;

    @Override
    public UserDetails registerUser(Customer customer, String secret, String superUserToken) {
        logger.info("Received register-user request.");

        //<editor-fold defaultstate="collapsed" desc="Debug-logging input params.">
        logger.debug(
                "Params of this request:\ncustomer={}\nsecret={}\nsuperUserToken={}",
                obfuscateConditional(stringify(customer)),
                obfuscateConditional(secret),
                obfuscateConditional(superUserToken)
        );
        //</editor-fold>

        var result = usersService.registerUser(customer, secret, parseToken(superUserToken));

        //<editor-fold defaultstate="collapsed" desc="Debug-logging output.">
        logger.debug("Responding with the following result: {}", result);
        //</editor-fold>

        return result;
    }
}
