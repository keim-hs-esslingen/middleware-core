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

import de.hsesslingen.keim.efs.middleware.provider.credentials.TokenCredentials;
import de.hsesslingen.keim.efs.mobility.exception.AbstractEfsException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author keim
 */
public interface ICredentialsApi {

    public static final String USER_ID_HEADER = "X-User-ID";
    public static final String USER_ID_DESCRIPTION = "A string valud that uniquely identifies a user. (e.g. an email adress, a username, ...)";
    public static final String SECRET_HEADER = "X-Secret";
    public static final String SECRET_DESCRIPTION = "The secret that authenticates a user.";
    public static final String TOKEN_HEADER = "X-Token";
    public static final String TOKEN_DESCRIPTION = "A token that identifies and authenticates a user, sometimes with a limited duration of validity.";

    /**
     * Allows creation of tokens based on the given user-id and secret.The
     * content of this token is provider specific and can be used as is.
     *
     * @param userId A string valud that uniquely identifies a user. (e.g. an
     * email adress, a username, ...)
     * @param secret The secret that authenticates a user.
     * @return An instance of TokenCredentials that contains a provider specific
     * token, which can be used as is.
     */
    @PostMapping("/credentials/token")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Create tokens.", notes = "Allows creation of tokens based on the given user-id and secret. The content of this token is provider specific and can be used as is.")
    public TokenCredentials createToken(
            @RequestHeader(name = USER_ID_HEADER, required = false) @ApiParam(USER_ID_DESCRIPTION) String userId,
            @RequestHeader(name = SECRET_HEADER) @ApiParam(SECRET_DESCRIPTION) String secret
    );

    /**
     * Invalidates (e.g. logs out) the given token.
     *
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity.
     * @throws AbstractEfsException
     */
    @DeleteMapping("/credentials/token")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Invalidate tokens.", notes = "Invalidates (e.g. logs out) the given token.")
    public void deleteToken(
            @RequestHeader(name = TOKEN_HEADER) @ApiParam(TOKEN_DESCRIPTION) String token
    );

    /**
     * This endpoint can be used to check whether the given token is still
     * valid. If the token is valid, "true" should be returned, if invalid,
     * "false" should be returned.
     *
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity.
     * @return true if valid, false if not.
     * @throws AbstractEfsException
     */
    @GetMapping("/credentials/token")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Check validity of credentials.", notes = "Checks whether the given token is still valid.")
    public boolean isTokenValid(
            @RequestHeader(name = TOKEN_HEADER) @ApiParam(TOKEN_DESCRIPTION) String token
    );

}
