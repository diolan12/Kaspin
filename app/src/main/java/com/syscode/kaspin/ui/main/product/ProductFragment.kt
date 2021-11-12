package com.syscode.kaspin.ui.main.product

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.syscode.kaspin.data.database.model.Product
import com.syscode.kaspin.data.network.service.FakeStoreService
import com.syscode.kaspin.databinding.FragmentProductBinding
import com.syscode.kaspin.internal.by
import com.syscode.kaspin.internal.lazyViewModel
import com.syscode.kaspin.ui.main.product.detail.ProductDetailActivity
import com.syscode.kaspin.ui.viewmodel.ProductViewModel
import com.syscode.kaspin.ui.viewmodel.ProductViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

class ProductFragment : Fragment(), DIAware, ProductAdapter.OnProductClickListener {
    override val di by closestDI()

    private val productViewModelFactory: ProductViewModelFactory by instance()
    private lateinit var productViewModel: ProductViewModel

    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!

    private val products = arrayListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        productViewModel = lazyViewModel(this, productViewModelFactory)

        _binding = FragmentProductBinding.inflate(inflater, container, false)

        initRecyclerView()

        productViewModel.products.observe(viewLifecycleOwner) {
            products.clear()
            products.addAll(it)
            binding.rvProducts.adapter?.notifyDataSetChanged()
        }
        binding.apply {
            rvProducts.setHasFixedSize(true)
            fabProductNew.setOnClickListener {
                Intent(requireContext(), ProductDetailActivity::class.java).apply {
                    startActivity(this)
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observe()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initRecyclerView() {
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ProductAdapter(products, viewLifecycleOwner.lifecycleScope, this@ProductFragment)
        }
    }

    private fun observe() = viewLifecycleOwner.lifecycleScope.launch {
        productViewModel.fakeStoreState.collect {
            when (it) {
                is FakeStoreService.State.Running -> binding.progressProducts.visibility = View.VISIBLE
                else -> binding.progressProducts.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun setOnClickListener(product: Product, pairs: List<Pair<View, String>>) {
        val option = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), pairs[0])
        Intent(requireContext(), ProductDetailActivity::class.java).apply {
            putExtra("product_id", product.id)
            putExtra("product_image", product.image)
            startActivity(this, option.toBundle())
        }
    }

    override fun setOnStockChangeListener(position: Int, id: Int, stock: Int) {
        updateProductStock(id, stock)
        binding.rvProducts.adapter?.notifyItemChanged(position)
    }

    private fun updateProductStock(id: Int, stock: Int) {
        Log.d("stock", "changed to $stock")
        productViewModel.updateStock(id, stock)
    }
}