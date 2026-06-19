package com.example.NearPharma.service;

import com.example.NearPharma.exception.PharmacyNotFoundException;
import com.example.NearPharma.model.Pharmacy;
import com.example.NearPharma.repo.PharmacyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PharmacyServiceTest {

    @Mock
    private PharmacyRepository pharmacyRepository;

    @Mock
    private RestTemplate restTemplate;

    private PharmacyService service;

    @BeforeEach
    void setUp() {
        service = new PharmacyService("test-api-key", pharmacyRepository, restTemplate);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Pharmacy makePharmacy(long id, String name, double lat, double lng) {
        Pharmacy p = new Pharmacy();
        p.setId(id);
        p.setName(name);
        p.setAddress("123 Test St");
        p.setLatitude(lat);
        p.setLongitude(lng);
        return p;
    }

    // ─── getAllPharmacies ─────────────────────────────────────────────────────

    @Test
    void getAllPharmacies_returnsAll() {
        List<Pharmacy> list = List.of(makePharmacy(1L, "Apollo", 12.9, 77.5));
        when(pharmacyRepository.findAll()).thenReturn(list);

        assertThat(service.getAllPharmacies()).hasSize(1);
        verify(pharmacyRepository).findAll();
    }

    // ─── getPharmacyById ─────────────────────────────────────────────────────

    @Test
    void getPharmacyById_found_returnsPharmacy() {
        Pharmacy p = makePharmacy(1L, "Apollo", 12.9, 77.5);
        when(pharmacyRepository.findById(1L)).thenReturn(Optional.of(p));

        assertThat(service.getPharmacyById(1L).getName()).isEqualTo("Apollo");
    }

    @Test
    void getPharmacyById_notFound_throwsPharmacyNotFoundException() {
        when(pharmacyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPharmacyById(99L))
                .isInstanceOf(PharmacyNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ─── createPharmacy ──────────────────────────────────────────────────────

    @Test
    void createPharmacy_savesAndReturns() {
        Pharmacy p = makePharmacy(0L, "MedPlus", 12.9, 77.5);
        when(pharmacyRepository.save(p)).thenReturn(p);

        Pharmacy saved = service.createPharmacy(p);
        assertThat(saved.getName()).isEqualTo("MedPlus");
        verify(pharmacyRepository).save(p);
    }

    // ─── updatePharmacy ──────────────────────────────────────────────────────

    @Test
    void updatePharmacy_updatesAllFields() {
        Pharmacy existing = makePharmacy(1L, "OldName", 12.9, 77.5);
        Pharmacy update = makePharmacy(0L, "NewName", 13.0, 77.6);
        update.setPhone("9999999999");
        update.setChain("Apollo");
        update.setCity("Bangalore");
        update.setState("Karnataka");

        when(pharmacyRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(pharmacyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Pharmacy result = service.updatePharmacy(1L, update);

        assertThat(result.getName()).isEqualTo("NewName");
        assertThat(result.getLatitude()).isEqualTo(13.0);
        assertThat(result.getCity()).isEqualTo("Bangalore");
    }

    @Test
    void updatePharmacy_notFound_throws() {
        when(pharmacyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updatePharmacy(99L, new Pharmacy()))
                .isInstanceOf(PharmacyNotFoundException.class);
    }

    // ─── deletePharmacy ──────────────────────────────────────────────────────

    @Test
    void deletePharmacy_existingId_deletesSuccessfully() {
        when(pharmacyRepository.existsById(1L)).thenReturn(true);

        service.deletePharmacy(1L);

        verify(pharmacyRepository).deleteById(1L);
    }

    @Test
    void deletePharmacy_notFound_throws() {
        when(pharmacyRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.deletePharmacy(99L))
                .isInstanceOf(PharmacyNotFoundException.class);
        verify(pharmacyRepository, never()).deleteById(any());
    }

    // ─── haversine ───────────────────────────────────────────────────────────

    @Test
    void haversine_samePoint_returnsZero() {
        assertThat(service.haversine(12.9716, 77.5946, 12.9716, 77.5946)).isEqualTo(0.0);
    }

    @Test
    void haversine_knownDistance_isApproxCorrect() {
        // Bangalore to ~1km north: roughly 0.009 degrees lat
        double dist = service.haversine(12.9716, 77.5946, 12.9806, 77.5946);
        assertThat(dist).isBetween(0.9, 1.1); // ~1 km
    }

    // ─── getDistances (local cache path) ─────────────────────────────────────

    @Test
    void getDistances_usesLocalPharmaciesWithinRadius() {
        // Pharmacy within 5km
        Pharmacy nearby = makePharmacy(1L, "Apollo", 12.975, 77.597);
        when(pharmacyRepository.findAll()).thenReturn(List.of(nearby));

        Map<String, Object> matrixResponse = Map.of(
                "distances", List.of(List.of(500)),
                "durations", List.of(List.of(120))
        );
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(matrixResponse, HttpStatus.OK));

        List<Map<String, Object>> result = service.getDistances(12.9716, 77.5946, "driving");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("name")).isEqualTo("Apollo");
        assertThat(result.get(0).get("distance")).isEqualTo("500 meters");
        // Should NOT save — pharmacies were found locally
        verify(pharmacyRepository, never()).saveAll(any());
    }

    @Test
    void getDistances_noLocalPharmacies_returnsEmptyWhenApiAlsoEmpty() {
        when(pharmacyRepository.findAll()).thenReturn(List.of());
        // RapidAPI returns empty results
        Map<String, Object> placesResponse = Map.of("results", List.of());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(placesResponse, HttpStatus.OK));

        List<Map<String, Object>> result = service.getDistances(12.9716, 77.5946, "driving");

        assertThat(result).isEmpty();
    }
}
