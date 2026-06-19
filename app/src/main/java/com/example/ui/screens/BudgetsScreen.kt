package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.Budget
import com.example.data.model.Category
import com.example.ui.viewmodel.MainViewModel
import java.util.*

@Composable
fun BudgetsScreen(
    viewModel: MainViewModel,
    lang: String
) {
    val categories by viewModel.categories.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    val spendByCategory by viewModel.spendByCategory.collectAsState()

    var isAddingBudget by remember { mutableStateOf(false) }
    var budgetAmountStr by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }

    if (selectedCategoryId.isEmpty() && categories.isNotEmpty()) {
        selectedCategoryId = categories.first().id
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("budgets_screen"),
        floatingActionButton = {
            Physical3DFloatingActionButton(
                onClick = { isAddingBudget = true },
                icon = Icons.Default.Add,
                contentDescription = "Add Budget Limit",
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_budget_fab")
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Header
            Column {
                Text(
                    text = if (lang == "ur") "Mahana Budget" else if (lang == "ps") "Zat Meishte Budget" else "Category Budgets",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (lang == "ur") "Makhsoos kharche k limits tayyar karein" else "Set monthly spending boundaries for specific categories.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            if (budgets.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(0.3f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (lang == "ur") "Koi makhsoos limit set nahi kia!" else "No custom category budgets set yet.",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (lang == "ur") "Neeche wale button se makhsoos category (jaise chai-dhaba or transport) ka limit lagayein takay alerts milen." else "Set monthly limit targets to avoid overspending on utilities, petrol, dining, or fast-food.",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(budgets) { budget ->
                        val cat = categories.find { it.id == budget.categoryId } ?: return@items
                        val currentSpend = spendByCategory[cat] ?: 0.0
                        val percentage = if (budget.monthlyLimit > 0.0) (currentSpend / budget.monthlyLimit).toFloat() else 0f
                        
                        val progressColor = when {
                            percentage >= 1.0f -> MaterialTheme.colorScheme.error
                            percentage >= 0.8f -> Color(0xFFFFA726) // Orange
                            else -> MaterialTheme.colorScheme.primary
                        }

                        Physical3DCard(
                            modifier = Modifier.fillMaxWidth().testTag("budget_item_card_${budget.id}"),
                            shape = RoundedCornerShape(20.dp),
                            backgroundColor = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                        val catColor = Color(android.graphics.Color.parseColor(cat.colorHex))
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(catColor.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(imageVector = icon, contentDescription = null, tint = catColor, modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = cat.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                    
                                    val daysLeft = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH) - Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                                    Text(
                                        text = if (lang == "ur") "$daysLeft Din baqi" else "$daysLeft days left",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                // Progress bar indicator
                                LinearProgressIndicator(
                                    progress = { percentage.coerceIn(0.0f, 1.0f) },
                                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                                    color = progressColor,
                                    trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.15f)
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = String.format("Spend: Rs. %,.0f", currentSpend),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = String.format("Limit: Rs. %,.0f", budget.monthlyLimit),
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                        }
                    }
                }
            }
        }
    }

    // Modal to create/edit budget limits
    if (isAddingBudget) {
        AlertDialog(
            onDismissRequest = { isAddingBudget = false },
            confirmButton = {
                Physical3DButton(
                    onClick = {
                        val limit = budgetAmountStr.toDoubleOrNull()
                        if (limit != null && limit > 0.0 && selectedCategoryId.isNotEmpty()) {
                            viewModel.updateBudget(selectedCategoryId, limit)
                            budgetAmountStr = ""
                            isAddingBudget = false
                        }
                    },
                    text = if (lang == "ur") "Muntakhib" else "Set Limit",
                    modifier = Modifier.width(135.dp).testTag("save_budget_button")
                )
            },
            dismissButton = {
                TextButton(onClick = { isAddingBudget = false }) {
                    Text("Cancel")
                }
            },
            title = {
                Text(text = if (lang == "ur") "Budget Set Karein" else "Set Spending Limit", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (lang == "ur") "Kis category par limit lagayein?" else "Assign monthly spending ceiling for a specific category:",
                        fontSize = 12.sp
                    )

                    // Pick category dropdown or simple linear row list
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            categories.forEach { cat ->
                                val isSelected = selectedCategoryId == cat.id
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(android.graphics.Color.parseColor(cat.colorHex)).copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { selectedCategoryId = cat.id }
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = cat.name, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Budget Amount Limit Field
                    OutlinedTextField(
                        value = budgetAmountStr,
                        onValueChange = { budgetAmountStr = it },
                        label = { Text(if (lang == "ur") "Mahana Limit (PKR)" else "Max Monthly Limit (PKR)") },
                        prefix = { Text("Rs. ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("budget_limit_field")
                    )
                }
            }
        )
    }
}
