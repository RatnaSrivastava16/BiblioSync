package com.example.bibliosync

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import android.util.Log

class Summarizer(private val apiKey: String) {

    private val client = OkHttpClient()

    fun summarizeText(text: String, callback: (String) -> Unit) {
        val json = JSONObject()
        json.put("inputs", text)

        val mediaType = "application/json".toMediaTypeOrNull()
        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://api-inference.huggingface.co/models/facebook/bart-large-cnn")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback("Failed to summarize")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("API_RESPONSE", "Raw: $responseBody")

                if (!response.isSuccessful || responseBody == null) {
                    callback("API Error: ${response.code}")
                    return
                }

                try {
                    val jsonArray = JSONArray(responseBody)
                    val summaryText = jsonArray.getJSONObject(0).getString("summary_text")
                    callback(summaryText)
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback("Failed to parse summary: ${e.message}")
                }
            }
        })
    }
}
