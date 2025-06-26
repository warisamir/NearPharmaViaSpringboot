package com.example.NearPharma.config;


import org.springframework.beans.factory.annotation.Value;

public class MapApiConfig {
    @Value("${rapidapi.key}")
    private String rapidApiKey;
    public String googleApiKey(){
        if(rapidApiKey==null || rapidApiKey.isEmpty()){
            throw new IllegalStateException("Google map api key is missing.");
        }
        return rapidApiKey;
    }

}
