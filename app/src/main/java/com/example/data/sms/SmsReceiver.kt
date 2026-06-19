package com.example.data.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.prefs.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val prefs = PreferencesManager(context)
            // Only parse if user has opted in to SMS Auto-logging!
            if (!prefs.smsAutoLoggingEnabled.value) {
                return
            }

            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val body = sms.messageBody ?: continue
                val sender = sms.displayOriginatingAddress ?: "Bank Alert"
                val timestamp = sms.timestampMillis

                Log.d("SmsReceiver", "Intercepted SMS from $sender: $body")

                // Try to parse using Pakistani Bank SMS Regex rules
                val parsed = SmsParser.parse(body, sender, timestamp)
                if (parsed != null) {
                    val db = AppDatabase.getDatabase(context)
                    val transaction = SmsParser.parsedToEntity(parsed, sender)

                    // Insert pending transaction into local DB asynchronously
                    CoroutineScope(Dispatchers.IO).launch {
                        db.transactionDao().insertTransaction(transaction)
                        Log.d("SmsReceiver", "Successfully logged pending transaction to Room: ${transaction.amount} PKR")
                        
                        // We can also trigger a notification warning the user about a pending transaction
                        // but let's draw it instantly on the HomeScreen dashboard!
                    }
                }
            }
        }
    }
}
