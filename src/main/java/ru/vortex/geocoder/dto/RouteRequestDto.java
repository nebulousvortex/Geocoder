package ru.vortex.geocoder.dto;

public class RouteRequestDto {
    private Long[] locationIds;

    public Long[] getLocationIds() { return locationIds; }
    public void setLocationIds(Long[] locationIds) { this.locationIds = locationIds; }
}