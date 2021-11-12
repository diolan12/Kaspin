package com.syscode.kaspin.ui.main.transaction

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.syscode.kaspin.R
import com.syscode.kaspin.data.database.model.Catalog
import com.syscode.kaspin.internal.toRupiah
import java.math.BigDecimal

class CartAdapter(
    private val carts: ArrayList<Catalog>,
    private val onCartEvent: OnCartEvent
): RecyclerView.Adapter<CartAdapter.CartItemHolder>() {
    inner class CartItemHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val detail: TextView = itemView.findViewById(R.id.item_cart_detail)
        val quantityOperation: TextView = itemView.findViewById(R.id.item_cart_quantity_operation)
        val sum: TextView = itemView.findViewById(R.id.item_cart_sum)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemHolder {
        return CartItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false))
    }

    override fun onBindViewHolder(holder: CartItemHolder, position: Int) {
        val cart = carts[position]

        holder.apply {
            if (cart.cart != null) {
                detail.text = cart.title
                quantityOperation.text = "Rp ${cart.price} x ${cart.cart!!.quantity}"
                Log.d("cart.price", cart.price)
                val summary = BigDecimal(cart.price.filter { it.isDigit() }).times(BigDecimal(cart.cart!!.quantity)).toPlainString()
                sum.text = "Rp ${summary.toRupiah()}"
                onCartEvent.setOnCartSummaryReturn(summary)
            }
        }
    }

    override fun getItemCount() = carts.size

    interface OnCartEvent {
        fun setOnCartSummaryReturn(summary: String)
    }
}