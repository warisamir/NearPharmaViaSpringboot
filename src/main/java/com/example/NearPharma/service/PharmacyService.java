package com.example.NearPharma.service;

import com.example.NearPharma.Mapper.parseDistanceMatrixResponse;
import com.example.NearPharma.Mapper.parseNearbyResponse;
import com.example.NearPharma.model.Pharmacy;
import com.example.NearPharma.repo.PharmacyRepository;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PharmacyService implements parseDistanceMatrixResponse, parseNearbyResponse {
    @Value("${google.api.key}")
    public String apiKey;
    @Autowired
    private PharmacyRepository pharmacyRepository;
    @Value("${google.api.key}")
    private String Apikey;
    private final RestTemplate restTemplate = new RestTemplate();

    public List<Pharmacy> getAllPharmacies() {
        return getAllPharmacies();
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
            String destinations = pharmacies.stream()
                    .map(p -> p.getLatitude() + "," + p.getLongitude())
                    .collect(Collectors.joining("|"));

            String url = String.format("https://maps.googleapis.com/maps/api/distancematrix/json?origins=%f,%f&destinations=%s&mode=%s&key=%s",
                    lat, lng, destinations, mode, apiKey);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return parseDistanceMatrixResponse(response.getBody(), pharmacies);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch distance matrix", e);
        }
    }

    public Map<String, Object> getDirections(Long id, double fromLat, double fromLng, String mode) {
        try {
            Pharmacy target = pharmacyRepository.findById(id).orElseThrow();
            String url = String.format("https://maps.googleapis.com/maps/api/directions/json?origin=%f,%f&destination=%f,%f&mode=%s&key=%s",
                    fromLat, fromLng, target.getLatitude(), target.getLongitude(), mode, apiKey);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch directions", e);
        }
    }

    public Map<String, Object> getNearbyPharmacies(Long id, int radius, List<String> chains) {
        try {
            Pharmacy source = pharmacyRepository.findById(id).orElseThrow();

            String url = String.format("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=%d&type=pharmacy&key=%s",
                    source.getLatitude(), source.getLongitude(), radius, apiKey);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return parseNearbyResponse(response.getBody(), chains);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch nearby pharmacies", e);
        }
    }

}
