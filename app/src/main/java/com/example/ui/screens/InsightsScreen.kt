package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel

@Composable
fun InsightsScreen(
    viewModel: MainViewModel,
    lang: String
) {
    val coachingInsight by viewModel.coachingInsight.collectAsState()
    val coachingLoading by viewModel.coachingLoading.collectAsState()

    // Pulsing alpha animation for loading state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_coaching")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("insights_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Core header
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (lang == "ur") "Hamsafar AI Salaahkar" else if (lang == "ps") "AI Adalat" else "AI Financial Coach",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (lang == "ur") "Gemini AI se makhsoos mashwaray lein" else "Personalized financial coaching powered by server-side Gemini AI.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Beautiful Card containing the plain-language coached text
        val coachCardGradient = Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                MaterialTheme.colorScheme.surfaceVariant
            )
        )

        Physical3DCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("insights_content_card"),
            shape = RoundedCornerShape(20.dp),
            backgroundColor = MaterialTheme.colorScheme.surface,
            internalPadding = 0.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(coachCardGradient)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (coachingLoading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.alpha(alphaAnim)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Thinking...",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (lang == "ur") "Gemini mashwara tayyar kar raha hai..." else "Gemini is examining your monthly spend math...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        
                        Text(
                            text = coachingInsight,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        Text(
                            text = if (lang == "ur") 
                                "Mashwara server par privacy k sath compute hota hai." 
                                else "Advice matches lower-income targets offline via caching.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Floating advice action triggers
        Physical3DButton(
            onClick = { if (!coachingLoading) viewModel.fetchAiCoachingInsight() },
            text = if (lang == "ur") "Naya AI Mashwara" else if (lang == "ps") "Nawa AI Salaah" else "Ask Gemini Partner",
            containerColor = if (coachingLoading) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .testTag("refresh_coaching_button")
        )
    }
}
