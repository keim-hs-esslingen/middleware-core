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
package de.hsesslingen.keim.efs.middleware.provider.credentials;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * This class holds values that describe some details of a user as associated
 * with a particular provider.
 * <p>
 * By now there is only one relevant property, {@link userId}. This is the ID
 * that the provider by which this provider known this user. It must be used for
 * creating tokens.
 *
 * @author ben
 */
@Data
@Accessors(chain = true)
public class UserDetails {

    /**
     * The ID of the mobility service that these user details belong to.
     */
    private String serviceId;

    /**
     * The ID of the customer as it should be used for generating tokens. This
     * might be the ID that was provided by the consumer, when registering this
     * user. It also might be a different ID, that was assigned automatically by
     * the provider.
     */
    private String userId;

}
