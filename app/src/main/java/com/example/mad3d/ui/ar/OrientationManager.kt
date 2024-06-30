package com.example.mad3d.ui.ar

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class OrientationManager(context: Context) : SensorEventListener {

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magneticField: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val accelValues = FloatArray(3)
    private val magValues = FloatArray(3)
    private val prevAccelValues = FloatArray(3) { 0f }
    private val smoothedAccelValues = FloatArray(3)
    private val orientationValues = FloatArray(3)
    private val rotationMatrix = FloatArray(16) // 4x4 matrix
    private val inclinationMatrix = FloatArray(9)
    private val k = 0.1f

    init {
        startListening()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            for (i in 0..2) {
                accelValues[i] = event.values[i]
                smoothedAccelValues[i] = accelValues[i] * k + prevAccelValues[i] * (1 - k)
                prevAccelValues[i] = smoothedAccelValues[i]
            }
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            for (i in 0..2) {
                magValues[i] = event.values[i]
            }
        }

        if (SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, smoothedAccelValues, magValues)) {
            SensorManager.getOrientation(rotationMatrix, orientationValues)
        }
    }

    fun getRotationMatrix(): FloatArray {
        val rotationMatrix4x4 = FloatArray(16)
        System.arraycopy(rotationMatrix, 0, rotationMatrix4x4, 0, 9)
        rotationMatrix4x4[9] = 0f
        rotationMatrix4x4[10] = 0f
        rotationMatrix4x4[11] = 0f
        rotationMatrix4x4[12] = 0f
        rotationMatrix4x4[13] = 0f
        rotationMatrix4x4[14] = 0f
        rotationMatrix4x4[15] = 1f

        return rotationMatrix4x4
    }

    fun startListening() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_UI)
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }
}
