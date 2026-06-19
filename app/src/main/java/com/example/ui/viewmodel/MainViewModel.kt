package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.gemini.GeminiCoachService
import com.example.data.model.Budget
import com.example.data.model.Category
import com.example.data.model.Transaction
import com.example.data.prefs.PreferencesManager
import com.example.data.repository.FinanceRepository
import com.example.data.sms.SmsParser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = FinanceRepository(db.categoryDao(), db.transactionDao(), db.budgetDao())
    private val prefsManager = PreferencesManager(application)
    private val geminiCoachService = GeminiCoachService(application)

    // Firebase Auth State
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firebaseUser = MutableStateFlow<FirebaseUser?>(null)
    val isOfflineMode = MutableStateFlow(false)

    val isSandboxActive: StateFlow<Boolean> = prefsManager.isSandboxActive
    val sandboxEmail: StateFlow<String?> = prefsManager.sandboxEmail

    fun loginSandboxUser(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            val verified = prefsManager.verifySandboxUser(email, password)
            if (verified) {
                prefsManager.setSandboxSession(true, email)
                isOfflineMode.value = true
                onSuccess()
            } else {
                onFailure("Incorrect credentials, or email is not yet registered. Let's register via Sign Up tab first!")
            }
        }
    }

    fun signUpSandboxUser(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            if (email.isBlank() || password.length < 6) {
                onFailure("Requires a valid email and a password of at least 6 characters.")
                return@launch
            }
            prefsManager.registerSandboxUser(email, password)
            prefsManager.setSandboxSession(true, email)
            isOfflineMode.value = true
            onSuccess()
        }
    }

    fun loginWithFirebase(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            firebaseUser.value = auth.currentUser
                            isOfflineMode.value = false
                            onSuccess()
                        } else {
                            onFailure(task.exception?.localizedMessage ?: "Authentication failed.")
                        }
                    }
            } catch (e: Exception) {
                onFailure(e.localizedMessage ?: "An error occurred.")
            }
        }
    }

    fun signUpWithFirebase(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            firebaseUser.value = auth.currentUser
                            isOfflineMode.value = false
                            onSuccess()
                        } else {
                            onFailure(task.exception?.localizedMessage ?: "Signup failed.")
                        }
                    }
            } catch (e: Exception) {
                onFailure(e.localizedMessage ?: "An error occurred.")
            }
        }
    }

    fun signOutFromFirebase() {
        viewModelScope.launch {
            try {
                auth.signOut()
                firebaseUser.value = null
            } catch (e: Exception) {
                // Ignore
            }
            prefsManager.setSandboxSession(false, null)
            isOfflineMode.value = false
        }
    }

    fun setOfflineMode(enabled: Boolean) {
        isOfflineMode.value = enabled
    }

    // Wire up Preference States
    val appLanguage: StateFlow<String> = prefsManager.appLanguage
    val startingBalance: StateFlow<Double> = prefsManager.startingBalance
    val hasOnboarded: StateFlow<Boolean> = prefsManager.hasOnboarded
    val pinPasscode: StateFlow<String?> = prefsManager.pinPasscode
    val smsAutoLoggingEnabled: StateFlow<Boolean> = prefsManager.smsAutoLoggingEnabled

    // Database Flows
    val categories: StateFlow<List<Category>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val confirmedTransactions: StateFlow<List<Transaction>> = repository.confirmedTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTransactions: StateFlow<List<Transaction>> = repository.pendingTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<Budget>> = repository.budgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Feedback Banner flow
    private val _budgetAlert = MutableStateFlow<String?>(null)
    val budgetAlert: StateFlow<String?> = _budgetAlert

    // Framer-motion inspired Celebration states
    private val _activeCelebration = MutableStateFlow<CelebrationData?>(null)
    val activeCelebration: StateFlow<CelebrationData?> = _activeCelebration

    fun triggerCelebration(title: String, subtitle: String, amount: String, type: CelebrationType) {
        viewModelScope.launch {
            _activeCelebration.value = CelebrationData(title, subtitle, amount, type)
        }
    }

    fun clearCelebration() {
        _activeCelebration.value = null
    }

    // AI Coaching States
    private val _coachingInsight = MutableStateFlow("")
    val coachingInsight: StateFlow<String> = _coachingInsight

    private val _coachingLoading = MutableStateFlow(false)
    val coachingLoading: StateFlow<Boolean> = _coachingLoading

    // Computed Financial Metrics combining DB state with Preferences
    val currentBalance: StateFlow<Double> = combine(startingBalance, confirmedTransactions) { start, txs ->
        val incomes = txs.filter { it.isIncome }.sumOf { it.amount }
        val expenses = txs.filter { !it.isIncome }.sumOf { it.amount }
        start + incomes - expenses
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val thisMonthSpend: StateFlow<Double> = confirmedTransactions.combine(appLanguage) { txs, _ ->
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        txs.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
            !it.isIncome && cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
        }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val spendByCategory: StateFlow<Map<Category, Double>> = combine(confirmedTransactions, categories) { txs, cats ->
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val thisMonthExpenses = txs.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
            !it.isIncome && cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
        }

        cats.associateWith { cat ->
            thisMonthExpenses.filter { it.categoryId == cat.id }.sumOf { it.amount }
        }.filterValues { it > 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val dailyBurnRate: StateFlow<Double> = confirmedTransactions.combine(appLanguage) { txs, _ ->
        val currentMillis = System.currentTimeMillis()
        val fourteenDaysAgo = currentMillis - (14 * 24 * 60 * 60 * 1000L)
        val expensesInWindow = txs.filter { !it.isIncome && it.dateMillis >= fourteenDaysAgo }
        
        if (expensesInWindow.isEmpty()) {
            0.0
        } else {
            val firstTxDate = expensesInWindow.minOf { it.dateMillis }
            val daysDiff = ((currentMillis - firstTxDate) / (24 * 60 * 60 * 1000.0)).coerceAtLeast(1.0).coerceAtMost(14.0)
            expensesInWindow.sumOf { it.amount } / daysDiff
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val forecastShortfallDay: StateFlow<Int?> = combine(currentBalance, dailyBurnRate) { balance, burnRate ->
        if (burnRate <= 0.0 || balance <= 0.0) return@combine null

        val daysToShortfall = (balance / burnRate).toInt()
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val daysRemaining = lastDay - currentDay

        if (daysToShortfall < daysRemaining) daysToShortfall else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            try {
                firebaseUser.value = auth.currentUser
            } catch (e: Exception) {
                // Ignore if Play Services / configuration is unready
            }

            if (isSandboxActive.value) {
                isOfflineMode.value = true
            }

            // Seeding default categories
            repository.ensureCategoriesSeeded()
            
            // Load initial cached AI coaching insight
            val lang = prefsManager.appLanguage.value
            _coachingInsight.value = geminiCoachService.getCachedInsightOrDefault(lang)
        }
    }

    fun completeOnboarding(balance: Double, language: String, pin: String?, smsLogging: Boolean) {
        viewModelScope.launch {
            prefsManager.setStartingBalance(balance)
            prefsManager.setAppLanguage(language)
            prefsManager.setPinPasscode(pin)
            prefsManager.setSmsAutoLoggingEnabled(smsLogging)
            prefsManager.setHasOnboarded(true)
            
            // Re-seed categories just in case
            repository.ensureCategoriesSeeded()
        }
    }

    fun clearBudgetAlert() {
        _budgetAlert.value = null
    }

    fun addTransaction(amount: Double, description: String, categoryId: String, isIncome: Boolean, source: String = "manual") {
        viewModelScope.launch {
            val tx = Transaction(
                amount = amount,
                dateMillis = System.currentTimeMillis(),
                description = description,
                categoryId = categoryId,
                isIncome = isIncome,
                source = source
            )
            repository.addTransaction(tx)

            // Trigger budget threshold alerts
            if (!isIncome) {
                checkBudgetThreshold(categoryId, amount)
            }

            // Trigger beautiful celebration overlay event
            val titleText = if (isIncome) "Income Logged! 💰" else "Expense Logged! ✨"
            triggerCelebration(
                title = titleText,
                subtitle = if (description.isNotBlank()) description else "Added manually to wallet",
                amount = String.format("Rs. %,.0f", amount),
                type = CelebrationType.EXPENSE_LOGGED
            )
        }
    }

    fun confirmPendingTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.confirmTransaction(transaction)
            if (!transaction.isIncome) {
                checkBudgetThreshold(transaction.categoryId, transaction.amount)
            }

            // Trigger beautiful celebration overlay event
            triggerCelebration(
                title = "SMS Alert Approved! ✅",
                subtitle = transaction.description,
                amount = String.format("Rs. %,.0f", transaction.amount),
                type = CelebrationType.EXPENSE_LOGGED
            )
        }
    }

    fun discardPendingTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun updateBudget(categoryId: String, monthlyLimit: Double) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)
            val budget = Budget(categoryId = categoryId, monthlyLimit = monthlyLimit, month = month, year = year)
            repository.addBudget(budget)

            val catName = categories.value.find { it.id == categoryId }?.name ?: "Category"
            // Trigger beautiful celebration overlay event for setting/completing budget goals
            triggerCelebration(
                title = "Budget Goal Configured! 🎯",
                subtitle = "Monthly target set for $catName",
                amount = String.format("Rs. %,.0f", monthlyLimit),
                type = CelebrationType.BUDGET_COMPLETED
            )
        }
    }

    fun fetchAiCoachingInsight() {
        viewModelScope.launch {
            _coachingLoading.value = true
            
            val total = thisMonthSpend.value
            val breakdown = spendByCategory.value.entries.associate { it.key.name to it.value }
            val shortfall = forecastShortfallDay.value
            
            val forecastMsg = if (shortfall != null) {
                "User is projected to run out of money in $shortfall days before month-end."
            } else {
                "User is in the green for this month."
            }

            val lang = appLanguage.value
            val response = geminiCoachService.getCoachingInsight(total, breakdown, forecastMsg, lang)
            _coachingInsight.value = response
            _coachingLoading.value = false
        }
    }

    fun changeLanguage(language: String) {
        prefsManager.setAppLanguage(language)
        // Refresh cached advice for this new language selector
        _coachingInsight.value = geminiCoachService.getCachedInsightOrDefault(language)
    }

    fun setSmsLoggingEnabled(enabled: Boolean) {
        prefsManager.setSmsAutoLoggingEnabled(enabled)
    }

    fun simulateSmsReceived(smsBody: String, sender: String) {
        viewModelScope.launch {
            val parsed = SmsParser.parse(smsBody, sender, System.currentTimeMillis())
            if (parsed != null) {
                val tx = SmsParser.parsedToEntity(parsed, sender).copy(isPending = true)
                repository.addTransaction(tx)
            }
        }
    }

    // Checking if this new transaction crosses 80% or 100% budget threshold of its category
    private suspend fun checkBudgetThreshold(catId: String, addedAmount: Double) {
        val calendar = Calendar.getInstance()
        val m = calendar.get(Calendar.MONTH) + 1
        val y = calendar.get(Calendar.YEAR)
        
        // Find if budget exists for this category
        val currentBudgets = budgets.value
        val categoryBudget = currentBudgets.find { it.categoryId == catId && it.month == m && it.year == y } ?: return

        // Fetch how much we spent this month on this category
        val allTxs = confirmedTransactions.value
        val currentMonthTxs = allTxs.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
            it.categoryId == catId && !it.isIncome && (cal.get(Calendar.MONTH) + 1) == m && cal.get(Calendar.YEAR) == y
        }

        val totalSpentPrior = currentMonthTxs.filter { it.dateMillis < System.currentTimeMillis() - 5000 }.sumOf { it.amount }
        val grandTotalSpent = totalSpentPrior + addedAmount
        val budgetLimit = categoryBudget.monthlyLimit

        val catName = categories.value.find { it.id == catId }?.name ?: "Category"

        if (grandTotalSpent >= budgetLimit) {
            _budgetAlert.value = "⚠️ BUDGET EXCEEDED! You spent Rs. $grandTotalSpent out of your Rs. $budgetLimit budget for $catName."
        } else if (grandTotalSpent >= (budgetLimit * 0.8) && totalSpentPrior < (budgetLimit * 0.8)) {
            _budgetAlert.value = "🔔 WARNING: You have used 80% of your Rs. $budgetLimit budget for $catName."
        }
    }
}

data class CelebrationData(
    val title: String,
    val subtitle: String,
    val amount: String,
    val type: CelebrationType
)

enum class CelebrationType {
    EXPENSE_LOGGED,
    BUDGET_COMPLETED
}
