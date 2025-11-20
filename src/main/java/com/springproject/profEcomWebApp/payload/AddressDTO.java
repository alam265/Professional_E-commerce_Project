package com.springproject.profEcomWebApp.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {

    private Long addressId;
    private String street;
    private String buildingName;
    private String state;
    private String pincode;
    private String country;

}
