package com.example.NearPharma.controller;

import com.example.NearPharma.model.Pharmacy;
import com.example.NearPharma.service.PharmacyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacies")
public class PharmacyController {
    @Autowired
    private PharmacyService pharmacyService;

    @GetMapping("/getAll")
    public ResponseEntity<List<Pharmacy>> getAllPharmacies() {
        return ResponseEntity.ok(pharmacyService.getAllPharmacies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pharmacy> getPharmacyById(@PathVariable Long id) {
        return ResponseEntity.ok(pharmacyService.getPharmacyById(id));
    }

    @PostMapping("/createpharmacy")
    public ResponseEntity<Pharmacy> createPharmacy(@Valid @RequestBody Pharmacy pharmacy) {
        return ResponseEntity.ok(pharmacyService.createPharmacy(pharmacy));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pharmacy> updatePharmacy(@PathVariable Long id, @Valid @RequestBody Pharmacy updatedPharmacy) {
        return ResponseEntity.ok(pharmacyService.updatePharmacy(id, updatedPharmacy));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePharmacy(@PathVariable Long id) {
        pharmacyService.deletePharmacy(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/distances")
    public ResponseEntity<?> getDistances(
            @RequestParam
            @DecimalMin("-90.0")
            @DecimalMax("90.0") double lat,
            @RequestParam @DecimalMin("-180.0")
            @DecimalMax("180.0") double lng,
            @RequestParam(defaultValue = "driving") String mode) {
        List<String> validModes = List.of("driving", "walking", "transit", "bicycling");
        if (!validModes.contains(mode)) {
            return ResponseEntity.badRequest().body("Invalid travel mode");
        }
        return ResponseEntity.ok(pharmacyService.getDistances(lat, lng, mode));
    }

    @GetMapping("/{id}/directions")
    public ResponseEntity<?> getDirections(
            @PathVariable Long id,
            @RequestParam double fromLat,
            @RequestParam double fromLng,
            @RequestParam(defaultValue = "driving") String mode) {
        return ResponseEntity.ok(pharmacyService.getDirections(id, fromLat, fromLng, mode));
    }

    @GetMapping("/{id}/nearby")
    public ResponseEntity<?> getNearbyPharmacies(
            @PathVariable Long id,
            @RequestParam(defaultValue = "2000")
            @Min(500)
            @Max(10000) int radius,
            @RequestParam(required = false) List<String> chains) {
        List<String> allowedChains = List.of("Apollo", "MedPlus", "Wellness Forever", "Jan Aushadhi", "Netmeds", "1mg");
        if (chains != null && !chains.stream().allMatch(allowedChains::contains)) {
            return ResponseEntity.badRequest().body("Invalid pharmacy chain(s)");
        }
        return ResponseEntity.ok(pharmacyService.getNearbyPharmacies(id, radius, chains));
    }

}
