package com.syscode.kaspin.ui.main.product

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.util.Pair
import androidx.recyclerview.widget.RecyclerView
import com.syscode.kaspin.R
import com.syscode.kaspin.data.database.model.Product
import com.syscode.kaspin.internal.GlideApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ProductAdapter(
    private val products: ArrayList<Product>,
    private val scope: CoroutineScope,
    private val onProductClickListener: OnProductClickListener? = null
): RecyclerView.Adapter<ProductAdapter.ProductItemHolder>() {
    inner class ProductItemHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.item_product_image)
        val title: TextView = itemView.findViewById(R.id.item_product_title)
        val category: TextView = itemView.findViewById(R.id.item_product_category)
        val uniqueId: TextView = itemView.findViewById(R.id.item_product_unique_id)
        val stock: TextView = itemView.findViewById(R.id.item_product_stock)
        val deleted: TextView = itemView.findViewById(R.id.item_product_deleted)
        val increase: Button = itemView.findViewById(R.id.item_product_stock_increase)
        val decrease: Button = itemView.findViewById(R.id.item_product_stock_decrease)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductItemHolder {
        return ProductItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false))
    }

    override fun onBindViewHolder(holder: ProductItemHolder, position: Int) {
        val product = products[position]

        holder.apply {
            title.text = product.title
            category.text = product.category
            uniqueId.text = product.code.toString()
            stock.text = itemView.context.getString(R.string.placeholder_stock, product.stock)

            if (product.deleted) deleted.visibility = View.VISIBLE
            else deleted.visibility = View.GONE

            increase.isClickable = !product.deleted
            increase.isEnabled = !product.deleted
            decrease.isClickable = !product.deleted
            decrease.isEnabled = !product.deleted

            imageLoad(itemView.context, product.image, image)

            itemView.setOnClickListener {
                onProductClickListener?.setOnClickListener(
                    product = product,
                    pairs = listOf(
                        Pair.create(image, image.transitionName)
                    )
                )
            }
            increase.setOnClickListener {
                onProductClickListener?.setOnStockChangeListener(
                    position = position,
                    id = product.id,
                    stock = product.stock+1
                )
            }
            decrease.setOnClickListener {
                onProductClickListener?.setOnStockChangeListener(
                    position = position,
                    id = product.id,
                    stock = product.stock-1
                )
            }
        }
    }

    private fun imageLoad(context: Context, url: String, imageView: ImageView) = scope.launch {
        GlideApp.with(context)
            .load(url)
            .into(imageView)
    }

    override fun getItemCount() = products.size

    interface OnProductClickListener {
        fun setOnClickListener(product: Product, pairs: List<Pair<View, String>>)
        fun setOnStockChangeListener(position: Int, id: Int, stock: Int)
    }
}