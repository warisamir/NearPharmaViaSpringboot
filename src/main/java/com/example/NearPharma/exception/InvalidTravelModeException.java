package com.example.NearPharma.exception;

/**
 * Exception thrown when an invalid travel mode is provided.
 */
public class InvalidTravelModeException extends RuntimeException {
    public InvalidTravelModeException(String mode) {
        super("Invalid travel mode: " + mode);
    }

    public InvalidTravelModeException(String mode, Throwable cause) {
        super("Invalid travel mode: " + mode, cause);
    }
}
