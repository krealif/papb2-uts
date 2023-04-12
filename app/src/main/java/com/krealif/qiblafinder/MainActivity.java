package com.krealif.qiblafinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    public static final String PERMISSION_GRANTED_ACTION = "com.krealif.qiblafinder.PERMISSION_GRANTED";
    public static final String UPDATE_LOCATION_ACTION = "com.krealif.qiblafinder.UPDATE_LOCATION";
    private final String MAPS_FRAGMENT = "TAG_MAPS";
    private final String COMPASS_FRAGMENT = "TAG_COMPASS";
    private final String INFO_FRAGMENT = "TAG_INFO";

    BottomNavigationView navigation;
    static Snackbar snackBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize default fragment
        switchFragment(MAPS_FRAGMENT);

        navigation = findViewById(R.id.bottom_nav_view);
        navigation.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_maps:
                    switchFragment(MAPS_FRAGMENT);
                    break;
                case R.id.navigation_compass:
                    switchFragment(COMPASS_FRAGMENT);
                    break;
                case R.id.navigation_info:
                    switchFragment(INFO_FRAGMENT);
                    break;
            }
            return true;
        });

        enableLocation();

        findViewById(R.id.fab).setOnClickListener(view -> {
            Intent intent = new Intent(UPDATE_LOCATION_ACTION);
            sendBroadcast(intent);
        });
    }

    private void enableLocation() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }else{
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    showSnackbar();
                    Intent intent = new Intent(PERMISSION_GRANTED_ACTION);
                    sendBroadcast(intent);
                }
            } else {
                showAlertDialog();
            }
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.permission_alert)
                .setMessage(R.string.permission_alert_message)
                .setPositiveButton(R.string.permission_alert_close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Close the app
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void switchFragment(String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fr_container);
        Fragment nextFragment = fragmentManager.findFragmentByTag(tag);

        if (currentFragment != null) {
            transaction.detach(currentFragment);
        }

        if (nextFragment == null) {
            nextFragment = createFragment(tag);
            transaction.add(R.id.fr_container, nextFragment, tag);
        } else {
            transaction.attach(nextFragment);
        }
        transaction.commit();
    }

    private Fragment createFragment(String tag) {
        Fragment result = null;
        switch (tag) {
            case MAPS_FRAGMENT:
                result = new MapsFragment();
                break;
            case COMPASS_FRAGMENT:
                result = new CompassFragment();
                break;
            case INFO_FRAGMENT:
                result = new InfoFragment();
                break;
        }
        return result;
    }

    public static void hideSnackBar() {
        if (snackBar != null) {
            snackBar.dismiss();
            snackBar = null;
        }
    }

    private void showSnackbar() {
        snackBar = Snackbar.make(findViewById(R.id.mainActivity), R.string.snackbar_getting_loc, Snackbar.LENGTH_INDEFINITE);
        snackBar.setAnchorView(navigation);
        snackBar.show();
    }
}