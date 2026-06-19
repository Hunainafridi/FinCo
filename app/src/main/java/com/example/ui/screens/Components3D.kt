package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Modern Liquid 3D Button - features a glossy gradient highlight,
 * dynamic bounciness on touch, soft color-matched projection shadows,
 * and high rounded bubble-pill edges.
 */
@Composable
fun Physical3DButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    borderColor: Color = Color.Transparent,
    shadowColor: Color = Color.Transparent
) {
    var isPressed by remember { mutableStateOf(false) }

    // Liquid wet spring physics curve
    val offsetAnimation by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 4.dp,
        animationSpec = spring(dampingRatio = 0.52f, stiffness = 160f)
    )

    // Soft colored glow projection shadow
    val resolvedShadowColor = if (shadowColor == Color.Transparent || shadowColor == Color.Black) {
        containerColor.copy(alpha = 0.38f)
    } else {
        shadowColor
    }

    // High quality ink-border or neon shine border
    val resolvedBorderColor = if (borderColor == Color.Transparent || borderColor == Color.Black) {
        containerColor.copy(alpha = 0.6f)
    } else {
        borderColor
    }

    val bubbleShape = RoundedCornerShape(32.dp)

    Box(
        modifier = modifier
            .padding(end = 4.dp, bottom = 4.dp)
            .pointerInput(onClick) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            }
    ) {
        // Soft Glow 3D Shadow projection
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 5.dp)
                .background(color = resolvedShadowColor, shape = bubbleShape)
        )

        // Front Interactive Layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = offsetAnimation, y = offsetAnimation)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            containerColor,
                            containerColor.copy(alpha = 0.82f)
                        )
                    ),
                    shape = bubbleShape
                )
                .border(1.5.dp, resolvedBorderColor, bubbleShape)
                .clip(bubbleShape)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            // Wet Glossy Highlight Streak
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 14.dp, vertical = 2.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
            )

            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = contentColor,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Modern Liquid 3D Card - possesses soft organic bubble curves,
 * double border accents (neon glass effect), and translucent colored neon shadow drop.
 */
@Composable
fun Physical3DCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = Color.Transparent,
    shadowColor: Color = Color.Transparent,
    shadowOffset: Dp = 4.dp,
    shape: RoundedCornerShape = RoundedCornerShape(26.dp),
    onClick: (() -> Unit)? = null,
    internalPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = backgroundColor.red + backgroundColor.green + backgroundColor.blue < 0.5f

    // Soft translucent shadow colored accent
    val resolvedShadowColor = if (shadowColor == Color.Transparent || shadowColor == Color.Black) {
        if (isDark) Color(0x3C000000) else Color(0x1B0E0B1E)
    } else {
        shadowColor
    }

    val resolvedBorderColor = if (borderColor == Color.Transparent || borderColor == Color.Black) {
        if (isDark) Color.White.copy(alpha = 0.12f) else backgroundColor.copy(alpha = 0.15f)
    } else {
        borderColor
    }

    Box(
        modifier = modifier.padding(end = shadowOffset, bottom = shadowOffset)
    ) {
        // Translucent 3D shadow depth
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset + 1.dp)
                .background(resolvedShadowColor, shape)
        )

        // Main Container Card
        val cardContent: @Composable () -> Unit = {
            Column(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                backgroundColor,
                                backgroundColor.copy(alpha = 0.96f)
                            )
                        )
                    )
                    .padding(internalPadding)
            ) {
                content()
            }
        }

        if (onClick != null) {
            Card(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = shape,
                border = BorderStroke(1.5.dp, resolvedBorderColor),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                content = { cardContent() }
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = shape,
                border = BorderStroke(1.5.dp, resolvedBorderColor),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                content = { cardContent() }
            )
        }
    }
}

/**
 * Modern Liquid 3D Floating Action Button
 */
@Composable
fun Physical3DFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    borderColor: Color = Color.Transparent,
    shadowColor: Color = Color.Transparent
) {
    var isPressed by remember { mutableStateOf(false) }

    val offsetAnimation by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 4.dp,
        animationSpec = spring(dampingRatio = 0.52f, stiffness = 160f)
    )

    val resolvedShadowColor = if (shadowColor == Color.Transparent || shadowColor == Color.Black) {
        containerColor.copy(alpha = 0.35f)
    } else {
        shadowColor
    }

    val resolvedBorderColor = if (borderColor == Color.Transparent || borderColor == Color.Black) {
        containerColor.copy(alpha = 0.5f)
    } else {
        borderColor
    }

    Box(
        modifier = modifier
            .padding(end = 4.dp, bottom = 4.dp)
            .pointerInput(onClick) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            }
    ) {
        // Soft colorful projection shadow
        Box(
            modifier = Modifier
                .size(56.dp)
                .offset(x = 4.dp, y = 5.dp)
                .background(resolvedShadowColor, CircleShape)
        )
        // FAB front circle
        Box(
            modifier = Modifier
                .size(56.dp)
                .offset(x = offsetAnimation, y = offsetAnimation)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            containerColor,
                            containerColor.copy(alpha = 0.85f)
                        )
                    ),
                    shape = CircleShape
                )
                .border(1.5.dp, resolvedBorderColor, CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Glint element
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .align(Alignment.TopCenter)
                    .padding(vertical = 2.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )

            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

/**
 * Modern Liquid 3D Small button chip
 */
@Composable
fun Physical3DSmallButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.secondary,
    contentColor: Color = MaterialTheme.colorScheme.onSecondary,
    borderColor: Color = Color.Transparent,
    shadowColor: Color = Color.Transparent
) {
    var isPressed by remember { mutableStateOf(false) }

    val offsetAnimation by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 3.dp,
        animationSpec = spring(dampingRatio = 0.52f, stiffness = 160f)
    )

    val resolvedShadowColor = if (shadowColor == Color.Transparent || shadowColor == Color.Black) {
        containerColor.copy(alpha = 0.32f)
    } else {
        shadowColor
    }

    val resolvedBorderColor = if (borderColor == Color.Transparent || borderColor == Color.Black) {
        containerColor.copy(alpha = 0.5f)
    } else {
        borderColor
    }

    val chipShape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .padding(end = 3.dp, bottom = 3.dp)
            .pointerInput(onClick) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            }
    ) {
        // Translucent glow shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 3.dp, y = 4.dp)
                .background(color = resolvedShadowColor, shape = chipShape)
        )
        // Button layer
        Box(
            modifier = Modifier
                .offset(x = offsetAnimation, y = offsetAnimation)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            containerColor,
                            containerColor.copy(alpha = 0.85f)
                        )
                    ),
                    shape = chipShape
                )
                .border(1.2.dp, resolvedBorderColor, chipShape)
                .clip(chipShape)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Little Glint
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .align(Alignment.TopCenter)
                    .padding(vertical = 1.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.18f), Color.Transparent)
                        ),
                        shape = chipShape
                    )
            )

            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

/**
 * Modern Liquid 3D Circular Keypad Item
 */
@Composable
fun Physical3DCircularKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = Color.Transparent,
    shadowColor: Color = Color.Transparent,
    content: @Composable BoxScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val offsetAnimation by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 4.dp,
        animationSpec = spring(dampingRatio = 0.52f, stiffness = 160f)
    )

    val resolvedShadowColor = if (shadowColor == Color.Transparent || shadowColor == Color.Black) {
        containerColor.copy(alpha = 0.28f)
    } else {
        shadowColor
    }

    val resolvedBorderColor = if (borderColor == Color.Transparent || borderColor == Color.Black) {
        containerColor.copy(alpha = 0.4f)
    } else {
        borderColor
    }

    Box(
        modifier = modifier
            .padding(end = 4.dp, bottom = 4.dp)
            .pointerInput(onClick) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            }
    ) {
        // Shadow Circle
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(resolvedShadowColor, CircleShape)
        )
        // Main Circle
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = offsetAnimation, y = offsetAnimation)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            containerColor,
                            containerColor.copy(alpha = 0.9f)
                        )
                    ),
                    shape = CircleShape
                )
                .border(1.5.dp, resolvedBorderColor, CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Highlight dome
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .align(Alignment.TopCenter)
                    .padding(vertical = 2.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )

            content()
        }
    }
}
