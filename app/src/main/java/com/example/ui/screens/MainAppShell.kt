package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.CelebrationData
import com.example.ui.viewmodel.CelebrationType
import kotlinx.coroutines.delay

enum class AppTab {
    DASHBOARD,
    BUDGETS,
    AI_INSIGHTS,
    SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell(viewModel: MainViewModel) {
    val firebaseUser by viewModel.firebaseUser.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()
    val hasOnboarded by viewModel.hasOnboarded.collectAsState()
    val pinPasscode by viewModel.pinPasscode.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val activeCelebration by viewModel.activeCelebration.collectAsState()

    var isUnlocked by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    var showAddTxDialog by remember { mutableStateOf(false) }
    var inTransactionHistoryMode by remember { mutableStateOf(false) }

    // Firebase Auth Screen Gate
    if (firebaseUser == null && !isOfflineMode) {
        AuthScreen(viewModel = viewModel)
        return
    }

    // Onboarding Gate
    if (!hasOnboarded) {
        OnboardingScreen(
            viewModel = viewModel,
            onComplete = {
                if (pinPasscode == null) {
                    isUnlocked = true
                }
            }
        )
        return
    }

    // Security Unlock Gate
    if (pinPasscode != null && !isUnlocked) {
        PinLockScreen(
            correctPin = pinPasscode!!,
            onSuccess = { isUnlocked = true }
        )
        return
    }

    // Main App Scaffold (Edge-to-Edge with full bottom navigation)
    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("main_app_shell"),
        topBar = {
            if (!inTransactionHistoryMode) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "FinCo",
                                fontSize = 20.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        floatingActionButton = {
            if (currentTab == AppTab.DASHBOARD && !inTransactionHistoryMode) {
                Physical3DFloatingActionButton(
                    onClick = { showAddTxDialog = true },
                    icon = Icons.Default.Add,
                    contentDescription = "Add Transaction",
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_transaction_fab")
                )
            }
        },
        bottomBar = {
            if (!inTransactionHistoryMode) {
                NavigationBar(
                    modifier = Modifier.testTag("main_bottom_nav"),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    val tabs = listOf(
                        Triple(AppTab.DASHBOARD, Icons.Default.Dashboard, if (appLanguage == "ur") "Khazana" else "Home"),
                        Triple(AppTab.BUDGETS, Icons.Default.PieChart, if (appLanguage == "ur") "Budget" else "Limits"),
                        Triple(AppTab.AI_INSIGHTS, Icons.Default.AutoAwesome, if (appLanguage == "ur") "Salaah" else "Insights"),
                        Triple(AppTab.SETTINGS, Icons.Default.Settings, if (appLanguage == "ur") "Usool" else "Settings")
                    )

                    tabs.forEach { (tab, icon, label) ->
                        val active = currentTab == tab
                        NavigationBarItem(
                            selected = active,
                            onClick = { currentTab = tab },
                            icon = { Icon(imageVector = icon, contentDescription = label) },
                            label = { Text(text = label, fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                            modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val isDark = isSystemInDarkTheme()
        val flowBgBrush = Brush.linearGradient(
            colors = if (isDark) {
                listOf(
                    MaterialTheme.colorScheme.background,
                    Color(0xFF0F082B),  // Subtly blended deep violet
                    MaterialTheme.colorScheme.background
                )
            } else {
                listOf(
                    MaterialTheme.colorScheme.background,
                    Color(0xFFEDE8FF),  // Subtly blended light lavender frost
                    MaterialTheme.colorScheme.background
                )
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(flowBgBrush)
                .padding(innerPadding)
        ) {
            when {
                inTransactionHistoryMode -> {
                    TransactionHistoryScreen(
                        viewModel = viewModel,
                        onBack = { inTransactionHistoryMode = false },
                        lang = appLanguage
                    )
                }
                currentTab == AppTab.DASHBOARD -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToTransactions = { inTransactionHistoryMode = true },
                        lang = appLanguage
                    )
                }
                currentTab == AppTab.BUDGETS -> {
                    BudgetsScreen(
                        viewModel = viewModel,
                        lang = appLanguage
                    )
                }
                currentTab == AppTab.AI_INSIGHTS -> {
                    InsightsScreen(
                        viewModel = viewModel,
                        lang = appLanguage
                    )
                }
                currentTab == AppTab.SETTINGS -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        lang = appLanguage
                    )
                }
            }

            // Framer-Motion inspired floating celebratory event banner overlay
            activeCelebration?.let { info ->
                CelebrationOverlay(
                    celebration = info,
                    onDismiss = { viewModel.clearCelebration() }
                )
            }
        }
    }

    // Modal Add/Edit Form Overlay dialog
    if (showAddTxDialog) {
        TransactionDialog(
            viewModel = viewModel,
            onDismiss = { showAddTxDialog = false },
            lang = appLanguage
        )
    }
}

@Composable
fun CelebrationOverlay(
    celebration: CelebrationData,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(celebration) {
        isVisible = true
        delay(2600)
        isVisible = false
        delay(400)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .pointerInput(Unit) {
                // Prevent intercepting taps on outer screens during celebration show
            },
        contentAlignment = Alignment.Center
    ) {
        // Sparkling interactive visual element
        SparkleParticles(active = isVisible)

        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioHighBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(),
            exit = scaleOut(
                animationSpec = spring(
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .widthIn(max = 350.dp)
            ) {
                // Shadow / 3D Offset
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 6.dp, y = 6.dp)
                        .background(Color.Black, RoundedCornerShape(28.dp))
                )

                // Front Badge Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(2.5.dp, Color.Black),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Badge Icon with bouncy background glow
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .border(2.dp, Color.Black, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (celebration.type == CelebrationType.EXPENSE_LOGGED) {
                                    Icons.Default.Celebration
                                } else {
                                    Icons.Default.EmojiEvents
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = celebration.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = celebration.subtitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // High-contrast Ledger Amount Badge
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                                .border(1.5.dp, Color.Black, RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = celebration.amount,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "SWEET PROGRESS!",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SparkleParticles(active: Boolean) {
    if (!active) return
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    
    // Animate a phase ratio from 0f to 1f
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val colors = listOf(
            Color(0xFFFFD700), // Gold
            Color(0xFF4CAF50), // Emerald Green
            Color(0xFF00bcd4), // Cyan
            Color(0xFFE91E63), // Pink
            Color(0xFFFF5722)  // Neon Orange
        )
        val seedRandom = java.util.Random(42)
        for (i in 0 until 18) {
            val angle = seedRandom.nextFloat() * 2f * Math.PI
            val distance = seedRandom.nextFloat() * 180f + 40f
            val maxRadius = seedRandom.nextFloat() * 7f + 3f
            
            val progress = phase
            val currentDist = distance * progress
            val x = (center.x + Math.cos(angle) * currentDist).toFloat()
            val y = (center.y + Math.sin(angle) * currentDist).toFloat()
            val alpha = 1f - progress
            val radius = maxRadius * (1f - progress * 0.5f)
            
            val color = colors[seedRandom.nextInt(colors.size)].copy(alpha = alpha)
            drawCircle(
                color = color,
                radius = radius,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}
