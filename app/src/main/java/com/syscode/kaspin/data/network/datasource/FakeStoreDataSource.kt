package com.syscode.kaspin.data.network.datasource

import com.syscode.kaspin.data.network.service.FakeStoreService
import kotlinx.coroutines.flow.StateFlow

interface FakeStoreDataSource {
    val fakeStoreState: StateFlow<FakeStoreService.State>

    suspend fun fetchAllProducts()
}