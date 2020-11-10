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
package conditionalbeans;

import de.hsesslingen.keim.efs.middleware.consumer.ServiceDirectoryProxy;
import de.hsesslingen.keim.efs.middleware.provider.CredentialsApi;
import de.hsesslingen.keim.efs.middleware.provider.IAssetsApi;
import de.hsesslingen.keim.efs.middleware.provider.IAssetsService;
import de.hsesslingen.keim.efs.middleware.provider.IBookingApi;
import de.hsesslingen.keim.efs.middleware.provider.IBookingService;
import de.hsesslingen.keim.efs.middleware.provider.ICredentialsService;
import de.hsesslingen.keim.efs.middleware.provider.IOptionsApi;
import de.hsesslingen.keim.efs.middleware.provider.IOptionsService;
import de.hsesslingen.keim.efs.middleware.provider.IPlacesApi;
import de.hsesslingen.keim.efs.middleware.provider.IPlacesService;
import de.hsesslingen.keim.efs.middleware.provider.ServiceInfoApi;
import de.hsesslingen.keim.efs.middleware.provider.credentials.ICredentialsDeserializer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author keim
 */
public abstract class BeansCollector {

    @Autowired(required = false)
    protected IPlacesApi placesApi;
    @Autowired(required = false)
    protected IPlacesService placesService;
    
    @Autowired(required = false)
    protected IAssetsApi assetsApi;
    @Autowired(required = false)
    protected IAssetsService assetsService;

    @Autowired(required = false)
    protected IOptionsApi optionsApi;
    @Autowired(required = false)
    protected IOptionsService optionsService;

    @Autowired(required = false)
    protected IBookingApi bookingApi;
    @Autowired(required = false)
    protected IBookingService bookingService;

    @Autowired(required = false)
    protected CredentialsApi credentialsApi;
    @Autowired(required = false)
    protected ICredentialsService credentialsService;
    @Autowired(required = false)
    protected ICredentialsDeserializer credentialsDeserializer;

    @Autowired(required = false)
    protected ServiceInfoApi serviceInfoApi;

    @Autowired(required = false)
    protected ServiceDirectoryProxy sdProxy;

}
