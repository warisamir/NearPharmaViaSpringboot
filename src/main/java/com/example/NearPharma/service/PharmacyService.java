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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PharmacyService implements parseDistanceMatrixResponse, parseNearbyResponse {
    @Value("${rapidApiKey.key}")
    private String rapidApiKey;

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

    public List<Map<String, Object>> getDistances(double lat, double lng, String mode) {
        try {
            List<Pharmacy> pharmacies = pharmacyRepository.findAll();
            StringBuilder origins = new StringBuilder(lat + "," + lng);
            String destinations = pharmacies.stream()
                    .map(p -> p.getLatitude() + "," + p.getLongitude())
                    .collect(Collectors.joining(";"));

            String url = "https://trueway-matrix.p.rapidapi.com/CalculateDrivingMatrix?" +
                    "origins=" + origins + "&destinations=" + destinations;

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-rapidapi-host", "trueway-matrix.p.rapidapi.com");
            headers.set("x-rapidapi-key", rapidApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            System.out.println("sub"+response.getBody());
            return parseDistanceMatrixResponse(response.getBody(), pharmacies);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch distance matrix from RapidAPI", e);
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
}
