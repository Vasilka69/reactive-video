package ru.vasili4.reactivevideo.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import ru.vasili4.reactivevideo.data.model.FileItem
import ru.vasili4.reactivevideo.data.model.ImageRecognitionResponse
import ru.vasili4.reactivevideo.data.model.UserCredentials
import ru.vasili4.reactivevideo.data.model.UserResponse
import ru.vasili4.reactivevideo.data.network.ApiErrors
import ru.vasili4.reactivevideo.data.network.ApiFactory
import ru.vasili4.reactivevideo.data.network.ApiMessageException
import ru.vasili4.reactivevideo.data.network.FileUploadRequestBody

class ReactiveVideoRepository(private val context: Context) {

    private val api get() = ApiFactory.api(context)

    suspend fun login(credentials: UserCredentials): String = withContext(Dispatchers.IO) {
        val response = api.login(credentials)
        ApiErrors.requireSuccessful(response)
        val header = response.headers()["Authorization"]
            ?: throw ApiMessageException("Токен не найден в ответе сервера")
        header.removePrefix("Bearer ").trim()
    }

    suspend fun register(credentials: UserCredentials) = withContext(Dispatchers.IO) {
        ApiErrors.requireSuccessful(api.register(credentials))
    }

    suspend fun getUser(): UserResponse = withContext(Dispatchers.IO) {
        api.getUser()
    }

    suspend fun getBuckets(): List<String> = withContext(Dispatchers.IO) {
        api.getBuckets()
    }

    suspend fun createBucket(bucketName: String) = withContext(Dispatchers.IO) {
        ApiErrors.requireSuccessful(api.createBucket(bucketName))
    }

    suspend fun deleteBucket(bucketName: String) = withContext(Dispatchers.IO) {
        ApiErrors.requireSuccessful(api.deleteBucket(bucketName))
    }

    suspend fun getFiles(): List<FileItem> = withContext(Dispatchers.IO) {
        api.getFiles()
    }

    suspend fun uploadFile(uri: Uri, bucket: String, filePath: String = "upload") = withContext(Dispatchers.IO) {
        val fileName = resolveFileName(uri)
        val requestBody = FileUploadRequestBody(context, uri)
        val filePart = MultipartBody.Part.createFormData("file", fileName, requestBody)
        ApiErrors.requireSuccessful(
            api.uploadFile(
                bucket.toRequestBody(MultipartBody.FORM),
                filePath.toRequestBody(MultipartBody.FORM),
                filePart,
            ),
        )
    }

    suspend fun deleteFile(fileId: String) = withContext(Dispatchers.IO) {
        ApiErrors.requireSuccessful(api.deleteFile(fileId))
    }

    suspend fun getTextContent(fileId: String): String = withContext(Dispatchers.IO) {
        api.getFileSync(fileId).string()
    }

    suspend fun getRecognition(fileId: String, useCache: Boolean): ImageRecognitionResponse = withContext(Dispatchers.IO) {
        if (useCache) {
            api.recognizeImageCached(fileId)
        } else {
            api.recognizeImage(fileId)
        }
    }

    suspend fun getTextToSpeech(fileId: String, useCache: Boolean): ByteArray = withContext(Dispatchers.IO) {
        val response = if (useCache) api.textToSpeechCached(fileId) else api.textToSpeech(fileId)
        response.bytes()
    }

    suspend fun getFileBytes(fileId: String, sync: Boolean = false): ByteArray = withContext(Dispatchers.IO) {
        if (sync) api.getFileSync(fileId).bytes() else api.getFileAsync(fileId).bytes()
    }

    suspend fun saveFileToDownloads(fileItem: FileItem): String = withContext(Dispatchers.IO) {
        val response = api.getFileAsync(fileItem.fileId)
        val outputName = buildDownloadName(fileItem.fileName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToMediaStore(response.bytes(), outputName)
        } else {
            saveToAppExternalDownloads(response.bytes(), outputName)
        }
    }

    private fun saveToMediaStore(bytes: ByteArray, outputName: String): String {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, outputName)
            put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw ApiMessageException("Не удалось создать файл в Downloads")

        resolver.openOutputStream(uri)?.use { stream ->
            stream.write(bytes)
        } ?: throw ApiMessageException("Не удалось записать файл")

        return "Downloads/$outputName"
    }

    private fun saveToAppExternalDownloads(bytes: ByteArray, outputName: String): String {
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: throw ApiMessageException("Не удалось получить директорию для загрузки")
        val file = File(directory, outputName)
        FileOutputStream(file).use { it.write(bytes) }
        return file.absolutePath
    }

    private fun buildDownloadName(originalName: String): String {
        val suffix = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "${originalName.substringBeforeLast('.', originalName)}_$suffix.${originalName.substringAfterLast('.', "bin")}"
    }

    private fun resolveFileName(uri: Uri): String {
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                return cursor.getString(nameIndex)
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/') ?: "upload.bin"
    }
}
