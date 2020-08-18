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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import de.hsesslingen.keim.efs.middleware.booking.BookingState;
import de.hsesslingen.keim.efs.mobility.service.Mode;
import de.hsesslingen.keim.efs.middleware.booking.NewBooking;
import de.hsesslingen.keim.efs.middleware.common.Leg;
import de.hsesslingen.keim.efs.middleware.common.Options;
import de.hsesslingen.keim.efs.middleware.common.Place;
import de.hsesslingen.keim.efs.middleware.consumer.ConsumerService;
import middleware.MiddlewareTestApplication;
import middleware.MiddlewareTestBase;

/**
 * @author k.sivarasah 5 Oct 2019
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MiddlewareTestApplication.class})
@AutoConfigureMockMvc
@TestPropertySource(properties = {"efs.middleware.consumer-api.enabled=true"})
public class ConsumerApiTest extends MiddlewareTestBase {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ConsumerService consumerService;

    private static final String STRING_FROM = "1.234,2.345";
    private static final Place PLACE_FROM = new Place("1.234,2.345");

    private static final String SERVICE_A_ID = "service-a";
    private static final String SERVICE_B_ID = "service-b";

    @Test
    public void getOptionsTest_200() throws Exception {
        when(consumerService.getOptions(Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(getDummyOptions(PLACE_FROM));

        mockMvc.perform(get(CONSUMER_API_OPTIONS).param("from", STRING_FROM))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$", hasSize(2)))
                .andDo(print());
        Mockito.reset(consumerService);
    }

    @Test
    public void getOptionsTest_Invalid_Position_400() throws Exception {
        mockMvc.perform(get(CONSUMER_API_OPTIONS).param("from", "1.234,2345"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.code", is(400)))
                .andDo(print());
    }

    @Test
    public void getOptionsTest_400() throws Exception {
        mockMvc.perform(get(CONSUMER_API_OPTIONS))
                .andExpect(status().is4xxClientError())
                .andDo(print());
    }

    @Test
    public void getBookingByStateTest_200() throws Exception {
        mockMvc.perform(get(CONSUMER_API_BOOKINGS).param("state", BookingState.BOOKED.toString()))
                .andExpect(status().is2xxSuccessful())
                .andDo(print());
    }

    @Test
    public void getBookingByStateTest_400() throws Exception {
        mockMvc.perform(get(CONSUMER_API_BOOKINGS).param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(400)))
                .andDo(print());
    }

    @Test
    public void getBookingById_200() throws Exception {
        mockMvc.perform(get(CONSUMER_API_BOOKINGS + "/booking_id_001?serviceId=" + SERVICE_A_ID))
                .andExpect(status().is2xxSuccessful())
                .andDo(print());
    }

    @Test
    public void getBookings_200() throws Exception {
        mockMvc.perform(get(CONSUMER_API_BOOKINGS))
                .andExpect(status().is2xxSuccessful())
                .andDo(print());
    }

    @Test
    public void putBookingWithoutId_MethodNotAllowed_405() throws Exception {
        mockMvc.perform(put(CONSUMER_API_BOOKINGS))
                .andExpect(status().isMethodNotAllowed())
                .andDo(print());
    }

    @Test
    public void postBooking_Missing_RequestBody_400() throws Exception {
        mockMvc.perform(post(CONSUMER_API_BOOKINGS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(400)))
                .andDo(print());
    }

    @Test
    public void postBooking_Invalid_Booking_400() throws Exception {
        NewBooking newBooking = new NewBooking();

        mockMvc.perform(post(CONSUMER_API_BOOKINGS)
                .content(mapper.writeValueAsBytes(newBooking)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.code", is(400)))
                .andDo(print());
    }

    @Test
    public void postBooking_Invalid_Time_400() throws Exception {
        NewBooking newBooking = getDummyNewBooking();
        newBooking.getLeg().setStartTime(Instant.now().minus(1, ChronoUnit.HOURS));

        mockMvc.perform(post(CONSUMER_API_BOOKINGS)
                .content(mapper.writeValueAsBytes(newBooking)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(400)))
                .andExpect(content().string(containsString("startTime")))
                .andDo(print());
    }

    @Test
    public void postBooking_Valid_Booking_200() throws Exception {
        NewBooking newBooking = getDummyNewBooking();

        mockMvc.perform(post(CONSUMER_API_BOOKINGS)
                .content(mapper.writeValueAsBytes(newBooking)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    private NewBooking getDummyNewBooking() {
        Leg dummyLeg = new Leg().setMode(Mode.BICYCLE);
        dummyLeg.setFrom(PLACE_FROM).setTo(PLACE_FROM).setStartTime(Instant.now().plusSeconds(1000))
                .setEndTime(Instant.now().plusSeconds(10000)).setServiceId("dummy-service");
        return new NewBooking().setLeg(dummyLeg);
    }

    private List<Options> getDummyOptions(Place from) {
        Options op1 = new Options().setLeg(new Leg(Instant.now(), from).setServiceId(SERVICE_A_ID).setMode(Mode.CAR));
        Options op2 = new Options().setLeg(new Leg(Instant.now(), from).setServiceId(SERVICE_B_ID).setMode(Mode.CAR));
        return Arrays.asList(op1, op2);
    }
}
