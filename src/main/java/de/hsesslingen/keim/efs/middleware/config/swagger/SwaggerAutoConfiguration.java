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
package de.hsesslingen.keim.efs.middleware.config.swagger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Default Configuration for SwaggerUI to expose REST-APIs implemented in the
 * middleware-core library.
 *
 * @author k.sivarasah
 * @author b.oesch 10 Oct 2019
 */
@Configuration
@ConditionalOnClass(Docket.class)
public class SwaggerAutoConfiguration {

    public static final String OPTIONS_API_TAG = "Options Api";
    public static final String BOOKING_API_TAG = "Booking Api";
    public static final String CREDENTIALS_API_TAG = "Credentials Api";
    public static final String CONSUMER_API_TAG = "Consumer Api";
    public static final String FLEX_DATETIME_DESC = "Date time value in a flexible format. "
            + "(Epoch millis, ISO zoned date time, ISO local date time, ISO date only, "
            + "ISO time only, ISO time only with offest e.g. +01:00, ... "
            + "Mising information is added, by simple assumptions. "
            + "If only time is given, \"today\" is added as date. "
            + "If a time zone is missing, the system default zone is used, ...)";

    @Value("${spring.application.name:}")
    private String serviceName;

    @Value("${efs.middleware.options-api.enabled:false}")
    private boolean optionsEnabled;

    @Value("${efs.middleware.booking-api.enabled:false}")
    private boolean bookingEnabled;

    @Value("${efs.middleware.credentials-api.enabled:false}")
    private boolean credentialsEnabled;

    @Value("${efs.middleware.consumer-api.enabled:false}")
    private boolean consumerEnabled;

    @Bean
    @ConditionalOnMissingBean
    public Docket api() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(or(
                        RequestHandlerSelectors.basePackage("de.hsesslingen.keim.efs.middleware.provider"),
                        RequestHandlerSelectors.basePackage("de.hsesslingen.keim.efs.middleware.consumer"),
                        RequestHandlerSelectors.basePackage("de.hsesslingen.keim.efs.adapter")
                ))
                .paths(PathSelectors.any())
                .build();

        setTags(docket);
        docket.apiInfo(apiInfo());

        return docket;
    }

    private void setTags(Docket docket) {
        var tags = new ArrayList<Tag>(5);

        if (optionsEnabled) {
            tags.add(new Tag(OPTIONS_API_TAG, "Options related API forsearching mobility options.", 1));
        }

        if (bookingEnabled) {
            tags.add(new Tag(BOOKING_API_TAG, "Booking related APIs with CRUD functionality.", 2));
        }

        if (credentialsEnabled) {
            tags.add(new Tag(CREDENTIALS_API_TAG, "Credentials related APIs for managing with credentials of remote APIs.", 3));
        }

        if (consumerEnabled) {
            tags.add(new Tag(CONSUMER_API_TAG, "APIs provided for consuming mobility services.", 4));
        }

        if (!tags.isEmpty()) {
            if (tags.size() > 1) {
                docket.tags(tags.get(0), tags.subList(1, tags.size()).toArray(new Tag[0]));
            } else {
                docket.tags(tags.get(0));
            }
        }
    }

    private ApiInfo apiInfo() {
        String serviceInfo = String.format("Middleware Service (%s)", serviceName);
        return new ApiInfo(serviceInfo,
                "API description of " + serviceInfo, "V1.1", null,
                new Contact("Hochschule Esslingen", "https://www.hs-esslingen.de", null),
                null, null, Collections.emptyList());
    }

    private static <T> Predicate<T> or(Predicate... predicates) {
        return t -> {
            for (var predicate : predicates) {
                if (predicate.test(t)) {
                    return true;
                }
            }

            return false;
        };
    }

}
