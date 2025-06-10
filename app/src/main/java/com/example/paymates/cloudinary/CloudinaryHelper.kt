package com.example.paymates.cloudinary

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit


object CloudinaryHelper {

    fun uploadToCloudinary(file: File, onUploaded: (String?) -> Unit) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            .addFormDataPart("upload_preset", "paymates_uploads")
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/dd6of956q/image/upload")
            .post(requestBody)
            .build()

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                onUploaded(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    try {
                        val json = JSONObject(responseBody)
                        val url = json.optString("secure_url", null)
                        onUploaded(url)
                    } catch (e: JSONException) {
                        onUploaded(null)
                    }
                } else {
                    onUploaded(null)
                }
            }
        })
    }

    fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File.createTempFile("temp_image", ".jpg", context.cacheDir)
            file.outputStream().use { inputStream.copyTo(it) }
            file
        } catch (e: IOException) {
            null
        }
    }
}
