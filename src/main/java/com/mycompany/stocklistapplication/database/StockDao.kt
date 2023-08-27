package com.mycompany.stocklistapplication.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.mycompany.stocklistapplication.Stock
import java.util.*

@Dao
interface StockDao {
    @Query("SELECT * FROM stock")
    fun getStocks(): LiveData<List<Stock>>

    @Query("SELECT * FROM stock WHERE id=(:id)")
    fun getStock(id: UUID): LiveData<Stock?>

    @Update
    fun updateStock(stock : Stock)

    @Insert
    fun addStock(stock : Stock)

    @Delete
    fun deleteStock(stock: Stock)

}