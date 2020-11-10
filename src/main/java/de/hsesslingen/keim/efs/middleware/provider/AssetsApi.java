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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hsesslingen.keim.efs.middleware.config.SwaggerAutoConfiguration;
import de.hsesslingen.keim.efs.middleware.model.Asset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author boesch, K.Sivarasah
 */
@Validated
@RestController
@ConditionalOnBean(IAssetsService.class)
@Api(tags = {SwaggerAutoConfiguration.ASSETS_API_TAG})
//@AutoConfigureAfter(ProviderProperties.class)
public class AssetsApi extends ProviderApiBase implements IAssetsApi {

    private static final Logger logger = getLogger(AssetsApi.class);

    @Autowired
    private IAssetsService assetService;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public Asset getAssetById(
            String assetId,
            String token
    ) {
        logger.info("Received request to get an asset by id.");

        //<editor-fold defaultstate="collapsed" desc="Debug logging input params...">
        logger.debug("Params of this request:\nassetId={}", assetId);
        //</editor-fold>

        var asset = assetService.getAssetById(assetId, parseToken(token));

        //<editor-fold defaultstate="collapsed" desc="Debug logging result object.">
        if (logger.isTraceEnabled()) {
            try {
                logger.debug("Responding with this asset: {}", mapper.writeValueAsString(asset));
            } catch (JsonProcessingException ex) {
                logger.warn("Erro when logging result object.");
            }
        }
        //</editor-fold>

        return asset;
    }

}
