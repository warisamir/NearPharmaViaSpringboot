package com.example.NearPharma.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name="pharmacies")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pharmacy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    String name;
    @NotBlank
    String address;
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private double latitude;

    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private double longitude;
    private String phone;
    private String chain;
    private String pincode;
    private String city;
    private String state;
    private boolean is24x7;

    public boolean isIs24x7() {
        return is24x7;
    }

    public void setIs24x7(boolean is24x7) {
        this.is24x7 = is24x7;
    }



    private Timestamp createdAt=new Timestamp(System.currentTimeMillis());

}
