package com.syscode.kaspin.data.database.model


import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "product", indices = [
    Index(value = ["id", "code"], unique = true)
])
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val code: Int,
    val title: String,
    val image: String,
    val category: String,
    val price: String,
    var stock: Int,
    var deleted: Boolean = false
)