package com.example.NearPharma.Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface parseNearbyResponse {
    default Map<String, Object> parseNearbyResponse(Map response, List<String> chains) {
        List<Map<String, Object>> nearbyList = new ArrayList<>();
        List results = (List) response.get("results");

        if (results == null) return Map.of("nearbyPharmacies", nearbyList);

        for (Object obj : results) {
            Map place = (Map) obj;

            String name = (String) place.get("name");
            if (chains != null && chains.stream().noneMatch(name::contains)) continue;

            Map<String, Object> pharmacy = new HashMap<>();
            pharmacy.put("placeId", place.get("place_id"));
            pharmacy.put("name", name);
            pharmacy.put("rating", place.getOrDefault("rating", 0.0));
            pharmacy.put("address", place.get("vicinity"));

            Map geometry = (Map) place.get("geometry");
            Map location = (Map) geometry.get("location");
            pharmacy.put("coordinates", location);

            // Optional: Open/Close and hours
            Map openingHours = (Map) place.get("opening_hours");
            pharmacy.put("isOpen", openingHours != null && Boolean.TRUE.equals(openingHours.get("open_now")));
            pharmacy.put("operatingHours", openingHours != null ? openingHours.toString() : "N/A");

            List<String> placeTypes = (List<String>) place.get("types");
            pharmacy.put("placeTypes", placeTypes);

            nearbyList.add(pharmacy);
        }

        return Map.of("nearbyPharmacies", nearbyList);
    }

}
