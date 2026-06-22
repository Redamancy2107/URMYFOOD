package com.urmyfood.shop.presentation.common

import android.os.Bundle
import androidx.navigation.NavController

fun NavController.safeNavigate(actionId: Int, args: Bundle? = null) {
    currentDestination?.getAction(actionId)?.let { navigate(actionId, args) }
}
