package com.liburngoding.androiddevtest2.ui.theme.main.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.liburngoding.androiddevtest2.ui.theme.main.data.model.Transaction

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTrasaction(transaction : Transaction)

    @Query("SELECT * FROM transactions")
    fun getAllTransaction(): LiveData<List<Transaction>>

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransactionById(transactionId: Int)

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: Int): Transaction?

    @Query("SELECT * FROM transactions WHERE created_at BETWEEN :startDate AND :endDate")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): LiveData<List<Transaction>>

    @Update
    suspend fun updateTransaction(transaction: Transaction)
}