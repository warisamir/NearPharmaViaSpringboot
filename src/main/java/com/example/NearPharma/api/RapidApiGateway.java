package com.example.NearPharma.api;

import com.example.NearPharma.constants.ApiConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Gateway for RapidAPI external calls.
 * Handles all communication with third-party APIs.
 */
@Component
public class RapidApiGateway {
    private static final Logger log = LoggerFactory.getLogger(RapidApiGateway.class);

    private final RestTemplate restTemplate;
    private final String rapidApiKey;

    public RapidApiGateway(RestTemplate restTemplate,
                          @Value("${rapidApiKey.key}") String rapidApiKey) {
        this.restTemplate = restTemplate;
        this.rapidApiKey = rapidApiKey;
    }

    /**
     * Call distance matrix endpoint.
     */
    public Map<?, ?> callDistanceMatrix(double lat, double lng, String destinations, String mode) {
        log.debug("Calling RapidAPI DistanceMatrix: origins=({},{}), destinations={}, mode={}",
                lat, lng, destinations, mode);

        String url = ApiConstants.RAPIDAPI_DISTANCE_MATRIX_URL
                + "?origins=" + lat + "," + lng
                + "&destinations=" + destinations
                + "&mode=" + mode;

        return callApi(url, ApiConstants.RAPIDAPI_HOST_MATRIX);
    }

    /**
     * Call directions endpoint.
     */
    public Map<?, ?> callDirections(String stops) {
        log.debug("Calling RapidAPI Directions: stops={}", stops);

        String url = ApiConstants.RAPIDAPI_DIRECTIONS_URL + "?stops=" + stops;
        return callApi(url, ApiConstants.RAPIDAPI_HOST_DIRECTIONS);
    }

    /**
     * Call nearby places endpoint.
     */
    public Map<?, ?> callNearbyPlaces(double lat, double lng, int radius, String type) {
        log.debug("Calling RapidAPI NearbyPlaces: location=({},{}), radius={}, type={}",
                lat, lng, radius, type);

        String url = ApiConstants.RAPIDAPI_PLACES_NEARBY_URL
                + "?location=" + lat + "," + lng
                + "&radius=" + radius
                + "&type=" + type;

        return callApi(url, ApiConstants.RAPIDAPI_HOST_PLACES);
    }

    /**
     * Call place search endpoint.
     */
    public Map<?, ?> callPlaceSearch(String text, String location, String types) {
        log.debug("Calling RapidAPI PlaceSearch: text={}, location={}, types={}",
                text, location, types);

        String url = ApiConstants.RAPIDAPI_PLACES_SEARCH_URL
                + "?text=" + text
                + "&location=" + location
                + (types != null ? "&types=" + types : "");

        return callApi(url, ApiConstants.RAPIDAPI_HOST_PLACES);
    }

    /**
     * Generic API call method.
     */
    private Map<?, ?> callApi(String url, String host) {
        try {
            HttpHeaders headers = buildHeaders(host);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            log.trace("API call successful for URL: {}", url);
            return response.getBody();
        } catch (Exception e) {
            log.error("API call failed for URL: {} - {} : {}",
                    url, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    /**
     * Build RapidAPI headers with authentication.
     */
    private HttpHeaders buildHeaders(String host) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-host", host);
        headers.set("x-rapidapi-key", rapidApiKey);
        log.trace("RapidAPI headers prepared for host: {}", host);
        return headers;
    }
}
