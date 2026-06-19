package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinLockScreen(
    correctPin: String,
    onSuccess: () -> Unit
) {
    var pinEntered by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

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
            .padding(24.dp)
            .testTag("pin_lock_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = if (pinError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Enter Security PIN",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Apna khufia PIN darj karein",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )

            // PIN dot slots
            Row(
                modifier = Modifier.padding(24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in 0 until 4) {
                    val active = i < pinEntered.length
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (pinError) MaterialTheme.colorScheme.error 
                                else if (active) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                            )
                    )
                }
            }
        }

        // Numeric Keypad Grid
        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "C")
            )

            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { key ->
                        if (key.isEmpty()) {
                            Box(modifier = Modifier.size(72.dp))
                        } else {
                            Physical3DCircularKey(
                                onClick = {
                                    pinError = false
                                    if (key == "C") {
                                        if (pinEntered.isNotEmpty()) {
                                            pinEntered = pinEntered.dropLast(1)
                                        }
                                    } else {
                                        if (pinEntered.length < 4) {
                                            pinEntered += key
                                            if (pinEntered.length == 4) {
                                                if (pinEntered == correctPin) {
                                                    onSuccess()
                                                } else {
                                                    pinError = true
                                                    pinEntered = ""
                                                }
                                            }
                                        }
                                    }
                                },
                                containerColor = if (key == "C") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(72.dp).testTag("pin_key_$key")
                            ) {
                                if (key == "C") {
                                    Icon(
                                        imageVector = Icons.Default.Backspace,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                } else {
                                    Text(
                                        text = key,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
