package com.syscode.kaspin.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "cart_item", indices = [
    Index(value = ["id"], unique = true)
])
data class CartItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val product: Int,
    val quantity: Int,
    @ColumnInfo(name = "checked_out")
    val checkedOut: Boolean = false
)
