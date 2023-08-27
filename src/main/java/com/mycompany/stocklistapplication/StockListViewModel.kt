package com.mycompany.stocklistapplication


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StockListViewModel : ViewModel() {
    private val stockRepository = StockRepository.get()
    val stockListLiveData = stockRepository.getStocks()
    private val filteredStocks = MutableLiveData<List<Stock>>()


    init {
        filteredStocks.value = emptyList()
        stockListLiveData.observeForever {
            filterStock(filteredStocks.value.orEmpty().toString())
        }
    }
    fun filterStock(query: String): List<Stock> {
        return if (query.isBlank()) {
            stockListLiveData.value.orEmpty()
        } else {
            stockListLiveData.value?.filter { task ->
                task.name.contains(query, ignoreCase = true)
            } ?: emptyList()
        }


    }
}