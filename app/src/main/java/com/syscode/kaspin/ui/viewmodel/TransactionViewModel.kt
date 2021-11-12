package com.syscode.kaspin.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syscode.kaspin.data.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val catalogs = transactionRepository.catalogs

    val carts = transactionRepository.carts

    val sales = transactionRepository.sales

    fun changeQuantity(cartId: Int = 0, productId: Int, quantity: Int) = viewModelScope.launch(Dispatchers.IO) {
        transactionRepository.changeQuantity(
            cartId = cartId,
            productId = productId,
            quantity = quantity
        )
    }

    fun clearCarts() = viewModelScope.launch(Dispatchers.IO) {
        transactionRepository.clear()
    }

    fun checkOut() = viewModelScope.launch(Dispatchers.IO) {
        transactionRepository.checkOut()
    }
    init {
        viewModelScope.launch(Dispatchers.IO) {
            transactionRepository.init()
        }
    }
}