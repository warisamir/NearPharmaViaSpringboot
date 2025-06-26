package com.example.NearPharma.config;


import org.springframework.beans.factory.annotation.Value;

public class MapApiConfig {
    @Value("${google.api.key}")
    public String apiKey;
    public String googleApiKey(){
        if(apiKey==null || apiKey.isEmpty()){
            throw new IllegalStateException("Google map api key is missing.");
        }
        return apiKey;
    }

}
