package com.example.NearPharma.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for pharmacy distance response in distance matrix queries.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PharmacyDistanceResponse {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String chain;
    private String distance;
    private String duration;
    private boolean isOpen;

    @JsonProperty("coordinates")
    private CoordinatesDto coordinates;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CoordinatesDto {
        private Double lat;
        private Double lng;
    }
}
