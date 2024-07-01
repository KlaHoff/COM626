package com.example.mad3d.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mad3d.ui.MainActivity

object PermissionsUtils {

    private const val REQUEST_CODE_LOCATION = 0
    private const val REQUEST_CODE_NOTIFICATIONS = 1
    private const val REQUEST_CODE_CAMERA = 2

    fun requestPermissions(activity: MainActivity) {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest.toTypedArray(),
                REQUEST_CODE_CAMERA
            )
        } else {
            requestNotificationPermission(activity)
            activity.initService()
        }
    }

    fun requestPermissions(fragment: Fragment, permissions: Array<String>) {
        fragment.requestPermissions(permissions, REQUEST_CODE_CAMERA)
    }

    fun checkPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

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

    fun onRequestPermissionsResult(activity: MainActivity, requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_LOCATION, REQUEST_CODE_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    requestNotificationPermission(activity)
                } else {
                    ToastUtils.showToast(activity, "Necessary permissions denied")
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
