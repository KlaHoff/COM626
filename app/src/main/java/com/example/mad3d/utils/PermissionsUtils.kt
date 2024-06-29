package com.example.mad3d.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mad3d.ui.MainActivity

object PermissionsUtils {

    private const val REQUEST_CODE_LOCATION = 0
    private const val REQUEST_CODE_NOTIFICATIONS = 1

    fun requestPermissions(activity: MainActivity) {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
        } else {
            requestNotificationPermission(activity)
            activity.initService()
        }
    }

    fun onRequestPermissionsResult(activity: MainActivity, requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestNotificationPermission(activity)
                } else {
                    ToastUtils.showToast(activity, "GPS permission denied")
                }
            }
            REQUEST_CODE_NOTIFICATIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    activity.initService()
                }
            }
        }
    }

    private fun requestNotificationPermission(activity: MainActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_NOTIFICATIONS)
            } else {
                activity.initService()
            }
        } else {
            activity.initService()
        }
    }
}
