package com.example.mad3d.ui

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.RequiresApi

class GpsService : Service() {

    private val binder = LocalBinder()
    private var locationManager: LocationManager? = null
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val intent = Intent("com.example.androidservices.LOCATION_UPDATE")
            intent.putExtra("latitude", location.latitude)
            intent.putExtra("longitude", location.longitude)
            sendBroadcast(intent)
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private val gpsCommandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.example.androidservices.START_GPS" -> startGps()
                "com.example.androidservices.STOP_GPS" -> stopGps()
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val filter = IntentFilter().apply {
            addAction("com.example.androidservices.START_GPS")
            addAction("com.example.androidservices.STOP_GPS")
        }
        registerReceiver(gpsCommandReceiver, filter, RECEIVER_NOT_EXPORTED)
        startGps()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(gpsCommandReceiver)
        stopGps()
    }

    inner class LocalBinder : Binder() {
        fun getService(): GpsService = this@GpsService
    }

    fun startGps() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        try {
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
        } catch (ex: SecurityException) {
            // Handle exception if location permission is not granted
        }
    }

    fun stopGps() {
        locationManager?.removeUpdates(locationListener)
    }
}
