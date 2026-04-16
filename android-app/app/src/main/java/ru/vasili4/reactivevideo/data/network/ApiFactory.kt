package ru.vasili4.reactivevideo.data.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.vasili4.reactivevideo.data.local.AppPreferences

object ApiFactory {

    fun api(context: Context): ReactiveVideoApi {
        val preferences = AppPreferences(context)
        return Retrofit.Builder()
            .baseUrl(preferences.getBaseUrl())
            .client(createClient(preferences))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReactiveVideoApi::class.java)
    }

    fun fileUrl(context: Context, fileId: String, async: Boolean = true): String {
        val baseUrl = AppPreferences(context).getBaseUrl()
        val suffix = if (async) "async" else "sync"
        return "${baseUrl}api/v1/reactive/file/$suffix/$fileId"
    }

    fun normalizeBaseUrl(raw: String): String {
        var url = raw.trim()
        if (url.isEmpty()) {
            url = AppPreferences.DEFAULT_BASE_URL
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://$url"
        }
        if (!url.endsWith("/")) {
            url += "/"
        }
        return url
    }

    private fun createClient(preferences: AppPreferences): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                preferences.getToken()
                    ?.takeIf { it.isNotBlank() }
                    ?.let { token -> requestBuilder.header("Authorization", token) }
                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(logging)
            .build()
    }
}
