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

import de.hsesslingen.keim.efs.middleware.config.swagger.SwaggerAutoConfiguration;
import de.hsesslingen.keim.efs.middleware.model.Customer;
import de.hsesslingen.keim.efs.middleware.provider.credentials.CredentialsUtils;
import de.hsesslingen.keim.efs.mobility.exception.AbstractEfsException;
import static de.hsesslingen.keim.efs.mobility.utils.EfsRequest.CREDENTIALS_HEADER_DESC;
import static de.hsesslingen.keim.efs.mobility.utils.EfsRequest.CREDENTIALS_HEADER_NAME;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author ben
 */
@Validated
@RestController
@ConditionalOnBean(ICredentialsService.class)
@Api(tags = {SwaggerAutoConfiguration.CREDENTIALS_API_TAG})
public class CredentialsApi {

    @Autowired
    private ICredentialsService credentialsService;

    @Autowired
    private CredentialsUtils credentialsUtils;

    /**
     * Allows to create a login token associated to the provided credentials.
     * This might be necessary for some providers, but not for all.
     *
     * @param credentials Credential data as json content string
     * @return Login token data in a provider specific format.
     */
    @PostMapping("/credentials/login-token")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Create login token", notes = "Allows to create a login-token associated to the provided credentials. This might be necessary for some providers, but not for all.")
    public String createLoginToken(
            @RequestHeader(name = CREDENTIALS_HEADER_NAME, required = true) @ApiParam(CREDENTIALS_HEADER_DESC) String credentials
    ) {
        return credentialsService.createLoginToken(credentialsUtils.fromString(credentials));
    }

    /**
     * Logs out (invalidates) the given login-token.
     *
     * @param credentials Provider specific credentials.
     * @return true if the logout was successful. false if some error occured.
     * @throws AbstractEfsException
     */
    @DeleteMapping("/credentials/login-token")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Log out user", notes = "Logs out (invalidates) the given login-token.")
    public boolean deleteLoginToken(
            @RequestHeader(name = CREDENTIALS_HEADER_NAME, required = true) @ApiParam(CREDENTIALS_HEADER_DESC) String credentials
    ) {
        return credentialsService.deleteLoginToken(credentialsUtils.fromString(credentials));
    }

    /**
     * Registers a new user at this service.
     *
     * @param credentials Credential data as json content string
     * @param userData Data about the user that might be required by some
     * providers.
     * @return Credentials data in a provider specific format.
     */
    @PostMapping("/credentials/user")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Register a new user", notes = "Registers a new user at this service.")
    public String registerUser(
            @RequestHeader(name = CREDENTIALS_HEADER_NAME, required = true) @ApiParam(CREDENTIALS_HEADER_DESC) String credentials,
            @RequestBody(required = false) @ApiParam("Possibly required extra data about the new user.") Customer userData
    ) {
        return credentialsService.registerUser(credentialsUtils.fromString(credentials), userData);
    }

    /**
     * Checks whether the given credentials are still valid and can be used for
     * booking purposes etc.
     *
     * @param credentials Provider specific credentials.
     * @return true if valid, false if not.
     * @throws AbstractEfsException
     */
    @GetMapping("/credentials/check")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Check validity of credentials.", notes = "Checks whether the given credentials are still valid and can be used for booking purposes etc.")
    public boolean checkCredentialsAreValid(
            @RequestHeader(name = CREDENTIALS_HEADER_NAME, required = true) @ApiParam(CREDENTIALS_HEADER_DESC) String credentials
    ) {
        return credentialsService.checkCredentialsAreValid(credentialsUtils.fromString(credentials));
    }

}
