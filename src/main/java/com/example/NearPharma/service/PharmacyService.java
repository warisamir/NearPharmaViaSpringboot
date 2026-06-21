package com.example.NearPharma.service;

import com.example.NearPharma.Mapper.parseDistanceMatrixResponse;
import com.example.NearPharma.Mapper.parseNearbyResponse;
import com.example.NearPharma.exception.PharmacyNotFoundException;
import com.example.NearPharma.model.Pharmacy;
import com.example.NearPharma.repo.PharmacyRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Comparator;

@Service
public class PharmacyService implements parseDistanceMatrixResponse, parseNearbyResponse {

    private static final Logger log = LoggerFactory.getLogger(PharmacyService.class);
    private static final double DEFAULT_RADIUS_KM = 15.0;
    private static final long CACHE_TTL_MS = 3600000; // 1 hour

    private final String rapidApiKey;
    private final PharmacyRepository pharmacyRepository;
    private final RestTemplate restTemplate;
    private final Map<String, CacheEntry> distanceCache = new ConcurrentHashMap<>();

    private static class CacheEntry {
        final Object data;
        final long timestamp;

        CacheEntry(Object data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL_MS;
        }
    }

    public PharmacyService(
            @Value("${rapidApiKey.key}") String rapidApiKey,
            PharmacyRepository pharmacyRepository,
            RestTemplate restTemplate) {
        this.rapidApiKey = rapidApiKey;
        this.pharmacyRepository = pharmacyRepository;
        this.restTemplate = restTemplate;
    }

    // ─── CRUD ────────────────────────────────────────────────────────────────

    public List<Pharmacy> getAllPharmacies() {
        log.info("Fetching all pharmacies from database");
        List<Pharmacy> pharmacies = pharmacyRepository.findAll();
        log.info("Retrieved {} pharmacies from database", pharmacies.size());
        return pharmacies;
    }

    public Pharmacy getPharmacyById(Long id) {
        log.debug("Fetching pharmacy with ID: {}", id);
        return pharmacyRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Pharmacy not found with ID: {}", id);
                    return new PharmacyNotFoundException(id);
                });
    }

    public Pharmacy createPharmacy(Pharmacy pharmacy) {
        log.info("Creating new pharmacy: {}", pharmacy.getName());
        Pharmacy saved = pharmacyRepository.save(pharmacy);
        log.info("Successfully created pharmacy with ID: {}, Name: {}", saved.getId(), saved.getName());
        return saved;
    }

    public Pharmacy updatePharmacy(Long id, Pharmacy updatedPharmacy) {
        log.info("Updating pharmacy with ID: {}", id);
        Pharmacy pharmacy = getPharmacyById(id);
        pharmacy.setName(updatedPharmacy.getName());
        pharmacy.setAddress(updatedPharmacy.getAddress());
        pharmacy.setLatitude(updatedPharmacy.getLatitude());
        pharmacy.setLongitude(updatedPharmacy.getLongitude());
        pharmacy.setPhone(updatedPharmacy.getPhone());
        pharmacy.setChain(updatedPharmacy.getChain());
        pharmacy.setPincode(updatedPharmacy.getPincode());
        pharmacy.setCity(updatedPharmacy.getCity());
        pharmacy.setState(updatedPharmacy.getState());
        pharmacy.setIs24x7(updatedPharmacy.isIs24x7());
        Pharmacy result = pharmacyRepository.save(pharmacy);
        log.info("Successfully updated pharmacy ID: {}, Name: {}", id, result.getName());
        return result;
    }

    public void deletePharmacy(Long id) {
        log.info("Deleting pharmacy with ID: {}", id);
        if (!pharmacyRepository.existsById(id)) {
            log.warn("Cannot delete - pharmacy not found with ID: {}", id);
            throw new PharmacyNotFoundException(id);
        }
        pharmacyRepository.deleteById(id);
        log.info("Successfully deleted pharmacy with ID: {}", id);
    }

    // ─── DISTANCE MATRIX ─────────────────────────────────────────────────────

    public List<Map<String, Object>> getDistances(double lat, double lng, String mode, double radiusKm) {
        log.info("=== DISTANCE MATRIX REQUEST === Location: ({},{}), Mode: {}, Radius: {}km", lat, lng, mode, radiusKm);
        List<Pharmacy> allPharmacies = pharmacyRepository.findAll();
        log.debug("Total pharmacies in database: {}", allPharmacies.size());

        // Filter local pharmacies within requested radius first, then get top 10 closest
        List<Pharmacy> nearbyPharmacies = allPharmacies.stream()
                .filter(p -> haversine(lat, lng, p.getLatitude(), p.getLongitude()) <= radiusKm)
                .sorted(Comparator.comparingDouble(p -> haversine(lat, lng, p.getLatitude(), p.getLongitude())))
                .limit(100)
                .collect(Collectors.toList());
        log.info("Found {} pharmacies within {}km radius", nearbyPharmacies.size(), radiusKm);

            // Fall back to RapidAPI Places if no local results
        if (nearbyPharmacies.isEmpty()) {
            log.warn("No local pharmacies found within {}km — fetching from RapidAPI", radiusKm);
            nearbyPharmacies = fetchNearbyPharmaciesFromRapidApi(lat, lng, radiusKm);
            if (nearbyPharmacies.isEmpty()) {
                log.warn("No pharmacies found from RapidAPI either. Returning empty list");
                return List.of();
            }
            log.info("Fetched {} pharmacies from RapidAPI, saving to database", nearbyPharmacies.size());
            pharmacyRepository.saveAll(nearbyPharmacies);
        }

        String cacheKey = lat + ":" + lng + ":" + mode + ":" + nearbyPharmacies.stream().map(p -> p.getId()).sorted().collect(Collectors.toList());
        CacheEntry cached = distanceCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            log.info("✓ CACHE HIT - Returning cached results for distances");
            return (List<Map<String, Object>>) cached.data;
        }
        log.debug("Cache MISS - Making RapidAPI call for distances");

        String destinations = nearbyPharmacies.stream()
                .map(p -> p.getLatitude() + "," + p.getLongitude())
                .collect(Collectors.joining(";"));

        String url = "https://trueway-matrix.p.rapidapi.com/CalculateDrivingMatrix?"
                + "origins=" + lat + "," + lng
                + "&destinations=" + destinations
                + "&mode=" + mode;

        log.debug("Calling RapidAPI DistanceMatrix with {} destinations", nearbyPharmacies.size());
        HttpHeaders headers = rapidApiHeaders("trueway-matrix.p.rapidapi.com");
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        log.debug("RapidAPI response received successfully");

        List<Map<String, Object>> result = parseDistanceMatrixResponse(response.getBody(), nearbyPharmacies);
        log.info("Parsed {} pharmacy distances from API response", result.size());
        distanceCache.put(cacheKey, new CacheEntry(result));
        log.debug("Cached results with key: {}", cacheKey);
        return result;
    }

    // ─── DIRECTIONS ──────────────────────────────────────────────────────────

    public Map<String, Object> getDirections(Long id, double fromLat, double fromLng, String mode) {
        log.info("=== DIRECTIONS REQUEST === Pharmacy ID: {}, From: ({},{}), Mode: {}", id, fromLat, fromLng, mode);
        Pharmacy target = getPharmacyById(id);
        log.debug("Target pharmacy: {} at ({},{})", target.getName(), target.getLatitude(), target.getLongitude());

        String stops = fromLat + "," + fromLng + ";" + target.getLatitude() + "," + target.getLongitude();

        String url = "https://trueway-directions2.p.rapidapi.com/FindDrivingRoute?stops=" + stops;
        log.debug("Calling RapidAPI directions endpoint");

        HttpHeaders headers = rapidApiHeaders("trueway-directions2.p.rapidapi.com");
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        log.info("✓ Directions retrieved successfully for pharmacy: {}", target.getName());

        return response.getBody();
    }

    // ─── NEARBY PHARMACIES ───────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public Map<String, Object> getNearbyPharmacies(Long id, int radius, List<String> chains) {
        log.info("=== NEARBY PHARMACIES REQUEST === Pharmacy ID: {}, Radius: {}m, Chains: {}", id, radius, chains);
        Pharmacy source = getPharmacyById(id);
        log.debug("Source pharmacy: {} at ({},{})", source.getName(), source.getLatitude(), source.getLongitude());

        String url = "https://trueway-places.p.rapidapi.com/FindPlacesNearby?"
                + "location=" + source.getLatitude() + "," + source.getLongitude()
                + "&radius=" + radius
                + "&type=pharmacy";

        log.debug("Calling RapidAPI FindPlacesNearby with radius: {}m", radius);
        HttpHeaders headers = rapidApiHeaders("trueway-places.p.rapidapi.com");
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        Map<String, Object> result = parseNearbyResponse(response.getBody(), chains);
        log.info("✓ Retrieved nearby pharmacies response");

        return result;
    }

    // ─── PLACE SEARCH ────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchPlaces(String query, List<String> types, double lat, double lng) {
        log.info("=== PLACE SEARCH REQUEST === Query: '{}', Types: {}, Location: ({},{})", query, types, lat, lng);

        String url = "https://trueway-places.p.rapidapi.com/FindPlaceByText?"
                + "text=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                + "&location=" + lat + "," + lng;

        if (types != null && !types.isEmpty()) {
            url += "&types=" + URLEncoder.encode(String.join(",", types), StandardCharsets.UTF_8);
        }

        log.debug("Calling RapidAPI FindPlaceByText");
        HttpHeaders headers = rapidApiHeaders("trueway-places.p.rapidapi.com");
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("results");
        if (results == null) {
            log.warn("No results found for place search query: '{}'", query);
            return List.of();
        }
        log.info("Found {} places for search query: '{}'", results.size(), query);

        return results.stream().map(place -> {
            Map<String, Object> location = (Map<String, Object>) place.get("location");
            Map<String, Object> entry = new HashMap<>();
            entry.put("name", place.get("name"));
            entry.put("address", place.get("address"));
            entry.put("coordinates", location != null
                    ? Map.of("lat", location.get("lat"), "lng", location.get("lng"))
                    : Map.of());
            entry.put("rating", place.get("rating"));
            entry.put("types", place.get("types"));
            return entry;
        }).collect(Collectors.toList());
    }

    // ─── INTERNAL HELPERS ────────────────────────────────────────────────────

    double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @SuppressWarnings("unchecked")
    private List<Pharmacy> fetchNearbyPharmaciesFromRapidApi(double lat, double lng, double radiusKm) {
        log.info("Fetching nearby pharmacies from RapidAPI: Location: ({},{}), Radius: {}km", lat, lng, radiusKm);
        try {
            String url = "https://trueway-places.p.rapidapi.com/FindPlacesNearby?"
                    + "location=" + lat + "," + lng
                    + "&type=pharmacy"
                    + "&radius=" + (int) (radiusKm * 1000);

            log.debug("Calling RapidAPI FindPlacesNearby endpoint");
            HttpHeaders headers = rapidApiHeaders("trueway-places.p.rapidapi.com");
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

            List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("results");
            if (results == null) {
                log.warn("No results returned from RapidAPI for location ({},{})", lat, lng);
                return List.of();
            }

            log.info("RapidAPI returned {} pharmacy results", results.size());
            List<Pharmacy> pharmacies = new ArrayList<>();
            for (Map<String, Object> place : results) {
                Map<String, Object> location = (Map<String, Object>) place.get("location");
                if (location == null) {
                    log.debug("Skipping place without location: {}", place.get("name"));
                    continue;
                }

                Pharmacy p = new Pharmacy();
                p.setName((String) place.get("name"));
                p.setAddress((String) place.getOrDefault("address", ""));
                p.setLatitude(Double.parseDouble(location.get("lat").toString()));
                p.setLongitude(Double.parseDouble(location.get("lng").toString()));
                p.setPhone((String) place.getOrDefault("phone_number", ""));
                pharmacies.add(p);
            }
            log.info("Successfully parsed {} pharmacies from RapidAPI response", pharmacies.size());
            return pharmacies;
        } catch (Exception e) {
            log.error("ERROR fetching from RapidAPI Places: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return List.of();
        }
    }

    private HttpHeaders rapidApiHeaders(String host) {
        log.debug("Building RapidAPI headers for host: {}", host);
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-host", host);
        headers.set("x-rapidapi-key", rapidApiKey);
        log.trace("RapidAPI headers prepared successfully");
        return headers;
    }
}
