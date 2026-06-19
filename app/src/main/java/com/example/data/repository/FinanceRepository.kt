package com.example.data.repository

import com.example.data.db.BudgetDao
import com.example.data.db.CategoryDao
import com.example.data.db.TransactionDao
import com.example.data.model.Budget
import com.example.data.model.Category
import com.example.data.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FinanceRepository(
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) {
    val categories: Flow<List<Category>> = categoryDao.getAllCategories()
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val confirmedTransactions: Flow<List<Transaction>> = transactionDao.getConfirmedTransactions()
    val pendingTransactions: Flow<List<Transaction>> = transactionDao.getPendingTransactions()
    val budgets: Flow<List<Budget>> = budgetDao.getAllBudgets()

    suspend fun ensureCategoriesSeeded() = withContext(Dispatchers.IO) {
        val existing = categoryDao.getAllCategoriesList()
        if (existing.isEmpty()) {
            categoryDao.insertCategories(Category.DEFAULT_CATEGORIES)
        }
    }

    suspend fun addTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun confirmTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        transactionDao.updateTransaction(transaction.copy(isPending = false))
    }

    suspend fun deleteTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Int) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun addCategory(category: Category) = withContext(Dispatchers.IO) {
        categoryDao.insertCategory(category)
    }

    suspend fun addBudget(budget: Budget) = withContext(Dispatchers.IO) {
        budgetDao.insertBudget(budget)
    }

    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>> {
        return budgetDao.getBudgetsForMonth(month, year)
    }

    suspend fun deleteBudget(id: Int) = withContext(Dispatchers.IO) {
        budgetDao.deleteBudgetById(id)
    }
}
