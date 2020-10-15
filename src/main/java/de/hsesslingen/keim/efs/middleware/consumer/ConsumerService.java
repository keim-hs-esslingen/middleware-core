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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.ResourceAccessException;

import de.hsesslingen.keim.efs.middleware.model.Booking;
import de.hsesslingen.keim.efs.middleware.model.BookingAction;
import de.hsesslingen.keim.efs.middleware.model.Customer;
import de.hsesslingen.keim.efs.middleware.model.Leg;
import de.hsesslingen.keim.efs.middleware.model.NewBooking;
import de.hsesslingen.keim.efs.middleware.model.Options;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import de.hsesslingen.keim.efs.mobility.utils.EfsRequest;
import de.hsesslingen.keim.efs.middleware.validation.PositionAsString;
import de.hsesslingen.keim.efs.mobility.service.MobilityType;
import de.hsesslingen.keim.efs.mobility.service.Mode;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.lang.Nullable;

/**
 * Allows service consumers to use service-features (get Offers/Options, make
 * Bookings, update Bookings etc.) of mobility-service-providers that are
 * registered in ServiceDirectory.
 * <p>
 * Communicates with the service directory to find providers that match the
 * consumers search criteria and propagates the options- and bookings requests
 * to those providers. It basically serves as a broker for accessing the
 * platform.
 *
 * @author k.sivarasah 28 Sep 2019 *
 */
@Validated
public class ConsumerService {

    private final static Logger logger = LoggerFactory.getLogger(ConsumerService.class);

    @Autowired
    private ServiceDirectoryProxy serviceDirectory;

    @Autowired
    private ObjectMapper mapper;

    private static final String CREDENTIALS_PATH = "/credentials";
    private static final String BOOKINGS_PATH = "/bookings";
    private static final String OPTIONS_PATH = "/bookings/options";

    private Map<String, String> extractConsumerCredentials(String credentials) {
        if (credentials == null || credentials.isEmpty()) {
            return new HashMap<>();
        }

        try {
            return mapper.readValue(credentials, new TypeReference<LinkedHashMap<String, String>>() {
            });
        } catch (IOException ex) {
            logger.error("Credentials provided in wrong format. They must be a map of credentials, which maps each services credentials to its id...", ex);
            return new HashMap<>();
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
     * Collects {@link Options} from all matching and available
     * Service-Providers
     *
     * @param from From where the ride shall start.
     * @param to To where the ride shall go.
     * @param startTime When the ride should start.
     * @param endTime When the ride should arrive.
     * @param radiusMeter The geographical search radius around {@link from} for
     * options.
     * @param share Whether sharing is allowed.
     * @param mobilityTypes List of mobility types to filter by.
     * @param modes List of modes to filter by.
     * @param serviceIds List of service provider ids that should be used.
     * @param credentials Credential data as json content string
     * @return
     */
    public List<Options> getOptions(
            @NotEmpty @PositionAsString String from,
            @PositionAsString String to,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            Boolean share,
            Set<MobilityType> mobilityTypes,
            Set<Mode> modes,
            Set<String> serviceIds,
            String credentials
    ) {
        //<editor-fold defaultstate="collapsed" desc="Long log.trace() statement...">
        if (logger.isTraceEnabled()) {
            // Output all params for debugging purposes.
            logger.trace("Going to fetch options with following parameters: "
                    + "from=%s, to=%s, startTime=%s, endTime=%s, radius=%d, share=%s, "
                    + "mobilityTypes=%s, modes=%s, serviceIds=%s",
                    from, to,
                    startTime == null ? "null" : startTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME),
                    endTime == null ? "null" : endTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME),
                    radiusMeter, share,
                    mobilityTypes.stream().map(m -> m.toString())
                            .collect(Collectors.joining(",")),
                    modes.stream().map(m -> m.toString())
                            .collect(Collectors.joining(",")),
                    serviceIds.stream().collect(Collectors.joining(",")));
        }
        //</editor-fold>

        logger.info("Requesting available services from Service-Directory...");
        var services = serviceDirectory.search(mobilityTypes, modes, serviceIds);

        // Extract credentials for the various services from the credentials string which
        // should contain a JSON-object with JSON formatted credentials per service id.
        var credentialsMap = extractConsumerCredentials(credentials);

        logger.info("Requesting options from available services...");

        // Preparing request in synchronous stream...
        var requests = services.stream()
                // Building a matching options request for each service...
                .map(service
                        -> buildOptionsRequest(service.getServiceUrl(),
                        credentialsMap.get(service.getId()), from, to,
                        startTime, endTime, radiusMeter, share)
                )
                // Calling outgoing request adapters from main thread to allow reading thread local storages in adapters.
                .map(request -> request.callOutgoingRequestAdapters())
                // Collecting requests before sending them, to ensure usage of main thread.
                .collect(Collectors.toList());

        // Sending the actual requests in parallel to increase performance...
        var options = requests.parallelStream()
                .map(request -> {
                    try {
                        return request.go();
                    } catch (Exception e) {
                        logger.error(
                                "Exception while getting options from url {}",
                                request.uriBuilder().build().toUriString(), e
                        );
                        return null;
                    }
                })
                // Filtering null response (due to exceptions)...
                .filter(response -> response != null)
                // Mapping to body and filtering nulls (due to null-response)...
                .map(response -> response.getBody())
                .filter(opts -> opts != null)
                // Flatmapping and collecting to get unified list of options...
                .flatMap(opts -> opts.stream())
                .collect(Collectors.toList());

        if (logger.isDebugEnabled()) {
            // Analyze the received options for faults and numbers...
            debugAnalyzeReceivedOptionsOrBookings(options.stream().map(o -> o.getLeg()), "option");
            logger.debug("Received %d options in total.", options.size());
        }

        return options;
    }

    private EfsRequest<List<Options>> buildOptionsRequest(
            String serviceUrl, String serviceCredentials,
            String from, String to,
            ZonedDateTime startTime, ZonedDateTime endTime,
            Integer radiusMeter, Boolean share) {

        // Start build the request object...
        var request = EfsRequest
                .get(serviceUrl + OPTIONS_PATH)
                .query("from", from)
                .expect(new ParameterizedTypeReference<List<Options>>() {
                });

        if (serviceCredentials != null) {
            request.credentials(serviceCredentials);
        }

        // Building query string by adding existing params...
        if (to != null) {
            request.query("to", to);
        }
        if (startTime != null) {
            request.query("startTime", startTime.toInstant().toEpochMilli());
        }
        if (endTime != null) {
            request.query("endTime", endTime.toInstant().toEpochMilli());
        }
        if (radiusMeter != null) {
            request.query("radius", radiusMeter);
        }
        if (share != null) {
            request.query("share", share);
        }

        return request;
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
        logger.info("Requesting available services from Service-Directory...");
        var services = serviceDirectory.search(null, null, serviceIds);

        // Extract credentials for the various services from the credentials string which
        // should contain a JSON-object with JSON formatted credentials per service id.
        var credentialsMap = extractConsumerCredentials(credentials);

        logger.info("Requesting bookings list from services...");
        var requests = services.stream()
                .map(service -> {
                    var serviceCredentials = credentialsMap.get(service.getId());

                    return EfsRequest
                            .get(service.getServiceUrl() + BOOKINGS_PATH)
                            .credentials(serviceCredentials)
                            .callOutgoingRequestAdapters() // Needed to be able to send off request in other thread.
                            .expect(new ParameterizedTypeReference<List<Booking>>() {
                            });
                })
                .collect(Collectors.toList());

        var bookings = requests.parallelStream()
                .map(req -> {
                    try {
                        return req.go();
                    } catch (Exception ex) {
                        logger.warn(
                                "Exception while getting bookings from url {}",
                                req.uriBuilder().build().toUriString(), ex
                        );
                        return null;
                    }
                })
                // Filtering null response (due to exceptions)...
                .filter(response -> response != null)
                // Mapping to body and filtering nulls (due to null-response)...
                .map(response -> response.getBody())
                .filter(serviceBookings -> serviceBookings != null)
                // Flatmapping and collecting to get unified list of bookings...
                .flatMap(serviceBookings -> serviceBookings.stream())
                .collect(Collectors.toList());

        if (logger.isDebugEnabled()) {
            // Analyze the received options for faults and numbers...
            debugAnalyzeReceivedOptionsOrBookings(bookings.stream().map(o -> o.getLeg()), "booking");
            logger.debug("Received %d bookings in total.", bookings.size());
        }

        return bookings;
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
     * @param booking the {@link Booking} object containing modified data
     * @param credentials Credential data as json content string
     * @return the modified {@link Booking} object
     */
    public Booking modifyBooking(@NotEmpty String id, @Valid Booking booking, String credentials) {
        String url = getBookingsUrlByServiceAndBookingId(booking.getLeg().getServiceId(), id);

        return EfsRequest.put(url)
                .credentials(credentials)
                .body(booking)
                .expect(Booking.class)
                .go()
                .getBody();
    }

    /**
     * Can be used to perform actions on bookings. This can be used to e.g.
     * unlock the door of rented vehicles, or stamp tickets...
     *
     * @param bookingId The ID of the booking on which to perform the action.
     * @param action The action that should be performed on the booking with the
     * given bookingId.
     * @param serviceId The ID of the service to which this booking belongs.
     * @param assetId The ID of the asset on which to perform this action. If
     * none specified, the service can choose how to handle this situation.
     * @param secret A secret that might be required by some services to perform
     * this action. (e.g. a PIN)
     * @param more Additional information that might be required by some
     * services in order to perform this action.
     * @param credentials The credentials needed to authorize oneself to perform
     * this action.
     */
    public void performAction(
            @NotNull String bookingId,
            @NotNull BookingAction action,
            @NotNull String serviceId,
            @Nullable String assetId,
            @Nullable String secret,
            @Nullable String more,
            @NotNull String credentials
    ) {
        String bookingUrl = getBookingsUrlByServiceAndBookingId(serviceId, bookingId);

        var request = EfsRequest.post(bookingUrl + "/action/" + action)
                .credentials(credentials);

        if (isNotBlank(assetId)) {
            request.query("assetId", assetId);
        }
        if (isNotBlank(secret)) {
            request.query("secret", secret);
        }
        if (isNotBlank(more)) {
            request.query("more", more);
        }

        request.go();
    }

    private String getServiceUrl(String serviceId) {
        return serviceDirectory.search().stream()
                .filter(service -> service.getId().equalsIgnoreCase(serviceId))
                .map(MobilityService::getServiceUrl).findAny()
                .orElseThrow(() -> new ResourceAccessException(serviceId + " is not available"));
    }

    private String getBookingsUrlByServiceId(String serviceId) {
        return getServiceUrl(serviceId) + BOOKINGS_PATH;
    }

    private String getBookingsUrlByServiceAndBookingId(String serviceId, String bookingId) {
        return getBookingsUrlByServiceId(serviceId) + "/" + bookingId;
    }

    private void debugAnalyzeReceivedOptionsOrBookings(Stream<Leg> legs, String optionOrBooking) {
        var itemsNumberMap = new HashMap<String, Integer>();

        legs.forEachOrdered(leg -> {
            if (leg == null) {
                logger.debug("Received an " + optionOrBooking + " with leg == null");
                return;
            }

            var serviceId = leg.getServiceId();

            if (serviceId == null) {
                logger.debug("Received an " + optionOrBooking + " with leg.serviceId == null");
                return;
            }

            var value = itemsNumberMap.get(serviceId);

            if (value == null) {
                itemsNumberMap.put(serviceId, 1);
            } else {
                itemsNumberMap.put(serviceId, value + 1);
            }
        });

        for (var serviceId : itemsNumberMap.keySet()) {
            logger.debug(String.format("Received %d " + optionOrBooking + "s from service \"%s\".", itemsNumberMap.get(serviceId), serviceId));
        }
    }

}
