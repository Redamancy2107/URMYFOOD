package com.urmyfood.shop.di

import com.urmyfood.shop.presentation.auth.registration.ShopRegistrationFlowViewModel

object ServiceLocator {
    fun provideShopRegistrationViewModelFactory() = ShopRegistrationFlowViewModel.Factory()
}
