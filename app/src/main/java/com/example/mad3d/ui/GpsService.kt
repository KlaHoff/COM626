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

// this service handles GPS location updates in the background
class GpsService : Service() {

    // binder object used to bind the service to an activity
    private val binder = LocalBinder()
    // location manager to manage GPS location updates
    private var locationManager: LocationManager? = null

    // location listener to receive GPS location updates
    private val locationListener = object : LocationListener {
        // this method is called when the location changes
        override fun onLocationChanged(location: Location) {
            // create an intent to broadcast the new location
            val intent = Intent("com.example.androidservices.LOCATION_UPDATE")
            intent.putExtra("latitude", location.latitude)
            intent.putExtra("longitude", location.longitude)
            sendBroadcast(intent) // broadcast the location update
        }

        // these are required by the interface but are not used
        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    // broadcast receiver to handle start and stop GPS commands
    private val gpsCommandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // check the action of the received intent
            when (intent?.action) {
                "com.example.androidservices.START_GPS" -> startGps() // start GPS updates
                "com.example.androidservices.STOP_GPS" -> stopGps() // stop GPS updates
            }
        }
    }

    // flag to keep track of whether the receiver is registered
    private var isReceiverRegistered = false

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        registerReceiver() // register the broadcast receiver
        startGps() // start GPS updates
        return START_STICKY // restart the service if it gets terminated
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiverSafely() // unregister the broadcast receiver
        stopGps() // stop GPS updates
    }

    // inner class to return the service instance
    inner class LocalBinder : Binder() {
        fun getService(): GpsService = this@GpsService
    }

    // this method starts GPS updates
    private fun startGps() {
        // get the location manager system service
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        try {
            // request location updates from the GPS provider
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
        } catch (ex: SecurityException) {
            // handle exception if location permission is not granted
        }
    }

    // this method stops GPS updates
    private fun stopGps() {
        locationManager?.removeUpdates(locationListener)
    }

    // this method registers the broadcast receiver
    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction("com.example.androidservices.START_GPS")
                addAction("com.example.androidservices.STOP_GPS")
            }
            // register the receiver with the intent filter
            registerReceiver(gpsCommandReceiver, filter, RECEIVER_NOT_EXPORTED)
            isReceiverRegistered = true // update the flag
        }
    }

    // this method unregisters the broadcast receiver safely
    private fun unregisterReceiverSafely() {
        if (isReceiverRegistered) {
            unregisterReceiver(gpsCommandReceiver) // unregister the receiver
            isReceiverRegistered = false // update the flag
        }
    }
}
