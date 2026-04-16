package ru.vasili4.reactivevideo.data.network

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

class FileUploadRequestBody(
    private val context: Context,
    private val uri: Uri,
) : RequestBody() {

    override fun contentType() = context.contentResolver.getType(uri)?.toMediaTypeOrNull()

    override fun contentLength(): Long {
        return context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
    }

    override fun writeTo(sink: BufferedSink) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            sink.writeAll(input.source())
        } ?: error("Не удалось открыть файл для загрузки")
    }
}
