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

import de.hsesslingen.keim.efs.middleware.model.Asset;
import de.hsesslingen.keim.efs.middleware.model.Leg;

import de.hsesslingen.keim.efs.middleware.provider.credentials.AbstractCredentials;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * This interface represents the API, that the assets-rest-controller uses to
 * perform the possible actions provided by the asset-API. That API will only be
 * available if this interface is implemented and provided as a spring bean.
 *
 * @author boesch
 * @param <C>
 */
public interface IAssetsService<C extends AbstractCredentials> {

    /**
     * Returns information about assets. The {@link assetId} can be found in leg
     * objects. If you query the information about an asset using this endpoint,
     * all properties of class {@link Asset} that are applicable should be
     * populated with their respective value. Therefore if the value of a
     * property in the returned object is null, this most probably means that
     * the provider considers this property to not be applicable to this asset.
     *
     * @param assetId The ID of the asset which shall be retrieved, which can be
     * found in other objects e.g. of type {@link Leg}.
     * @param credentials The credentials needed to authenticate and authorize
     * oneself to perform this action.
     * @return
     */
    @Nullable
    public Asset getAssetById(@NonNull String assetId, C credentials);

}
