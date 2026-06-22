package com.urmyfood.admin

import android.app.Application
import com.urmyfood.admin.data.local.SessionManager

class AdminApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
    }
}
