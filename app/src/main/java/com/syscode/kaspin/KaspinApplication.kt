package com.syscode.kaspin

import android.app.Application
import com.syscode.kaspin.data.database.KaspinDatabase
import com.syscode.kaspin.data.network.datasource.FakeStoreDataSource
import com.syscode.kaspin.data.network.datasource.FakeStoreDataSourceImpl
import com.syscode.kaspin.data.network.service.FakeStoreService
import com.syscode.kaspin.data.repository.FakeStoreRepository
import com.syscode.kaspin.data.repository.FakeStoreRepositoryImpl
import com.syscode.kaspin.data.repository.TransactionRepository
import com.syscode.kaspin.data.repository.TransactionRepositoryImpl
import com.syscode.kaspin.ui.viewmodel.ProductViewModel
import com.syscode.kaspin.ui.viewmodel.ProductViewModelFactory
import com.syscode.kaspin.ui.viewmodel.TransactionViewModelFactory
import kotlinx.coroutines.DelicateCoroutinesApi
import org.kodein.di.*
import org.kodein.di.android.x.androidXModule

class KaspinApplication : Application(), DIAware {
    override val di = DI.lazy {
        import(androidXModule(this@KaspinApplication))

        // database
        bindSingleton { KaspinDatabase(instance()) }
        // dao
        bindSingleton { instance<KaspinDatabase>().productDao() }
        bindSingleton { instance<KaspinDatabase>().cartItemDao() }

        // rest service
        bindSingleton { FakeStoreService() }

        // data source
        bindSingleton<FakeStoreDataSource> { FakeStoreDataSourceImpl(instance()) }

        // repository
        bindSingleton<FakeStoreRepository> { FakeStoreRepositoryImpl(instance(), instance()) }
        bindSingleton<TransactionRepository> { TransactionRepositoryImpl(instance(), instance()) }

        // view model factory
        bindProvider { ProductViewModelFactory(instance()) }
        bindProvider { TransactionViewModelFactory(instance()) }
    }
}