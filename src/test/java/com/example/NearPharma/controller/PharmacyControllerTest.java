package com.example.NearPharma.controller;

import com.example.NearPharma.exception.PharmacyNotFoundException;
import com.example.NearPharma.model.Pharmacy;
import com.example.NearPharma.service.PharmacyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PharmacyController.class)
class PharmacyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PharmacyService pharmacyService;

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Pharmacy makePharmacy(long id) {
        Pharmacy p = new Pharmacy();
        p.setId(id);
        p.setName("Apollo Pharmacy");
        p.setAddress("MG Road, Bangalore");
        p.setLatitude(12.9716);
        p.setLongitude(77.5946);
        return p;
    }

    // ─── GET /api/pharmacies/getAll ───────────────────────────────────────────

    @Test
    void getAllPharmacies_returns200WithList() throws Exception {
        when(pharmacyService.getAllPharmacies()).thenReturn(List.of(makePharmacy(1L)));

        mockMvc.perform(get("/api/pharmacies/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Apollo Pharmacy"));
    }

    // ─── GET /api/pharmacies/{id} ─────────────────────────────────────────────

    @Test
    void getPharmacyById_found_returns200() throws Exception {
        when(pharmacyService.getPharmacyById(1L)).thenReturn(makePharmacy(1L));

        mockMvc.perform(get("/api/pharmacies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getPharmacyById_notFound_returns404() throws Exception {
        when(pharmacyService.getPharmacyById(99L)).thenThrow(new PharmacyNotFoundException(99L));

        mockMvc.perform(get("/api/pharmacies/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("99")));
    }

    // ─── POST /api/pharmacies/createpharmacy ──────────────────────────────────

    @Test
    void createPharmacy_validBody_returns200() throws Exception {
        Pharmacy p = makePharmacy(0L);
        when(pharmacyService.createPharmacy(any())).thenReturn(makePharmacy(1L));

        mockMvc.perform(post("/api/pharmacies/createpharmacy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createPharmacy_blankName_returns400() throws Exception {
        Pharmacy invalid = makePharmacy(0L);
        invalid.setName("");

        mockMvc.perform(post("/api/pharmacies/createpharmacy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    // ─── PUT /api/pharmacies/{id} ─────────────────────────────────────────────

    @Test
    void updatePharmacy_returns200() throws Exception {
        Pharmacy updated = makePharmacy(1L);
        updated.setName("Updated Pharmacy");
        when(pharmacyService.updatePharmacy(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/pharmacies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Pharmacy"));
    }

    // ─── DELETE /api/pharmacies/{id} ──────────────────────────────────────────

    @Test
    void deletePharmacy_returns204() throws Exception {
        doNothing().when(pharmacyService).deletePharmacy(1L);

        mockMvc.perform(delete("/api/pharmacies/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletePharmacy_notFound_returns404() throws Exception {
        doThrow(new PharmacyNotFoundException(99L)).when(pharmacyService).deletePharmacy(99L);

        mockMvc.perform(delete("/api/pharmacies/99"))
                .andExpect(status().isNotFound());
    }

    // ─── GET /api/pharmacies/distances ────────────────────────────────────────

    @Test
    void getDistances_validParams_returns200() throws Exception {
        when(pharmacyService.getDistances(anyDouble(), anyDouble(), anyString()))
                .thenReturn(List.of(Map.of("name", "Apollo", "distance", "500 meters")));

        mockMvc.perform(get("/api/pharmacies/distances")
                        .param("lat", "12.9716")
                        .param("lng", "77.5946")
                        .param("mode", "driving"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Apollo"));
    }

    @Test
    void getDistances_invalidMode_returns400() throws Exception {
        mockMvc.perform(get("/api/pharmacies/distances")
                        .param("lat", "12.9716")
                        .param("lng", "77.5946")
                        .param("mode", "teleport"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDistances_latOutOfRange_returns400() throws Exception {
        mockMvc.perform(get("/api/pharmacies/distances")
                        .param("lat", "999.0")
                        .param("lng", "77.5946"))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /api/pharmacies/{id}/nearby ──────────────────────────────────────

    @Test
    void getNearbyPharmacies_validChain_returns200() throws Exception {
        when(pharmacyService.getNearbyPharmacies(eq(1L), anyInt(), any()))
                .thenReturn(Map.of("nearbyPharmacies", List.of()));

        mockMvc.perform(get("/api/pharmacies/1/nearby")
                        .param("radius", "2000")
                        .param("chains", "Apollo"))
                .andExpect(status().isOk());
    }

    @Test
    void getNearbyPharmacies_invalidChain_returns400() throws Exception {
        mockMvc.perform(get("/api/pharmacies/1/nearby")
                        .param("chains", "FakeChain"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getNearbyPharmacies_radiusTooSmall_returns400() throws Exception {
        mockMvc.perform(get("/api/pharmacies/1/nearby")
                        .param("radius", "100"))
                .andExpect(status().isBadRequest());
    }
}
