package com.urmyfood.user.data.local

import android.content.Context
import com.urmyfood.user.domain.model.TokenProvider

class TokenManager(context: Context) : 
    com.urmyfood.shared.data.local.TokenManager(context, "urmyfood_prefs"), 
    TokenProvider
