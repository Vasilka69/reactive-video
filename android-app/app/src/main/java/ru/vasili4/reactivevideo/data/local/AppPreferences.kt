package ru.vasili4.reactivevideo.data.local

import android.content.Context
import ru.vasili4.reactivevideo.data.network.ApiFactory

class AppPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun setToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    fun getBaseUrl(): String = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL

    fun setBaseUrl(url: String) {
        prefs.edit().putString(KEY_BASE_URL, ApiFactory.normalizeBaseUrl(url)).apply()
    }

    companion object {
        private const val PREFS_NAME = "reactive_video"
        private const val KEY_TOKEN = "token"
        private const val KEY_BASE_URL = "base_url"
        const val DEFAULT_BASE_URL = "http://10.0.2.2:8081/"
    }
}
