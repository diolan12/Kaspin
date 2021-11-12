package com.syscode.kaspin.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.syscode.kaspin.data.database.model.Product

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(data: List<Product>)

    @Query("SELECT * FROM product ORDER BY deleted ASC, title")
    fun getAllWithTrash(): LiveData<List<Product>>

    @Query("SELECT * FROM product WHERE deleted = 0")
    fun getAllClean(): LiveData<List<Product>>

    @Query("SELECT * FROM product WHERE deleted = 1")
    fun getAllTrash(): LiveData<List<Product>>

    @Query("SELECT * FROM product WHERE id = :idKey")
    fun getById(idKey: Int): LiveData<Product>

    @Query("SELECT * FROM product WHERE id = :idKey")
    fun getByIdP(idKey: Int): Product

    @Query("SELECT COUNT(id) FROM product")
    fun rowCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun create(data: Product)

    @Update(entity = Product::class, onConflict = OnConflictStrategy.REPLACE)
    fun update(data: Product)

    @Query("UPDATE product SET stock = :stock WHERE id = :idKey")
    fun updateStock(idKey: Int, stock: Int)

    @Query("UPDATE product SET deleted = 1 WHERE id = :idKey")
    fun delete(idKey: Int)

    @Query("UPDATE product SET deleted = 0 WHERE id = :idKey")
    fun restore(idKey: Int)

    @Query("DELETE FROM product")
    fun clear()
}