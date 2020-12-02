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

import de.hsesslingen.keim.efs.middleware.model.Customer;
import de.hsesslingen.keim.efs.middleware.provider.credentials.AbstractCredentials;
import de.hsesslingen.keim.efs.middleware.provider.credentials.UserDetails;

/**
 *
 * @author ben
 * @param <C>
 */
public interface IUsersService<C extends AbstractCredentials> {

    /**
     * Can be used to register new users at this provider.Not all providers
     * support this feature.Use the Service-Info-API to obtain information about
     * which endpoints are supported and which are not.
     *
     * @param customer Data about the user that should be created. The minimum
     * information that must be present differs from provider to provider. Use
     * the Service-Info-API to know thich values must be present in this object.
     * @param secret The secret that should be used for authenticating this user
     * later on.
     * @param superUserCredentials Valid credentials of an optional super user
     * account, that the newly registered user should be associated with, i.e.
     * added to. This value is optional because not all providers require users
     * to belong to super users. These credentials are not to be confused with
     * the credentials of the user that is about to be created.
     * @return An object that contains the user-id which was set by the provider
     * for the new user and which can be used together with the secret given by
     * the consumer for authentication.
     */
    public UserDetails registerUser(Customer customer, String secret, C superUserCredentials);

}
