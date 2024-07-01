package com.example.mad3d.utils

import android.content.Context
import androidx.preference.PreferenceManager

// this object helps save and retrieve the filter selection in shared preferences

object FilterPreferenceHelper {
    // this constant is the key used store and retrieve the filter value
    private const val FILTER_KEY = "filter_key"

    // saves the selected filter to shared preferences
    fun saveFilter(context: Context, filter: String?) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit().putString(FILTER_KEY, filter).apply()
    }

    // this function retrieves the saved filter from shared preferences
    fun getFilter(context: Context): String? {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        // return the saved filter value or null if not found
        return sharedPreferences.getString(FILTER_KEY, null)
    }
}
