package ru.vasili4.reactivevideo.data.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import ru.vasili4.reactivevideo.data.model.FileItem
import ru.vasili4.reactivevideo.data.model.ImageRecognitionResponse
import ru.vasili4.reactivevideo.data.model.UserCredentials
import ru.vasili4.reactivevideo.data.model.UserResponse

interface ReactiveVideoApi {

    @POST("api/v1/reactive/user/login")
    suspend fun login(
        @retrofit2.http.Body credentials: UserCredentials,
    ): Response<Unit>

    @POST("api/v1/reactive/user/register")
    suspend fun register(
        @retrofit2.http.Body credentials: UserCredentials,
    ): Response<Unit>

    @GET("api/v1/reactive/user")
    suspend fun getUser(): UserResponse

    @GET("api/v1/s3-bucket")
    suspend fun getBuckets(): List<String>

    @POST("api/v1/s3-bucket")
    suspend fun createBucket(
        @Query("bucketName") bucketName: String,
    ): Response<Unit>

    @DELETE("api/v1/s3-bucket")
    suspend fun deleteBucket(
        @Query("bucketName") bucketName: String,
    ): Response<Unit>

    @GET("api/v1/reactive/file-metadata")
    suspend fun getFiles(): List<FileItem>

    @Multipart
    @POST("api/v1/reactive/file")
    suspend fun uploadFile(
        @Part("bucket") bucket: RequestBody,
        @Part("filePath") filePath: RequestBody,
        @Part file: MultipartBody.Part,
    ): Response<String>

    @DELETE("api/v1/reactive/file/{id}")
    suspend fun deleteFile(
        @Path("id") fileId: String,
    ): Response<Unit>

    @Streaming
    @GET("api/v1/reactive/file/async/{id}")
    suspend fun getFileAsync(
        @Path("id") fileId: String,
    ): ResponseBody

    @Streaming
    @GET("api/v1/reactive/file/sync/{id}")
    suspend fun getFileSync(
        @Path("id") fileId: String,
    ): ResponseBody

    @GET("api/v1/reactive/image-recognition/{id}")
    suspend fun recognizeImage(
        @Path("id") fileId: String,
    ): ImageRecognitionResponse

    @GET("api/v1/reactive/image-recognition/cached/{id}")
    suspend fun recognizeImageCached(
        @Path("id") fileId: String,
    ): ImageRecognitionResponse

    @Streaming
    @GET("api/v1/reactive/text-to-speech/{id}")
    suspend fun textToSpeech(
        @Path("id") fileId: String,
    ): ResponseBody

    @Streaming
    @GET("api/v1/reactive/text-to-speech/cached/{id}")
    suspend fun textToSpeechCached(
        @Path("id") fileId: String,
    ): ResponseBody
}
