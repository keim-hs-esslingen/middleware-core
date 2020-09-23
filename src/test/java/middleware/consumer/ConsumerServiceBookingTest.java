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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.reset;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import de.hsesslingen.keim.efs.middleware.model.Booking;
import de.hsesslingen.keim.efs.middleware.model.NewBooking;
import de.hsesslingen.keim.efs.middleware.consumer.ServiceDirectoryProxy;
import de.hsesslingen.keim.efs.middleware.consumer.ConsumerService;
import java.net.URI;
import middleware.MiddlewareTestApplication;
import middleware.MiddlewareTestBase;
import static org.mockito.Mockito.when;

/**
 * @author k.sivarasah 20 Nov 2019
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MiddlewareTestApplication.class})
public class ConsumerServiceBookingTest extends MiddlewareTestBase {

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

    public static final String BOOKING_JSON = "booking.json";
    public static final String NEWBOOKING_JSON = "newbooking.json";

    @Before
    public void init() {
        when(serviceDirectory.search()).thenReturn(Arrays.asList(getMobilityService(SERVICE_ID)));
    }

    @Test
    public void getBookingById_Test() throws IOException {
        Booking booking = dummyBooking();
        ResponseEntity<Booking> entity = ResponseEntity.ok(booking);

        when(restTemplate.exchange(
                Mockito.argThat((URI uri) -> {
                    return uri.getPath().contains(BOOKINGS_PATH);
                }),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(),
                Mockito.any(Class.class)
        )).thenReturn(entity);

        Booking result = consumerService.getBookingById(booking.getId(), SERVICE_ID, null);
        assertNotNull(result);
        assertEquals(booking, result);
        reset(restTemplate);
    }

    @Test
    public void createBooking_Test() throws IOException {
        Booking booking = dummyBooking();
        ResponseEntity<Booking> entity = ResponseEntity.ok(booking);

        when(restTemplate.exchange(
                Mockito.argThat((URI uri) -> {
                    return uri.getPath().contains(BOOKINGS_PATH);
                }),
                Mockito.eq(HttpMethod.POST),
                Mockito.any(),
                Mockito.any(Class.class)
        )).thenReturn(entity);

        Booking result = consumerService.createBooking(dummyNewBooking(), null);
        assertNotNull(result);
        assertEquals(booking, result);
        reset(restTemplate);
    }

    @Test
    public void testLoadFile() throws IOException {
        NewBooking nb = dummyNewBooking();
        System.out.println("NB: " + nb.toString());
        System.out.println("Start: " + nb.getLeg().getStartTime());
        System.out.println("End: " + nb.getLeg().getEndTime());
    }

    public Booking dummyBooking() throws IOException {
        return loadFromFile(BOOKING_JSON, Booking.class);
    }

    public NewBooking dummyNewBooking() throws IOException {
        return loadFromFile(NEWBOOKING_JSON, NewBooking.class);
    }

    public <T> T loadFromFile(String filename, Class<T> toClass) throws IOException {
        String url = getClass().getClassLoader().getResource(filename).toString();
        return mapper.readValue(new URL(url), toClass);
    }

}
