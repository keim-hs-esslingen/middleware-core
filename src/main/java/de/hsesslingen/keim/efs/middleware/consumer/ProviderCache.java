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

import de.hsesslingen.keim.efs.mobility.requests.DefaultRequestTemplate;
import static de.hsesslingen.keim.efs.middleware.consumer.ServiceDirectoryProxy.buildGetAllRequest;
import de.hsesslingen.keim.efs.mobility.service.MobilityService;
import java.util.Collection;
import static java.util.Collections.unmodifiableCollection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import static java.util.stream.Collectors.toMap;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author keim
 */
@Service
@Lazy
@EnableScheduling
public class ProviderCache {

    private static final Logger logger = getLogger(MiddlewareService.class);

    @Value("${middleware.service-directory-url}")
    private String baseUrl;

    @Autowired
    private DefaultRequestTemplate rt;

    private CompletableFuture<Map<String, ProviderProxy>> providersFuture = new CompletableFuture<>();

    private synchronized CompletableFuture<Map<String, ProviderProxy>> getProvidersFuture() {
        return providersFuture;
    }

    private synchronized void setProvidersFuture(CompletableFuture<Map<String, ProviderProxy>> providersFuture) {
        this.providersFuture = providersFuture;
    }

    /**
     * Performs the actual request of getting all services from the service
     * directory. This method does not use the bean instance of
     * ServiceDirectoryProxy because that one does not mark requests to the
     * service directory as internal.
     *
     * @return
     */
    private List<MobilityService> fetchAvailableProviders() {
        ResponseEntity<List<MobilityService>> response;

        try {
            response = buildGetAllRequest(baseUrl, rt)
                    .toInternal()
                    .go();
        } catch (Exception ex) {
            return List.of();
        }

        if (response == null) {
            logger.warn("Services request returned \"null\" as response. This must be some kind of error.");
            return List.of();
        }

        var list = response.getBody();

        if (list == null) {
            logger.warn("The retunred services list from ServiceDirectory is \"null\".");
            return List.of();
        }

        return list;
    }

    /**
     * Makes some sanity checks on retrieved mobility service objects to prevent
     * exceptions in later processing of them. Semantical changes to the objects
     * are reduced to minimum.
     *
     * @param service
     */
    private void sanitizeMobilityService(MobilityService service) {
        if (service.getApis() == null) {
            service.setApis(Set.of());
        }
        if (service.getModes() == null) {
            service.setModes(Set.of());
        }
    }

    /**
     * Refreshes the cached providers by querying the service directory again.
     */
    @Scheduled(
            initialDelayString = "${middleware.refresh-provider-cache-initial-delay:0}",
            fixedRateString = "${middleware.refresh-provider-cache-rate:86400000}"
    )
    public void refreshAvailableProviders() {
        logger.info("Refreshing available services from service-directory.");

        var all = fetchAvailableProviders();
        var services = all.stream()
                // Sanitize invalid services to prevent null pointers and other stuff.
                .peek(this::sanitizeMobilityService)
                .map(s -> new ProviderProxy(s, rt))
                .collect(toMap(p -> p.getServiceId(), p -> p));

        if (providersFuture.isDone()) {
            setProvidersFuture(new CompletableFuture<>());
        }

        providersFuture.complete(services);
        logger.debug("Done refreshing available services.");
    }

    /**
     * Retrieves the current provider map from the future. This function is
     * private because the users of {@link ProviderCache} are not supposed to
     * mutate the map collection.
     *
     * @return
     */
    private Map<String, ProviderProxy> getProvidersMap() {
        try {
            return getProvidersFuture().get();
        } catch (InterruptedException | ExecutionException ex) {
            logger.warn("Thread got interrupted while waiting for services to be retrieved.");
            return getProvidersMap();
        }
    }

    /**
     * Gets a particular {@link ProviderProxy} by its service id.
     *
     * @param serviceId
     * @return
     */
    public ProviderProxy getProvider(String serviceId) {
        return getProvidersMap().get(serviceId);
    }

    /**
     * Get a list of all cached providers.
     *
     * @return
     */
    public Collection<ProviderProxy> getProviders() {
        return unmodifiableCollection(getProvidersMap().values());
    }

}
