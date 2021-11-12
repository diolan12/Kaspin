package com.syscode.kaspin.data.network.datasource

import android.util.Log
import com.syscode.kaspin.data.network.service.FakeStoreService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeStoreDataSourceImpl(
    private val fakeStoreService: FakeStoreService
) : FakeStoreDataSource {
    private val _fakeStoreState = MutableStateFlow<FakeStoreService.State>(FakeStoreService.State.Idle)
    override val fakeStoreState: StateFlow<FakeStoreService.State>
        get() = _fakeStoreState

    override suspend fun fetchAllProducts() {
        _fakeStoreState.tryEmit(FakeStoreService.State.Running)
        Log.i("DataSource", "running...")
        try {
            fakeStoreService.asyncGetAllProducts().apply {
                _fakeStoreState.tryEmit(FakeStoreService.State.Success(this))
                Log.d("Success", this.toString())
            }
        } catch (e: Exception) {
            _fakeStoreState.tryEmit(FakeStoreService.State.Error(e))
            Log.e("Error", e.toString())

        } finally {
            _fakeStoreState.tryEmit(FakeStoreService.State.Idle)
            Log.i("DataSource", "idle")
        }
    }
}