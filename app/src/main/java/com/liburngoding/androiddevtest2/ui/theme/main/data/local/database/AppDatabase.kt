package com.liburngoding.androiddevtest2.ui.theme.main.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.liburngoding.androiddevtest2.ui.theme.main.data.local.dao.TransactionDao
import com.liburngoding.androiddevtest2.ui.theme.main.data.model.Transaction

@Database(entities = [Transaction::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao() : TransactionDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance"
                ).build()
                INSTANCE = instance
                instance
            }
        }

    }
}