package com.krealif.qiblafinder;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.CancellationTokenSource;

public class LocationHelper {
    private static LocationHelper instance;
    private final FusedLocationProviderClient fusedLocationProviderClient;

    private LocationHelper(Context context) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public static LocationHelper getInstance(Context context) {
        if (instance == null) {
            instance = new LocationHelper(context);
        }
        return instance;
    }

    public void getCurrentLocation(Context context, final LocationCallback locationCallback) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
            fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        locationCallback.onLocationResult(location);
                    } else {
                        locationCallback.onLocationError("No location available");
                    }
                }).addOnFailureListener(e -> locationCallback.onLocationError(e.getMessage()));
        }
    }

    public static Location getLocationFromLatLng(LatLng latLng) {
        Location location = new Location("");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        return location;
    }

    public static float getBearingToLocation(Location currentLocation, LatLng latLng) {
        Location latLngLocation = getLocationFromLatLng(latLng);
        float bearingValue = currentLocation.bearingTo(latLngLocation);
        // range of 0 to 360
        if (bearingValue < 0) {
            bearingValue += 360;
        }

        return bearingValue;
    }

    public interface LocationCallback {
        void onLocationResult(Location location);
        void onLocationError(String errorMessage);
    }
}