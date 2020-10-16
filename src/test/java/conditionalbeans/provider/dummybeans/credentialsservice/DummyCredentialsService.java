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
package conditionalbeans.provider.dummybeans.credentialsservice;

import conditionalbeans.provider.dummybeans.DummyBean;
import de.hsesslingen.keim.efs.middleware.model.Customer;
import de.hsesslingen.keim.efs.middleware.provider.ICredentialsService;
import de.hsesslingen.keim.efs.middleware.provider.credentials.AbstractCredentials;
import de.hsesslingen.keim.efs.mobility.exception.AbstractEfsException;
import org.springframework.stereotype.Component;

/**
 *
 * @author keim
 */
@Component
public class DummyCredentialsService extends DummyBean implements ICredentialsService<AbstractCredentials> {

    @Override
    public String createLoginToken(AbstractCredentials credentials) throws AbstractEfsException {
        throw new UnsupportedOperationException("This should never be called.");
    }

    @Override
    public boolean deleteLoginToken(AbstractCredentials credentials) throws AbstractEfsException {
        throw new UnsupportedOperationException("This should never be called.");
    }

    @Override
    public String registerUser(AbstractCredentials credentials, Customer userData) throws AbstractEfsException {
        throw new UnsupportedOperationException("This should never be called.");
    }

    @Override
    public boolean checkCredentialsAreValid(AbstractCredentials credentials) throws AbstractEfsException {
        throw new UnsupportedOperationException("This should never be called.");
    }

}
