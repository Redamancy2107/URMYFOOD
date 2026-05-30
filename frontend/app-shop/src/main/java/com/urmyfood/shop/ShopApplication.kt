package com.urmyfood.shop

import android.app.Application
import com.urmyfood.shop.di.ServiceLocator

class ShopApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
