package com.example.mad3d.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.mad3d.R

// This is just a customized Toast because I don't like the default one

object ToastUtils {
    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.custom_toast, null)

        val text: TextView = view.findViewById(R.id.toast_text)
        text.text = message

        val toast = Toast(context)
        toast.duration = duration
        toast.view = view

        // offset for screen height to place it better on the screen
        val displayMetrics = context.resources.displayMetrics
        val yOffset = (displayMetrics.heightPixels * 0.55).toInt()

        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, yOffset)
        toast.show()
    }
}
