package com.syscode.kaspin.data.repository

import androidx.lifecycle.LiveData
import com.syscode.kaspin.data.database.dao.ProductDao
import com.syscode.kaspin.data.database.model.Product
import com.syscode.kaspin.data.network.datasource.FakeStoreDataSource
import com.syscode.kaspin.data.network.service.FakeStoreService
import com.syscode.kaspin.internal.randomInt
import com.syscode.kaspin.internal.toRupiah
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@DelicateCoroutinesApi
class FakeStoreRepositoryImpl(
    private val fakeStoreDataSource: FakeStoreDataSource,
    private val productDao: ProductDao
) : FakeStoreRepository {
    init {
        GlobalScope.launch(Dispatchers.IO) {
            fakeStoreDataSource.fakeStoreState.collect { it ->
                when (it) {
                    is FakeStoreService.State.Success -> {
                        val _data = arrayListOf<Product>()
                        it.data.map {
                            val price = (it.price * 14500).toInt().toString()
                            _data.add(
                                Product(id = it.id,
                                    code = randomInt(),
                                    title = it.title,
                                    image = it.image,
                                    category = it.category,
                                    price = price.toRupiah(),
                                    stock = (1..25).random()
                                )
                            )
                        }
                        persistAll(_data)
                    }
                    else -> {}
                }
            }
        }
    }

    override val state: StateFlow<FakeStoreService.State>
        get() = fakeStoreDataSource.fakeStoreState

    override val products: LiveData<List<Product>>
        get() = productDao.getAllWithTrash()

    override val productsClean: LiveData<List<Product>>
        get() = productDao.getAllClean()

    override val productsBin: LiveData<List<Product>>
        get() = productDao.getAllWithTrash()

    override suspend fun getAllProducts(async: Boolean, withTrash: Boolean) {
        // async data with rest api
        if (productDao.rowCount() == 0 || async) {
            fakeStoreDataSource.fetchAllProducts()
        }
    }

    override suspend fun getProduct(id: Int): LiveData<Product> {
        return productDao.getById(id)
    }

    override suspend fun saveProduct(product: Product) {
        productDao.create(product)
    }

    override suspend fun updateProduct(product: Product) {
        productDao.update(product)
    }

    override suspend fun updateStock(id: Int, stock: Int) {
        productDao.updateStock(idKey = id, stock = stock)
    }

    override suspend fun deleteProduct(id: Int) {
        productDao.delete(id)
    }

    override suspend fun restoreProduct(id: Int) {
        productDao.restore(id)
    }

    private fun persistAll(products: ArrayList<Product>) {
        productDao.upsertAll(products)
    }

}

