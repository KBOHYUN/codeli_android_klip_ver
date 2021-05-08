package com.example.codeli_klip;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class GpsTracker extends Service implements LocationListener {
    private final Context mContext;

    private LocationManager locationManager;
    private Location location;
    double latitude;
    double longitude;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    //private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    private static final long MIN_TIME_BW_UPDATES = 1;

    public GpsTracker(Context context) {
        this.mContext = context;
        getLocation();
    }

    public Location getLocation() {
        System.out.println("getlocaton start");
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            System.out.println("provider enable " +isGPSEnabled +" "+isNetworkEnabled);
            if (!isGPSEnabled && !isNetworkEnabled) {
            }
            else {
                int hasFineLocationPermission = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION);
                int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION);

                if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

                }
                else return null;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            System.out.println("network: "+latitude +" "+longitude);
                        }
                    }
                } if (isGPSEnabled) {
                    System.out.println("isGpsenable");
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                        if (locationManager != null) {
                            String locationProvider = LocationManager.GPS_PROVIDER;
                            location = locationManager.getLastKnownLocation(locationProvider);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) { Log.d("@@@", ""+e.toString()); }
        return location;
    }

    public double getLatitude() {
        if(location != null) {
            latitude = location.getLatitude();
        } return latitude;
    }
    public double getLongitude() {
        if(location != null) {
            longitude = location.getLongitude();
        } return longitude;
    }
    @Override public void onLocationChanged(Location location) {

    }
    @Override public void onProviderDisabled(String provider) {

    }
    @Override public void onProviderEnabled(String provider) {

    }
    @Override public void onStatusChanged(String provider, int status, Bundle extras) {

    }
    @Override public IBinder onBind(Intent arg0) {
        return null;
    }
    public void stopUsingGPS() {
        if(locationManager != null) {
            locationManager.removeUpdates(GpsTracker.this);
        }
    }
}
