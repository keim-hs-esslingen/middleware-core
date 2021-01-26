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
import de.hsesslingen.keim.efs.middleware.model.Option;
import static de.hsesslingen.keim.efs.middleware.provider.ITokensApi.TOKEN_DESCRIPTION;
import de.hsesslingen.keim.efs.mobility.config.EfsSwaggerApiResponseSupport;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.requests.MiddlewareRequest;
import static de.hsesslingen.keim.efs.mobility.requests.MiddlewareRequest.TOKEN_HEADER;
import de.hsesslingen.keim.efs.mobility.requests.MiddlewareRequestTemplate;
import io.swagger.annotations.ApiParam;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This API serves for querying a mobility service provider for mobility
 * {@link Option}. These options can be understood as possibilities for future
 * bookings.
 * <p>
 * <h3>Additional note:</h3>
 * This interface also provides static methods for building HTTP requests, that
 * match the endpoints defined in it. They are build upon the
 * {@link MiddlewareRequest} class.
 *
 * @author keim
 */
@EfsSwaggerApiResponseSupport
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public interface IAssetsApi {

    public static final String PATH = "/assets";

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
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ITokensApi} for more
     * details on tokens. Most providers do not require a token for querying
     * options using the {@link IAssetApi}.
     * @return
     */
    @GetMapping(PATH + "/{assetId}")
    @ResponseStatus(HttpStatus.OK)
    public Asset getAssetById(
            @ApiParam("The ID of the asset which shall be retrieved, which can be found in other objects, e.g. in Legs.")
            @PathVariable String assetId,
            //
            @ApiParam(value = TOKEN_DESCRIPTION)
            @RequestHeader(name = TOKEN_HEADER, required = false) String token
    );

    /**
     * Assembles a request, matching the {@code GET /asset/{id}} endpoint, for
     * the service with the given url using the given token. See
     * {@link IAssetsApi#getAssetById(String, String)} for JavaDoc on that
     * endpoint.<p>
     * The params are checked for null values and added only if they are present
     * and sensible.
     * <p>
     * The returned request can be send using {@code request.go()} which will
     * return a {@link ResponseEntity}.
     *
     * @param serviceUrl The base url of the mobility service that should be
     * queried. Use {@link MobilityService#getServiceUrl()} to get this url.
     * @param assetId The ID of the asset which shall be retrieved.
     * @param token A token that identifies and authenticates a user, sometimes
     * with a limited duration of validity. See {@link ITokensApi} for more
     * details on tokens. Most providers do not require a token for querying
     * options using the {@link IAssetApi}.
     * @param requestTemplate The template that should be used as foundation for
     * building the request.
     * @return
     */
    public static MiddlewareRequest<Asset> buildGetAssetByIdRequest(
            String serviceUrl,
            String assetId,
            String token,
            MiddlewareRequestTemplate requestTemplate
    ) {
        var request = requestTemplate.get(serviceUrl + PATH + "/" + assetId)
                .expect(Asset.class);

        if (isNotBlank(token)) {
            request.token(token);
        }

        return request;
    }

}
