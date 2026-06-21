package com.example.NearPharma.enums;

import java.util.Arrays;
import java.util.List;

/**
 * Enum for supported travel modes.
 */
public enum TravelMode {
    DRIVING("driving", "🚗 Driving"),
    WALKING("walking", "🚶 Walking"),
    BICYCLING("bicycling", "🚴 Cycling"),
    TRANSIT("transit", "🚌 Transit");

    private final String value;
    private final String label;

    TravelMode(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Get list of valid mode values.
     */
    public static List<String> getValidModes() {
        return Arrays.stream(values()).map(TravelMode::getValue).toList();
    }

    /**
     * Check if mode is valid.
     */
    public static boolean isValid(String mode) {
        return getValidModes().contains(mode);
    }

    /**
     * Get TravelMode from string value.
     */
    public static TravelMode fromValue(String value) {
        return Arrays.stream(values())
                .filter(mode -> mode.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid travel mode: " + value));
    }
}
