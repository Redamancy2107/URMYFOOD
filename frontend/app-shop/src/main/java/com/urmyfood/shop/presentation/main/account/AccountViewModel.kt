package com.urmyfood.shop.presentation.main.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.urmyfood.shop.di.ServiceLocator

class AccountViewModel : ViewModel() {

    // Mock data - remove when BE ready
    private val _shopName = MutableLiveData("Quán Ăn Ngon")
    val shopName: LiveData<String> = _shopName

    // Mock data - remove when BE ready
    private val _shopRating = MutableLiveData(4.7f)
    val shopRating: LiveData<Float> = _shopRating

    // Mock data - remove when BE ready
    private val _ratingCount = MutableLiveData("1.3k")
    val ratingCount: LiveData<String> = _ratingCount

    fun logout() {
        ServiceLocator.tokenManager.clear()
    }
}
