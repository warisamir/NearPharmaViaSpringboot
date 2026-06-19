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

@Service
public class PharmacyService implements parseDistanceMatrixResponse, parseNearbyResponse {

    private static final Logger log = LoggerFactory.getLogger(PharmacyService.class);
    private static final double DEFAULT_RADIUS_KM = 15.0;

    private final String rapidApiKey;
    private final PharmacyRepository pharmacyRepository;
    private final RestTemplate restTemplate;

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
        return pharmacyRepository.findAll();
    }

    public Pharmacy getPharmacyById(Long id) {
        return pharmacyRepository.findById(id)
                .orElseThrow(() -> new PharmacyNotFoundException(id));
    }

    public Pharmacy createPharmacy(Pharmacy pharmacy) {
        return pharmacyRepository.save(pharmacy);
    }

    public Pharmacy updatePharmacy(Long id, Pharmacy updatedPharmacy) {
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
        return pharmacyRepository.save(pharmacy);
    }

    public void deletePharmacy(Long id) {
        if (!pharmacyRepository.existsById(id)) {
            throw new PharmacyNotFoundException(id);
        }
        pharmacyRepository.deleteById(id);
    }

    // ─── DISTANCE MATRIX ─────────────────────────────────────────────────────

    public List<Map<String, Object>> getDistances(double lat, double lng, String mode, double radiusKm) {
        List<Pharmacy> allPharmacies = pharmacyRepository.findAll();

        // Filter local pharmacies within requested radius first
        List<Pharmacy> nearbyPharmacies = allPharmacies.stream()
                .filter(p -> haversine(lat, lng, p.getLatitude(), p.getLongitude()) <= radiusKm)
                .collect(Collectors.toList());

            // Fall back to RapidAPI Places if no local results
        if (nearbyPharmacies.isEmpty()) {
            log.info("No local pharmacies found within {}km — fetching from RapidAPI", radiusKm);
            nearbyPharmacies = fetchNearbyPharmaciesFromRapidApi(lat, lng, radiusKm);
            if (nearbyPharmacies.isEmpty()) {
                return List.of();
            }
            pharmacyRepository.saveAll(nearbyPharmacies);
        }

        String destinations = nearbyPharmacies.stream()
                .map(p -> p.getLatitude() + "," + p.getLongitude())
                .collect(Collectors.joining(";"));

        String url = "https://trueway-matrix.p.rapidapi.com/CalculateDrivingMatrix?"
                + "origins=" + lat + "," + lng
                + "&destinations=" + destinations
                + "&mode=" + mode;

        HttpHeaders headers = rapidApiHeaders("trueway-matrix.p.rapidapi.com");
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        return parseDistanceMatrixResponse(response.getBody(), nearbyPharmacies);
    }

    // ─── DIRECTIONS ──────────────────────────────────────────────────────────

    public Map<String, Object> getDirections(Long id, double fromLat, double fromLng, String mode) {
        Pharmacy target = getPharmacyById(id);
        String stops = fromLat + "," + fromLng + ";" + target.getLatitude() + "," + target.getLongitude();

        String url = "https://trueway-directions2.p.rapidapi.com/FindDrivingRoute?stops=" + stops;

        HttpHeaders headers = rapidApiHeaders("trueway-directions2.p.rapidapi.com");
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        return response.getBody();
    }

    // ─── NEARBY PHARMACIES ───────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public Map<String, Object> getNearbyPharmacies(Long id, int radius, List<String> chains) {
        Pharmacy source = getPharmacyById(id);

        String url = "https://trueway-places.p.rapidapi.com/FindPlacesNearby?"
                + "location=" + source.getLatitude() + "," + source.getLongitude()
                + "&radius=" + radius
                + "&type=pharmacy";

        HttpHeaders headers = rapidApiHeaders("trueway-places.p.rapidapi.com");
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        return parseNearbyResponse(response.getBody(), chains);
    }

    // ─── PLACE SEARCH ────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchPlaces(String query, List<String> types, double lat, double lng) {
        String url = "https://trueway-places.p.rapidapi.com/FindPlaceByText?"
                + "text=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                + "&location=" + lat + "," + lng;

        if (types != null && !types.isEmpty()) {
            url += "&types=" + URLEncoder.encode(String.join(",", types), StandardCharsets.UTF_8);
        }

        HttpHeaders headers = rapidApiHeaders("trueway-places.p.rapidapi.com");
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("results");
        if (results == null) return List.of();

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
        try {
            String url = "https://trueway-places.p.rapidapi.com/FindPlacesNearby?"
                    + "location=" + lat + "," + lng
                    + "&type=pharmacy"
                    + "&radius=" + (int) (radiusKm * 1000);

            HttpHeaders headers = rapidApiHeaders("trueway-places.p.rapidapi.com");
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

            List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("results");
            if (results == null) return List.of();

            List<Pharmacy> pharmacies = new ArrayList<>();
            for (Map<String, Object> place : results) {
                Map<String, Object> location = (Map<String, Object>) place.get("location");
                if (location == null) continue;

                Pharmacy p = new Pharmacy();
                p.setName((String) place.get("name"));
                p.setAddress((String) place.getOrDefault("address", ""));
                p.setLatitude(Double.parseDouble(location.get("lat").toString()));
                p.setLongitude(Double.parseDouble(location.get("lng").toString()));
                p.setPhone((String) place.getOrDefault("phone_number", ""));
                pharmacies.add(p);
            }
            return pharmacies;
        } catch (Exception e) {
            log.error("Error fetching from Places API: {}", e.getMessage());
            return List.of();
        }
    }

    private HttpHeaders rapidApiHeaders(String host) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-host", host);
        headers.set("x-rapidapi-key", rapidApiKey);
        return headers;
    }
}
