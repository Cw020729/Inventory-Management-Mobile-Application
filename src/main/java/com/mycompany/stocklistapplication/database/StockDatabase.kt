package com.mycompany.stocklistapplication.database


import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mycompany.stocklistapplication.Stock
import java.util.*

private const val DATABASE_NAME = "stock-database"

@Database(entities = [Stock::class], version = 2, exportSchema = false)
@TypeConverters(StockTypeConverters::class)
abstract class StockDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao

}
