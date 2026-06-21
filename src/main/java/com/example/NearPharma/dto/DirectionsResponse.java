package com.example.NearPharma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * DTO for directions/routing response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirectionsResponse {
    private Map<String, Object> route;
    private Long pharmacyId;
    private String pharmacyName;
}
