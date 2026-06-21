package com.example.NearPharma.constants;

/**
 * Constants for external API integrations.
 */
public final class ApiConstants {
    private ApiConstants() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    // RapidAPI Endpoints
    public static final String RAPIDAPI_DISTANCE_MATRIX_URL =
            "https://trueway-matrix.p.rapidapi.com/CalculateDrivingMatrix";

    public static final String RAPIDAPI_DIRECTIONS_URL =
            "https://trueway-directions2.p.rapidapi.com/FindDrivingRoute";

    public static final String RAPIDAPI_PLACES_NEARBY_URL =
            "https://trueway-places.p.rapidapi.com/FindPlacesNearby";

    public static final String RAPIDAPI_PLACES_SEARCH_URL =
            "https://trueway-places.p.rapidapi.com/FindPlaceByText";

    // RapidAPI Headers
    public static final String RAPIDAPI_HOST_MATRIX = "trueway-matrix.p.rapidapi.com";
    public static final String RAPIDAPI_HOST_DIRECTIONS = "trueway-directions2.p.rapidapi.com";
    public static final String RAPIDAPI_HOST_PLACES = "trueway-places.p.rapidapi.com";

    // Request Parameters
    public static final int MAX_PHARMACIES_PER_REQUEST = 10;
    public static final long CACHE_TTL_MILLISECONDS = 3600000; // 1 hour
    public static final int SLIDER_DEBOUNCE_MILLISECONDS = 500;
    public static final double DEFAULT_RADIUS_KM = 15.0;
    public static final int EARTH_RADIUS_KM = 6371;
}
