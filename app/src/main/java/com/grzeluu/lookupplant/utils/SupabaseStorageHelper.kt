package com.grzeluu.lookupplant.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object SupabaseStorageHelper {

    private const val SUPABASE_URL = "https://ecrbhjdrxmdaglssqhfo.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVjcmJoamRyeG1kYWdsc3NxaGZvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTkxNjk4OTAsImV4cCI6MjA3NDc0NTg5MH0.3V0K5YOY9aPXeQKZ91Ib9JBAV5Nb7x3mQ2Get846U6U"
    private const val BUCKET_NAME = "plant-images"

    interface UploadCallback {
        fun onSuccess(publicUrl: String)
        fun onFailure(error: String)
    }

    fun uploadImage(
        context: Context,
        imageUri: Uri,
        fileName: String,
        callback: UploadCallback
    ) {
        Thread {
            try {
                val file = uriToFile(context, imageUri)
                if (file == null || !file.exists()) {
                    callback.onFailure("Local file not found for uri: $imageUri")
                    return@Thread
                }

                val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
                val encodedFileName = URLEncoder.encode(fileName, "UTF-8")
                val uploadUrl = "$SUPABASE_URL/storage/v1/object/$BUCKET_NAME/$encodedFileName"

                val client = OkHttpClient.Builder()
                    .callTimeout(60, TimeUnit.SECONDS)
                    .build()

                val fileBytes = file.readBytes()

                val putRequestBody = fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())

                val putRequest = Request.Builder()
                    .url(uploadUrl)
                    .put(putRequestBody)
                    .addHeader("Authorization", "Bearer $SUPABASE_KEY")
                    .addHeader("apikey", SUPABASE_KEY) // иногда требуется
                    .addHeader("Content-Type", mimeType)
                    .build()

                val putResponse = client.newCall(putRequest).execute()
                val putBodyString = putResponse.body?.string()

                if (putResponse.isSuccessful) {
                    val publicUrl = "$SUPABASE_URL/storage/v1/object/public/$BUCKET_NAME/$encodedFileName"
                    callback.onSuccess(publicUrl)
                    file.delete()
                    return@Thread
                } else {
                    Log.e("ADD_PLANT", "PUT failed code=${putResponse.code} msg=${putResponse.message} body=$putBodyString")
                }

                val requestFileBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
                val multipartBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", fileName, requestFileBody)
                    .build()

                val multipartRequest = Request.Builder()
                    .url("$SUPABASE_URL/storage/v1/object/$BUCKET_NAME/")
                    .post(multipartBody)
                    .addHeader("Authorization", "Bearer $SUPABASE_KEY")
                    .addHeader("apikey", SUPABASE_KEY)
                    .build()

                val multipartResponse = client.newCall(multipartRequest).execute()
                val multipartBodyString = multipartResponse.body?.string()

                if (multipartResponse.isSuccessful) {
                    val publicUrl = "$SUPABASE_URL/storage/v1/object/public/$BUCKET_NAME/$encodedFileName"
                    callback.onSuccess(publicUrl)
                } else {
                    Log.e("ADD_PLANT", "MULTIPART failed code=${multipartResponse.code} msg=${multipartResponse.message} body=$multipartBodyString")
                    callback.onFailure("Upload failed (PUT and multipart both failed). PUT: ${putResponse.code} ${putBodyString}; MULTIPART: ${multipartResponse.code} ${multipartBodyString}")
                }

                file.delete()
            } catch (e: Exception) {
                Log.e("ADD_PLANT", "Exception during upload", e)
                callback.onFailure("Exception: ${e.message}")
            }
        }.start()
    }


    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open input stream")

        val tempFile = File(context.cacheDir, "temp_upload_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(tempFile)

        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }
}