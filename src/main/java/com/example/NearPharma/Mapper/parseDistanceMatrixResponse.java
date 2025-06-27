package com.example.NearPharma.Mapper;

import com.example.NearPharma.model.Pharmacy;

import java.util.*;
import java.util.stream.Collectors;

public interface parseDistanceMatrixResponse {
    @SuppressWarnings("unchecked")
    default List<Map<String, Object>> parseDistanceMatrixResponse(Map response, List<Pharmacy> pharmacies) {
        List<Map<String, Object>> result = new ArrayList<>();

        List<List<Integer>> distances = (List<List<Integer>>) response.get("distances");
        List<List<Integer>> durations = (List<List<Integer>>) response.get("durations");

        if (distances == null || durations == null || distances.isEmpty() || durations.isEmpty()) {
            return result;
        }

        List<Integer> distanceList = distances.get(0); // We sent 1 origin, many destinations
        List<Integer> durationList = durations.get(0);

        for (int i = 0; i < pharmacies.size(); i++) {
            Pharmacy pharmacy = pharmacies.get(i);

            Integer distance = i < distanceList.size() ? distanceList.get(i) : null;
            Integer duration = i < durationList.size() ? durationList.get(i) : null;

            if (distance == null || duration == null) continue;

            Map<String, Object> item = new HashMap<>();
            item.put("id", pharmacy.getId());
            item.put("name", pharmacy.getName());
            item.put("address", pharmacy.getAddress());
            item.put("coordinates", Map.of("lat", pharmacy.getLatitude(), "lng", pharmacy.getLongitude()));
            item.put("phone", pharmacy.getPhone());
            item.put("isOpen", true); // You can update this via Places API later
            item.put("distance", distance + " meters");
            item.put("duration", duration + " seconds");

            result.add(item);
        }

        // Sort by duration ascending
        return result.stream()
                .sorted(Comparator.comparingInt(m -> Integer.parseInt(((String) m.get("duration")).replaceAll("[^0-9]", ""))))
                .collect(Collectors.toList());
    }


}
