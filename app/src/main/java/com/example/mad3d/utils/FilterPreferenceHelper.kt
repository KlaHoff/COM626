package com.example.mad3d.utils

import android.content.Context
import androidx.preference.PreferenceManager

object FilterPreferenceHelper {
    private const val FILTER_KEY = "filter_key"

    fun saveFilter(context: Context, filter: String?) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit().putString(FILTER_KEY, filter).apply()
    }

    fun getFilter(context: Context): String? {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getString(FILTER_KEY, null)
    }
}
