package io.github.annusshahzad.solarx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.annusshahzad.solarx.ui.login.LoginRoute
import io.github.annusshahzad.solarx.ui.monitor.HomeRoute
import io.github.annusshahzad.solarx.ui.theme.SolarXTheme

private object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SolarXTheme {
                SolarXNavHost()
            }
        }
    }
}

@Composable
private fun SolarXNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginRoute(onLoggedIn = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                    launchSingleTop = true
                }
            })
        }
        composable(Routes.HOME) {
            // The monitor ViewModel is scoped to this destination, so a new login starts a fresh poller.
            HomeRoute(onChangeInverter = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.HOME) { inclusive = true }
                    launchSingleTop = true
                }
            })
        }
    }
}
