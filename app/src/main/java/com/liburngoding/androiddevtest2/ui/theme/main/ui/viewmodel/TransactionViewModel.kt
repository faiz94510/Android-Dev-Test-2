package com.liburngoding.androiddevtest2.ui.theme.main.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.liburngoding.androiddevtest2.ui.theme.main.data.local.database.AppDatabase
import com.liburngoding.androiddevtest2.ui.theme.main.data.model.Transaction
import com.liburngoding.androiddevtest2.ui.theme.main.data.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransactionRepository
    val allTransaction: LiveData<List<Transaction>>
    private val _filteredTransactions = MutableLiveData<List<Transaction>>() // MutableLiveData untuk transaksi terfilter
    val filteredTransactions: LiveData<List<Transaction>> get() = _filteredTransactions
    init {
        val userDao = AppDatabase.getDatabase(application).transactionDao()
        repository = TransactionRepository(userDao)
        allTransaction = repository.allTransaction

    }

    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }

    fun deleteTransaction(transactionId: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteTransaction(transactionId)
    }

    fun getTransactionById(transactionId: Int): LiveData<Transaction?> {
        val transactionLiveData = MutableLiveData<Transaction?>()
        viewModelScope.launch(Dispatchers.IO) {
            val transaction = repository.getTransactionById(transactionId)
            transactionLiveData.postValue(transaction)
        }
        return transactionLiveData
    }

    fun updateTransactionsByDateRange(startDate: String, endDate: String) {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val startTimestamp = dateFormat.parse(startDate)?.time ?: 0L
        val endTimestamp = dateFormat.parse(endDate)?.time ?: 0L

        repository.getTransactionsByDateRange(startTimestamp, endTimestamp).observeForever { transactions ->
            _filteredTransactions.value = transactions // Memperbarui transaksi terfilter
        }
    }

    fun updateTransaction(transaction: Transaction) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateTransaction(transaction)
    }
}