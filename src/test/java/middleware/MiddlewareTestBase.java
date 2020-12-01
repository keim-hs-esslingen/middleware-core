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
package middleware;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.hsesslingen.keim.efs.middleware.model.Leg;
import de.hsesslingen.keim.efs.middleware.model.Option;
import de.hsesslingen.keim.efs.middleware.model.Place;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.service.Mode;
import static java.time.ZonedDateTime.now;

/**
 * @author k.sivarasah 28 Nov 2019
 */
public class MiddlewareTestBase {

    public static final String BOOKINGS_PATH = "/api/bookings";
    public static final String OPTIONS_PATH = "/api/options";

    public static final String CONSUMER_API_BOOKINGS = "/consumer/api/bookings";
    public static final String CONSUMER_API_OPTIONS = "/consumer/api/bookings/options";

    public static final String SERVICE_ID = "demo";

    public static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public Option getDummyOption(String serviceId, String from) {
        return new Option()
                .setLeg(new Leg()
                        .setStartTime(now())
                        .setEndTime(now())
                        .setFrom(Place.fromCoordinates(from))
                        .setMode(Mode.CAR)
                );
    }

    public List<Option> getDummyOptions(String serviceId, String from) {
        return List.of(getDummyOption(serviceId, from));
    }

    public MobilityService getMobilityService(String serviceId) {
        return new MobilityService().setId(serviceId)
                .setProviderName("dummy-provider")
                .setServiceName("dummy-service")
                .setServiceUrl("http://dummy-service-url/api");
    }

    public <T> T convert(String content, Class<T> toClass) {
        try {
            return mapper.readValue(content, toClass);
        } catch (IOException e) {
            return null;
        }
    }

    public String toString(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (IOException e) {
            return null;
        }
    }
}
