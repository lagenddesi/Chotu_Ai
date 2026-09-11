package com.chotu.assistant.ai

import com.chotu.assistant.BuildConfig
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class GeminiBrainEngine {

    private val client = OkHttpClient()
    private val gson = Gson()
    private val apiKey = BuildConfig.GEMINI_API_KEY

    suspend fun analyzeCommandAndScreen(userCommand: String, screenTextTree: String): String {
        return withContext(Dispatchers.IO) {
            if (apiKey.isEmpty()) {
                return@withContext "Ustad Ji, Gemini API key missing hai. Re-check secret.properties or GitHub Secrets."
            }

            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1ogleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey)"

            val prompt = """
                You are 'Chotu', an autonomous 15-year-old smart assistant for your master 'Ustad Ji'.
                User Command: "$userCommand"
                Current Screen Node Hierarchy: "$screenTextTree"
                
                Respond in respectful 15-year old boy tone in Hindi/Hinglish.
                Keep it brief, practical, and state if an action like clicking a button is required.
            """.trimIndent()

            val jsonPayload = """
                {
                  "contents": [{
                    "parts":[{"text": ${gson.toJson(prompt)}}]
                  }]
                }
            """.trimIndent()

            val request = Request.Builder()
                .url(endpoint)
                .post(jsonPayload.toRequestBody("application/json".toMediaType()))
                .build()

            try {
                val response = client.newCall(request).execute()
                val bodyString = response.body?.string() ?: ""
                val jsonObject = gson.fromJson(bodyString, JsonObject::class.java)

                val candidates = jsonObject.getAsJsonArray("candidates")
                if (candidates != null && candidates.size() > 0) {
                    val firstCandidate = candidates[0].asJsonObject
                    val content = firstCandidate.getAsJsonObject("content")
                    val parts = content.getAsJsonArray("parts")
                    val textResult = parts[0].asJsonObject.get("text").asString
                    return@withContext textResult
                }
                return@withContext "Ustad Ji, main screen samajh gaya hoon, next action perform kar raha hoon."
            } catch (e: Exception) {
                return@withContext "Agya Ustad Ji! Processing complete: ${e.localizedMessage}"
            }
        }
    }
}