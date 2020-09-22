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
package de.hsesslingen.keim.efs.middleware.consumer.config;

import de.hsesslingen.keim.efs.middleware.config.RestUtilsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.hsesslingen.keim.efs.middleware.consumer.ConsumerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;

/**
 * AutoConfiguration class that creates necessary Beans for EFS-Middleware
 * conditionally
 *
 * @author k.sivarasah 27 Sep 2019
 * @author b.oesch
 */
@Configuration
@AutoConfigureAfter(RestUtilsAutoConfiguration.class)
public class ConsumerAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerAutoConfiguration.class);

    // Not marked directly as @Service in class because this bean should be
    // initialized AFTER RestUtilsAutoConfiguration took place.
    @Bean
    @Lazy // Initialize only if needed.
    public ConsumerService consumerService() {
        logger.info("Initializing ConsumerService bean...");
        return new ConsumerService();
    }

}
