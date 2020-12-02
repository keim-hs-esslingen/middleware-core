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
import static de.hsesslingen.keim.efs.middleware.provider.ICredentialsApi.SECRET_DESCRIPTION;
import static de.hsesslingen.keim.efs.middleware.provider.ICredentialsApi.TOKEN_DESCRIPTION;
import static de.hsesslingen.keim.efs.middleware.provider.ICredentialsApi.USERS_PATH;
import de.hsesslingen.keim.efs.middleware.provider.credentials.UserDetails;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.utils.EfsRequest;
import static de.hsesslingen.keim.efs.mobility.utils.EfsRequest.SECRET_HEADER;
import static de.hsesslingen.keim.efs.mobility.utils.EfsRequest.TOKEN_HEADER;
import io.swagger.annotations.ApiParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 *
 * @author ben
 */
public interface IUsersApi {

    /**
     * Can be used to register new users at this provider. Not all providers
     * support this feature. Use the Service-Info-API to obtain information
     * about whether this feature is supported or not. The relevant flag is
     * {@link MobilityService#supportsUserRegistration}.
     *
     * @param customer Data about the user that should be created. The minimum
     * information that must be present differs from provider to provider. Use
     * the Service-Info-API to know thich values must be present in this object.
     * @param secret The secret that should be used for authenticating this user
     * later on.
     * @param superUserToken A valid token of an optional super user account,
     * that the newly registered user should be associated with, i.e. added to.
     * This value is optional because not all providers require users to belong
     * to super users. This token is not to be confused with the credentials of
     * the user that is about to be created. Therefore the {@link secret} of
     * this new user is also not the same as the one used to create
     * {@link superUserToken}.
     * @return An object that contains the user-id which was set by the provider
     * for the new user and which can be used together with the secret given by
     * the consumer for authentication.
     */
    @PostMapping(USERS_PATH)
    public UserDetails registerUser(
            @RequestBody() Customer customer,
            //
            @ApiParam(SECRET_DESCRIPTION)
            @RequestHeader(name = SECRET_HEADER) String secret,
            //
            @ApiParam(TOKEN_DESCRIPTION)
            @RequestHeader(name = TOKEN_HEADER, required = false) String superUserToken
    );

    /**
     * Assembles a request, matching the {@code POST /credentials/users}
     * endpoint, for the service with the given url.See
     * {@link ICredentialsApi#registerUser(Customer, String)} for JavaDoc on
     * that endpoint.<p>
     * The returned request can be sent using {@code request.go()} which will
     * return a {@link ResponseEntity}.
     *
     *
     * @param serviceUrl The base url of the mobility service that should be
     * queried. Use {@link MobilityService#getServiceUrl()} to get this url.
     * @param customer Data about the user that should be created. The minimum
     * information that must be present differs from provider to provider. Use
     * the Service-Info-API to know thich values must be present in this object.
     * @param secret The secret that should be used for authenticating this user
     * later on.
     * @param superUserToken A valid token of an optional super user account,
     * that the newly registered user should be associated with, i.e. added to.
     * This value is optional because not all providers require users to belong
     * to super users. This token is not to be confused with the credentials of
     * the user that is about to be created. Therefore the {@link secret} of
     * this new user is also not the same as used to create
     * {@link superUserToken}.
     * @return An object that contains the user-id which was set by the provider
     * for the new user and which can be used together with the secret given by
     * the consumer for authentication.
     */
    public static EfsRequest<UserDetails> buildRegisterUserRequest(
            String serviceUrl,
            Customer customer,
            String secret,
            String superUserToken
    ) {
        return EfsRequest.post(serviceUrl + USERS_PATH)
                .expect(UserDetails.class)
                .body(customer)
                .token(superUserToken)
                .secret(secret);
    }

}
