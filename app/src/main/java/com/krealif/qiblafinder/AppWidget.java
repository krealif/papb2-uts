package com.krealif.qiblafinder;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Implementation of App Widget functionality.
 */
public class AppWidget extends AppWidgetProvider {

    LocationHelper locationHelper;
    Location currentLocation;

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);

        locationHelper = LocationHelper.getInstance(context);
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted
            // Get the user's location
            locationHelper.getCurrentLocation(context, new LocationHelper.LocationCallback() {
                @Override
                public void onLocationResult(Location location) {
                    currentLocation = location;
                    float bearingToKaaba = LocationHelper.getBearingToLocation(currentLocation, MapsFragment.kaabaCoordinate);
                    views.setTextViewText(R.id.qibla_txt, String.format(Locale.getDefault(),"%d°", (int)bearingToKaaba));

                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    List<Address> addresses;
                    try {
                        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        String locationName = address.getLocality();
                        views.setTextViewText(R.id.my_loc_txt, locationName);
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
                @Override
                public void onLocationError(String errorMessage) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }

        String dateString = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date());
        views.setTextViewText(R.id.last_update_txt, dateString);

        Intent intentUpdate = new Intent(context, AppWidget.class);
        intentUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] idArray = new int[]{appWidgetId};
        intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intentUpdate
                , PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.refresh_button, pendingIntent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}