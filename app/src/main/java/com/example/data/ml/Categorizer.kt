package com.example.data.ml

import java.util.Locale

data class CategorizationResult(
    val categoryId: String,
    val confidence: Double
)

object Categorizer {
    fun categorize(description: String): CategorizationResult {
        val text = description.lowercase(Locale.getDefault()).trim()
        if (text.isEmpty()) {
            return CategorizationResult("uncategorized", 0.0)
        }

        // Define categorizing patterns with corresponding weights
        val rules = listOf(
            // Groceries
            Rule(listOf("grocery", "grossry", "metro", "super", "mart", "store", "fatah", "sauda", "bazaar", "saman", "hyperstar", "carrefour", "karyana"), "grocery", 0.85),
            
            // Food & Dining
            Rule(listOf("food", "kfc", "mcdonald", "pizza", "dining", "dhaba", "cafe", "tea", "chai", "restaurant", "hotel", "khana", "kharch", "lunch", "dinner", "breakfast", "roti", "biryani", "samosa"), "food", 0.90),
            
            // Transport
            Rule(listOf("transport", "uber", "careem", "indrive", "bykea", "petrol", "diesel", "fuel", "cng", "bus", "train", "ticket", "metrobus", "orange line", "taxi", "fare", "bike", "mechanic", "toll"), "transport", 0.88),
            
            // Bills
            Rule(listOf("bill", "lesco", "kelectric", "k-electric", "sngpl", "ptcl", "electricity", "gas", "water", "internet", "bijli", "utility", "easyload", "jazz", "telenor", "ufone", "zong", "mobile balance", "wifi"), "bills", 0.92),
            
            // Education
            Rule(listOf("school", "college", "university", "fees", "fee", "book", "stationery", "tution", "academy", "register", "copy"), "education", 0.85),
            
            // Salary
            Rule(listOf("salary", "paycheck", "pay", "tankhwah", "salary received", "bonus", "naukri"), "salary", 0.95),
            
            // Other Income
            Rule(listOf("profit", "gift", "refund", "bachat", "cashback", "profit", "interest", "inam", "received from"), "other_income", 0.80)
        )

        var bestMatch: Rule? = null
        var maxMatches = 0

        for (rule in rules) {
            var matchCount = 0
            for (keyword in rule.keywords) {
                if (text.contains(keyword)) {
                    matchCount++
                }
            }
            if (matchCount > maxMatches) {
                maxMatches = matchCount
                bestMatch = rule
            }
        }

        if (bestMatch != null && maxMatches > 0) {
            // Confidence decays slightly if description is extremely long and contain noise
            val lengthPenalty = if (text.length > 50) 0.1 else 0.0
            val confidence = (bestMatch.baseConfidence - lengthPenalty).coerceIn(0.1, 1.0)
            return CategorizationResult(bestMatch.categoryId, confidence)
        }

        return CategorizationResult("uncategorized", 0.3) // confidence is < 0.6 fallback
    }

    private data class Rule(
        val keywords: List<String>,
        val categoryId: String,
        val baseConfidence: Double
    )
}
