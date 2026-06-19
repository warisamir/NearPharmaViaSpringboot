package com.example.NearPharma.Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses the TrueWay Places /FindPlacesNearby response.
 * TrueWay returns: { results: [{ name, address, location: { lat, lng }, rating, ... }] }
 */
public interface parseNearbyResponse {

    @SuppressWarnings("unchecked")
    default Map<String, Object> parseNearbyResponse(Map<String, Object> response, List<String> chains) {
        List<Map<String, Object>> nearbyList = new ArrayList<>();
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");

        if (results == null) return Map.of("nearbyPharmacies", nearbyList);

        for (Map<String, Object> place : results) {
            String name = (String) place.get("name");
            if (name == null) continue;
            if (chains != null && chains.stream().noneMatch(name::contains)) continue;

            // TrueWay Places: location is a top-level field, not nested under geometry
            Map<String, Object> location = (Map<String, Object>) place.get("location");
            if (location == null) continue;

            Map<String, Object> pharmacy = new HashMap<>();
            pharmacy.put("placeId", place.get("place_id"));
            pharmacy.put("name", name);
            pharmacy.put("rating", place.getOrDefault("rating", 0.0));
            pharmacy.put("address", place.get("address"));
            pharmacy.put("coordinates", Map.of(
                    "lat", location.get("lat"),
                    "lng", location.get("lng")
            ));

            // open_now is returned directly in TrueWay response (not nested)
            Object openNow = place.get("open_now");
            pharmacy.put("isOpen", Boolean.TRUE.equals(openNow));

            nearbyList.add(pharmacy);
        }

        return Map.of("nearbyPharmacies", nearbyList);
    }
}
