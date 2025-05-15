package com.example.foccuss.data.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class ApiConfig(private val context: Context) {
    private val TAG = "ApiConfig"
    private val PREF_NAME = "api_config"
    private val KEY_API_URL = "api_url"
    private val DEFAULT_API_URL = "http://IP_SERVER:3000"
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    fun getApiUrl(): String {
        val url = prefs.getString(KEY_API_URL, DEFAULT_API_URL) ?: DEFAULT_API_URL
        Log.d(TAG, "Getting API URL: $url")
        return url
    }
    
    fun setApiUrl(url: String) {
        prefs.edit().putString(KEY_API_URL, url).apply()
    }
} 