package com.syscode.kaspin.ui.viewmodel

import android.view.View
import android.widget.EditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ValidationViewModel : ViewModel() {
    private val _isValid = MutableLiveData<Boolean>()
    val isValid: LiveData<Boolean>
        get() = _isValid
    private val validables = mutableMapOf<Int, Boolean>()

    fun registerValidables(vararg views: View) {
        views.map {
            validables[it.id] = false
        }
        _isValid.postValue(!validables.containsValue(false) && validables.isNotEmpty())
    }

    fun validate(view: View, condition: Boolean) {
        validables[view.id] = condition
        _isValid.postValue(!validables.containsValue(false) && validables.isNotEmpty())
    }

    fun validate(view: View, condition: Boolean, function: (validity: Boolean) -> Unit) {
        validables[view.id] = condition
        _isValid.postValue(!validables.containsValue(false) && validables.isNotEmpty())
        function.invoke(condition)
    }
    fun validate(view: EditText, condition: Boolean, function: (text: String, validity: Boolean) -> Unit) {
        validables[view.id] = condition
        _isValid.postValue(!validables.containsValue(false) && validables.isNotEmpty())
        function.invoke(view.text.toString(), condition)
    }

    init {
        validables.clear()
    }

    override fun onCleared() {
        validables.clear()
        super.onCleared()
    }
}