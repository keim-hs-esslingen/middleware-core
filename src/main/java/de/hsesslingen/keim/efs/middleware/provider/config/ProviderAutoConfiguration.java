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
package de.hsesslingen.keim.efs.middleware.provider.config;

import de.hsesslingen.keim.efs.middleware.config.RestUtilsAutoConfiguration;
import de.hsesslingen.keim.efs.middleware.provider.credentials.ICredentialsFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 *
 * @author keim
 */
@Lazy // Initialize the beans in this class only when they are needed.
@Configuration
@AutoConfigureAfter(RestUtilsAutoConfiguration.class)
public class ProviderAutoConfiguration {

    private static final Log log = LogFactory.getLog(ProviderAutoConfiguration.class);

    // Not marked directly as @Service in class because this bean should be
    // initialized AFTER RestUtilsAutoConfiguration took place.
    @Bean
    @ConditionalOnMissingBean
    public IMobilityServiceConfigurationProperties mobilityServiceConfigurationProperties() {
        log.info("Initializing MobilityServiceConfigurationProperties bean...");
        return new MobilityServiceConfigurationProperties();
    }

    // Not marked directly as @Service in class because this bean should be
    // initialized AFTER RestUtilsAutoConfiguration took place.
    @Bean
    @ConditionalOnMissingBean
    public ICredentialsFactory credentialsFactory() {
        log.info("Initializing DefaultCredentialsFactory bean...");
        return new DefaultCredentialsFactory();
    }
}