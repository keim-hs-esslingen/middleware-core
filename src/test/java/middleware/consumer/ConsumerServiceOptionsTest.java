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

import java.util.Arrays;
import java.util.List;

import javax.validation.ConstraintViolationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import de.hsesslingen.keim.efs.middleware.model.Options;
import de.hsesslingen.keim.efs.middleware.model.Place;
import de.hsesslingen.keim.efs.middleware.consumer.ServiceDirectoryProxy;
import de.hsesslingen.keim.efs.middleware.consumer.ConsumerService;
import de.hsesslingen.keim.efs.mobility.utils.EfsRequest;
import java.net.URI;
import middleware.MiddlewareTestApplication;
import middleware.MiddlewareTestBase;
import org.junit.Before;

/**
 * @author k.sivarasah 5 Oct 2019
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MiddlewareTestApplication.class})
public class ConsumerServiceOptionsTest extends MiddlewareTestBase {

    @Autowired
    ConsumerService consumerService;

    @MockBean
    ServiceDirectoryProxy serviceDirectory;

    @MockBean
    @Qualifier("restTemplateLoadBalanced")
    RestTemplate restTemplate;

    @MockBean
    @Qualifier("restTemplateSimple")
    RestTemplate restTemplateSimple;

    @Before
    public void before() {
        EfsRequest.configureRestTemplates(restTemplate, restTemplateSimple);
    }

    @Test
    public void getOptionsTest_ConstViolation_Exception() {
        when(serviceDirectory.search(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Arrays.asList());

        assertThatThrownBy(() -> consumerService.getOptions(null, null, null, null, null, null, null, null, null, null))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("from");

        reset(serviceDirectory);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getOptionsTest_OK() {
        when(serviceDirectory.search(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Arrays.asList(getMobilityService(SERVICE_ID)));

        String placeFrom = "1.23,1.12";
        ResponseEntity<List<Options>> entity = ResponseEntity.ok(getDummyOptions(SERVICE_ID, placeFrom));

        when(restTemplate.exchange(
                Mockito.argThat((URI uri) -> {
                    return uri.getPath().contains(OPTIONS_PATH);
                }),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(),
                Mockito.any(ParameterizedTypeReference.class)
        )).thenReturn(entity);

        when(restTemplate.exchange(
                Mockito.argThat((String uri) -> {
                    return uri.contains(OPTIONS_PATH);
                }),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(),
                Mockito.any(ParameterizedTypeReference.class)
        )).thenReturn(entity);

        when(restTemplateSimple.exchange(
                Mockito.argThat((URI uri) -> {
                    return uri.getPath().contains(OPTIONS_PATH);
                }),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(),
                Mockito.any(ParameterizedTypeReference.class)
        )).thenReturn(entity);

        when(restTemplateSimple.exchange(
                Mockito.argThat((String uri) -> {
                    return uri.contains(OPTIONS_PATH);
                }),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(),
                Mockito.any(ParameterizedTypeReference.class)
        )).thenReturn(entity);

        List<Options> optionsResult = consumerService.getOptions(placeFrom, null, null, null, null, null, null, null, null, null);
        assertNotNull(optionsResult);
        assertEquals(1, optionsResult.size());
        assertEquals(SERVICE_ID, optionsResult.get(0).getLeg().getServiceId());
        assertEquals(new Place(placeFrom), optionsResult.get(0).getLeg().getFrom());
        reset(serviceDirectory, restTemplate, restTemplateSimple);
    }

}
