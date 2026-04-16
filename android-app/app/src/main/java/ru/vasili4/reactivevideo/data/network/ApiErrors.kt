package ru.vasili4.reactivevideo.data.network

import com.google.gson.Gson
import retrofit2.HttpException
import retrofit2.Response
import ru.vasili4.reactivevideo.data.model.ApiErrorResponse

object ApiErrors {

    private val gson = Gson()

    fun fromThrowable(throwable: Throwable): String {
        return when (throwable) {
            is ApiMessageException -> throwable.message.orEmpty()
            is HttpException -> {
                val rawBody = throwable.response()?.errorBody()?.string()
                parseMessage(rawBody) ?: "HTTP ${throwable.code()}"
            }
            else -> throwable.message ?: "Неизвестная ошибка"
        }
    }

    fun isUnauthorized(throwable: Throwable): Boolean {
        return throwable is HttpException && throwable.code() == 401
    }

    fun <T> requireSuccessful(response: Response<T>) {
        if (!response.isSuccessful) {
            throw ApiMessageException(parseMessage(response.errorBody()?.string()) ?: "HTTP ${response.code()}")
        }
    }

    private fun parseMessage(rawBody: String?): String? {
        if (rawBody.isNullOrBlank()) {
            return null
        }
        return runCatching {
            gson.fromJson(rawBody, ApiErrorResponse::class.java).message
        }.getOrNull() ?: rawBody
    }
}

class ApiMessageException(message: String) : IllegalStateException(message)
