package com.syscode.kaspin.data.database.model

data class Catalog(
    val productId: Int,
    val title: String,
    val category: String,
    val image: String,
    val stock: Int,
    val price: String,
    var cart: CartItem?
)
