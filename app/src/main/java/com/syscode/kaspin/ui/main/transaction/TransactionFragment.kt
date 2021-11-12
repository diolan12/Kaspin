package com.syscode.kaspin.ui.main.transaction

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.syscode.kaspin.R
import com.syscode.kaspin.data.database.model.Catalog
import com.syscode.kaspin.data.network.service.FakeStoreService
import com.syscode.kaspin.databinding.FragmentTransactionBinding
import com.syscode.kaspin.internal.GlideApp
import com.syscode.kaspin.internal.lazyViewModel
import com.syscode.kaspin.internal.toRupiah
import com.syscode.kaspin.ui.viewmodel.ProductViewModel
import com.syscode.kaspin.ui.viewmodel.ProductViewModelFactory
import com.syscode.kaspin.ui.viewmodel.TransactionViewModel
import com.syscode.kaspin.ui.viewmodel.TransactionViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import java.math.BigDecimal

class TransactionFragment : Fragment(), DIAware, TransactionAdapter.OnTransactionListener,
    CartAdapter.OnCartEvent {
    override val di by closestDI()

    private val productViewModelFactory: ProductViewModelFactory by instance()
    private lateinit var productViewModel: ProductViewModel
    private val transactionViewModelFactory: TransactionViewModelFactory by instance()
    private lateinit var transactionViewModel: TransactionViewModel

    private var _binding: FragmentTransactionBinding? = null
    private val binding get() = _binding!!

    private lateinit var checkoutBottomSheet: BottomSheetBehavior<ConstraintLayout>

    private val catalogs = arrayListOf<Catalog>()

    private val cartsCatalog = arrayListOf<Catalog>()
    private var cartsTotalPrice = BigDecimal(0)

    private var cartNotEmpty: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        productViewModel = lazyViewModel(this, productViewModelFactory)
        transactionViewModel = lazyViewModel(this, transactionViewModelFactory)

        _binding = FragmentTransactionBinding.inflate(inflater, container, false)

        initRecyclerView()

        checkoutBottomSheet = BottomSheetBehavior.from(binding.bottomSheet.bottomSheetCartContainer)
        checkoutBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
        binding.bottomSheet.bottomSheetCartContainer.setOnClickListener{
            checkoutBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        }

        transactionViewModel.catalogs.observe(viewLifecycleOwner) { cats ->
            val tcc = cats.filter { it.cart != null }
            catalogs.clear()
            catalogs.addAll(cats)
            cartsCatalog.clear()
            cartsCatalog.addAll(tcc)
            cartsTotalPrice = BigDecimal(0)
            binding.rvCatalog.adapter?.notifyDataSetChanged()
            binding.bottomSheet.rvCartContentList.adapter?.notifyDataSetChanged()
        }
        transactionViewModel.carts.observe(viewLifecycleOwner) {
            Log.d("cartCountUI", "size: $it")
            cartNotEmpty = it.isNotEmpty()
            bottomSheetBehaviour()
            if (it.isNotEmpty()) {
                checkoutBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
                binding.bottomSheet.cartTitle.text =
                    getString(R.string.checkout_cart_title, it.size)
            } else {
                checkoutBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
                binding.bottomSheet.cartTitle.text = ""
            }
        }
        binding.apply {
            rvCatalog.setHasFixedSize(true)
            rvCatalog.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                        bottomSheetBehaviour()
                    }
                    super.onScrollStateChanged(recyclerView, newState)
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    downwards = (dy > 0)
                    super.onScrolled(recyclerView, dx, dy)
                }
            })
            bottomSheet.rvCartContentList.setHasFixedSize(true)
            checkoutBottomSheet.addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    Log.d("onStateChanged", "state $newState $cartNotEmpty")
                    bottomSheetState = newState
                    bottomSheetBehaviour()
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                    Log.d("onSlide", "slideOffset $slideOffset")
                }
            })
            bottomSheet.cartButtonClear.setOnClickListener {
                transactionViewModel.clearCarts()
            }
            bottomSheet.cartButtonCheckout.setOnClickListener {
                //modal dialog konfirmasi
                AlertDialog.Builder(requireContext()).apply {
                    setTitle("Checkout?")
                    setMessage("Are you sure to checkout all items in cart?")
                    setPositiveButton("Checkout") { _, _ ->
                        checkout()
                    }
                    setNegativeButton("Recheck") { _, _ ->

                    }
                }.create().show()
            }
        }
        return binding.root
    }

    private var downwards: Boolean = false
    private var bottomSheetState: Int = BottomSheetBehavior.STATE_COLLAPSED
    private fun bottomSheetBehaviour() {
        // hide when downwards
        if (downwards) {
            checkoutBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            if (cartNotEmpty) {
                if (bottomSheetState == BottomSheetBehavior.STATE_HIDDEN && cartNotEmpty) {
                    checkoutBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }else {
                checkoutBottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
    }

    private fun checkout() {
        transactionViewModel.checkOut()
        checkoutSuccess()
    }

    private fun checkoutSuccess() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_checkout_success, binding.root, false)
        val image: ImageView = dialogView.rootView.findViewById(R.id.checkout_dialog_image)
        GlideApp.with(dialogView)
            .load(R.drawable.checkout_success)
            .into(image)
        val dismiss: MaterialButton = dialogView.rootView.findViewById(R.id.checkout_dialog_dismiss)
        val home: MaterialButton = dialogView.rootView.findViewById(R.id.checkout_dialog_home)

        val checkoutDialog = AlertDialog.Builder(requireContext()).apply {
            setView(dialogView.rootView)
            setCancelable(true)
        }.create()
        dismiss.setOnClickListener {
            checkoutDialog.dismiss()
        }
        home.setOnClickListener {
            checkoutDialog.dismiss()
            this.findNavController().navigate(R.id.nav_home)
        }
        checkoutDialog.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observe()
        super.onViewCreated(view, savedInstanceState)
    }

    private lateinit var catalogLinearLayoutManager: LinearLayoutManager
    private fun initRecyclerView() {
        catalogLinearLayoutManager = LinearLayoutManager(requireContext())
        binding.apply {
            rvCatalog.layoutManager = catalogLinearLayoutManager
            rvCatalog.adapter = TransactionAdapter(
                catalogs,
                viewLifecycleOwner.lifecycleScope,
                this@TransactionFragment
            )
            bottomSheet.rvCartContentList.layoutManager = LinearLayoutManager(requireContext())
            bottomSheet.rvCartContentList.adapter =
                CartAdapter(cartsCatalog, this@TransactionFragment)
        }
    }

    private fun observe() = viewLifecycleOwner.lifecycleScope.launch {
        productViewModel.fakeStoreState.collect {
            when (it) {
                is FakeStoreService.State.Running -> binding.progressCatalog.visibility =
                    View.VISIBLE
                else -> binding.progressCatalog.visibility = View.GONE

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun setOnCartChangeListener(position: Int, cartId: Int, productId: Int, total: Int) {
        updateCart(cartId, productId, total)
    }

    private fun updateCart(cartId: Int, productId: Int, total: Int) {
        transactionViewModel.changeQuantity(
            cartId = cartId,
            productId = productId,
            quantity = total
        )
    }

    override fun setOnCartSummaryReturn(summary: String) {
        cartsTotalPrice = cartsTotalPrice.add(BigDecimal(summary.filter { it.isDigit() }))
        binding.bottomSheet.cartContentTotal.text =
            "Rp ${cartsTotalPrice.toPlainString().toRupiah()}"
    }


}