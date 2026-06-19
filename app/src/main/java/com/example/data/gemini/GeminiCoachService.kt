package com.example.data.gemini

import android.content.Context
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

class GeminiCoachService(context: Context) {
    private val sharedPrefs = context.getSharedPreferences("finco_coaching_cache", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiService: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }

    suspend fun getCoachingInsight(
        totalSpend: Double,
        categoryBreakdownSec: Map<String, Double>,
        forecastMessage: String,
        language: String // "en", "ur", "ps"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "FinCo: Roman translite/translation of spend insight cached. Please configure GEMINI_API_KEY in the Secrets Panel to get real-time AI-powered voice/text coaching!"
        }

        // Construct the structured prompt
        val categorySummary = categoryBreakdownSec.entries.joinToString { "${it.key}: Rs. ${it.value}" }
        val promptText = buildString {
            append("Analyse this structured monthly spend summary for an underbanked smartphone user in Pakistan:\n")
            append("- Current Monthly Spend: Rs. $totalSpend\n")
            append("- Breakdown by category: $categorySummary\n")
            append("- Direct Cashflow Forecast: $forecastMessage\n\n")
            append("Instructions:\n")
            when (language) {
                "ur" -> {
                    append("1. Write the advice in precisely 2-3 sentences in Roman Urdu (phonetic Urdu using English keyboard letters, e.g., 'Aapka kharch is haftay bara hai kyun kay...'). Do NOT use Arabic or Urdu keyboard script. Use Roman Urdu only.\n")
                    append("2. Speak like a friendly, companionable local financial buddy ('bhai' or 'dost'). Be encouraging and down-to-earth.\n")
                }
                "ps" -> {
                    append("1. Write the advice in precisely 2-3 sentences in Roman Pashto (phonetic Pashto using English letters, e.g., 'Paisa de der kharch kra...'). Do NOT use Pashto keyboard script. Use Roman Pashto only.\n")
                    append("2. Speak like a helpful, respectful friend. Be practical.\n")
                }
                else -> {
                    append("1. Write the advice in precisely 2-3 sentences in simple plain English.\n")
                    append("2. Speak like an encouraging, friendly financial buddy. Frame suggestions within low-income household budget realities.\n")
                }
            }
            append("3. Address budget shortfalls directly if any, otherwise give a smart local tip for saving (e.g., buying in bulk, cutting down on extra tea/cafes, saving in a digital wallet).")
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = promptText))))
        )

        try {
            val response = apiService.generateContent(apiKey, request)
            val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!resultText.isNullOrBlank()) {
                val cleanResultText = resultText.trim()
                // Cache the fresh insight with selected language
                sharedPrefs.edit()
                    .putString("cached_insight_$language", cleanResultText)
                    .putLong("cached_insight_timestamp_$language", System.currentTimeMillis())
                    .apply()
                return@withContext cleanResultText
            } else {
                return@withContext getCachedInsightOrDefault(language)
            }
        } catch (e: Exception) {
            return@withContext getCachedInsightOrDefault(language)
        }
    }

    fun getCachedInsightOrDefault(language: String): String {
        val cached = sharedPrefs.getString("cached_insight_$language", null)
        val timestamp = sharedPrefs.getLong("cached_insight_timestamp_$language", 0)
        
        if (cached != null) {
            val lastUpdated = android.text.format.DateFormat.format("dd MMM yyyy, hh:mm a", timestamp)
            return "$cached\n\n(Offline Cache - Last Updated: $lastUpdated)"
        }

        // Return a localized friendly default advice based on language
        return when (language) {
            "ur" -> "Bachat ki aadat dalein. Har mahine kuch paise alag rakhna aapko achanak kharche se bacha sakta hai. Apna budget tight rakhein aur chai-dhaba ke fuzool kharche thore kam karein."
            "ps" -> "Khpal baiza bach kawal saba ror dpara der da fawaidey kar de. Kar khana bandi kharch kam kra aao sparala zata sara satey."
            else -> "Try creating a direct savings habit. Setting aside even a tiny weekly sum helps buffer your household against sudden medical or utility expenses. Monitor your utility bills closely this month."
        }
    }
}
