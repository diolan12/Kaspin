package com.syscode.kaspin.internal

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import java.math.BigDecimal


fun randomInt(): Int {
    return (10000000..99999999).random()
}

fun String.toRupiah(): String {
    val a = this.filter { it.isDigit() }
    val b = BigDecimal(a)
    val c = b.toString().reversed().chunked(3)
    val d = c.map { it.reversed() }
    return d.reversed().joinToString(".")
}

fun EditText.setOnTextChangedListener(function: (s: String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            function.invoke(s.toString())
        }

    })
}

infix fun ViewModel.by(lazyViewModel: Unit) = lazyViewModel
// lazy view-model
inline fun <reified ViewModelT : ViewModel> lazyViewModel(
    owner: ViewModelStoreOwner,
    factory: ViewModelProvider.NewInstanceFactory? = null
): ViewModelT {
    return if (factory == null) {
        ViewModelProvider(owner)[ViewModelT::class.java]
    } else {
        ViewModelProvider(owner, factory)[ViewModelT::class.java]
    }
}