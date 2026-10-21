package io.github.annusshahzad.solarx

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.annusshahzad.solarx.ui.login.LoginRoute
import io.github.annusshahzad.solarx.ui.monitor.HomeRoute
import io.github.annusshahzad.solarx.ui.theme.SolarXTheme

/** Matches windowSplashScreenAnimationDuration in themes.xml. */
private const val SPLASH_ANIMATION_MS = 900L
private const val SPLASH_EXIT_MS = 300L

private object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            // Let the logo animation finish on a cold start, then zoom out of it into the app.
            val shownAt = SystemClock.uptimeMillis()
            splash.setKeepOnScreenCondition { SystemClock.uptimeMillis() - shownAt < SPLASH_ANIMATION_MS }
            splash.setOnExitAnimationListener { provider ->
                provider.iconView.animate().scaleX(1.6f).scaleY(1.6f).setDuration(SPLASH_EXIT_MS).start()
                provider.view.animate()
                    .alpha(0f)
                    .setDuration(SPLASH_EXIT_MS)
                    .withEndAction { provider.remove() }
                    .start()
            }
        }
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
