package com.usertracker.googlemaptrackuser.ModelClass;

public class LocationType {

    private String phone;
    private String latitude;
    private String longitude;

    public LocationType(String phone, String latitude, String longitude) {
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LocationType() {
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
