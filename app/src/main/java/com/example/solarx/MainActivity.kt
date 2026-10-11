package com.example.solarx

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.solarx.pages.HomePage
import com.example.solarx.pages.LoginPage
import com.example.solarx.ui.theme.SolarXTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

enum class AppScreen {
    Login,
    Dashboard,
    Details,
    Settings
}

@Composable
fun NavApp(navBar: NavHostController) {
    NavHost(navController = navBar, startDestination = "LoginPage") {
        composable(route="LoginPage") {
            LoginPage(navBar)
        }
        composable(route="Dashboard")
        {
            HomePage()
        }
    }
}

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SolarXTheme {
                Scaffold(modifier = Modifier.fillMaxSize(), contentColor = MaterialTheme.colorScheme.background) {
                    var navBar = rememberNavController()
                    NavApp(navBar = navBar)
                }
            }
        }
    }
}