package pl.treekt.fallingdetector.service;

import lombok.Data;

@Data
public class DetectorLocation {

    private double latitude;
    private double longitude;
    private String city;
    private String country;
    private String postalCode;
    private String placeDescription;
    private String street;
    private String streetNumber;

}
