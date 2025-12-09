package com.example.miniproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.miniproject.admin.AdminScreen
import com.example.miniproject.booking.Navigation.AppNavGraph
import com.example.miniproject.ui.theme.MiniProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            /*Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                // Pass the padding to the AppNavigation function
                AppNavigation(
                    modifier = Modifier.padding(innerPadding),
                )
            }*/

            val navController = rememberNavController()
            AppNavGraph(navController)
        }
    }


    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        MiniProjectTheme {
            Greeting("Android")
        }
    }

    @Composable
    fun AppNavigation(modifier: Modifier) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = "admin"
        ) {
            composable("admin") {
                AdminScreen(navController = navController)
            }
        }
    }
}