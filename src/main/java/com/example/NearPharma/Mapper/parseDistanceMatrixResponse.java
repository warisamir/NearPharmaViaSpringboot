package com.example.NearPharma.Mapper;

import com.example.NearPharma.model.Pharmacy;

import java.util.*;
import java.util.stream.Collectors;

public interface parseDistanceMatrixResponse {
    default List<Map<String, Object>> parseDistanceMatrixResponse(Map response, List<Pharmacy> pharmacies) {
        List<Map<String, Object>> result = new ArrayList<>();

        List rows = (List) response.get("rows");
        if (rows == null || rows.isEmpty()) return result;

        List elements = (List) ((Map) rows.get(0)).get("elements");

        for (int i = 0; i < elements.size(); i++) {
            Map element = (Map) elements.get(i);
            Pharmacy pharmacy = pharmacies.get(i);

            if (!"OK".equals(element.get("status"))) continue;

            Map<String, Object> item = new HashMap<>();
            item.put("id", pharmacy.getId());
            item.put("name", pharmacy.getName());
            item.put("address", pharmacy.getAddress());
            item.put("coordinates", Map.of("lat", pharmacy.getLatitude(), "lng", pharmacy.getLongitude()));
            item.put("phone", pharmacy.getPhone());
            item.put("isOpen", true); // Optional: you may enhance this with Places API for real-time open status

            Map distance = (Map) element.get("distance");
            Map duration = (Map) element.get("duration");
            item.put("distance", distance.get("text"));
            item.put("duration", duration.get("text"));

            result.add(item);
        }

        return result.stream()
                .sorted(Comparator.comparing(m -> ((String) m.get("duration")).replaceAll("[^0-9]", ""), Comparator.naturalOrder()))
                .collect(Collectors.toList());
    }

}
