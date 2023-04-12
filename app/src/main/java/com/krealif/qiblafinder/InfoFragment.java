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
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class InfoFragment extends Fragment implements CompassSensor.OnSensorChangedListener{

    private CompassSensor compassSensor;
    private LocationHelper locationHelper;
    private Location currentLocation;

    private TextView coordinateTxt;
    private TextView distanceTxt;
    private TextView azimuthTxt;
    private TextView bearingTxt;

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
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        coordinateTxt = view.findViewById(R.id.coordinate_txt);
        distanceTxt = view.findViewById(R.id.distance_txt);
        azimuthTxt = view.findViewById(R.id.azimuth_txta);
        bearingTxt = view.findViewById(R.id.bearing_txt);

        return view;
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

    private void getMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted
            // Get the user's location
            locationHelper.getCurrentLocation(getContext(), new LocationHelper.LocationCallback() {
                @Override
                public void onLocationResult(Location location) {
                    currentLocation = location;
                    // set text coordinate
                    String coor = String.format(Locale.getDefault(), "%f %f", location.getLatitude(),location.getLongitude());
                    coordinateTxt.setText(coor);
                    // set text distance
                    float distance = location.distanceTo(LocationHelper.getLocationFromLatLng(MapsFragment.kaabaCoordinate))/1000;
                    distanceTxt.setText(String.format(Locale.getDefault(), "%.2f KM", distance));
                    // set text bearing
                    float bearingToKaaba = LocationHelper.getBearingToLocation(currentLocation, MapsFragment.kaabaCoordinate);
                    bearingTxt.setText(String.format(Locale.getDefault(), "%.2f", bearingToKaaba));
                }
                @Override
                public void onLocationError(String errorMessage) {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onSensorChanged(float azimuth) {
        int azimuthInt = (int) azimuth;
        azimuthTxt.setText(String.format(Locale.getDefault(), "%d", azimuthInt));
    }
}