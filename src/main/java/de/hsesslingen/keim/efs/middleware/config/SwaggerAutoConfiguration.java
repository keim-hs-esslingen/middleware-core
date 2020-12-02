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

import de.hsesslingen.keim.efs.middleware.provider.AssetsApi;
import de.hsesslingen.keim.efs.middleware.provider.BookingApi;
import de.hsesslingen.keim.efs.middleware.provider.CredentialsApi;
import de.hsesslingen.keim.efs.middleware.provider.OptionsApi;
import de.hsesslingen.keim.efs.middleware.provider.PlacesApi;
import de.hsesslingen.keim.efs.middleware.provider.ServiceInfoApi;
import de.hsesslingen.keim.efs.middleware.provider.UsersApi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Autowired;

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

    public static final String API_VERSION = "V1.3";

    public static final String PLACES_API_TAG = "Places-API";
    public static final String ASSETS_API_TAG = "Assets-API";
    public static final String OPTIONS_API_TAG = "Options-API";
    public static final String BOOKING_API_TAG = "Booking-API";
    public static final String CREDENTIALS_API_TAG = "Credentials-API";
    public static final String USERS_API_TAG = "Users-API";
    public static final String SERVICE_INFO_API_TAG = "Service-Info-API";

    public static final String FLEX_DATETIME_DESC = "Date time value in a flexible format. "
            + "(Epoch millis, ISO zoned date time, ISO local date time, ISO date only, "
            + "ISO time only, ISO time only with offest e.g. +01:00, ... "
            + "Mising information is added, by simple assumptions. "
            + "If only time is given, \"today\" is added as date. "
            + "If a time zone is missing, the system default zone is used, ...)";

    @Value("${spring.application.name:}")
    private String serviceName;

    @Autowired(required = false)
    private PlacesApi placesApi;

    @Autowired(required = false)
    private AssetsApi assetsApi;

    @Autowired(required = false)
    private OptionsApi optionsApi;

    @Autowired(required = false)
    private BookingApi bookingApi;

    @Autowired(required = false)
    private CredentialsApi credentialsApi;

    @Autowired(required = false)
    private UsersApi usersApi;

    @Autowired(required = false)
    private ServiceInfoApi serviceInfoApi;

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
        var tags = new ArrayList<Tag>(4);

        if (placesApi != null) {
            tags.add(new Tag(PLACES_API_TAG, "API for searching provider specific places, like bus stops...", 1));
        }

        if (assetsApi != null) {
            tags.add(new Tag(ASSETS_API_TAG, "API for getting information about assets of this provider.", 2));
        }

        if (optionsApi != null) {
            tags.add(new Tag(OPTIONS_API_TAG, "API for searching mobility options.", 3));
        }

        if (bookingApi != null) {
            tags.add(new Tag(BOOKING_API_TAG, "Booking related API with CRUD functionality.", 4));
        }

        if (credentialsApi != null) {
            tags.add(new Tag(CREDENTIALS_API_TAG, "Credentials related API for managing with credentials of remote APIs.", 5));
        }
        if (usersApi != null) {
            tags.add(new Tag(USERS_API_TAG, "API for dealing with user-data registered at mobility service providers.", 6));
        }

        if (serviceInfoApi != null) {
            tags.add(new Tag(SERVICE_INFO_API_TAG, "API for getting information about this mobility service.", 99));
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
        return new ApiInfo(serviceName, "API description of " + serviceName,
                API_VERSION, null,
                new Contact("Hochschule Esslingen", "https://www.hs-esslingen.de", null),
                null, null, Collections.emptyList());
    }

    private static <T> Predicate<T> or(Predicate<T>... predicates) {
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
