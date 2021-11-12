package com.syscode.kaspin.ui.main.transaction

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.syscode.kaspin.R
import com.syscode.kaspin.data.database.model.Catalog
import com.syscode.kaspin.internal.GlideApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class TransactionAdapter(
    private val catalogs: ArrayList<Catalog>,
    private val scope: CoroutineScope,
    private val onTransactionListener: OnTransactionListener
) : RecyclerView.Adapter<TransactionAdapter.CatalogItemHolder>() {
    inner class CatalogItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.item_catalog_image)
        val title: TextView = itemView.findViewById(R.id.item_catalog_title)
        val category: TextView = itemView.findViewById(R.id.item_catalog_category)
        val cartAction: LinearLayout = itemView.findViewById(R.id.item_cart_action)
        val cartIncrease: Button = itemView.findViewById(R.id.item_cart_total_increase)
        val cartTotal: TextView = itemView.findViewById(R.id.item_cart_total)
        val cartDecrease: Button = itemView.findViewById(R.id.item_cart_total_decrease)
        val cartAdd: ImageButton = itemView.findViewById(R.id.item_cart_add)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogItemHolder {
        return CatalogItemHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_catalog, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CatalogItemHolder, position: Int) {
        val catalogItem = catalogs[position]

        holder.apply {
            title.text = catalogItem.title
            category.text = catalogItem.category
            imageLoad(itemView.context, catalogItem.image, image)
            Log.i("adapter", "${catalogItem.cart}")

            if (catalogItem.cart != null) {
                cartAdd.visibility = View.GONE
                cartAction.visibility = View.VISIBLE
                cartTotal.text = itemView.context.getString(
                    R.string.placeholder_quantity,
                    catalogItem.cart!!.quantity
                )
                cartDecrease.setOnClickListener {
                    catalogItem.cart!!.let { cart ->
                        onTransactionListener.setOnCartChangeListener(
                            position,
                            cartId = cart.id,
                            productId = cart.product,
                            total = cart.quantity - 1
                        )
                    }
                }
                cartIncrease.setOnClickListener {
                    catalogItem.cart!!.let { cart ->
                        onTransactionListener.setOnCartChangeListener(
                            position,
                            cartId = cart.id,
                            productId = cart.product,
                            total = cart.quantity + 1
                        )

                    }
                }
                (catalogItem.cart!!.quantity < catalogItem.stock).apply {
                    Log.i("stock == quantity", "quantity ${catalogItem.cart!!.quantity} less than stock ${catalogItem.stock} $this")
                    cartIncrease.isEnabled = this
                    cartIncrease.isClickable = this
                }
            } else {
                cartAdd.visibility = View.VISIBLE
                cartAction.visibility = View.GONE
                cartAdd.setOnClickListener {
                    Log.d("cartAdd", "${catalogItem.cart}")
                    onTransactionListener.setOnCartChangeListener(
                        position,
                        productId = catalogItem.productId,
                        total = 1
                    )
                }
            }
        }
    }

    private fun imageLoad(context: Context, url: String, imageView: ImageView) = scope.launch {
        GlideApp.with(context)
            .load(url)
            .into(imageView)
    }

    override fun getItemCount() = catalogs.size

    interface OnTransactionListener {
        fun setOnCartChangeListener(position: Int, cartId: Int = 0, productId: Int, total: Int)
    }
}