package com.syscode.kaspin.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.syscode.kaspin.data.database.model.CartItem
import com.syscode.kaspin.data.repository.TransactionRepository

@Dao
interface CartItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun create(cartItem: CartItem)

    @Query("SELECT * FROM cart_item WHERE checked_out = 0")
    fun getCartItems(): LiveData<List<CartItem>>

    @Query("SELECT COUNT(id) FROM cart_item WHERE id = :cartItemId")
    fun getCartItem(cartItemId: Int): Int

    @Query("SELECT COUNT(id) FROM cart_item WHERE checked_out = 0")
    fun getCartItemsCount(): Int

    @Query("SELECT * FROM cart_item WHERE checked_out = 1")
    fun getCartCheckedOut(): LiveData<List<CartItem>>

    @Query("SELECT COUNT(id) FROM cart_item WHERE checked_out = 1")
    fun getCartCheckedOutCount(): Int

    @Query("UPDATE cart_item SET quantity = :quantity WHERE id = :cartItemId")
    fun changeQuantity(cartItemId: Int, quantity: Int)

    @Query("UPDATE cart_item SET checked_out = 1 WHERE checked_out = 0")
    fun checkOut()

    @Query("DELETE FROM cart_item WHERE id = :cartItemId")
    fun deleteCartItem(cartItemId: Int)

    @Delete
    fun deleteCartItem(cartItem: CartItem)

    @Delete
    fun clearCarts(cartItems: List<CartItem>)

    @Query("DELETE FROM cart_item")
    fun shred()
}