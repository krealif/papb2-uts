package com.krealif.qiblafinder;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * This code was adapted from
 *  programmerworld.co/android/how-to-create-your-own-compass-android-app-using-Magnetometer-field-and-accelerometer-sensors-in-android-studio-complete-source-code
 */
public class CompassSensor implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorMagnetometer;

    private float[] accelerometerReading = new float[3];
    private float[] magnetometerReading = new float[3];

    private float[] orientationAngles = new float[3];
    private float[] rotationMatrix = new float[9];

    private OnSensorChangedListener listener;

    public CompassSensor(SensorManager sensorManager, OnSensorChangedListener listener) {
        this.sensorManager = sensorManager;
        this.listener = listener;
        sensorMagnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void registerSensor() {
        if(sensorAccelerometer != null) {
            sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if(sensorMagnetometer != null) {
            sensorManager.registerListener(this, sensorMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void unregisterSensor() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int sensorType = sensorEvent.sensor.getType();
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerReading = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magnetometerReading = sensorEvent.values.clone();
                break;
            default:
                return;
        }

        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
        SensorManager.getOrientation(rotationMatrix, orientationAngles);
        int headingDegree = (int) (Math.toDegrees(orientationAngles[0])+360)%360;

        if (listener != null) {
            listener.onSensorChanged(headingDegree);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // code here
    }

    public interface OnSensorChangedListener {
        void onSensorChanged(float azimuth);
    }
}
