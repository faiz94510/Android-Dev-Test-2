package com.liburngoding.androiddevtest2.ui.theme.main.data.repository

import androidx.lifecycle.LiveData
import androidx.room.Query
import com.liburngoding.androiddevtest2.ui.theme.main.data.local.dao.TransactionDao
import com.liburngoding.androiddevtest2.ui.theme.main.data.model.Transaction

class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransaction : LiveData<List<Transaction>> = transactionDao.getAllTransaction()

    suspend fun insert(transaction: Transaction){
        transactionDao.insertTrasaction(transaction)
    }

    suspend fun deleteTransaction(transactionId: Int) {
        transactionDao.deleteTransactionById(transactionId)
    }

    suspend fun getTransactionById(transactionId: Int): Transaction? {
        return transactionDao.getTransactionById(transactionId)
    }

    fun getTransactionsByDateRange(startTimestamp: Long, endTimestamp: Long): LiveData<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(startTimestamp, endTimestamp)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }


}