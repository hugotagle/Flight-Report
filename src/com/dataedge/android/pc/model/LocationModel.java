package com.dataedge.android.pc.model;

import com.google.android.maps.GeoPoint;

public class LocationModel {

    private double longitude;
    private double latitude;
    private double bearing;
    private double altitude;
    private GeoPoint point;

    public LocationModel() {

    }

    public LocationModel(double latitude, double longitude, double bearing, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.bearing = bearing;
        this.altitude = altitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public GeoPoint getPoint() {
        return new GeoPoint((int) (this.latitude * 1E6), (int) (this.longitude * 1E6));
    }
    
    public void setPoint(GeoPoint point) {
        this.point = point;
    }
}
