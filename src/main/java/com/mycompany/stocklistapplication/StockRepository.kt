package com.mycompany.stocklistapplication

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import java.util.UUID
import java.util.concurrent.Executor
import com.mycompany.stocklistapplication.database.StockDao
import com.mycompany.stocklistapplication.database.StockDatabase
import java.util.concurrent.Executors

private const val DATABASE_NAME = "stock-database"

class StockRepository private constructor(context: Context) {
    private val database: StockDatabase =
        Room.databaseBuilder(context.applicationContext, StockDatabase::class.java, DATABASE_NAME)
            .build()
    private val stockDao: StockDao = database.stockDao()

    private val executor: Executor = Executors.newSingleThreadExecutor()

    fun getStocks(): LiveData<List<Stock>> = stockDao.getStocks()

    fun getStock(id: UUID): LiveData<Stock?> = stockDao.getStock(id)

    fun updateStock(stock: Stock) {
        executor.execute {
            stockDao.updateStock(stock)
        }
    }

    fun addStock(stock: Stock) {
        executor.execute {
            stockDao.addStock(stock)
        }
    }

    fun deleteStock(stock: Stock) {
        executor.execute {
            stockDao.deleteStock(stock)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: StockRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = StockRepository(context)
            }
        }

        fun get(): StockRepository {
            return INSTANCE ?: throw IllegalStateException("StockRepository must be initialized")
        }
    }
}
