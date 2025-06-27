package com.example.NearPharma.service;

import com.example.NearPharma.Mapper.parseDistanceMatrixResponse;
import com.example.NearPharma.Mapper.parseNearbyResponse;
import com.example.NearPharma.model.Pharmacy;
import com.example.NearPharma.repo.PharmacyRepository;

import org.springframework.beans.factory.annotation.Autowired;
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
    @Value("${rapidApiKey.key}")
    private String rapidApiKey;
    private Double radiusKm=2.00;
    @Autowired
    private PharmacyRepository pharmacyRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Pharmacy> getAllPharmacies() {
        return pharmacyRepository.findAll();
    }

    public Pharmacy getPharmacyById(Long id) {
        return pharmacyRepository.findById(id).orElseThrow(() -> new RuntimeException("Pharmacy not found"));
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
        pharmacyRepository.deleteById(id);
    }

//    public List<Map<String, Object>> getDistances(double lat, double lng, String mode) {
//        try {
//            List<Pharmacy> pharmacies = pharmacyRepository.findAll();
//            StringBuilder origins = new StringBuilder(lat + "," + lng);
//            String destinations = pharmacies.stream()
//                    .map(p -> p.getLatitude() + "," + p.getLongitude())
//                    .collect(Collectors.joining(";"));
//
//            String url = "https://trueway-matrix.p.rapidapi.com/CalculateDrivingMatrix?" +
//                    "origins=" + origins + "&destinations=" + destinations;
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("x-rapidapi-host", "trueway-matrix.p.rapidapi.com");
//            headers.set("x-rapidapi-key", rapidApiKey);
//            HttpEntity<String> entity = new HttpEntity<>(headers);
//
//            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
//            System.out.println("sub"+response.getBody());
//            return parseDistanceMatrixResponse(response.getBody(), pharmacies);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to fetch distance matrix from RapidAPI", e);
//        }
//    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the Earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public List<Map<String, Object>> getDistances(double lat, double lng, String mode) {
        try {
            List<Pharmacy> allPharmacies = pharmacyRepository.findAll();

            // Step 1: Filter local pharmacies within radius
            List<Pharmacy> nearbyPharmacies = allPharmacies.stream()
                    .filter(p -> haversine(lat, lng, p.getLatitude(), p.getLongitude()) <= radiusKm)
                    .collect(Collectors.toList());

            // Step 2: If no nearby pharmacies locally, fetch from RapidAPI Places API
            if (nearbyPharmacies.isEmpty()) {
                nearbyPharmacies = fetchNearbyPharmaciesFromRapidApi(lat, lng, radiusKm);
                if (nearbyPharmacies.isEmpty()) {
                    return List.of(); // No pharmacies even from external source
                }

                // Optional: Save to DB for future calls
                pharmacyRepository.saveAll(nearbyPharmacies);
            }

            // Step 3: Build destinations string for Matrix API
            String destinations = nearbyPharmacies.stream()
                    .map(p -> p.getLatitude() + "," + p.getLongitude())
                    .collect(Collectors.joining(";"));

            String url = "https://trueway-matrix.p.rapidapi.com/CalculateDrivingMatrix?" +
                    "origins=" + lat + "," + lng +
                    "&destinations=" + destinations +
                    "&mode=" + mode;

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-rapidapi-host", "trueway-matrix.p.rapidapi.com");
            headers.set("x-rapidapi-key", rapidApiKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            return parseDistanceMatrixResponse(response.getBody(), nearbyPharmacies);

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch distance matrix from RapidAPI", e);
        }
    }

    private List<Pharmacy> fetchNearbyPharmaciesFromRapidApi(double lat, double lng, double radiusKm) {
        try {
            String url = "https://trueway-places.p.rapidapi.com/FindPlacesNearby" +
                    "?location=" + lat + "," + lng +
                    "&type=pharmacy" +
                    "&radius=" + (int)(radiusKm * 1000);

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-rapidapi-host", "trueway-places.p.rapidapi.com");
            headers.set("x-rapidapi-key", rapidApiKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("results");
            System.out.print(results);
            List<Pharmacy> pharmacies = new ArrayList<>();

            for (Map<String, Object> place : results) {
                Map<String, Object> location = (Map<String, Object>) place.get("location");

                Pharmacy p = new Pharmacy();
                p.setName((String) place.get("name"));
                p.setAddress((String) place.get("address"));
                p.setLatitude(Double.parseDouble(location.get("lat").toString()));
                p.setLongitude(Double.parseDouble(location.get("lng").toString()));
                p.setPhone((String) place.getOrDefault("phone_number", "")); // safe fallback

                pharmacies.add(p);
            }

            return pharmacies;
        } catch (Exception e) {
            System.err.println("Error fetching from Places API: " + e.getMessage());
            return List.of();
        }
    }

    public Map<String, Object> getDirections(Long id, double fromLat, double fromLng, String mode) {
        try {
            Pharmacy target = pharmacyRepository.findById(id).orElseThrow();
            String stops = fromLat + "," + fromLng + ";" + target.getLatitude() + "," + target.getLongitude();

            String url = "https://trueway-directions2.p.rapidapi.com/FindDrivingRoute?stops=" + stops;

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-rapidapi-host", "trueway-directions2.p.rapidapi.com");
            headers.set("x-rapidapi-key", rapidApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            System.out.println("wars"+response.getBody());
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch directions from RapidAPI", e);
        }
    }


    public Map<String, Object> getNearbyPharmacies(Long id, int radius, List<String> chains) {
        try {
            Pharmacy source = pharmacyRepository.findById(id).orElseThrow();

            String url = "https://trueway-places.p.rapidapi.com/FindPlacesNearby?" +
                    "location=" + source.getLatitude() + "," + source.getLongitude() +
                    "&radius=" + radius + "&type=pharmacy";

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-rapidapi-host", "trueway-places.p.rapidapi.com");
            headers.set("x-rapidapi-key", rapidApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return parseNearbyResponse(response.getBody(), chains);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch nearby pharmacies from RapidAPI", e);
        }
    }

    public List<Map<String, Object>> searchPlaces(String query, List<String> types, double lat, double lng) {
        try {
            String url = "https://trueway-places.p.rapidapi.com/FindPlaceByText?" +
                    "text=" + URLEncoder.encode(query, StandardCharsets.UTF_8) +
                    "&location=" + lat + "," + lng;

            if (types != null && !types.isEmpty()) {
                String typeParam = String.join(",", types);
                url += "&types=" + URLEncoder.encode(typeParam, StandardCharsets.UTF_8);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-rapidapi-host", "trueway-places.p.rapidapi.com");
            headers.set("x-rapidapi-key", rapidApiKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("results");

            List<Map<String, Object>> formatted = new ArrayList<>();
            for (Map<String, Object> place : results) {
                Map<String, Object> location = (Map<String, Object>) place.get("location");

                Map<String, Object> entry = new HashMap<>();
                entry.put("name", place.get("name"));
                entry.put("address", place.get("address"));
                entry.put("coordinates", Map.of(
                        "lat", location.get("lat"),
                        "lng", location.get("lng")
                ));
                entry.put("rating", place.get("rating")); // Optional if present
                entry.put("types", place.get("types"));

                formatted.add(entry);
            }

            return formatted;

        } catch (Exception e) {
            throw new RuntimeException("Failed to search places", e);
        }
    }

}
