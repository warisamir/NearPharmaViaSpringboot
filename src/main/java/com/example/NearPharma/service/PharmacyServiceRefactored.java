package com.example.NearPharma.service;

import com.example.NearPharma.api.RapidApiGateway;
import com.example.NearPharma.constants.ApiConstants;
import com.example.NearPharma.dto.PharmacyDistanceResponse;
import com.example.NearPharma.enums.TravelMode;
import com.example.NearPharma.exception.InvalidTravelModeException;
import com.example.NearPharma.exception.PharmacyNotFoundException;
import com.example.NearPharma.model.Pharmacy;
import com.example.NearPharma.repo.PharmacyRepository;
import com.example.NearPharma.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for pharmacy-related operations.
 * Handles CRUD, distance calculations, and location-based queries.
 */
@Service
@RequiredArgsConstructor
public class PharmacyServiceRefactored {
    private static final Logger log = LoggerFactory.getLogger(PharmacyServiceRefactored.class);

    private final PharmacyRepository pharmacyRepository;
    private final RapidApiGateway rapidApiGateway;
    private final Map<String, CacheEntry> distanceCache = new ConcurrentHashMap<>();

    // ─── CRUD OPERATIONS ─────────────────────────────────────────────────────

    /**
     * Retrieve all pharmacies.
     */
    public List<Pharmacy> getAllPharmacies() {
        log.info("Fetching all pharmacies from database");
        List<Pharmacy> pharmacies = pharmacyRepository.findAll();
        log.info("Retrieved {} pharmacies", pharmacies.size());
        return pharmacies;
    }

    /**
     * Retrieve pharmacy by ID.
     */
    public Pharmacy getPharmacyById(Long id) {
        log.debug("Fetching pharmacy with ID: {}", id);
        return pharmacyRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Pharmacy not found with ID: {}", id);
                    return new PharmacyNotFoundException(id);
                });
    }

    /**
     * Create a new pharmacy.
     */
    public Pharmacy createPharmacy(Pharmacy pharmacy) {
        log.info("Creating new pharmacy: {}", pharmacy.getName());
        Pharmacy saved = pharmacyRepository.save(pharmacy);
        log.info("✓ Created pharmacy ID: {}, Name: {}", saved.getId(), saved.getName());
        return saved;
    }

    /**
     * Update existing pharmacy.
     */
    public Pharmacy updatePharmacy(Long id, Pharmacy updatedPharmacy) {
        log.info("Updating pharmacy ID: {}", id);
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
        log.info("✓ Updated pharmacy ID: {}", id);
        return result;
    }

    /**
     * Delete pharmacy by ID.
     */
    public void deletePharmacy(Long id) {
        log.info("Deleting pharmacy ID: {}", id);
        if (!pharmacyRepository.existsById(id)) {
            log.warn("Cannot delete - pharmacy not found with ID: {}", id);
            throw new PharmacyNotFoundException(id);
        }
        pharmacyRepository.deleteById(id);
        log.info("✓ Deleted pharmacy ID: {}", id);
    }

    // ─── LOCATION-BASED QUERIES ──────────────────────────────────────────────

    /**
     * Get nearby pharmacies with distances sorted by travel time.
     *
     * @param lat User latitude
     * @param lng User longitude
     * @param mode Travel mode (driving, walking, etc.)
     * @param radiusKm Search radius in kilometers
     * @return List of nearby pharmacies with distances
     */
    public List<PharmacyDistanceResponse> getDistances(double lat, double lng, String mode, double radiusKm) {
        log.info("=== DISTANCE QUERY === Location: ({},{}), Mode: {}, Radius: {}km", lat, lng, mode, radiusKm);

        // Validate travel mode
        if (!TravelMode.isValid(mode)) {
            log.warn("Invalid travel mode: {}", mode);
            throw new InvalidTravelModeException(mode);
        }

        // Find nearby pharmacies
        List<Pharmacy> nearbyPharmacies = findNearbyPharmacies(lat, lng, radiusKm);
        log.info("Found {} pharmacies within {}km", nearbyPharmacies.size(), radiusKm);

        if (nearbyPharmacies.isEmpty()) {
            log.info("No pharmacies found within radius");
            return List.of();
        }

        // Check cache
        String cacheKey = buildCacheKey(lat, lng, mode, nearbyPharmacies);
        if (isCacheHit(cacheKey)) {
            log.info("✓ CACHE HIT - Returning cached results");
            return (List<PharmacyDistanceResponse>) distanceCache.get(cacheKey).data;
        }

        // Call API to get distances
        log.debug("Cache MISS - Calling RapidAPI DistanceMatrix");
        List<PharmacyDistanceResponse> results = callDistanceMatrix(lat, lng, mode, nearbyPharmacies);

        // Cache results
        distanceCache.put(cacheKey, new CacheEntry(results));
        log.debug("Cached results with key: {}", cacheKey);

        return results;
    }

    /**
     * Get turn-by-turn directions to a specific pharmacy.
     *
     * @param pharmacyId Target pharmacy ID
     * @param fromLat Starting latitude
     * @param fromLng Starting longitude
     * @param mode Travel mode
     * @return Directions response
     */
    public Map<String, Object> getDirections(Long pharmacyId, double fromLat, double fromLng, String mode) {
        log.info("=== DIRECTIONS QUERY === Pharmacy ID: {}, From: ({},{}), Mode: {}",
                pharmacyId, fromLat, fromLng, mode);

        Pharmacy pharmacy = getPharmacyById(pharmacyId);
        log.debug("Target pharmacy: {} at ({},{})", pharmacy.getName(),
                pharmacy.getLatitude(), pharmacy.getLongitude());

        String stops = fromLat + "," + fromLng + ";" + pharmacy.getLatitude() + "," + pharmacy.getLongitude();

        try {
            Map<?, ?> response = rapidApiGateway.callDirections(stops);
            log.info("✓ Directions retrieved successfully for: {}", pharmacy.getName());
            return (Map<String, Object>) response;
        } catch (Exception e) {
            log.error("Failed to get directions for pharmacy ID: {}", pharmacyId, e);
            throw e;
        }
    }

    /**
     * Find nearby pharmacies of a specific pharmacy.
     *
     * @param pharmacyId Source pharmacy ID
     * @param radiusMeters Search radius in meters
     * @param chains Optional pharmacy chains filter
     * @return Nearby pharmacies response
     */
    public Map<String, Object> getNearbyPharmacies(Long pharmacyId, int radiusMeters, List<String> chains) {
        log.info("=== NEARBY QUERY === Pharmacy ID: {}, Radius: {}m, Chains: {}",
                pharmacyId, radiusMeters, chains);

        Pharmacy source = getPharmacyById(pharmacyId);
        log.debug("Source pharmacy: {} at ({},{})", source.getName(),
                source.getLatitude(), source.getLongitude());

        try {
            Map<?, ?> response = rapidApiGateway.callNearbyPlaces(
                    source.getLatitude(),
                    source.getLongitude(),
                    radiusMeters,
                    "pharmacy"
            );
            log.info("✓ Retrieved nearby pharmacies");
            return (Map<String, Object>) response;
        } catch (Exception e) {
            log.error("Failed to get nearby pharmacies for ID: {}", pharmacyId, e);
            throw e;
        }
    }

    // ─── INTERNAL HELPER METHODS ─────────────────────────────────────────────

    /**
     * Find nearby pharmacies within radius, sorted by distance.
     */
    private List<Pharmacy> findNearbyPharmacies(double lat, double lng, double radiusKm) {
        return pharmacyRepository.findAll().stream()
                .filter(p -> DistanceCalculator.isWithinRadius(lat, lng, p.getLatitude(), p.getLongitude(), radiusKm))
                .sorted(Comparator.comparingDouble(p -> DistanceCalculator.calculateDistance(
                        lat, lng, p.getLatitude(), p.getLongitude())))
                .limit(ApiConstants.MAX_PHARMACIES_PER_REQUEST)
                .collect(Collectors.toList());
    }

    /**
     * Call RapidAPI distance matrix and build response.
     */
    private List<PharmacyDistanceResponse> callDistanceMatrix(double lat, double lng, String mode,
                                                               List<Pharmacy> pharmacies) {
        String destinations = pharmacies.stream()
                .map(p -> p.getLatitude() + "," + p.getLongitude())
                .collect(Collectors.joining(";"));

        try {
            Map<?, ?> apiResponse = rapidApiGateway.callDistanceMatrix(lat, lng, destinations, mode);
            List<PharmacyDistanceResponse> results = parseDistanceResponse(apiResponse, pharmacies);
            log.info("Parsed {} pharmacy distances", results.size());
            return results;
        } catch (Exception e) {
            log.error("RapidAPI DistanceMatrix call failed", e);
            throw e;
        }
    }

    /**
     * Parse API response and build typed response objects.
     */
    private List<PharmacyDistanceResponse> parseDistanceResponse(Map<?, ?> apiResponse, List<Pharmacy> pharmacies) {
        List<PharmacyDistanceResponse> results = new ArrayList<>();
        List<?> distances = (List<?>) apiResponse.get("distances");
        List<?> durations = (List<?>) apiResponse.get("durations");

        if (distances != null && durations != null && !distances.isEmpty()) {
            List<?> distanceRow = (List<?>) distances.get(0);
            List<?> durationRow = (List<?>) durations.get(0);

            for (int i = 0; i < pharmacies.size() && i < distanceRow.size(); i++) {
                Pharmacy pharmacy = pharmacies.get(i);
                Integer distanceMeters = ((Number) distanceRow.get(i)).intValue();
                Integer durationSeconds = ((Number) durationRow.get(i)).intValue();

                results.add(PharmacyDistanceResponse.builder()
                        .id(pharmacy.getId())
                        .name(pharmacy.getName())
                        .address(pharmacy.getAddress())
                        .phone(pharmacy.getPhone())
                        .chain(pharmacy.getChain())
                        .distance(formatDistance(distanceMeters))
                        .duration(formatDuration(durationSeconds))
                        .isOpen(pharmacy.isIs24x7())
                        .coordinates(PharmacyDistanceResponse.CoordinatesDto.builder()
                                .lat(pharmacy.getLatitude())
                                .lng(pharmacy.getLongitude())
                                .build())
                        .build());
            }
        }

        return results;
    }

    /**
     * Build cache key from query parameters and pharmacy IDs.
     */
    private String buildCacheKey(double lat, double lng, String mode, List<Pharmacy> pharmacies) {
        return lat + ":" + lng + ":" + mode + ":" +
                pharmacies.stream().map(Pharmacy::getId).sorted().collect(Collectors.toList());
    }

    /**
     * Check if cache entry exists and is valid.
     */
    private boolean isCacheHit(String key) {
        CacheEntry entry = distanceCache.get(key);
        return entry != null && !entry.isExpired();
    }

    /**
     * Format distance in meters to readable string.
     */
    private String formatDistance(Integer meters) {
        if (meters == null || meters < 1000) {
            return (meters != null ? meters : 0) + " meters";
        }
        return String.format("%.1f km", meters / 1000.0);
    }

    /**
     * Format duration in seconds to readable string.
     */
    private String formatDuration(Integer seconds) {
        if (seconds == null) return "0 seconds";
        if (seconds < 60) return seconds + " seconds";
        if (seconds < 3600) return (seconds / 60) + " minutes";
        return (seconds / 3600) + " hours";
    }

    // ─── CACHE ENTRY ─────────────────────────────────────────────────────────

    /**
     * Internal cache entry with TTL.
     */
    private static class CacheEntry {
        final Object data;
        final long timestamp;

        CacheEntry(Object data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > ApiConstants.CACHE_TTL_MILLISECONDS;
        }
    }
}
