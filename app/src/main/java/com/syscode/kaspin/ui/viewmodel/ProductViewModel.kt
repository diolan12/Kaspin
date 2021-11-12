package com.syscode.kaspin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syscode.kaspin.data.database.model.Product
import com.syscode.kaspin.data.network.service.FakeStoreService
import com.syscode.kaspin.data.repository.FakeStoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel(
    private val fakeStoreRepository: FakeStoreRepository
): ViewModel() {
    val fakeStoreState: StateFlow<FakeStoreService.State> = fakeStoreRepository.state

    val products = fakeStoreRepository.products
    val productsClean = fakeStoreRepository.productsClean
    val productsBin = fakeStoreRepository.productsBin

    val categories = listOf("electronics", "jewelery", "men's clothing", "women's clothing")

    suspend fun getProduct(id: Int) = fakeStoreRepository.getProduct(id)

    fun create(product: Product) = viewModelScope.launch(Dispatchers.IO) {
        fakeStoreRepository.saveProduct(product)
    }
    fun update(product: Product) = viewModelScope.launch(Dispatchers.IO) {
        fakeStoreRepository.updateProduct(product)
    }
    fun updateStock(id: Int, stock: Int) = viewModelScope.launch(Dispatchers.IO) {
        fakeStoreRepository.updateStock(id, stock)
    }
    fun delete(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        fakeStoreRepository.deleteProduct(id)
    }
    fun restore(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        fakeStoreRepository.restoreProduct(id)
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fakeStoreRepository.getAllProducts()
        }
    }
}