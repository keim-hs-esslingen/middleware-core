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
import de.hsesslingen.keim.efs.middleware.model.Customer;
import de.hsesslingen.keim.efs.mobility.exception.AbstractEfsException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 *
 * @author ben
 * @param <C>
 */
public interface ICredentialsService<C extends AbstractCredentials> {

    /**
     * Allows to create a login token associated to the provided credentials.
     * This might be necessary for some providers, but not for all.
     *
     * @param credentials Credential data in provider specific format.
     * @return Login token data in a provider specific format.
     */
    public @NonNull
    String createLoginToken(@NonNull C credentials) throws AbstractEfsException;

    /**
     * Logs out (invalidates) the given login-token.
     *
     * @param credentials Provider specific credentials.
     * @return true if the logout was successful. false if some error occured.
     * @throws AbstractEfsException
     */
    public boolean deleteLoginToken(@NonNull C credentials) throws AbstractEfsException;

    /**
     * Registers a new user at this service.
     *
     * @param credentials Credential data in provider specific format.
     * @param userData Data about the user that might be required by some
     * providers.
     * @return Credentials data in a provider specific format.
     */
    public @NonNull
    String registerUser(@NonNull C credentials, @Nullable Customer userData) throws AbstractEfsException;

    /**
     * Checks whether the given credentials are still valid and can be used for
     * booking purposes etc.
     *
     * @param credentials Provider specific credentials.
     * @return true if valid, false if not.
     * @throws AbstractEfsException
     */
    public boolean checkCredentialsAreValid(@NonNull C credentials) throws AbstractEfsException;

}
