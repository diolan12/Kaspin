package com.syscode.kaspin.data.repository

import androidx.lifecycle.LiveData
import com.syscode.kaspin.data.database.model.CartItem
import com.syscode.kaspin.data.database.model.Catalog

interface TransactionRepository {
    val catalogs: LiveData<List<Catalog>>

    val carts: LiveData<List<CartItem>>

    val sales: LiveData<List<CartItem>>

    fun init()

    suspend fun changeQuantity(cartId: Int, productId: Int, quantity: Int)

    suspend fun clear()

    suspend fun checkOut()

}