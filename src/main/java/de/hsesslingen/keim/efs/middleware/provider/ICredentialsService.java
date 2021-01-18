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

import de.hsesslingen.keim.efs.middleware.provider.credentials.AbstractCredentials;
import de.hsesslingen.keim.efs.middleware.provider.credentials.TokenCredentials;
import java.util.Map;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * This interface represents the API, that the credentials rest controller uses
 * to perform the possible actions provided by the credentials API. That API
 * will only be available if this interface is implemented and provided as a
 * spring bean.
 *
 * @author ben
 * @param <C>
 */
public interface ICredentialsService<C extends AbstractCredentials> {

    /**
     * Allows creation of tokens based on the given user-id and secret.The
     * format of the tokens is intentionally not defined. They represent some
     * chunk of data in the form of a string, that is generated by a mobility
     * service provider and also recognized by it. Tokens are used to identify
     * <em>and</em> authenticate users, very similar to how session tokens are
     * used all over the web.
     * <p>
     * To create a token you need a user-id (e.g. username or email), which
     * represents the users ID at the particular mobility service provider, and
     * the corresponding secret (e.g. password), that is needed for
     * authentication and authorication to obtain a token. The token is
     * generated by the provider and returned to the caller, which can use this
     * token in subsequent requests to any EFS-API until its validity has
     * expired.
     *
     * @param userId A string value that uniquely identifies a user. (e.g. an
     * email adress, a username, ...). This value can be left out if applicable
     * at the particular mobility service provider.
     * @param secret The secret that authenticates the user with the given
     * {@link userId} or identifies <em>and</em> authenticates the user at the
     * same time, if not {@link userId} is applicable for this mobility service
     * provider. Either this or {@link credentials} provides the essential
     * authenticating piece of information. Can be {@code null} if
     * {@link credentials} is provided.
     * @param credentials A collection of secrets needed for authentication. Can
     * be {@code null}, if {@link secret} is provided.
     * @return An instance of TokenCredentials that contains a provider specific
     * token, which can be used as is.
     */
    @NonNull
    public TokenCredentials createToken(
            @Nullable String userId,
            @Nullable String secret,
            @Nullable Map<String, String> credentials
    );

    /**
     * Invalidates (e.g. logs out) the given token.
     *
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity.
     */
    public void deleteToken(String token);

    /**
     * This endpoint can be used to check whether the given token is still
     * valid. If the token is valid, "true" should be returned, if invalid,
     * "false" should be returned.
     *
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity.
     * @return true if valid, false if not.
     */
    public boolean isTokenValid(String token);

}
