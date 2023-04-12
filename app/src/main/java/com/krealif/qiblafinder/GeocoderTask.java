package com.krealif.qiblafinder;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeocoderTask extends AsyncTask<Double, Void, String> {
    private final AppWidgetManager appWidgetManager;
    private final int appWidgetId;
    private final Geocoder geocoder;
    private final RemoteViews views;

    public GeocoderTask(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        this.appWidgetManager = appWidgetManager;
        this.appWidgetId = appWidgetId;

        geocoder = new Geocoder(context, Locale.getDefault());
        views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
    }

    @Override
    protected String doInBackground(Double... params) {
        double latitude = params[0];
        double longitude = params[1];

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return address.getSubAdminArea();
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder error: " + e.getMessage());
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        // Update the widget UI with the regency/district name
        views.setTextViewText(R.id.my_loc_txt, result);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}