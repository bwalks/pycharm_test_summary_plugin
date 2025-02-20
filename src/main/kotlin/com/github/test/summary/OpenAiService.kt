package com.github.test.summary

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.credentialStore.CredentialAttributes
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.Service
import com.github.test.summary.settings.TestSummarySettings
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

@Service
class OpenAiService {
    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    fun getSummary(errorMessage: String): String {
        val apiKey = getOpenAiApiKey() ?: throw IllegalStateException("OpenAI API key not found")
        
        val requestJson = JSONObject()
            .put("model", TestSummarySettings.getInstance().openAiModel)
            .put("messages", listOf(
                JSONObject()
                    .put("role", "system")
                    .put("content", TestSummarySettings.getInstance().systemPrompt),
                JSONObject()
                    .put("role", "user")
                    .put("content", TestSummarySettings.getInstance().userPromptTemplate.replace("{error_message}", errorMessage))
            ))
            .toString()

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestJson.toRequestBody(JSON))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected response: ${response.code}")
            
            val responseBody = response.body?.string() ?: throw IOException("Empty response body")
            val jsonResponse = JsonParser.parseString(responseBody).asJsonObject
            return jsonResponse
                .getAsJsonArray("choices")
                .get(0).asJsonObject
                .getAsJsonObject("message")
                .get("content").asString
        }
    }

    private fun getOpenAiApiKey(): String? {
        val credentialAttributes = CredentialAttributes("OpenAI_API_Key")
        return PasswordSafe.instance.getPassword(credentialAttributes)
    }
}
