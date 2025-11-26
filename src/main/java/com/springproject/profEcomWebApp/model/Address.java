package com.springproject.profEcomWebApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    @NotBlank
    @Size(min = 4, message = "Street must be atleast 4 characters")
    private String street;

    @NotBlank
    @Size(min = 3, message = "Building name must be least 3 characters")
    private String buildingName;

    @NotBlank
    @Size(min = 2, message = "state must be atleast 2 characters")
    private String state;

    @NotBlank
    @Size(min = 4, message = "pincode must be atleast 4 characters")
    private String pincode;

    private String city;

    @NotBlank
    @Size(min = 4, message = "country must be atleast 4 characters")
    private String country;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Address(String street, String buildingName, String state, String pincode, String country, User user) {
        this.street = street;
        this.buildingName = buildingName;
        this.state = state;
        this.pincode = pincode;
        this.country = country;
        this.user = user;
    }
}
