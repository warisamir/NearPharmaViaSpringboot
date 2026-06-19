package com.example.NearPharma.exception;

public class PharmacyNotFoundException extends RuntimeException {
    public PharmacyNotFoundException(Long id) {
        super("Pharmacy not found with id: " + id);
    }
}
