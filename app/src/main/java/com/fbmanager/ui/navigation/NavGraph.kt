package com.fbmanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fbmanager.ui.screens.devices.DeviceListScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "devices") {
        composable("devices") {
            DeviceListScreen()
        }
    }
}
