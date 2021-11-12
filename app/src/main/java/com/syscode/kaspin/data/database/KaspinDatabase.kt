package com.syscode.kaspin.data.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.syscode.kaspin.data.database.dao.CartItemDao
import com.syscode.kaspin.data.database.dao.ProductDao
import com.syscode.kaspin.data.database.model.CartItem
import com.syscode.kaspin.data.database.model.Product

@Database(
    entities = [
        Product::class,
        CartItem::class
    ],
    version = 1
)
abstract class KaspinDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartItemDao(): CartItemDao

    companion object {
        @Volatile
        private var databaseInstance: KaspinDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = databaseInstance ?: synchronized(LOCK) {
            databaseInstance ?: buildDatabase(context).also { databaseInstance = it }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                KaspinDatabase::class.java,
                "relaxza-v1"
            ).build()
    }
}
