package com.krealif.qiblafinder;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

public class CompassFragment extends Fragment implements CompassSensor.OnSensorChangedListener {

    private CompassSensor compassSensor;
    private LocationHelper locationHelper;

    private Location currentLocation;
    private float bearingToKaaba;

    private ImageView compassRose;
    private ImageView compassNeedle;
    private TextView txtAzimuth;

    private final BroadcastReceiver updateLocationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MainActivity.UPDATE_LOCATION_ACTION)) {
                // call getMyLocation twice to force update location
                getMyLocation();
                // delay
                new Handler().postDelayed(() -> getMyLocation(), 100);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationHelper = LocationHelper.getInstance(getContext());
        getMyLocation();
        compassSensor = new CompassSensor((SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_compass, container, false);
        compassRose = view.findViewById(R.id.compass_rose);
        compassNeedle = view.findViewById(R.id.compass_needle);
        txtAzimuth = view.findViewById(R.id.azimuth_txt);
        return view;
    }

    private void getMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted
            // Get the user's location
            locationHelper.getCurrentLocation(getContext(), new LocationHelper.LocationCallback() {
                @Override
                public void onLocationResult(Location location) {
                    currentLocation = location;
                    bearingToKaaba = LocationHelper.getBearingToLocation(currentLocation, MapsFragment.kaabaCoordinate);
                    compassRose.setRotation(bearingToKaaba);
                }
                @Override
                public void onLocationError(String errorMessage) {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getMyLocation();
        requireActivity().registerReceiver(updateLocationBroadcastReceiver, new IntentFilter(MainActivity.UPDATE_LOCATION_ACTION));
        compassSensor.registerSensor();
    }

    @Override
    public void onPause() {
        super.onPause();
        requireActivity().unregisterReceiver(updateLocationBroadcastReceiver);
        compassSensor.unregisterSensor();
    }

    @Override
    public void onSensorChanged(float azimuth) {
        if (bearingToKaaba != 0) {
            String s = String.format(Locale.getDefault(),"%d°", (int)azimuth);
            txtAzimuth.setText(s);
            // headingDegree minus the bearing so that the direction of 0 degrees is the kaaba
            compassNeedle.setRotation(-(azimuth-bearingToKaaba));
        }
    }
}