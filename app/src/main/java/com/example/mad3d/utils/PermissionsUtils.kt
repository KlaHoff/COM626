package com.example.mad3d.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mad3d.ui.MainActivity

// utility object for handling permissions in the app
object PermissionsUtils {

    // constants representing request codes for different permissions
    private const val REQUEST_CODE_LOCATION = 0
    private const val REQUEST_CODE_NOTIFICATIONS = 1
    private const val REQUEST_CODE_CAMERA = 2

    // requests necessary permissions for the app from an activity
    fun requestPermissions(activity: MainActivity) {
        // list to hold permissions that need to be requested
        val permissionsToRequest = mutableListOf<String>()

        // check if location permission is granted
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // check if camera permission is granted
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }

        // if there are permissions to request, request them
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest.toTypedArray(),
                REQUEST_CODE_CAMERA
            )
        } else {
            // if no permissions to request, check for notification permission and start service
            requestNotificationPermission(activity)
            activity.initService()
        }
    }

    // requests permissions from a fragment
    fun requestPermissions(fragment: Fragment, permissions: Array<String>) {
        fragment.requestPermissions(permissions, REQUEST_CODE_CAMERA)
    }

    // checks if all specified permissions are granted
    fun checkPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // handles the result of a permissions request and invokes a callback with the result
    fun onRequestPermissionsResult(
        grantResults: IntArray,
        onPermissionsResult: (Boolean) -> Unit
    ) {
        if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            onPermissionsResult(true)
        } else {
            onPermissionsResult(false)
        }
    }

    // handles the result of a permissions request for the activity
    fun onRequestPermissionsResult(activity: MainActivity, requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_LOCATION, REQUEST_CODE_CAMERA -> {
                // if all permissions are granted, request notification permission
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    requestNotificationPermission(activity)
                } else {
                    ToastUtils.showToast(activity, "Necessary permissions denied")
                }
            }
            REQUEST_CODE_NOTIFICATIONS -> {
                // if notification permission is granted, start the service
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    activity.initService()
                }
            }
        }
    }

    // requests notification permission if needed
    private fun requestNotificationPermission(activity: MainActivity) {
        // check if the Android version is Tiramisu or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // check if notification permission is granted
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                // request notification permission
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_NOTIFICATIONS)
            } else {
                // if permission is already granted, start the service
                activity.initService()
            }
        } else {
            // if the Android version is lower, just start the service
            activity.initService()
        }
    }
}
