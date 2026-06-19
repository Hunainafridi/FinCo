package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AuthScreen(viewModel: MainViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }

    // Engine Selector: True = Sandbox/Local, False = Firebase/Cloud
    var useSandboxEngine by remember { mutableStateOf(true) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val appLanguage by viewModel.appLanguage.collectAsState()

    // Volumetric 3D color schemes based on device state
    val darkTheme = isSystemInDarkTheme()
    val shadowColor = if (darkTheme) Color(0xFF161517) else Color(0xFF1D1B1E)
    val borderColor = if (darkTheme) Color(0xFFE6E1E5) else Color(0xFF1D1B1E)
    val cardBg = if (darkTheme) Color(0xFF2A282B) else Color(0xFFFAF7F5)

    val flowBgBrush = androidx.compose.ui.graphics.Brush.linearGradient(
        colors = if (darkTheme) {
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

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("auth_screen_scaffold"),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(flowBgBrush)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 3D Generated visual hero card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp, end = 4.dp)
            ) {
                // Shadow
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .background(shadowColor, RoundedCornerShape(24.dp))
                )
                // Card with image
                Card(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(2.dp, borderColor)
                ) {
                    Image(
                        painter = painterResource(id = com.example.R.drawable.img_auth_hero),
                        contentDescription = "Safe Vault Hero Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Brand Header with 3D Pop Badge
            Box(
                modifier = Modifier
                    .padding(bottom = 24.dp, end = 4.dp)
            ) {
                // Shadow
                Box(
                    modifier = Modifier
                        .size(height = 80.dp, width = 290.dp)
                        .offset(x = 4.dp, y = 4.dp)
                        .background(shadowColor, RoundedCornerShape(24.dp))
                )
                // Front
                Box(
                    modifier = Modifier
                        .size(height = 80.dp, width = 290.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                        .border(2.5.dp, borderColor, RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = "FinCo Personal",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "Secure & Smart Money Tracker",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Main Credential Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                // Shadow
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 6.dp, y = 6.dp)
                        .background(shadowColor, RoundedCornerShape(32.dp))
                )
                // Front card
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("auth_credentials_card"),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(2.5.dp, borderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Segmented Asymmetric Tab controls for Sign In / Sign Up
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (darkTheme) Color(0xFF1A181C) else Color(0xFFEAE5E1),
                                    RoundedCornerShape(16.dp)
                               )
                                .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
                                .padding(4.dp)
                        ) {
                            boxTabButton(
                                text = "Sign In",
                                active = !isSignUp,
                                onClick = {
                                    isSignUp = false
                                    errorMessage = null
                                    successMessage = null
                                },
                                modifier = Modifier.weight(1f),
                                darkTheme = darkTheme,
                                borderColor = borderColor
                            )
                            boxTabButton(
                                text = "Sign Up",
                                active = isSignUp,
                                onClick = {
                                    isSignUp = true
                                    errorMessage = null
                                    successMessage = null
                                },
                                modifier = Modifier.weight(1f),
                                darkTheme = darkTheme,
                                borderColor = borderColor
                            )
                        }

                        // DATABASE ENGINE SELECTION ROW
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "AUTHENTICATION ENGINE:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Local Sandbox Button
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            useSandboxEngine = true
                                            errorMessage = null
                                            successMessage = null
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(
                                        if (useSandboxEngine) 2.dp else 1.dp,
                                        if (useSandboxEngine) MaterialTheme.colorScheme.primary else borderColor.copy(alpha = 0.3f)
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (useSandboxEngine) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                        } else {
                                            Color.Transparent
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = if (useSandboxEngine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Local Sandbox",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (useSandboxEngine) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }

                                // Firebase Live Sync Button
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            useSandboxEngine = false
                                            errorMessage = null
                                            successMessage = null
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(
                                        if (!useSandboxEngine) 2.dp else 1.dp,
                                        if (!useSandboxEngine) MaterialTheme.colorScheme.primary else borderColor.copy(alpha = 0.3f)
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (!useSandboxEngine) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                        } else {
                                            Color.Transparent
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudQueue,
                                            contentDescription = null,
                                            tint = if (!useSandboxEngine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Firebase Cloud",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (!useSandboxEngine) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = if (useSandboxEngine) {
                                if (isSignUp) "Register offline credentials locally in device Sandbox storage" else "Unlock your local sandbox container securely"
                            } else {
                                if (isSignUp) "Register standard secure credentials with Firebase Live Cloud" else "Sync and verify your financial ledger online via Firebase"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        // Input Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            placeholder = { Text("e.g. user@finco.domain") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth().testTag("auth_email_field"),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = borderColor.copy(alpha = 0.6f)
                            )
                        )

                        // Input Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            placeholder = { Text("Min. 6 characters") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("auth_password_field"),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = borderColor.copy(alpha = 0.6f)
                            )
                        )

                        // Confirm Password (Sign up only)
                        if (isSignUp) {
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth().testTag("auth_confirm_password_field"),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = borderColor.copy(alpha = 0.6f)
                                )
                            )
                        }

                        // Feedback Status alerts
                        if (errorMessage != null) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (darkTheme) Color(0xFF5A1C1D) else Color(0xFFFFDAD6),
                                    contentColor = if (darkTheme) Color(0xFFFFDAD6) else Color(0xFF410002)
                                ),
                                border = BorderStroke(1.5.dp, Color(0xFF9C4523))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Error, contentDescription = "Error")
                                    Text(text = errorMessage!!, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        if (successMessage != null) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (darkTheme) Color(0xFF143015) else Color(0xFFD4EDDA),
                                    contentColor = if (darkTheme) Color(0xFFD4EDDA) else Color(0xFF155724)
                                ),
                                border = BorderStroke(1.5.dp, Color(0xFF28A745))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = Color(0xFF28A745))
                                    Text(text = successMessage!!, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // Physical 3D Clickable Submit Button
                        if (isLoading) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            Physical3DButton(
                                text = if (isSignUp) {
                                    if (useSandboxEngine) "CREATE SANDBOX PROFILE" else "CREATE CLOUD ACCOUNT"
                                } else {
                                    if (useSandboxEngine) "SECURE SANDBOX LOGIN" else "SECURE CLOUD SIGN IN"
                                },
                                onClick = {
                                    errorMessage = null
                                    successMessage = null
                                    if (email.isBlank() || password.isBlank()) {
                                        errorMessage = "Email and Password cannot be blank."
                                        return@Physical3DButton
                                    }
                                    if (password.length < 6) {
                                        errorMessage = "Password must be at least 6 characters long."
                                        return@Physical3DButton
                                    }
                                    if (isSignUp && password != confirmPassword) {
                                        errorMessage = "Passwords do not match."
                                        return@Physical3DButton
                                    }

                                    isLoading = true
                                    if (useSandboxEngine) {
                                        // Sandbox authentication engine fallback
                                        if (isSignUp) {
                                            viewModel.signUpSandboxUser(
                                                email = email.trim(),
                                                password = password,
                                                onSuccess = {
                                                    isLoading = false
                                                    successMessage = "Sandbox local profile created successfully!"
                                                },
                                                onFailure = { err ->
                                                    isLoading = false
                                                    errorMessage = err
                                                }
                                            )
                                        } else {
                                            viewModel.loginSandboxUser(
                                                email = email.trim(),
                                                password = password,
                                                onSuccess = {
                                                    isLoading = false
                                                    successMessage = "Signed in to secure Sandbox!"
                                                },
                                                onFailure = { err ->
                                                    isLoading = false
                                                    errorMessage = err
                                                }
                                            )
                                        }
                                    } else {
                                        // Live Firebase module Authentication Engine
                                        if (isSignUp) {
                                            viewModel.signUpWithFirebase(
                                                email = email.trim(),
                                                password = password,
                                                onSuccess = {
                                                    isLoading = false
                                                    successMessage = "Account created with Firebase Cloud!"
                                                },
                                                onFailure = { err ->
                                                    isLoading = false
                                                    // Detect developer configuration error / absence of keys gracefully
                                                    if (err.contains("keys", ignoreCase = true) || err.contains("unconfigured", ignoreCase = true) || err.contains("configuration", ignoreCase = true) || err.contains("developer", ignoreCase = true) || err.contains("null", ignoreCase = true)) {
                                                        errorMessage = "Firebase Cloud keys are currently unconfigured in this package. We automatically enabled Local Sandbox database for you! Tap register again to complete!"
                                                        useSandboxEngine = true
                                                    } else {
                                                        errorMessage = err
                                                    }
                                                }
                                            )
                                        } else {
                                            viewModel.loginWithFirebase(
                                                email = email.trim(),
                                                password = password,
                                                onSuccess = {
                                                    isLoading = false
                                                    successMessage = "Signed in successfully!"
                                                },
                                                onFailure = { err ->
                                                    isLoading = false
                                                    // Detect developer configuration error / absence of keys gracefully
                                                    if (err.contains("keys", ignoreCase = true) || err.contains("unconfigured", ignoreCase = true) || err.contains("configuration", ignoreCase = true) || err.contains("developer", ignoreCase = true) || err.contains("null", ignoreCase = true)) {
                                                        errorMessage = "Firebase Cloud is unready in this build. We automatically configured Local Sandbox mode! Click login again to unlock!"
                                                        useSandboxEngine = true
                                                    } else {
                                                        errorMessage = err
                                                    }
                                                }
                                            )
                                        }
                                    }
                                },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                borderColor = borderColor,
                                shadowColor = shadowColor,
                                modifier = Modifier.fillMaxWidth().testTag("auth_submit_button")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Skip / Offline Demo Bypass
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            ) {
                // Secondary 3D outline style skip button
                Physical3DButton(
                    text = "GUEST / DIRECT ACCESS MODE",
                    onClick = {
                        viewModel.setOfflineMode(true)
                    },
                    containerColor = if (darkTheme) Color(0xFF382C29) else Color(0xFFF3E8E3),
                    contentColor = if (darkTheme) Color(0xFFF0E0DB) else Color(0xFF211A18),
                    borderColor = borderColor,
                    shadowColor = shadowColor.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth().testTag("auth_offline_bypass_button")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun boxTabButton(
    text: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    darkTheme: Boolean,
    borderColor: Color
) {
    val containerBg = if (active) {
        if (darkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    val contentColor = if (active) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        if (darkTheme) Color(0xFFCAC4D0) else Color(0xFF49454F)
    }

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerBg,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (active) BorderStroke(1.5.dp, borderColor) else null,
        modifier = modifier.height(44.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun Physical3DCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.onBackground,
    shadowColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier.padding(end = 4.dp, bottom = 4.dp)) {
        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(color = shadowColor, shape = RoundedCornerShape(24.dp))
        )
        // Content
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            border = BorderStroke(2.dp, borderColor)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                content()
            }
        }
    }
}


