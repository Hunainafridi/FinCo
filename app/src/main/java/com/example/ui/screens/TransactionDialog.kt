package com.example.ui.screens

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ml.Categorizer
import com.example.data.model.Category
import com.example.data.sms.SmsParser
import com.example.data.voice.VoiceNluParser
import com.example.ui.viewmodel.MainViewModel
import java.util.*

@Composable
fun TransactionDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    lang: String
) {
    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf("uncategorized") }
    var suggestedCategoryName by remember { mutableStateOf<String?>(null) }
    var confidenceScore by remember { mutableStateOf<Double?>(null) }

    val categories by viewModel.categories.collectAsState()

    // Voice simulation helpers
    var isVoicePanelOpen by remember { mutableStateOf(false) }
    var voiceInputText by remember { mutableStateOf("") }

    // On-device ML classification triggered on typing descriptions
    LaunchedEffect(description) {
        if (description.isNotBlank()) {
            val result = Categorizer.categorize(description)
            // Auto suggest category if confidence is strong
            if (result.confidence >= 0.6) {
                selectedCategoryId = result.categoryId
                suggestedCategoryName = categories.find { it.id == result.categoryId }?.name
                confidenceScore = result.confidence
            } else {
                suggestedCategoryName = null
                confidenceScore = null
            }
        } else {
            suggestedCategoryName = null
            confidenceScore = null
        }
    }

    // Android native Speech Recognizer launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (!results.isNullOrEmpty()) {
            val spokenText = results[0]
            val parseResult = VoiceNluParser.parse(spokenText, lang)
            if (parseResult != null) {
                amountStr = parseResult.amount.toString()
                description = parseResult.text
                isIncome = parseResult.isIncome
                selectedCategoryId = parseResult.categoryId
                confidenceScore = parseResult.confidence
                suggestedCategoryName = categories.find { it.id == parseResult.categoryId }?.name
            } else {
                description = spokenText
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Physical3DButton(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    if (amount != null && amount > 0.0) {
                        viewModel.addTransaction(
                            amount = amount,
                            description = description.ifBlank { if (isIncome) "Income" else "Expense" },
                            categoryId = selectedCategoryId,
                            isIncome = isIncome,
                            source = "manual"
                        )
                        onDismiss()
                    }
                },
                text = if (lang == "ur") "Mehfooz" else "Save",
                modifier = Modifier.width(130.dp).testTag("save_transaction_button")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = if (lang == "ur") "Wapas" else "Cancel")
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (lang == "ur") "Naya Kharch / Kamayi" else if (lang == "ps") "Nawi Masraf" else "Add Log Entry",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                // Mic button
                IconButton(
                    onClick = {
                        try {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (lang == "en") "en-US" else if (lang == "ur") "ur-PK" else "ps-AF")
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Transaction detail...")
                            }
                            speechLauncher.launch(intent)
                        } catch (e: Exception) {
                            // Fallback to in-app simulation dialog
                            isVoicePanelOpen = true
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .testTag("microphone_button")
                ) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Input", tint = MaterialTheme.colorScheme.primary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tone Selector (Income / Expense)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = !isIncome,
                        onClick = { isIncome = false },
                        label = { Text(if (lang == "ur") "Kharch (Expense)" else "Expense") },
                        modifier = Modifier.testTag("expense_toggle_chip")
                    )
                    FilterChip(
                        selected = isIncome,
                        onClick = { isIncome = true },
                        label = { Text(if (lang == "ur") "Kamayi (Income)" else "Income") },
                        modifier = Modifier.testTag("income_toggle_chip")
                    )
                }

                // Amount Text Field
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(if (lang == "ur") "Raqam (PKR)" else "Amount in PKR") },
                    prefix = { Text("Rs. ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("amount_field")
                )

                // Keyword/Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (lang == "ur") "Wazahat (e.g., McDonald's, petrol)" else "Description / Tag") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("description_field")
                )

                // Fast AI Suggestion Badge
                if (suggestedCategoryName != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "On-Device ML Suggests: $suggestedCategoryName (${String.format("%.0f%%", (confidenceScore ?: 0.0) * 100)})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Category Grid Selector
                Text(
                    text = if (lang == "ur") "Muntakhid Category" else "Select Category",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Show all categories in horizontal scrolling row
                        categories.forEach { cat ->
                            val isSelected = selectedCategoryId == cat.id
                            val catColor = Color(android.graphics.Color.parseColor(cat.colorHex))
                            
                            Card(
                                onClick = { selectedCategoryId = cat.id },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) catColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier
                                    .width(72.dp)
                                    .fillMaxHeight(),
                                border = if (isSelected) BorderStroke(1.5.dp, catColor) else null
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(4.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val icon = when (cat.iconKey) {
                                        "shopping_cart" -> Icons.Default.ShoppingCart
                                        "restaurant" -> Icons.Default.Restaurant
                                        "directions_car" -> Icons.Default.LocalGasStation
                                        "receipt_long" -> Icons.Default.ReceiptLong
                                        "school" -> Icons.Default.School
                                        "payments" -> Icons.Default.Payments
                                        "add_card" -> Icons.Default.AccountBalanceWallet
                                        else -> Icons.Default.Help
                                    }
                                    Icon(imageVector = icon, contentDescription = null, tint = catColor, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = cat.name,
                                        fontSize = 9.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick Simulation Testing section!
                Spacer(modifier = Modifier.height(4.dp))
                Physical3DButton(
                    onClick = { isVoicePanelOpen = true },
                    text = "AI Voice / SMS Sim",
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )

    // Voice NLU & SMS simulation bottom sheet layout
    if (isVoicePanelOpen) {
        AlertDialog(
            onDismissRequest = { isVoicePanelOpen = false },
            confirmButton = {
                TextButton(onClick = { isVoicePanelOpen = false }) {
                    Text("Done")
                }
            },
            title = {
                Text("App Mock Simulation Console", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Since you are testing in the browser emulator, use these quick presets to simulate real speech or bank alert messages locally!",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Presets label
                    Text("Speech-To-Text (Voice NLU) Presets:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    val voicePresets = listOf(
                        "500 rupees kharch kiye khane par" to "ur",
                        "2500 for electricity bill" to "en",
                        "3000 rupai da transport dpara kharch shwal" to "ps",
                        "45000 tankhwah mili" to "ur"
                    )

                    voicePresets.forEach { (phrase, language) ->
                        Card(
                            onClick = {
                                val result = VoiceNluParser.parse(phrase, language)
                                if (result != null) {
                                    amountStr = result.amount.toString()
                                    description = result.text
                                    isIncome = result.isIncome
                                    selectedCategoryId = result.categoryId
                                    confidenceScore = result.confidence
                                    suggestedCategoryName = categories.find { it.id == result.categoryId }?.name
                                }
                                isVoicePanelOpen = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(text = "\"$phrase\"", fontSize = 12.sp, modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Bank SMS Push Notification Presets:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    val smsPresets = listOf(
                        "Rs. 1,500.00 debited ending 1234 at McDonald's" to "HBL",
                        "Alert: Rs. 4,200.00 spent ending 4321 at Metro Grocery" to "Alfalah",
                        "Your account ending in 5678 has been credited with PKR 25,000.00" to "Meezan"
                    )

                    smsPresets.forEach { (smsBody, sender) ->
                        Card(
                            onClick = {
                                val parsed = SmsParser.parse(smsBody, sender, System.currentTimeMillis())
                                if (parsed != null) {
                                    val transaction = SmsParser.parsedToEntity(parsed, sender)
                                    // Manually add directly back into pending table
                                    viewModel.confirmPendingTransaction(transaction.copy(isPending = true))
                                }
                                isVoicePanelOpen = false
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "$sender: $smsBody", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Tapping inserts this alert into Dashboard!", fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        )
    }
}

// Border Stroke Helper
fun BorderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)
