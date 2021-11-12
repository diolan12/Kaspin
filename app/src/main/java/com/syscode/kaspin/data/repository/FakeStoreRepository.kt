package com.syscode.kaspin.data.repository

import androidx.lifecycle.LiveData
import com.syscode.kaspin.data.database.model.Product
import com.syscode.kaspin.data.network.service.FakeStoreService
import kotlinx.coroutines.flow.StateFlow

interface FakeStoreRepository {
    val state: StateFlow<FakeStoreService.State>

    val products: LiveData<List<Product>>

    val productsClean: LiveData<List<Product>>

    val productsBin: LiveData<List<Product>>

    suspend fun getAllProducts(async: Boolean = false, withTrash: Boolean = false)

    suspend fun getProduct(id: Int): LiveData<Product>

    suspend fun saveProduct(product: Product)

    suspend fun updateProduct(product: Product)

    suspend fun updateStock(id: Int, stock: Int)

    suspend fun deleteProduct(id: Int)

    suspend fun restoreProduct(id: Int)
}