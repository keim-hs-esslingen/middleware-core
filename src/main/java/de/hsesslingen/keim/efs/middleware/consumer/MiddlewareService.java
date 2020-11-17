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

import de.hsesslingen.keim.efs.middleware.model.Booking;
import de.hsesslingen.keim.efs.middleware.model.ICoordinates;
import de.hsesslingen.keim.efs.middleware.model.Option;
import de.hsesslingen.keim.efs.middleware.model.Place;
import de.hsesslingen.keim.efs.middleware.provider.IBookingApi;
import de.hsesslingen.keim.efs.middleware.provider.IOptionsApi;
import de.hsesslingen.keim.efs.mobility.service.MobilityService.API;
import static de.hsesslingen.keim.efs.mobility.service.MobilityService.API.BOOKING_API;
import static de.hsesslingen.keim.efs.mobility.service.MobilityService.API.OPTIONS_API;
import static de.hsesslingen.keim.efs.mobility.service.MobilityService.API.PLACES_API;
import de.hsesslingen.keim.efs.mobility.service.Mode;
import java.time.ZonedDateTime;
import static java.util.Collections.disjoint;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 *
 * @author keim
 */
@Service
@Lazy
public class MiddlewareService {

    private static final Function<String, String> defaultTokenGetter = (serviceId) -> null;

    @Autowired
    private ProviderCache providerCache;

    /**
     * Gets all available providers as a list of {@link ProviderProxy}. This
     * list is read from the {@link ProviderCache}.
     *
     * @return
     */
    public List<ProviderProxy> getProviders() {
        return providerCache.getProviders();
    }

    /**
     * Gets a stream of available providers matching the given
     * {@link serviceIds}. The stream is read from {@link ProviderCache}.
     *
     * @param serviceIds
     * @return
     */
    public Stream<ProviderProxy> getProviders(Set<String> serviceIds) {
        if (serviceIds == null || serviceIds.isEmpty()) {
            return getProviders().stream();
        }

        return getProviders().stream().filter(p -> serviceIds.contains(p.getServiceId()));
    }

    /**
     * Gets a partiular {@link ProviderProxy} from {@link ProviderCache}.
     *
     * @param serviceId
     * @return
     */
    public ProviderProxy getProvider(String serviceId) {
        return getProviders().stream()
                .filter(p -> p.getServiceId().equals(serviceId))
                .findAny()
                .orElse(null);
    }

    /**
     * Gets a filtered list of {@link ProviderProxy} from {@link ProviderCache}.
     *
     * @param anyOfTheseModesSupported
     * @param allOfTheseApisSupported
     * @return
     */
    public Stream<ProviderProxy> getProviders(
            Set<Mode> anyOfTheseModesSupported,
            Set<API> allOfTheseApisSupported
    ) {
        var stream = getProviders().stream();

        if (allOfTheseApisSupported != null && !allOfTheseApisSupported.isEmpty()) {
            stream = stream.filter(s -> s.getService().getApis().containsAll(allOfTheseApisSupported));
        }

        if (anyOfTheseModesSupported != null && !anyOfTheseModesSupported.isEmpty()) {
            stream = stream.filter(s -> !disjoint(anyOfTheseModesSupported, s.getService().getModes()));
        }

        return stream;
    }

    /**
     * Searches all providers that support the {@link IPlaceApi} (Places-API)
     * for places, using the given search criteria.
     * <p>
     * The returned stream can be collected to a list. You can use
     * short-circuiting functions like {@link Stream#limit(long)} or
     * {@link Stream#takeWhile(Predicate)} to limit the overall amount of
     * results retrieved and make the retrieval faster.
     * <p>
     * However, because the results are merged to a single stream within this
     * method, it is impossible to group the collected results by their
     * provider. Therefore, if you want to have full control over request timing
     * and provider grouping, use one of the {@link getProvider()} methods and
     * then use
     * {@link ProviderProxy#searchPlaces(String,ICoordinates, Integer, Integer, String)}
     * on each returned provider proxy.
     *
     * @param query
     * @param areaCenter
     * @param radiusMeter
     * @param limitToPerProvider
     * @param serviceTokenGetter A function that allows getting a ready-to-use
     * token for a given service id. The argument of the function is the service
     * id for which this function should return a token. The function can also
     * simply return {@code null} if no token is required.
     * @return
     */
    public Stream<Place> searchPlaces(
            String query,
            ICoordinates areaCenter,
            Integer radiusMeter,
            Integer limitToPerProvider,
            Function<String, String> serviceTokenGetter
    ) {
        var tokenGetter = serviceTokenGetter == null ? defaultTokenGetter : serviceTokenGetter;

        var requests = getProviders().stream()
                .filter(p -> p.supportsApi(PLACES_API))
                .map(p -> p.createSearchPlacesRequest(query, areaCenter, radiusMeter, limitToPerProvider, tokenGetter.apply(p.getServiceId())))
                .peek(r -> r.callOutgoingRequestAdapters())
                .collect(toList());

        return requests.parallelStream()
                .map(request -> request.go())
                .filter(response -> response != null)
                .map(response -> response.getBody())
                .filter(places -> places != null)
                .flatMap(places -> places.stream());
    }

    /**
     * Searches all providers that support the {@link IPlaceApi} (Places-API)
     * for places, using the given search criteria.
     * <p>
     * The returned stream can be collected to a list. You can use
     * short-circuiting functions like {@link Stream#limit(long)} or
     * {@link Stream#takeWhile(Predicate)} to limit the overall amount of
     * results retrieved and make the retrieval faster.
     * <p>
     * However, because the results are merged to a single stream within this
     * method, it is impossible to group the collected results by their
     * provider. Therefore, if you want to have full control over request timing
     * and provider grouping, use one of the {@link getProvider()} methods and
     * then use
     * {@link ProviderProxy#searchPlaces(String,ICoordinates, Integer, Integer, String)}
     * on each returned provider proxy.
     *
     * @param query
     * @param areaCenter
     * @param radiusMeter
     * @param limitToPerProvider
     * @param serviceIdTokenMap A map of tokens per service id. This map is
     * supposed to provide a token for each service id queried. The map can also
     * return {@code null} if no token is required.
     * @return
     */
    public Stream<Place> searchPlaces(
            String query,
            ICoordinates areaCenter,
            Integer radiusMeter,
            Integer limitToPerProvider,
            Map<String, String> serviceIdTokenMap
    ) {
        Map<String, String> tokenMap = serviceIdTokenMap == null ? Map.of() : serviceIdTokenMap;

        var requests = getProviders(tokenMap.keySet())
                .filter(p -> p.supportsApi(PLACES_API))
                .map(p -> p.createSearchPlacesRequest(query, areaCenter, radiusMeter, limitToPerProvider, tokenMap.get(p.getServiceId())))
                .peek(r -> r.callOutgoingRequestAdapters())
                .collect(toList());

        return requests.parallelStream()
                .map(request -> request.go())
                .filter(response -> response != null)
                .map(response -> response.getBody())
                .filter(places -> places != null)
                .flatMap(places -> places.stream());
    }

    /**
     * Queries all providers that support the {@link IOptionsApi} (Options-API)
     * for options, using the given criteria.
     * <p>
     * The returned stream can be collected to a list. You can use
     * short-circuiting functions like {@link Stream#limit(long)} or
     * {@link Stream#takeWhile(Predicate)} to limit the overall amount of
     * results retrieved and make the retrieval faster.
     * <p>
     * However, because the results are merged to a single stream within this
     * method, it is impossible to group the collected results by their
     * provider. Therefore, if you want to have full control over request timing
     * and provider grouping, use one of the {@link getProvider()} methods and
     * then use {@link ProviderProxy#getOptions()} on each returned provider
     * proxy.
     *
     * @param from
     * @param to
     * @param startTime
     * @param endTime
     * @param radiusMeter
     * @param sharingAllowed
     * @param modesAllowed
     * @param limitToPerProvider
     * @param includeGeoPaths
     * @param serviceTokenGetter A function that allows getting a ready-to-use
     * token for a given service id. The argument of the function is the service
     * id for which this function should return a token. The function can also
     * simply return {@code null} if no token is required.
     * @return
     */
    public Stream<Option> getOptions(
            ICoordinates from,
            ICoordinates to,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            Boolean sharingAllowed,
            Set<Mode> modesAllowed,
            Integer limitToPerProvider,
            Boolean includeGeoPaths,
            Function<String, String> serviceTokenGetter
    ) {
        var tokenGetter = serviceTokenGetter == null ? defaultTokenGetter : serviceTokenGetter;

        var requests = getProviders(modesAllowed, Set.of(OPTIONS_API))
                .map(p -> p.createGetOptionsRequest(from, to, startTime, endTime, radiusMeter, sharingAllowed, modesAllowed, limitToPerProvider, includeGeoPaths, tokenGetter.apply(p.getServiceId())))
                .peek(r -> r.callOutgoingRequestAdapters())
                .collect(toList());

        return requests.parallelStream()
                .map(request -> request.go())
                .filter(response -> response != null)
                .map(response -> response.getBody())
                .filter(options -> options != null)
                .flatMap(options -> options.stream());
    }

    /**
     * Queries all providers that support the {@link IOptionsApi} (Options-API)
     * for options, using the given criteria.
     * <p>
     * The returned stream can be collected to a list. You can use
     * short-circuiting functions like {@link Stream#limit(long)} or
     * {@link Stream#takeWhile(Predicate)} to limit the overall amount of
     * results retrieved and make the retrieval faster.
     * <p>
     * However, because the results are merged to a single stream within this
     * method, it is impossible to group the collected results by their
     * provider. Therefore, if you want to have full control over request timing
     * and provider grouping, use one of the {@link getProvider()} methods and
     * then use {@link ProviderProxy#getOptions()} on each returned provider
     * proxy.
     *
     * @param from
     * @param to
     * @param startTime
     * @param endTime
     * @param radiusMeter
     * @param sharingAllowed
     * @param modesAllowed
     * @param limitToPerProvider
     * @param includeGeoPaths
     * @param serviceIdTokenMap A map of tokens per service id. This map is
     * supposed to provide a token for each service id queried. The map can also
     * return {@code null} if no token is required.
     * @return
     */
    public Stream<Option> getOptions(
            ICoordinates from,
            ICoordinates to,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Integer radiusMeter,
            Boolean sharingAllowed,
            Set<Mode> modesAllowed,
            Integer limitToPerProvider,
            Boolean includeGeoPaths,
            Map<String, String> serviceIdTokenMap
    ) {
        Map<String, String> tokenMap = serviceIdTokenMap == null ? Map.of() : serviceIdTokenMap;

        var ids = tokenMap.keySet();

        var requests = getProviders(modesAllowed, Set.of(OPTIONS_API))
                .filter(p -> ids.contains(p.getServiceId()))
                .map(p -> p.createGetOptionsRequest(from, to, startTime, endTime, radiusMeter, sharingAllowed, modesAllowed, limitToPerProvider, includeGeoPaths, tokenMap.get(p.getServiceId())))
                .peek(r -> r.callOutgoingRequestAdapters())
                .collect(toList());

        return requests.parallelStream()
                .map(request -> request.go())
                .filter(response -> response != null)
                .map(response -> response.getBody())
                .filter(options -> options != null)
                .flatMap(options -> options.stream());
    }

    /**
     * Queries all providers that support the {@link IBookingApi} (Booking-API)
     * for bookings, using the given criteria.
     * <p>
     * The returned stream can be collected to a list. You can use
     * short-circuiting functions like {@link Stream#limit(long)} or
     * {@link Stream#takeWhile(Predicate)} to limit the overall amount of
     * results retrieved and make the retrieval faster.
     * <p>
     * However, because the results are merged to a single stream within this
     * method, it is impossible to group the collected results by their
     * provider. Therefore, if you want to have full control over request timing
     * and provider grouping, use one of the {@link getProvider()} methods and
     * then use {@link ProviderProxy#getBookings()} on each returned provider
     * proxy.
     *
     * @param serviceIds
     * @param serviceTokenGetter A function that allows getting a ready-to-use
     * token for a given service id. The argument of the function is the service
     * id for which this function should return a token. The function can also
     * simply return {@code null} if no token is required.
     * @return
     */
    public Stream<Booking> getBookings(
            Set<String> serviceIds,
            Function<String, String> serviceTokenGetter
    ) {
        var tokenGetter = serviceTokenGetter == null ? defaultTokenGetter : serviceTokenGetter;

        var requests = getProviders(serviceIds)
                .filter(p -> p.supportsApi(BOOKING_API))
                .map(p -> p.createGetBookingsRequest(tokenGetter.apply(p.getServiceId())))
                .peek(r -> r.callOutgoingRequestAdapters())
                .collect(toList());

        return requests.parallelStream()
                .map(request -> request.go())
                .filter(response -> response != null)
                .map(response -> response.getBody())
                .filter(options -> options != null)
                .flatMap(options -> options.stream());
    }

    /**
     * Queries all providers that support the {@link IBookingApi} (Booking-API)
     * for bookings, using the given criteria.
     * <p>
     * The returned stream can be collected to a list. You can use
     * short-circuiting functions like {@link Stream#limit(long)} or
     * {@link Stream#takeWhile(Predicate)} to limit the overall amount of
     * results retrieved and make the retrieval faster.
     * <p>
     * However, because the results are merged to a single stream within this
     * method, it is impossible to group the collected results by their
     * provider. Therefore, if you want to have full control over request timing
     * and provider grouping, use one of the {@link getProvider()} methods and
     * then use {@link ProviderProxy#getBookings()} on each returned provider
     * proxy.
     *
     * @param serviceIdTokenMap A map of tokens per service id. This map is
     * supposed to provide a token for each service id queried. The map can also
     * return {@code null} if no token is required.
     * @return
     */
    public Stream<Booking> getBookings(Map<String, String> serviceIdTokenMap) {
        Map<String, String> tokenMap = serviceIdTokenMap == null ? Map.of() : serviceIdTokenMap;
        return getBookings(tokenMap.keySet(), tokenMap::get);
    }

}
