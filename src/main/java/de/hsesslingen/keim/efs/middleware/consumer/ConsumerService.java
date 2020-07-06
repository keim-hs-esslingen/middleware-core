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
package de.hsesslingen.keim.efs.middleware.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.util.UriComponentsBuilder;

import de.hsesslingen.keim.efs.middleware.booking.Booking;
import de.hsesslingen.keim.efs.middleware.booking.BookingAction;
import de.hsesslingen.keim.efs.middleware.booking.Customer;
import de.hsesslingen.keim.efs.middleware.booking.NewBooking;
import de.hsesslingen.keim.efs.middleware.common.Options;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.middleware.utils.EfsRequest;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;

/**
 * Allows service consumers to use service-features (get Offers/Options, make
 * Bookings, update Bookings etc.) of mobility-service-providers that are
 * registered in ServiceDirectory.
 * <p>
 * Communicates with the service directory to find providers that match the
 * consumers search criteria and propagates the options- and bookings requests
 * to those providers. It basically serves as a broker for accessing the
 * plattform.
 *
 * @author k.sivarasah 28 Sep 2019 *
 */
@Validated
public class ConsumerService {

    private final static Logger log = LoggerFactory.getLogger(ConsumerService.class);

    @Autowired
    private ServiceDirectoryProxy serviceDirectory;

    private final ObjectMapper mapper = new ObjectMapper();

    private static final String CREDENTIALS_PATH = "/credentials";
    private static final String BOOKINGS_PATH = "/bookings";
    private static final String OPTIONS_PATH = "/bookings/options";

    private ConsumerCredentialsCollection extractConsumerCredentials(String credentials) {
        if (credentials == null || credentials.isEmpty()) {
            return ConsumerCredentialsCollection.empty();
        }

        try {
            Map<String, String> credentialsMap = mapper.readValue(credentials, new TypeReference<LinkedHashMap<String, String>>() {
            });

            return new ConsumerCredentialsCollection(credentialsMap);
        } catch (IOException ex) {
            log.error("Credentials provided in wrong format. They must be a map of credentials, which maps each services credentials to its id...", ex);
            return ConsumerCredentialsCollection.empty();
        }
    }

    public String createLoginToken(String serviceId, String credentials) {
        String url = getServiceUrl(serviceId) + CREDENTIALS_PATH + "/login-token";
        return EfsRequest.post(url).credentials(credentials).expect(String.class).go().getBody();
    }

    public Boolean deleteLoginToken(String serviceId, String credentials) {
        String url = getServiceUrl(serviceId) + CREDENTIALS_PATH + "/login-token";
        return EfsRequest.delete(url).credentials(credentials).expect(Boolean.class).go().getBody();
    }

    public String registerUser(String serviceId, String credentials, @Nullable Customer customer) {
        String url = getServiceUrl(serviceId) + CREDENTIALS_PATH + "/user";

        if (customer == null) {
            return EfsRequest.post(url).credentials(credentials).expect(String.class).go().getBody();
        } else {
            return EfsRequest.post(url).credentials(credentials).body(customer).expect(String.class).go().getBody();
        }
    }

    public Boolean checkCredentialsAreValid(String serviceId, String credentials) {
        String url = getServiceUrl(serviceId) + CREDENTIALS_PATH + "/check";
        return EfsRequest.get(url).credentials(credentials).expect(Boolean.class).go().getBody();
    }

    /**
     * Collects {@link Options} from all available Service-Providers
     *
     * @param optionsRequest {@link OptionsRequest} containing filter parameters
     * @param credentials Credential data as json content string
     * @return List of {@link Options}
     */
    public List<Options> getOptions(@Valid OptionsRequest optionsRequest, String credentials) {

        if (log.isTraceEnabled()) {
            try {
                log.trace("Received following get options request:\n" + mapper.writeValueAsString(optionsRequest));
            } catch (JsonProcessingException ex) {
                log.trace("Received a get options request, but wasn't able to serialize it to JSON.");
            }
        } else {
            log.info("Received get options request.");
        }

        List<MobilityService> services = serviceDirectory.search(optionsRequest.getMobilityTypes(), optionsRequest.getModes(), optionsRequest.getServiceIds());
        ConsumerCredentialsCollection credentialCollection = extractConsumerCredentials(credentials);

        log.info("Requesting options from available services...");

        return services.parallelStream().flatMap(service -> {
            try {
                String serviceCredentials = credentialCollection.ofService(service.getId());

                ResponseEntity<List<Options>> response = EfsRequest
                        .get(getOptionsUrl(service.getServiceUrl(), optionsRequest))
                        .credentials(serviceCredentials)
                        .expect(new ParameterizedTypeReference<List<Options>>() {
                        })
                        .go();

                List<Options> body = response.getBody();

                if (body != null) {
                    log.info("Service " + service.getId() + " returned " + body.size() + " options.");
                    return body.stream();
                } else {
                    log.warn("Service " + service.getId() + " returned \"null\" upon requesting options.");
                    return Stream.empty();
                }
            } catch (Exception e) {
                log.error("Exception while getting options from url {}", service.getServiceUrl(), e);
                return Stream.empty();
            }
        }).collect(Collectors.toList());
    }

    /**
     * Requests all bookings associated with the provided credentials from the
     * specified services.
     *
     * @param serviceIds
     * @param credentials
     * @return
     */
    public List<Booking> getBookings(Set<String> serviceIds, String credentials) {

        log.info("Received get bookings request for " + serviceIds.size() + " services.");

        List<MobilityService> services = serviceDirectory.search(null, null, serviceIds);
        ConsumerCredentialsCollection credentialCollection = extractConsumerCredentials(credentials);

        log.info("Requesting bookings list from services...");

        return services.parallelStream().flatMap(service -> {
            try {
                String serviceCredentials = credentialCollection.ofService(service.getId());

                ResponseEntity<List<Booking>> response = EfsRequest
                        .get(service.getServiceUrl() + BOOKINGS_PATH)
                        .credentials(serviceCredentials)
                        .expect(new ParameterizedTypeReference<List<Booking>>() {
                        })
                        .go();

                List<Booking> body = response.getBody();

                if (body != null) {
                    log.warn("Service " + service.getId() + " returned " + body.size() + " bookings.");
                    return body.stream();
                } else {
                    log.warn("Service " + service.getId() + " returned null upon requesting all bookings.");
                    return Stream.empty();
                }
            } catch (Exception e) {
                log.error("Exception while getting bookings from url {}", service.getServiceUrl(), e);
                return Stream.empty();
            }
        }).collect(Collectors.toList());
    }

    /**
     * Gets a {@link Booking} from the service using its id
     *
     * @param id the booking id
     * @param serviceId the service id (also known as service id)
     * @param credentials Credential data as json content string
     * @return the {@link Booking} object
     */
    public Booking getBookingById(@NotEmpty String id, @NotEmpty String serviceId, String credentials) {
        String url = getBookingsUrlByServiceAndBookingId(serviceId, id);
        return EfsRequest.get(url).credentials(credentials).expect(Booking.class).go().getBody();
    }

    /**
     * Creates a new booking using the service-provider-interface and returns
     * it. The servideId will be extracted from the Leg of {@link NewBooking}.
     *
     * @param newBooking {@link NewBooking} that should be created
     * @param credentials Credential data as json content string
     * @return {@link Booking} that was created
     */
    public Booking createBooking(@Valid NewBooking newBooking, String credentials) {
        String serviceId = newBooking.getLeg().getServiceId();
        return EfsRequest.post(getBookingsUrlByServiceId(serviceId)).credentials(credentials).body(newBooking).expect(Booking.class).go().getBody();
    }

    /**
     * Updates an existing {@link Booking} with new details
     *
     * @param id the booking id
     * @param action an action that might be requested for a booking.
     * @param booking the {@link Booking} object containing modified data
     * @param credentials Credential data as json content string
     * @return the modified {@link Booking} object
     */
    public Booking modifyBooking(@NotEmpty String id, @Nullable BookingAction action, @Valid Booking booking, String credentials) {
        String url = getBookingsUrlByServiceAndBookingId(booking.getLeg().getServiceId(), id);
        var request = EfsRequest.put(url)
                .credentials(credentials)
                .body(booking)
                .expect(Booking.class);

        if (action != null) {
            request.query("action", action);
        }

        return request
                .go()
                .getBody();
    }

    private String getServiceUrl(String serviceId) {
        return serviceDirectory.search().stream()
                .filter(service -> service.getId().equalsIgnoreCase(serviceId))
                .map(MobilityService::getServiceUrl).findAny()
                .orElseThrow(() -> new ResourceAccessException(serviceId + " is not available"));
    }

    private String getOptionsUrl(String baseUrl, OptionsRequest optionsRequest) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl + OPTIONS_PATH)
                .queryParam("from", optionsRequest.getFrom());

        if (optionsRequest.getTo() != null) {
            uriBuilder.queryParam("to", optionsRequest.getTo());
        }
        if (optionsRequest.getRadius() != null) {
            uriBuilder.queryParam("radius", optionsRequest.getRadius());
        }
        if (optionsRequest.getShare() != null) {
            uriBuilder.queryParam("share", optionsRequest.getShare());
        }
        if (optionsRequest.getStartTime() != null) {
            uriBuilder.queryParam("startTime", optionsRequest.getStartTime().toEpochMilli());
        }
        if (optionsRequest.getEndTime() != null) {
            uriBuilder.queryParam("endTime", optionsRequest.getEndTime().toEpochMilli());
        }

        return uriBuilder.toUriString();
    }

    private String getBookingsUrlByServiceId(String serviceId) {
        return String.format("%s%s", getServiceUrl(serviceId), BOOKINGS_PATH);
    }

    private String getBookingsUrlByServiceAndBookingId(String serviceId, String bookingId) {
        return String.format("%s/%s", getBookingsUrlByServiceId(serviceId), bookingId);
    }
}
