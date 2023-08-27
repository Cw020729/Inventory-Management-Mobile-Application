package com.mycompany.stocklistapplication
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Stock(
    @PrimaryKey var id: UUID = UUID.randomUUID(),
    var name: String = "",
    var manufacture: String = "",
    var costPerItem: Double = 0.00,
    var quantity: Int = 0,
    var restockDate: Date = Date(),
    var itemPicture: String = "" // Path or URL to the item picture
)

