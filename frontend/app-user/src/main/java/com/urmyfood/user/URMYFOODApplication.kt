package com.urmyfood.user

import android.app.Application
import com.urmyfood.user.di.ServiceLocator

/**
 * Base Application class for the app.
 * Initializes the ServiceLocator on startup.
 */
class URMYFOODApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
