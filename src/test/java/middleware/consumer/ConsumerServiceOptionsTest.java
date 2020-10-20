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
package middleware.consumer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import de.hsesslingen.keim.efs.middleware.model.Place;
import de.hsesslingen.keim.efs.middleware.consumer.ServiceDirectoryProxy;
import de.hsesslingen.keim.efs.middleware.consumer.ConsumerService;
import de.hsesslingen.keim.efs.mobility.service.MobilityService.API;
import de.hsesslingen.keim.efs.mobility.utils.EfsRequest;
import java.net.URI;
import static java.util.Arrays.asList;
import java.util.Set;
import middleware.MiddlewareTestApplication;
import middleware.MiddlewareTestBase;
import org.junit.Before;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

/**
 * @author k.sivarasah 5 Oct 2019
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MiddlewareTestApplication.class})
@TestPropertySource(properties = {"middleware.consumer.api.enabled=true"})
public class ConsumerServiceOptionsTest extends MiddlewareTestBase {

    @Autowired
    ConsumerService consumerService;

    @MockBean
    ServiceDirectoryProxy serviceDirectory;

    @MockBean
    @Qualifier("middlewareRestTemplate")
    RestTemplate restTemplate;

    @Before
    public void before() {
        EfsRequest.configureRestTemplates(restTemplate);
    }

    @Test
    public void getOptionsTest_ConstViolation_Exception() {
        when(serviceDirectory.search(any(), any(), any())).thenReturn(List.of());

        assertThatThrownBy(() -> consumerService.getOptions(null, null, null, null, null, null, null, null, null, null))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("from");

        reset(serviceDirectory);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getOptionsTest_OK() {
        when(serviceDirectory.search(any(), any(), any(), argThat((Set<API> apis) -> true)))
                .thenReturn(asList(getMobilityService(SERVICE_ID)));

        when(serviceDirectory.search(any(), any(), any(), (API[]) any()))
                .thenReturn(asList(getMobilityService(SERVICE_ID)));

        var placeFrom = "1.23,1.12";
        var response = ResponseEntity.ok(getDummyOptions(SERVICE_ID, placeFrom));

        when(restTemplate.exchange(
                argThat((URI uri) -> uri.getPath().contains(OPTIONS_PATH)),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(response);

        when(restTemplate.exchange(
                argThat((String uri) -> uri.contains(OPTIONS_PATH)),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(response);

        var optionsResult = consumerService.getOptions(placeFrom, null, null, null, null, null, null, null, null, null);
        assertNotNull(optionsResult);
        assertEquals(1, optionsResult.size());
        assertEquals(SERVICE_ID, optionsResult.get(0).getLeg().getServiceId());
        assertEquals(new Place(placeFrom), optionsResult.get(0).getLeg().getFrom());
        reset(serviceDirectory, restTemplate, restTemplate);
    }

}
