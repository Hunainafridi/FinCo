package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.Transaction
import com.example.ui.viewmodel.MainViewModel

@Composable
fun TransactionHistoryScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    lang: String
) {
    val transactions by viewModel.confirmedTransactions.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }

    val filteredList = transactions.filter { tx ->
        val catMatches = selectedCategoryId == null || tx.categoryId == selectedCategoryId
        val queryMatches = searchQuery.isEmpty() || tx.description.contains(searchQuery, ignoreCase = true)
        catMatches && queryMatches
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("transaction_history_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // App bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("back_to_dashboard")) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (lang == "ur") "Kul Kharche aur Kamayi" else if (lang == "ps") "Kul Masraf" else "All Logs History",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Search Bar row
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (lang == "ur") "Dhoondein..." else "Search keyword...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("tx_history_search")
        )

        // Horizontal scrolling category filters
        Text(
            text = if (lang == "ur") "Category k mutabiq filter karein:" else "Filter by category:",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // "All" pill
                FilterChip(
                    selected = selectedCategoryId == null,
                    onClick = { selectedCategoryId = null },
                    label = { Text("All") },
                    shape = RoundedCornerShape(14.dp)
                )
                categories.forEach { cat ->
                    val isSelected = selectedCategoryId == cat.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryId = if (isSelected) null else cat.id },
                        label = { Text(cat.name) },
                        shape = RoundedCornerShape(14.dp),
                        colors = if (isSelected) {
                            val catColor = Color(android.graphics.Color.parseColor(cat.colorHex))
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor = catColor.copy(alpha = 0.25f),
                                selectedLabelColor = catColor
                            )
                        } else FilterChipDefaults.filterChipColors()
                    )
                }
            }
        }

        // Scrolling lists
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (lang == "ur") "Koi cheez nahi mili" else "No matching transactions found.",
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList) { tx ->
                    val catObj = categories.find { it.id == tx.categoryId }
                    TransactionItem(
                        tx = tx,
                        category = catObj,
                        onDelete = { viewModel.deleteTransaction(tx) },
                        lang = lang
                    )
                }
            }
        }
    }
}
