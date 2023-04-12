package com.krealif.qiblafinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsFragment extends Fragment implements CompassSensor.OnSensorChangedListener {

    public static GoogleMap map;
    LocationHelper locationHelper;

    public static final LatLng kaabaCoordinate = new LatLng(21.422542, 39.826139);
    private Marker currentMarker;
    private Polyline polyline;
    private Location currentLocation;

    private CompassSensor compassSensor;

    private Button resetCameraBtn;
    private boolean isCameraLocked = true;

    private final BroadcastReceiver permissionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MainActivity.PERMISSION_GRANTED_ACTION)) {
                getMyLocation();
                requireActivity().unregisterReceiver(permissionBroadcastReceiver);
            }
        }
    };

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

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;

            googleMap.addMarker(new MarkerOptions()
                    .position(kaabaCoordinate)
                    .icon(bitmapFromVector(R.drawable.kaaba_icon))
                    .anchor(0.5f, 0.5f));

            map.setOnCameraMoveStartedListener(reason -> {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE && isCameraLocked) {
                    isCameraLocked = false;
                    resetCameraBtn.setVisibility(View.VISIBLE);
                }
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        resetCameraBtn = view.findViewById(R.id.reset_button);
        resetCameraBtn.setOnClickListener(view1 -> {
            isCameraLocked = true;
            resetCameraBtn.setVisibility(View.INVISIBLE);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationHelper = LocationHelper.getInstance(getContext());
        compassSensor = new CompassSensor((SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE), this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getMyLocation();
        requireActivity().registerReceiver(permissionBroadcastReceiver, new IntentFilter(MainActivity.PERMISSION_GRANTED_ACTION));
        requireActivity().registerReceiver(updateLocationBroadcastReceiver, new IntentFilter(MainActivity.UPDATE_LOCATION_ACTION));
        compassSensor.registerSensor();

        if (!isCameraLocked) {
            resetCameraBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        requireActivity().unregisterReceiver(permissionBroadcastReceiver);
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
                    if (currentMarker != null && polyline != null) {
                        currentMarker.remove();
                        polyline.remove();
                    }
                    addCurrentMarker();
                    MainActivity.hideSnackBar();
                }
                @Override
                public void onLocationError(String errorMessage) {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void addCurrentMarker() {
        if (map != null && currentLocation != null) {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

            MarkerOptions newMarker = new MarkerOptions()
                    .position(latLng)
                    .icon(bitmapFromVector(R.drawable.baseline_circle_24))
                    .anchor(0.5f, 0.5f);
            currentMarker = map.addMarker(newMarker);
            drawLine(latLng);
        }
    }

    private void drawLine(LatLng myLoc) {
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(kaabaCoordinate, myLoc)
                .width(6)
                .color(R.color.teal_700);
        polyline = map.addPolyline(polylineOptions);
    }

    private BitmapDescriptor bitmapFromVector(int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(getContext(), vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onSensorChanged(float azimuth) {
        if (isCameraLocked) {
            if (map != null && currentLocation != null) {
                CameraPosition currentCameraPosition = map.getCameraPosition();
                CameraPosition newCameraPosition = new CameraPosition.Builder(currentCameraPosition)
                        .target(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                        .bearing(azimuth)
                        .zoom(map.getCameraPosition().zoom)
                        .build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition), 100, null);
            }
        }
    }
}