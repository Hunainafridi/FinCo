package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.Transaction
import com.example.ui.viewmodel.MainViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    lang: String
) {
    val context = LocalContext.current
    val transactions by viewModel.confirmedTransactions.collectAsState()
    
    val appLanguage by viewModel.appLanguage.collectAsState()
    val startingBalance by viewModel.startingBalance.collectAsState()
    val pinPasscode by viewModel.pinPasscode.collectAsState()
    val smsLogging by viewModel.smsAutoLoggingEnabled.collectAsState()

    val isSandboxActive by viewModel.isSandboxActive.collectAsState()
    val sandboxEmail by viewModel.sandboxEmail.collectAsState()

    var isAddingPin by remember { mutableStateOf(false) }
    var pinText by remember { mutableStateOf("") }
    var pinTextVerify by remember { mutableStateOf("") }
    var pinErrorText by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Core Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (lang == "ur") "App Ki Tarjeehaat" else if (lang == "ps") "Zat Tarjeehaat" else "Settings & Export",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (lang == "ur") "Khaarje exports aur zubaan badlein" else "Tweak operational defaults, manage PIN passcodes, and trigger CSV shares.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Section 0: Operational Account Security Status Card
        val firebaseUser by viewModel.firebaseUser.collectAsState()
        Physical3DCard(
            modifier = Modifier.fillMaxWidth().testTag("firebase_account_card"),
            shape = RoundedCornerShape(20.dp),
            backgroundColor = MaterialTheme.colorScheme.surface
        ) {
            Text(
                text = if (lang == "ur") "اکاؤنٹ سیکورٹی کی ترتیب" else "Security & Account Integration",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = if (firebaseUser != null) {
                    "🌐 Connected Cloud: ${firebaseUser?.email}"
                } else if (isSandboxActive) {
                    "🔒 Local Sandbox: $sandboxEmail"
                } else {
                    "👤 Running in Offline / Guest Mode"
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Physical3DButton(
                onClick = {
                    viewModel.signOutFromFirebase()
                    viewModel.setOfflineMode(false)
                    Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show()
                },
                text = if (lang == "ur") "Log Out" else "Sign Out / Switch Account",
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                modifier = Modifier.fillMaxWidth().testTag("firebase_logout_button")
            )
        }

        // Section 1: Tongue Select (Language Change)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("language_settings_card"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (lang == "ur") "Zuban Muntakhib Karein" else "Application Language Choice",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val languages = listOf("en" to "English", "ur" to "اردو (Urdu)", "ps" to "پښتو (Pashto)")
                    languages.forEach { (code, name) ->
                        val active = appLanguage == code
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .clickable { viewModel.changeLanguage(code) }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
            }
        }

        // Section 2: Automations (SMS Tracking Toggle)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == "ur") "Khufia SMS Tracking" else "SMS Alert Parsing",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (lang == "ur") "Bank k SMS aane par pending entry tayyar karein" else "Intercept incoming bank alert messages locally offline.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
                    )
                }
                Switch(
                    checked = smsLogging,
                    onCheckedChange = { viewModel.setSmsLoggingEnabled(it) },
                    modifier = Modifier.testTag("settings_sms_switch")
                )
            }
        }

        // Section 3: Lock Security Options
        Physical3DCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            backgroundColor = MaterialTheme.colorScheme.surface
        ) {
            Text(
                text = if (lang == "ur") "PIN Lock Ki Hifazat" else "App PIN lock credentials",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (pinPasscode != null) (if (lang == "ur") "PIN active hai" else "PIN Lock Screen is Active") else (if (lang == "ur") "PIN band hai" else "App is currently Unsecured"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
                )
                
                if (pinPasscode != null) {
                    TextButton(
                        onClick = { viewModel.completeOnboarding(startingBalance, appLanguage, null, smsLogging) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(if (lang == "ur") "Khatam" else "Disable Lock")
                    }
                } else {
                    Physical3DSmallButton(
                        onClick = { isAddingPin = true },
                        text = if (lang == "ur") "Set" else "Set PIN",
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        // Section 4: Data Export CSV
        Physical3DCard(
            modifier = Modifier.fillMaxWidth().testTag("export_csv_card"),
            shape = RoundedCornerShape(20.dp),
            backgroundColor = MaterialTheme.colorScheme.surface
        ) {
            Text(
                text = if (lang == "ur") "Khaarje File Export" else "Export Offline Data Sheet",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = if (lang == "ur") "Apna tamam kharch CSV format k sath save/share karein" else "Instantly share or backup your local ledger matching any spreadsheet app.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
            )
            Spacer(modifier = Modifier.height(14.dp))

            Physical3DButton(
                onClick = {
                    if (eventsToCsvAndShare(context, transactions)) {
                        Toast.makeText(context, "CSV ledger exported!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "No transactions to export.", Toast.LENGTH_SHORT).show()
                    }
                },
                text = if (lang == "ur") "Share Karein" else "Share Ledger CSV",
                modifier = Modifier.fillMaxWidth().testTag("trigger_csv_export")
            )
        }
    }

    if (isAddingPin) {
        AlertDialog(
            onDismissRequest = { isAddingPin = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinText.length != 4) {
                            pinErrorText = "PIN must be exactly 4 digits"
                            return@Button
                        }
                        if (pinText != pinTextVerify) {
                            pinErrorText = "PIN credentials do not match"
                            return@Button
                        }
                        viewModel.completeOnboarding(startingBalance, appLanguage, pinText, smsLogging)
                        pinText = ""
                        pinTextVerify = ""
                        pinErrorText = null
                        isAddingPin = false
                    },
                    modifier = Modifier.testTag("save_pin_button")
                ) {
                    Text("Set PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddingPin = false }) {
                    Text("Cancel")
                }
            },
            title = {
                Text("Setup lock PIN", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add a secure 4 digit lock screen passcode:")
                    OutlinedTextField(
                        value = pinText,
                        onValueChange = { if (it.length <= 4) pinText = it },
                        label = { Text("PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pinTextVerify,
                        onValueChange = { if (it.length <= 4) pinTextVerify = it },
                        label = { Text("Verify PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (pinErrorText != null) {
                        Text(text = pinErrorText!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }
        )
    }
}

// Full CSV Ledger Export utility
fun eventsToCsvAndShare(context: Context, transactions: List<Transaction>): Boolean {
    if (transactions.isEmpty()) return false

    val csvHeader = "ID,Amount,Currency,Date,Description,CategoryId,Type,Source\n"
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    val csvBody = buildString {
        append(csvHeader)
        transactions.forEach { tx ->
            val dateStr = format.format(Date(tx.dateMillis))
            val typeStr = if (tx.isIncome) "INCOME" else "EXPENSE"
            append("${tx.id},${tx.amount},${tx.currency},$dateStr,\"${tx.description.replace("\"", "\"\"")}\",${tx.categoryId},$typeStr,${tx.source}\n")
        }
    }

    try {
        val file = File(context.cacheDir, "FinCo_Ledger_Export.csv")
        val stream = FileOutputStream(file)
        stream.write(csvBody.toByteArray())
        stream.close()

        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "FinCo Finance Ledger Export")
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Export Transactions Checklist"))
        return true
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}
