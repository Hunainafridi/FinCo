package com.example.data.sms

import com.example.data.ml.Categorizer
import com.example.data.model.Transaction
import java.util.Locale
import java.util.regex.Pattern

data class ParsedSms(
    val amount: Double,
    val isIncome: Boolean,
    val merchant: String,
    val accountNumber: String?,
    val dateMillis: Long,
    val detectedCategoryId: String,
    val confidence: Double
)

object SmsParser {
    // List of regex patterns with matching groups
    private val patterns = listOf(
        // Format 1: Rs. 1,500.00 debited from your account ending 1234 at McDonald's
        SmsFormat(
            pattern = Pattern.compile("rs\\.?\\s*([\\d,]+\\.?\\d*)\\s+debited\\s+from\\s+your\\s+account\\s+ending\\s+(\\d+)\\s+at\\s+([^\\n]+)", Pattern.CASE_INSENSITIVE),
            amountGroup = 1,
            isIncome = false,
            accountGroup = 2,
            merchantGroup = 3
        ),
        // Format 2: Alert: Rs. 4,200.00 spent on card 4321 at Metro Grocery on 18-06
        SmsFormat(
            pattern = Pattern.compile("rs\\.?\\s*([\\d,]+\\.?\\d*)\\s+spent\\s+on\\s+card\\s+(\\d+)\\s+at\\s+([^\\n]+)", Pattern.CASE_INSENSITIVE),
            amountGroup = 1,
            isIncome = false,
            accountGroup = 2,
            merchantGroup = 3
        ),
        // Format 3: Rs. 25,000.00 credited to your account ending 5678
        SmsFormat(
            pattern = Pattern.compile("rs\\.?\\s*([\\d,]+\\.?\\d*)\\s+credited\\s+to\\s+your\\s+account\\s+ending\\s+(\\d+)", Pattern.CASE_INSENSITIVE),
            amountGroup = 1,
            isIncome = true,
            accountGroup = 2,
            merchantGroup = null
        ),
        // Format 4: Your account ending in 9876 has been credited with PKR 15,000.00
        SmsFormat(
            pattern = Pattern.compile("account\\s+ending\\s+in\\s+(\\d+)\\s+has\\s+been\\s+credited\\s+with\\s+(?:pkr|rs\\.?)\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE),
            amountGroup = 2,
            isIncome = true,
            accountGroup = 1,
            merchantGroup = null
        ),
        // Format 5: Transaction Alert: PKR 2,800.00 with card 1122 at Shell Fuel Station
        SmsFormat(
            pattern = Pattern.compile("pkr\\s*([\\d,]+\\.?\\d*)\\s+with\\s+card\\s+(\\d+)\\s+at\\s+([^\\n]+)", Pattern.CASE_INSENSITIVE),
            amountGroup = 1,
            isIncome = false,
            accountGroup = 2,
            merchantGroup = 3
        )
    )

    fun parse(smsBody: String, sender: String?, timestamp: Long): ParsedSms? {
        val cleanBody = smsBody.replace("\n", " ").trim()
        for (format in patterns) {
            val matcher = format.pattern.matcher(cleanBody)
            if (matcher.find()) {
                try {
                    val amountStr = matcher.group(format.amountGroup)?.replace(",", "") ?: continue
                    val amount = amountStr.toDouble()

                    val isIncome = format.isIncome
                    val accountNumber = format.accountGroup?.let { matcher.group(it) }
                    
                    var merchant = "Unknown Transaction"
                    if (format.merchantGroup != null) {
                        val mGroup = matcher.group(format.merchantGroup)
                        if (mGroup != null) {
                            merchant = mGroup.split(Pattern.compile(" on | at |\\.|,| ending | with "), 2)[0].trim()
                        }
                    } else {
                        merchant = if (isIncome) "Income Transfer" else "Expense Transfer"
                    }

                    // Run the categorizer on the merchant name
                    val catResult = Categorizer.categorize(merchant)

                    return ParsedSms(
                        amount = amount,
                        isIncome = isIncome,
                        merchant = merchant,
                        accountNumber = accountNumber,
                        dateMillis = timestamp,
                        detectedCategoryId = catResult.categoryId,
                        confidence = catResult.confidence
                    )
                } catch (e: Exception) {
                    // Fail-safe per pattern
                    continue
                }
            }
        }
        return null
    }

    fun parsedToEntity(sms: ParsedSms, sender: String?): Transaction {
        return Transaction(
            amount = sms.amount,
            currency = "PKR",
            dateMillis = sms.dateMillis,
            description = sms.merchant + (sender?.let { " ($it)" } ?: ""),
            categoryId = sms.detectedCategoryId,
            source = "sms",
            isIncome = sms.isIncome,
            mlConfidence = sms.confidence,
            rawSmsSender = sender,
            isPending = true // Must be confirmed by user!
        )
    }

    private data class SmsFormat(
        val pattern: Pattern,
        val amountGroup: Int,
        val isIncome: Boolean,
        val accountGroup: Int?,
        val merchantGroup: Int?
    )
}
