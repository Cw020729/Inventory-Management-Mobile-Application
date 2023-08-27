package com.mycompany.stocklistapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.*

class StockDetailViewModel : ViewModel() {
    private val stockRepository: StockRepository = StockRepository.get()
    private val stockIdLiveData = MutableLiveData<UUID>()

    var stockLiveData: LiveData<Stock?> = Transformations.switchMap(stockIdLiveData) {
        stockRepository.getStock(it)
    }

    fun loadStock(stockId: UUID) {
        stockIdLiveData.value = stockId
    }



}