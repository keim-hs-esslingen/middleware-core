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
package de.hsesslingen.keim.efs.middleware.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.hsesslingen.keim.efs.middleware.provider.credentials.DefaultCredentialsFactory;
import de.hsesslingen.keim.efs.middleware.consumer.ConsumerService;
import de.hsesslingen.keim.efs.middleware.common.ServiceDirectoryProxy;
import de.hsesslingen.keim.efs.middleware.provider.credentials.ICredentialsFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Lazy;

/**
 * AutoConfiguration class that creates necessary Beans for EFS-Middleware
 * conditionally
 *
 * @author k.sivarasah 27 Sep 2019
 * @author b.oesch
 */
@Lazy // Initialize the beans in this class only when they are needed.
@Configuration
@AutoConfigureAfter(RestUtilsAutoConfiguration.class)
public class MiddlewareAutoConfiguration {

    private static final Log log = LogFactory.getLog(MiddlewareAutoConfiguration.class);

    /**
     * The ServiceDirectoryProxy is used by the following components:
     * <ul>
     * <li>{@link ProviderRegistrator}: To register the configured mobility
     * service.</li>
     * <li>{@link ConsumerService}: To query the service directory for available
     * services.</li>
     * </ul>
     * <p>
     * Apart from that it can also be autowired directly by user-created.
     * components.
     *
     * @return
     */
    @Bean
    public ServiceDirectoryProxy serviceDirectoryProxy() {
        log.info("Initializing ServiceDirectoryProxy bean...");
        return new ServiceDirectoryProxy();
    }

    /**
     * The ConsumerService is only needed if either the ConsumerApi is enabled,
     * or if the user wants to use the ConsumerService directly by autowiring it
     * into his own components. If a user soley acts as a provider, the
     * ConsumerService is not needed at all.
     *
     * @return
     */
    @Bean
    public ConsumerService consumerService() {
        log.info("Initializing ConsumerService bean...");
        return new ConsumerService();
    }

    /**
     * The MobilityServiceConfigurationProperties contain information about the
     * service that is provided by this middleware user. Therefore this Bean is
     * not needed if the user soley acts as a consumer.
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public IMobilityServiceConfigurationProperties mobilityServiceConfigurationProperties() {
        log.info("Initializing MobilityServiceConfigurationProperties bean...");
        return new MobilityServiceConfigurationProperties();
    }

    /**
     * CredentialsFactories are only needed if the user s acts as a provider.
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public ICredentialsFactory credentialsFactory() {
        log.info("Initializing DefaultCredentialsFactory bean...");
        return new DefaultCredentialsFactory();
    }

}
