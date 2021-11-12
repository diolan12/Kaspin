package com.syscode.kaspin.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.syscode.kaspin.data.database.dao.CartItemDao
import com.syscode.kaspin.data.database.dao.ProductDao
import com.syscode.kaspin.data.database.model.CartItem
import com.syscode.kaspin.data.database.model.Catalog
import com.syscode.kaspin.data.database.model.Product

class TransactionRepositoryImpl(
    private val productDao: ProductDao,
    private val cartItemDao: CartItemDao
) : TransactionRepository {

    private val tempCatalogs = arrayListOf<Catalog>()
    private val tempProducts = arrayListOf<Product>()
    private val tempCarts = arrayListOf<CartItem>()
    init {
        productDao.getAllClean().observeForever { products ->
            tempProducts.clear()
            tempProducts.addAll(products.filter { it.stock != 0 })
            Log.i("changedProduct", "size ${products.size}")
            init()
        }
        cartItemDao.getCartItems().observeForever { cartItems ->
            tempCarts.clear()
            tempCarts.addAll(cartItems)
            Log.i("changedCart", "size ${cartItems.size}")
            init()
        }
    }

    private val _catalogs = MutableLiveData<List<Catalog>>()
    override val catalogs: LiveData<List<Catalog>>
        get() = _catalogs

    override val carts: LiveData<List<CartItem>>
        get() = cartItemDao.getCartItems()
    override val sales: LiveData<List<CartItem>>
        get() = cartItemDao.getCartCheckedOut()

    override fun init() {
        tempCatalogs.clear()
        tempProducts.map { product ->
            tempCatalogs.add(
                Catalog(
                productId = product.id,
                    title = product.title,
                    category = product.category,
                    image = product.image,
                    stock = product.stock,
                    price = product.price,
                    cart = tempCarts.firstOrNull { it.product == product.id }
                ))
        }
        _catalogs.postValue(tempCatalogs)
    }

    override suspend fun changeQuantity(cartId: Int, productId: Int, quantity: Int) {
        if (quantity == 0) {
            // delete if 0
            cartItemDao.deleteCartItem(cartItem = CartItem(
                id = cartId,
                product = productId,
                quantity = quantity
            ))
            tempCatalogs.map {
                if (it.productId == productId) it.cart = null
            }
            init()
            Log.i("changeQuantity", "deleted cart $cartId")
        } else {
            if (cartItemDao.getCartItem(cartItemId = cartId) == 0) {
                // create if 0
                cartItemDao.create(
                    CartItem(
                        id = cartId,
                        product = productId,
                        quantity = quantity
                    )
                )
                init()
                Log.i("changeQuantity", "created cart")
            } else {
                // updating
                cartItemDao.changeQuantity(cartItemId = cartId, quantity = quantity)
                init()
                Log.i("changeQuantity", "updated cart $cartId quantity $quantity")
            }
        }
        init()
    }

    override suspend fun clear() {
        cartItemDao.clearCarts(tempCarts)
        init()
    }

    override suspend fun checkOut() {
        // sebelum checkout, kurangi stok produk
        tempCarts.map {
            productDao.updateStock(it.product, stock = productDao.getByIdP(it.product).stock - it.quantity)
        }
        cartItemDao.checkOut()
        init()
    }
}