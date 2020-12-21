package com.pluto.persolrevenuemanager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.Calendar;

public class AppReceiver extends BroadcastReceiver {

    private RevenueManager revenueManager;
    private LocationManager locationManager;
    private Utils utils;
    @Override
    public void onReceive(final Context context, Intent intent) {

        revenueManager = new RevenueManager(context);
        utils = new Utils(context);
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here.
            revenueManager.setAlarmForAllScheduledTasks();
            return;
        }

        if (intent.getAction().equals("android.intent.action.SCHEDULED_TASKS")) {
            if(utils.isGPSenabled()){
                locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(location == null){
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                            @Override
                            public void onLocationChanged(@NonNull Location location) {
                                Toast.makeText(context, "Latitude = " + location.getLatitude(), Toast.LENGTH_SHORT).show();
                                Log.d("latitude",location.getLatitude()+"");
                                locationManager.removeUpdates(this);
                                String locationStr = location.getLatitude()+","+location.getLongitude();
                                revenueManager.saveLocationLog(locationStr);
                            }

                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {

                            }

                            @Override
                            public void onProviderEnabled(String provider) {

                            }

                            @Override
                            public void onProviderDisabled(String provider) {

                            }
                        });
                    } else {
                        if(location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
                            // Do something with the recent location fix
                            //  otherwise wait for the update below
                            Toast.makeText(context, "Old location", Toast.LENGTH_SHORT).show();
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                                @Override
                                public void onLocationChanged(@NonNull Location location) {
                                    Toast.makeText(context, "Latitude = " + location.getLatitude(), Toast.LENGTH_SHORT).show();
                                    Log.d("latitude",location.getLatitude()+"");
                                    locationManager.removeUpdates(this);
                                    String locationStr = location.getLatitude()+","+location.getLongitude();
                                    revenueManager.saveLocationLog(locationStr);
                                }

                                @Override
                                public void onStatusChanged(String provider, int status, Bundle extras) {

                                }

                                @Override
                                public void onProviderEnabled(String provider) {

                                }

                                @Override
                                public void onProviderDisabled(String provider) {

                                }
                            });
                        } else {
                            Toast.makeText(context, "Latitude = " + location.getLatitude(), Toast.LENGTH_SHORT).show();
                            Log.d("latitude",location.getLatitude()+"");
                            String locationStr = location.getLatitude()+","+location.getLongitude();
                            revenueManager.saveLocationLog(locationStr);
                        }
                    }


                }
            } else {
                revenueManager.saveLocationLog("0.00,0.00");
            }

        }

    }
}
