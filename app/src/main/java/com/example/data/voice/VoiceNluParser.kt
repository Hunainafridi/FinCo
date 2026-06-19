package com.example.data.voice

import com.example.data.ml.Categorizer
import com.example.data.model.Transaction
import java.util.Locale
import java.util.regex.Pattern

data class VoiceParseResult(
    val amount: Double,
    val isIncome: Boolean,
    val categoryId: String,
    val text: String,
    val confidence: Double
)

object VoiceNluParser {
    // Dictionary of keywords per language mapped to categoryIds
    private val englishKeywords = mapOf(
        "grocery" to listOf("grocery", "groceries", "supermarket", "mart", "store", "bazaar", "rashan"),
        "food" to listOf("eat", "food", "dining", "lunch", "dinner", "breakfast", "restaurant", "cafe", "tea", "chai", "hotel", "biryani", "burger", "pizza"),
        "transport" to listOf("transport", "ride", "uber", "careem", "indrive", "bykea", "petrol", "fuel", "diesel", "ticket", "bus", "taxi", "fare", "bike"),
        "bills" to listOf("bill", "lesco", "kelectric", "ptcl", "electricity", "gas", "utility", "easyload", "load", "mobile balance", "wifi", "internet"),
        "education" to listOf("school", "college", "university", "fees", "fee", "book", "stationery", "tution", "academy"),
        "salary" to listOf("salary", "paycheck", "pay", "bonus", "earnings", "salary received"),
        "other_income" to listOf("gift", "refund", "profit", "cashback", "saving", "bachat")
    )

    private val urduKeywords = mapOf(
        "grocery" to listOf("sauda", "rashan", "saman", "karyana", "sabzi", "bazaar", "ghee", "chini"),
        "food" to listOf("khana", "khurch", "chai", "dastarkhwan", "chaye", "rotiyan", "biryani", "dhaba", "nashta", "nashtay"),
        "transport" to listOf("gari", "petrol", "fuel", "safari", "kiraia", "kiraya", "bykea", "uber", "careem", "indrive", "ticket"),
        "bills" to listOf("bill", "bijli", "gas", "utility", "easyload", "load", "phone", "net", "internet"),
        "education" to listOf("school", "college", "university", "fees", "fee", "pahrhai", "parhai", "kitab", "stationery"),
        "salary" to listOf("tankhwah", "tanxwahn", "kamayi", "naukri", "mulaazmat", "amdani"),
        "other_income" to listOf("paisa mila", "inam", "tofa", "gift", "bachat", "munafa")
    )

    private val pashtoKeywords = mapOf(
        "grocery" to listOf("sauda", "khoraki", "rashan", "bazaar", "saman"),
        "food" to listOf("khuraak", "choe", "khaney", "masale", "dodey", "doday", "nashta"),
        "transport" to listOf("kiraya", "petrol", "tel", "gari", "motar", "safari", "bykea", "ticket"),
        "bills" to listOf("bill", "brekhna", "gas", "easyload", "utility"),
        "education" to listOf("school", "college", "fees", "fee", "kitab", "parsha"),
        "salary" to listOf("tankhwah", "amdani", "paise milao", "shari"),
        "other_income" to listOf("inam", "tofa", "gift", "bachat", "faida")
    )

    fun parse(text: String, language: String): VoiceParseResult? {
        val cleanText = text.lowercase(Locale.getDefault()).trim()
        if (cleanText.isEmpty()) return null

        // 1. Extract Number (Amount) using regex
        val numberPattern = Pattern.compile("(\\d+(?:,\\d+)*(?:\\.\\d+)?)")
        val matcher = numberPattern.matcher(cleanText)
        var amount = 0.0
        if (matcher.find()) {
            try {
                amount = matcher.group(1)!!.replace(",", "").toDouble()
            } catch (e: Exception) {
                // Return null if cannot parse number
                return null
            }
        } else {
            return null // No amount spoken
        }

        // 2. Identify Keywords and select Category based on language
        val keywordsDict = when (language.lowercase(Locale.getDefault())) {
            "ur" -> urduKeywords
            "ps" -> pashtoKeywords
            else -> englishKeywords
        }

        var detectedCategory = "uncategorized"
        var bestConfidence = 0.3
        var maxMatches = 0

        for ((catId, keywords) in keywordsDict) {
            var matchCount = 0
            for (kw in keywords) {
                if (cleanText.contains(kw)) {
                    matchCount++
                }
            }
            if (matchCount > maxMatches) {
                maxMatches = matchCount
                detectedCategory = catId
                bestConfidence = 0.85
            }
        }

        // Fallback to general Categorizer if no language-specific keywords match
        if (detectedCategory == "uncategorized") {
            val generalCat = Categorizer.categorize(cleanText)
            detectedCategory = generalCat.categoryId
            bestConfidence = generalCat.confidence
        }

        // Determine if it represents income
        val isIncome = detectedCategory == "salary" || detectedCategory == "other_income" || 
                cleanText.contains("earned") || cleanText.contains("mili") || 
                cleanText.contains("shwa") || cleanText.contains("received")

        return VoiceParseResult(
            amount = amount,
            isIncome = isIncome,
            categoryId = detectedCategory,
            text = text,
            confidence = bestConfidence
        )
    }
}
