package com.example.mad3d.ui.ar

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class OrientationManager(context: Context) : SensorEventListener {

    // sensor manager to get access to the device's sensors
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    // getting the accelerometer and magnetic field sensors
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magneticField: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    // arrays to store sensor values
    private val accelValues = FloatArray(3)
    private val magValues = FloatArray(3)
    private val prevAccelValues = FloatArray(3) { 0f } // Initial previous values set to zero
    private val smoothedAccelValues = FloatArray(3)
    private val orientationValues = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val inclinationMatrix = FloatArray(9)
    private val k = 0.1f // Smoothing factor

    init {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    // this gets called whenever there's new sensor data
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // update accelerometer values and smooth them out
            for (i in 0..2) {
                accelValues[i] = event.values[i]
                smoothedAccelValues[i] = accelValues[i] * k + prevAccelValues[i] * (1 - k)
                prevAccelValues[i] = smoothedAccelValues[i]
            }
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            // update magnetic field values
            for (i in 0..2) {
                magValues[i] = event.values[i]
            }
        }

        // calculate the rotation matrix if we have both accelerometer and magnetic field data
        if (SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, smoothedAccelValues, magValues)) {
            // get the orientation values (azimuth, pitch, roll)
            SensorManager.getOrientation(rotationMatrix, orientationValues)
        }
    }

    fun getRotationMatrix(): FloatArray {
        return rotationMatrix
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }
}
