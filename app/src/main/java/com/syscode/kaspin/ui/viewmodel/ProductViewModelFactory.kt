package com.syscode.kaspin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.syscode.kaspin.data.repository.FakeStoreRepository

class ProductViewModelFactory(
    private val fakeStoreRepository: FakeStoreRepository
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ProductViewModel(fakeStoreRepository) as T
    }
}