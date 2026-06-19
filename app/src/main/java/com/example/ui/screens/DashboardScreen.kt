package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.Transaction
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToTransactions: () -> Unit,
    lang: String
) {
    val balance by viewModel.currentBalance.collectAsState()
    val monthSpend by viewModel.thisMonthSpend.collectAsState()
    val burnRate by viewModel.dailyBurnRate.collectAsState()
    val shortfallDay by viewModel.forecastShortfallDay.collectAsState()
    
    val transactions by viewModel.confirmedTransactions.collectAsState()
    val pendingSmsTxs by viewModel.pendingTransactions.collectAsState()
    val spendByCategory by viewModel.spendByCategory.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val budgetAlert by viewModel.budgetAlert.collectAsState()

    var filterCategory by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Header
        item {
            HeaderSection(lang = lang, balance = balance, monthSpend = monthSpend)
        }

        // Budget Alerts Dismissible Banner
        if (budgetAlert != null) {
            item {
                BudgetAlertBanner(message = budgetAlert!!, onDismiss = { viewModel.clearBudgetAlert() })
            }
        }

        // Forecast / Cash-Flow Forecast banner card
        item {
            ForecastBanner(shortfallDay = shortfallDay, burnRate = burnRate, lang = lang)
        }

        // SMS Logging Quick Actions Bento Block
        item {
            SmsLoggingSimulatorBox(viewModel = viewModel, lang = lang)
        }

        // Pending bank SMS cards (if any)
        if (pendingSmsTxs.isNotEmpty()) {
            item {
                Text(
                    text = if (lang == "ur") "Naye SMS Alerts (Confirmation)" else if (lang == "ps") "SMS Alerts (Confirm)" else "New SMS Alerts (Tap to Confirm)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }
            items(pendingSmsTxs) { tx ->
                PendingSmsCard(
                    tx = tx,
                    categories = categories,
                    onConfirm = { viewModel.confirmPendingTransaction(tx) },
                    onDismiss = { viewModel.discardPendingTransaction(tx) },
                    lang = lang
                )
            }
        }

        // Spend Statistics card (Donut Chart & Totals)
        item {
            Physical3DCard(
                modifier = Modifier.fillMaxWidth().testTag("spend_chart_card"),
                shape = RoundedCornerShape(20.dp),
                backgroundColor = MaterialTheme.colorScheme.surface
            ) {
                    Text(
                        text = if (lang == "ur") "Is Mahine Ka Kharch" else if (lang == "ps") "De Meishte Kharch" else "This Month's Spending",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (spendByCategory.isEmpty()) {
                        EmptyStateSpend(lang = lang)
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Donut Chart
                            Box(
                                modifier = Modifier.size(130.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                DonutChart(spendMap = spendByCategory)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "PKR",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = String.format("%,.0f", monthSpend),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            // Category legend list
                            Column(modifier = Modifier.weight(1f)) {
                                spendByCategory.entries.take(4).forEach { (cat, amount) ->
                                    val isFilter = filterCategory == cat.id
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isFilter) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                            .clickable {
                                                filterCategory = if (isFilter) null else cat.id
                                            }
                                            .padding(vertical = 4.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color(android.graphics.Color.parseColor(cat.colorHex)))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = cat.name,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = String.format("Rs. %,.0f", amount),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

        // Recent Confirmed Transactions List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (lang == "ur") "Haaliya Kharche" else if (lang == "ps") "Haaleh Masraf" else "Recent Transactions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (transactions.isNotEmpty()) {
                    Text(
                        text = if (lang == "ur") "Mazeed" else "View All",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { onNavigateToTransactions() }
                            .testTag("view_all_transactions")
                    )
                }
            }
        }

        // Transaction list items
        if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary.copy(0.4f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (lang == "ur") "Abhi tak koi kharch darj nahi kia." else "No transactions logged yet.",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            val filteredTxs = if (filterCategory != null) {
                transactions.filter { it.categoryId == filterCategory }
            } else {
                transactions
            }

            items(filteredTxs.take(10)) { tx ->
                val categoryObj = categories.find { it.id == tx.categoryId }
                TransactionItem(
                    tx = tx,
                    category = categoryObj,
                    onDelete = { viewModel.deleteTransaction(tx) },
                    lang = lang
                )
            }
        }
    }
}

@Composable
fun HeaderSection(lang: String, balance: Double, monthSpend: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Welcome Text
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (lang == "ur") "Assalam-o-Alaikum!" else if (lang == "ps") "Zat Assalam!" else "Welcome Back,",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Text(
                    text = "FinCo Personal",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            // A small avatar-like decorative block
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Two side-by-side asymmetric clean Bento Cards in 3D shadow!
        val darkTheme = androidx.compose.foundation.isSystemInDarkTheme()
        val shadowColor = if (darkTheme) Color(0xFF161517) else Color(0xFF1D1B1E)
        val borderColor = if (darkTheme) Color(0xFFE6E1E5) else Color(0xFF1D1B1E)

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left Bento Card - Available Balance (Primary Solid color with 3D Pop Shadow)
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .height(130.dp)
            ) {
                // 3D Shadow Layer
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .background(color = shadowColor, shape = RoundedCornerShape(28.dp))
                )
                // Front visual Card
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    border = BorderStroke(2.dp, borderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == "ur") "KUL KHAZANA" else "AVAILABLE BALANCE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            )
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = String.format("Rs. %,.0f", balance),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            // Right Bento Card - Spend Tracker (Accent Pastel Card with 3D Pop Shadow)
            Box(
                modifier = Modifier
                    .weight(1.0f)
                    .height(130.dp)
            ) {
                // 3D Shadow Layer
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .background(color = shadowColor, shape = RoundedCornerShape(28.dp))
                )
                // Front visual Card
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    border = BorderStroke(2.dp, borderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == "ur") "KHARCH" else "SPENT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.7f)
                            )
                            Icon(
                                imageVector = Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (lang == "ur") "Is Mahine" else "This Month",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.7f)
                            )
                            Text(
                                text = String.format("Rs. %,.0f", monthSpend),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onTertiary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetAlertBanner(message: String, onDismiss: () -> Unit) {
    Physical3DCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF5A1C1D) else Color(0xFFFFDAD6)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning, 
                    contentDescription = null, 
                    tint = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFFFFB59D) else Color(0xFF9C4523)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = message,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFFFFDAD6) else Color(0xFF410002)
                )
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Close, 
                    contentDescription = "Close", 
                    modifier = Modifier.size(16.dp),
                    tint = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFFFFDAD6) else Color(0xFF410002)
                )
            }
        }
    }
}

@Composable
fun ForecastBanner(shortfallDay: Int?, burnRate: Double, lang: String) {
    val isAlert = shortfallDay != null
    // Beautiful bento color background:
    // If warning: use light peach warning red `#FFDAD6` (or dark counterpart #5A1C1D)
    // If safe: use nice cream `#F3E8E3`
    val containerColor = if (isAlert) {
        if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF5A1C1D) else Color(0xFFFFDAD6)
    } else {
        if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF382C29) else Color(0xFFF3E8E3)
    }
    
    val contentColor = if (isAlert) {
        if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFFFFDAD6) else Color(0xFF410002)
    } else {
        if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFFF0E0DB) else Color(0xFF211A18)
    }

    val tagText = if (isAlert) {
        if (lang == "ur") "ALTEHAAT" else "HIGH RISK"
    } else {
        if (lang == "ur") "BEHTER" else "SAFE RATE"
    }

    val tagBgColor = if (isAlert) {
        if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF902A2B) else Color(0xFF410002)
    } else {
        if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF9C4523) else Color(0xFF9C4523)
    }

    val tagTextColor = Color.White

    val text = when {
        isAlert && lang == "ur" -> "Kharach ki raftar tez hai! Maujuda bachat agle $shortfallDay dinon mein khatam ho skti hai."
        isAlert && lang == "ps" -> "De kharch raftar der tez de! Pese ba pe $shortfallDay khalas shai."
        isAlert -> "Burn rate is high! At this speed, you may run out of money in $shortfallDay days before month-end."
        lang == "ur" -> "Aap bilkul munasib raftar par hain. Mahine k aakhir tak Rs. ${String.format("%,.0f", burnRate)} rozana bachat ke sath chal sakte hain."
        lang == "ps" -> "Zat baand khareed bachat lari kigi. Mahine aakhir pore Rs. ${String.format("%,.0f", burnRate)} baqi paseday shai."
        else -> "You are fully in the green! Your estimated expenses are balanced, daily average is Rs. ${String.format("%,.0f", burnRate)}."
    }

    val darkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    val shadowColor = if (darkTheme) Color(0xFF161517) else Color(0xFF1D1B1E)
    val borderColor = if (darkTheme) Color(0xFFE6E1E5) else Color(0xFF1D1B1E)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp, end = 4.dp)
    ) {
        // 3D Shadow Layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(color = shadowColor, shape = RoundedCornerShape(28.dp))
        )
        // Main Visual Front Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("forecast_banner_card"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
            border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
        ) {
            Column(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isAlert) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAlert) (if (lang == "ur") "Khatray Ki Ghanti!" else "Impending Budget Shortfall!") else (if (lang == "ur") "Kamyab Karkardagi" else "Cash-Flow Forecast"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = contentColor
                    )
                }
                
                // M3 styled Mini priority pill badge
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = tagBgColor)
                ) {
                    Text(
                        text = tagText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = tagTextColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = text,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = contentColor.copy(alpha = 0.85f)
            )

            // Dynamic tracking indicator line mirroring HTML bento progress elements
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp).fillMaxWidth()
            ) {
                val progressValue = if (isAlert) 0.35f else 0.85f
                val progressText = if (isAlert) "35%" else "85%"
                
                LinearProgressIndicator(
                    progress = { progressValue },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = tagBgColor,
                    trackColor = contentColor.copy(alpha = 0.15f)
                )
                Text(
                    text = progressText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
        }
    }
}
}

@Composable
fun PendingSmsCard(
    tx: Transaction,
    categories: List<Category>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    lang: String
) {
    val categoryName = categories.find { it.id == tx.categoryId }?.name ?: "Uncategorized"
    
    Physical3DCard(
        modifier = Modifier.fillMaxWidth().testTag("pending_sms_card"),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (tx.isIncome) "Income Parsed" else "Expense Detected (PKR)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = tx.description,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Suggest: $categoryName", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.65f))
                    if (tx.mlConfidence != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "(${String.format("%.0f%%", tx.mlConfidence * 100)})", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            
            Text(
                text = String.format("Rs. %,.0f", tx.amount),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = if (tx.isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Discard Pending", tint = MaterialTheme.colorScheme.error)
                }
                IconButton(onClick = onConfirm, modifier = Modifier.size(36.dp).testTag("confirm_pending_btn")) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Confirm Pending", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun EmptyStateSpend(lang: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (lang == "ur") "Pehle kharch darj karein takay chart ban sakay." else "Add starting transactions to generate visual breakdown.",
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)
        )
    }
}

@Composable
fun DonutChart(spendMap: Map<Category, Double>) {
    val total = spendMap.values.sum()
    if (total <= 0.0) return

    val colors = spendMap.keys.map { Color(android.graphics.Color.parseColor(it.colorHex)) }
    val proportions = spendMap.values.map { (it / total).toFloat() }

    Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
        var startAngle = -90f
        proportions.forEachIndexed { index, prop ->
            val sweepAngle = prop * 360f
            drawArc(
                color = colors[index],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 24f, cap = StrokeCap.Round)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun TransactionItem(
    tx: Transaction,
    category: Category?,
    onDelete: () -> Unit,
    lang: String
) {
    val icon = when (category?.iconKey) {
        "shopping_cart" -> Icons.Default.ShoppingCart
        "restaurant" -> Icons.Default.Restaurant
        "directions_car" -> Icons.Default.LocalGasStation
        "receipt_long" -> Icons.Default.ReceiptLong
        "school" -> Icons.Default.School
        "payments" -> Icons.Default.Payments
        "add_card" -> Icons.Default.AccountBalanceWallet
        else -> Icons.Default.Help
    }
    val iconColor = category?.colorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: MaterialTheme.colorScheme.primary

    val format = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val dateText = format.format(Date(tx.dateMillis))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(12.dp)
            .testTag("transaction_item_${tx.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = tx.description,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$dateText • ${tx.source.uppercase(Locale.ROOT)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)
                )
            }
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${if (tx.isIncome) "+" else "-"} Rs. ${String.format("%,.0f", tx.amount)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (tx.isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp).testTag("delete_tx_btn")) {
                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(0.6f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

// Helpers
// Empty to prevent cyclical recursion

@Composable
fun SmsLoggingSimulatorBox(viewModel: MainViewModel, lang: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val darkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    val shadowColor = if (darkTheme) Color(0xFF161517) else Color(0xFF1D1B1E)
    val borderColor = if (darkTheme) Color(0xFFE6E1E5) else Color(0xFF1D1B1E)
    val cardBgColor = if (darkTheme) Color(0xFF2E2C30) else Color(0xFFFCF9F6)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp, end = 4.dp)
    ) {
        // Drop Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(color = shadowColor, shape = RoundedCornerShape(28.dp))
        )
        // Card Front
        Card(
            modifier = Modifier.fillMaxWidth().testTag("sms_simulator_bento_card"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header of the Bento Module
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang == "ur") "SMS لاگنگ سمیلیٹر" else "Bento SMS Logging Quick Actions",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Offline tag
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = if (lang == "ur") "آف لائن" else "OFFLINE READY",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                Text(
                    text = if (lang == "ur") 
                        "بینک کے فرضی پیغامات موصول کرنے اور بٹوے میں خودکار لاگنگ کی جانچ کے لیے نیچے ٹیپ کریں:" 
                    else 
                        "Tap below to simulate realistic bank alerts and test instant auto-logging parsing rules:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Grid of 4 quick simulation triggers
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Action 1: McDonald's Debit (Food & Retail)
                        BentoSimulateBtn(
                            title = "McDonald PLC Alert",
                            desc = "Spend Rs. 1,500",
                            icon = Icons.Default.Fastfood,
                            color = Color(0xFFFFB300),
                            onClick = {
                                viewModel.simulateSmsReceived(
                                    "Rs. 1,500.00 debited from your account ending 1234 at McDonald's",
                                    "HBL-Alert"
                                )
                                android.widget.Toast.makeText(context, "HBL SMS Simulated: Rs. 1500 Food Debit!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // Action 2: Salary Credit (Income)
                        BentoSimulateBtn(
                            title = "Payroll Direct Transfer",
                            desc = "Earn Rs. 25,000",
                            icon = Icons.Default.CardMembership,
                            color = Color(0xFF4CAF50),
                            onClick = {
                                viewModel.simulateSmsReceived(
                                    "Rs. 25,000.00 credited to your account ending 5678",
                                    "MCB-Direct"
                                )
                                android.widget.Toast.makeText(context, "MCB SMS Simulated: Rs. 25000 Income Added!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Action 3: Metro Grocery (Essential Groceries)
                        BentoSimulateBtn(
                            title = "Metro Grocery Co",
                            desc = "Spend Rs. 4,200",
                            icon = Icons.Default.LocalGroceryStore,
                            color = Color(0xFF03A9F4),
                            onClick = {
                                viewModel.simulateSmsReceived(
                                    "rs. 4,200.00 spent on card 4321 at Metro Grocery",
                                    "MeezanBank"
                                )
                                android.widget.Toast.makeText(context, "Meezan SMS Simulated: Rs. 4200 Groceries!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // Action 4: Fuel Station Transport
                        BentoSimulateBtn(
                            title = "Shell Station PKR",
                            desc = "Spend Rs. 2,800",
                            icon = Icons.Default.LocalGasStation,
                            color = Color(0xFFE91E63),
                            onClick = {
                                viewModel.simulateSmsReceived(
                                    "PKR 2,800.00 with card 1122 at Shell Fuel Station",
                                    "UBL-Alerts"
                                )
                                android.widget.Toast.makeText(context, "UBL SMS Simulated: Rs. 2800 Fuel Debit!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BentoSimulateBtn(
    title: String,
    desc: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val offsetAnim by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isPressed) 0.dp else 2.5.dp,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        )
    )

    val darkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    val borderColor = if (darkTheme) Color(0xFFE6E1E5) else Color(0xFF1D1B1E)
    val buttonBg = if (darkTheme) Color(0xFF3F3D41) else Color(0xFFF7F2EE)

    Box(
        modifier = modifier
            .padding(bottom = 3.dp, end = 3.dp)
            .height(56.dp)
            .pointerInput(Unit) {
                this.detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            }
    ) {
        // Shadow base
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 3.dp, y = 3.dp)
                .background(Color.Black, RoundedCornerShape(16.dp))
        )
        // Button Front Layer
        Row(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = offsetAnim, y = offsetAnim)
                .background(buttonBg, RoundedCornerShape(16.dp))
                .background(color.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(verticalArrangement = Arrangement.Center, modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = desc,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

