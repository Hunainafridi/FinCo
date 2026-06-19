package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Brush
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: MainViewModel,
    onComplete: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })
    
    var selectedLang by remember { mutableStateOf("en") }
    var startingBalanceStr by remember { mutableStateOf("25000") }
    var pinSetup by remember { mutableStateOf("") }
    var pinSetupVerify by remember { mutableStateOf("") }
    var smsTrackingEnabled by remember { mutableStateOf(true) }

    var balanceError by remember { mutableStateOf<String?>(null) }
    var pinError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("onboarding_screen"),
        containerColor = Color.Transparent
    ) { innerPadding ->
        val isDark = isSystemInDarkTheme()
        val flowBgBrush = Brush.linearGradient(
            colors = if (isDark) {
                listOf(
                    MaterialTheme.colorScheme.background,
                    Color(0xFF0F082B),
                    MaterialTheme.colorScheme.background
                )
            } else {
                listOf(
                    MaterialTheme.colorScheme.background,
                    Color(0xFFEDE8FF),
                    MaterialTheme.colorScheme.background
                )
            }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(flowBgBrush)
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = "Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "FinCo",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Pager for slides
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> SlideLanguage(
                        selectedLanguage = selectedLang,
                        onLanguageChange = { 
                            selectedLang = it
                            viewModel.changeLanguage(it)
                        }
                    )
                    1 -> SlideBalance(
                        balanceValue = startingBalanceStr,
                        onBalanceChange = { 
                            startingBalanceStr = it
                            balanceError = null
                        },
                        errorMessage = balanceError,
                        lang = selectedLang
                    )
                    2 -> SlideSecuritySms(
                        pinValue = pinSetup,
                        onPinChange = { pinSetup = it; pinError = null },
                        verifyValue = pinSetupVerify,
                        onVerifyChange = { pinSetupVerify = it; pinError = null },
                        smsEnabled = smsTrackingEnabled,
                        onSmsToggle = { smsTrackingEnabled = it },
                        errorMessage = pinError,
                        lang = selectedLang
                    )
                }
            }

            // Footer navigation
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Page Indicator Dot Row
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    repeat(3) { index ->
                        val color = if (pagerState.currentPage == index) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        }
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(if (pagerState.currentPage == index) 10.dp else 7.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                // Call to Action button
                val isLastPage = pagerState.currentPage == 2
                val buttonText = when {
                    isLastPage -> if (selectedLang == "ur") "Aghaz Karein" else if (selectedLang == "ps") "Zat Shuru Krein" else "Get Started"
                    else -> if (selectedLang == "ur") "Aagay Barhein" else if (selectedLang == "ps") "Walanday Shrei" else "Next"
                }

                Physical3DButton(
                    onClick = {
                        if (pagerState.currentPage == 1) {
                            val balance = startingBalanceStr.toDoubleOrNull()
                            if (balance == null || balance < 0.0) {
                                balanceError = if (selectedLang == "ur") "Meharbani kar k darust raqam likhein" else "Please enter a valid start balance."
                                return@Physical3DButton
                            }
                        }

                        if (pagerState.currentPage == 2) {
                            if (pinSetup.isNotEmpty()) {
                                if (pinSetup.length < 4) {
                                    pinError = "PIN must be at least 4 digits"
                                    return@Physical3DButton
                                }
                                if (pinSetup != pinSetupVerify) {
                                    pinError = "PIN passcodes do not match"
                                    return@Physical3DButton
                                }
                            }

                            // Complete onboarding state
                            val balanceVal = startingBalanceStr.toDoubleOrNull() ?: 25000.0
                            viewModel.completeOnboarding(
                                balance = balanceVal,
                                language = selectedLang,
                                pin = pinSetup.ifEmpty { null },
                                smsLogging = smsTrackingEnabled
                            )
                            onComplete()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    text = buttonText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_next_button")
                )
            }
        }
    }
}

@Composable
fun SlideLanguage(
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Translate,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Text(
            text = "Select App Language",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Apni pasandida zubaani muntakhib karein \n (mubaaril scale)",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        val languages = listOf(
            Triple("en", "English", "Default International"),
            Triple("ur", "اردو (Urdu)", "Roman Urdu Transliterated"),
            Triple("ps", "پښتو (Pashto)", "Roman Pashto Transliterated")
        )

        languages.forEach { (code, name, desc) ->
            val isSelected = selectedLanguage == code
            Physical3DCard(
                onClick = { onLanguageChange(code) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("lang_card_$code"),
                shape = RoundedCornerShape(14.dp),
                backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .padding(2.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    RadioButton(
                        selected = isSelected,
                        onClick = { onLanguageChange(code) }
                    )
                }
            }
        }
    }
}

@Composable
fun SlideBalance(
    balanceValue: String,
    onBalanceChange: (String) -> Unit,
    errorMessage: String?,
    lang: String
) {
    val title = if (lang == "ur") "Shuruati Balance" else if (lang == "ps") "Shuruati Balance" else "Starting Cash Balance"
    val sub = if (lang == "ur") "Aapki jaib m rkhay cash/wallet ki mojuda raqam likhein" else "Enter the actual cash/wallet amount you currently have."
    val hint = if (lang == "ur") "Mojuda Raqam (PKR)" else "Current Balance (PKR)"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AccountBalanceWallet,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
        )
        Text(
            text = sub,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = balanceValue,
            onValueChange = onBalanceChange,
            label = { Text(text = hint) },
            prefix = { Text("Rs. ") },
            shape = RoundedCornerShape(12.dp),
            isError = errorMessage != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .testTag("starting_balance_field")
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun SlideSecuritySms(
    pinValue: String,
    onPinChange: (String) -> Unit,
    verifyValue: String,
    onVerifyChange: (String) -> Unit,
    smsEnabled: Boolean,
    onSmsToggle: (Boolean) -> Unit,
    errorMessage: String?,
    lang: String
) {
    val title = if (lang == "ur") "Amniyat aur Khufia Setup" else "Security & Local Automations"
    val sub = if (lang == "ur") "PIN lock (ikhtiyari) aur SMS auto-logging ijazat" else "Optional PIN lock screen and offline SMS expense tracking."

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = sub,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // PIN Setup Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (lang == "ur") "Jaib PIN Lock (Ikhtiyari)" else "Privacy PIN Lock (Optional)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedTextField(
                        value = pinValue,
                        onValueChange = { if (it.length <= 4) onPinChange(it) },
                        label = { Text("PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 6.dp)
                    )
                    OutlinedTextField(
                        value = verifyValue,
                        onValueChange = { if (it.length <= 4) onVerifyChange(it) },
                        label = { Text("Verify") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 6.dp)
                    )
                }
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // SMS Authorization Dialog Rationale Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == "ur") "Sms Bank Alerts Auto-Logging" else "On-Device Bank SMS Parsing",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (lang == "ur") 
                            "Jaib app khudkar tariqay se aapk bank SMS alerts se kharch log karti hai. Koi data bahar nahi jata." 
                            else "Reads incoming bank alerts offline to pre-fill spending logs. 100% private, no data leaves your phone.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = smsEnabled,
                    onCheckedChange = onSmsToggle,
                    modifier = Modifier.testTag("onboarding_sms_switch")
                )
            }
        }
    }
}
