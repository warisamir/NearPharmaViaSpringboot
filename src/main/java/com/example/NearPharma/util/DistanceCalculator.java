package com.example.NearPharma.util;

import com.example.NearPharma.constants.ApiConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for distance calculations using Haversine formula.
 */
public final class DistanceCalculator {
    private static final Logger log = LoggerFactory.getLogger(DistanceCalculator.class);

    private DistanceCalculator() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Calculate distance between two coordinates using Haversine formula.
     *
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = ApiConstants.EARTH_RADIUS_KM * c;

        log.trace("Calculated distance: {} km between ({},{}) and ({},{})",
                distance, lat1, lon1, lat2, lon2);

        return distance;
    }

    /**
     * Check if a point is within radius of another point.
     *
     * @param lat1 Reference latitude
     * @param lon1 Reference longitude
     * @param lat2 Point latitude
     * @param lon2 Point longitude
     * @param radiusKm Search radius in kilometers
     * @return true if point is within radius
     */
    public static boolean isWithinRadius(double lat1, double lon1, double lat2, double lon2, double radiusKm) {
        double distance = calculateDistance(lat1, lon1, lat2, lon2);
        return distance <= radiusKm;
    }
}
