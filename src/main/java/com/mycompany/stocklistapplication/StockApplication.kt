package com.mycompany.stocklistapplication

import android.app.Application

class StockApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        StockRepository.initialize(this)
    }
}