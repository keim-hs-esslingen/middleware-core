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

import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.hsesslingen.keim.efs.middleware.config.actuator.HealthCheckContributor;
import de.hsesslingen.keim.efs.middleware.config.actuator.ServiceInfoContributor;

/**
 * AutoConfiguration for HealthCheck and InfoContributor
 * 
 * @author k.sivarasah
 * 10 Oct 2019
 */

@Configuration
@AutoConfigureAfter(MiddlewareAutoConfiguration.class)
@ConditionalOnClass(HealthIndicator.class)
public class MiddlewareActuatorAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public InfoContributor serviceInfoContributor() {
		return new ServiceInfoContributor();
	}
	
	@Bean
	@ConditionalOnMissingBean
	public HealthIndicator healthCheckContributor() {
		return new HealthCheckContributor();
	}
}
