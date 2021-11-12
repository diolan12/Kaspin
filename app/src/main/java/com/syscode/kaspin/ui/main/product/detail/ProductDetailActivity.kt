package com.syscode.kaspin.ui.main.product.detail

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.HttpException
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.syscode.kaspin.R
import com.syscode.kaspin.data.database.model.Product
import com.syscode.kaspin.databinding.ActivityProductDetailBinding
import com.syscode.kaspin.internal.*
import com.syscode.kaspin.ui.viewmodel.ProductViewModel
import com.syscode.kaspin.ui.viewmodel.ProductViewModelFactory
import com.syscode.kaspin.ui.viewmodel.ValidationViewModel
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import kotlin.properties.Delegates

class ProductDetailActivity : AppCompatActivity(), DIAware {
    override val di by closestDI()

    private val productViewModelFactory: ProductViewModelFactory by instance()
    private lateinit var productViewModel: ProductViewModel
    private lateinit var validationViewModel: ValidationViewModel

    private var _binding: ActivityProductDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ArrayAdapter<String>

    private var productId by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityProductDetailBinding.inflate(layoutInflater)

        setContentView(binding.root)

        productId = intent.getIntExtra("product_id", 0)
        val image = intent.getStringExtra("product_image")

        productViewModel = lazyViewModel(this, productViewModelFactory)
        validationViewModel = lazyViewModel(this)

        adapter = ArrayAdapter(
            this@ProductDetailActivity,
            R.layout.item_dropdown_category,
            productViewModel.categories
        )

        if (productId != 0) {
            // edit mode
            title = getString(R.string.product_detail_edit)
            if (image != null) loadImage(image)
            bindData(productId)
            binding.apply {
                detailProductCodeLayout.endIconDrawable = null
                detailProductActionUpdate.setOnClickListener { updateProduct(
                    Product(
                        id = productId,
                        code = detailProductCode.text?.filter { it.isDigit() }.toString().toInt(),
                        title = detailProductTitle.text.toString(),
                        image = detailProductImage.text.toString(),
                        category = detailProductCategory.text.toString(),
                        stock = detailProductStock.text?.filter { it.isDigit() }.toString().toInt(),
                        price = detailProductPrice.text.toString().toRupiah()
                    )
                ) }
                detailProductActionDelete.setOnClickListener {
                    deleteProduct(productId)
                }
                detailProductActionRestore.setOnClickListener {
                    restoreProduct(productId)
                }
            }

        } else {
            // create mode
            title = getString(R.string.product_detail_new)
            binding.apply {
                detailProductActionCreate.visibility = View.VISIBLE
                detailProductCodeLayout.setEndIconOnClickListener {
                    detailProductCode.setText(randomInt().toString())
                }
                detailProductActionCreate.setOnClickListener { createProduct(
                    Product(
                        code = detailProductCode.text?.filter { it.isDigit() }.toString().toInt(),
                        title = detailProductTitle.text.toString(),
                        image = detailProductImage.text.toString(),
                        category = detailProductCategory.text.toString(),
                        stock = detailProductStock.text?.filter { it.isDigit() }.toString().toInt(),
                        price = detailProductPrice.text.toString().toRupiah()
                    )
                ) }
            }

        }
        bindValidator()
        binding.apply {
            detailProductButtonCheck.setOnClickListener { loadImage(detailProductImage.text.toString()) }

            (detailProductCategory).setAdapter(adapter)

            validationViewModel.registerValidables(
                detailProductImage,
                detailProductCode,
                detailProductTitle,
                detailProductImagePreview,
                detailProductCategory,
                detailProductStock,
                detailProductPrice
            )
            detailProductTitle.setOnTextChangedListener {
                validationViewModel.validate(detailProductTitle, it.isNotBlank())
            }
            detailProductCode.setOnTextChangedListener {
                validationViewModel.validate(detailProductCode, it.length == 8) { valid ->
                    detailProductCodeLayout.error = if (valid) ""
                    else "Kode barang kurang dari 8 karakter"
                }
            }
            detailProductImage.setOnTextChangedListener {
                validationViewModel.validate(detailProductImage, it.isNotBlank())
            }
            detailProductCategory.setOnTextChangedListener {
                validationViewModel.validate(detailProductCategory, it.isNotBlank())
            }
            detailProductStock.setOnTextChangedListener {
                validationViewModel.validate(detailProductStock, it.isNotBlank())
            }
            detailProductPrice.setOnTextChangedListener {
                validationViewModel.validate(detailProductPrice, it.isNotBlank())
            }
            if (productId == 0) {
                detailProductCode.setText(randomInt().toString())
                detailProductStock.setText("0")
            }
        }
    }

    private fun bindData(id: Int) = lifecycleScope.launch {
        productViewModel.getProduct(id).observe(this@ProductDetailActivity) {
            binding.apply {
                detailProductTitle.setText(it.title)
                detailProductCode.setText(it.code.toString())
                detailProductImage.setText(it.image)
                detailProductCategory.setText(it.category, false)
                detailProductStock.setText(it.stock.toString())
                detailProductPrice.setText(it.price)

                detailProductTitle.isEnabled = !it.deleted
                detailProductCode.isEnabled = !it.deleted
                detailProductImage.isEnabled = !it.deleted
                detailProductCategory.isEnabled = !it.deleted
                detailProductStock.isEnabled = !it.deleted
                detailProductPrice.isEnabled = !it.deleted
                detailProductButtonCheck.isEnabled = !it.deleted

                detailProductTitle.isClickable = !it.deleted
                detailProductCode.isClickable = !it.deleted
                detailProductImage.isClickable = !it.deleted
                detailProductCategory.isClickable = !it.deleted
                detailProductStock.isClickable = !it.deleted
                detailProductPrice.isClickable = !it.deleted
                detailProductButtonCheck.isClickable = !it.deleted
                if (!it.deleted) {
                    detailProductActionUpdate.visibility = View.VISIBLE
                    detailProductActionDelete.visibility = View.VISIBLE
                } else detailProductActionRestore.visibility = View.VISIBLE
            }
        }
    }

    private fun bindValidator() = lifecycleScope.launch {
        validationViewModel.isValid.observe(this@ProductDetailActivity) {
            binding.apply {
                detailProductActionCreate.isEnabled = it
                detailProductActionCreate.isClickable = it
                detailProductActionUpdate.isEnabled = it
                detailProductActionUpdate.isClickable = it
                detailProductActionDelete.isEnabled = it
                detailProductActionDelete.isClickable = it
            }
        }
    }

    private fun loadImage(url: String) {
        GlideApp.with(this@ProductDetailActivity)
            .load(url)
            .addListener(object: RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (e != null && e.cause is HttpException) Toast.makeText(this@ProductDetailActivity, "Mohon cek URL gambar", Toast.LENGTH_LONG).show()
                    validationViewModel.validate(binding.detailProductImagePreview, false)
                    binding.detailProductImageLayout.error = "Error loading URL"
                    return e != null
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    validationViewModel.validate(binding.detailProductImagePreview, true)
                    binding.detailProductImagePreview.setImageDrawable(resource)
                    binding.detailProductImageLayout.error = ""
                    return resource != null
                }

            })
            .into(binding.detailProductImagePreview)
    }

    private fun createProduct(product: Product) {
        productViewModel.create(product)
        finish()
    }
    private fun updateProduct(product: Product) {
        productViewModel.update(product)
        finish()
    }

    private fun deleteProduct(id: Int) {
        productViewModel.delete(id)
        finish()
    }

    private fun restoreProduct(id: Int) {
        productViewModel.restore(id)
        finish()
    }

}