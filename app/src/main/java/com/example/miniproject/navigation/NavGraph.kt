// navigation/NavGraph.kt
package com.example.miniproject.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.miniproject.admin.AdminScreen
import com.example.miniproject.loginSignup.*
import com.example.miniproject.ui.theme.HomeScreen

/**
 * Sets up the navigation graph for the entire application
 */
fun NavGraphBuilder.setupNavigation() {
    // Login Screen
    composable(Routes.Login.route) { navBackStackEntry ->
        LoginScreen(
            navController = navBackStackEntry.findNavController()
        )
    }

    // Student Login
    composable(Routes.StudentLogin.route) { navBackStackEntry ->
        StudentLoginScreen(
            navController = navBackStackEntry.findNavController()
        )
    }

    // Staff Login
    composable(Routes.StaffLogin.route) { navBackStackEntry ->
        StaffLoginScreen(
            navController = navBackStackEntry.findNavController()
        )
    }

    // Admin Login
    composable(Routes.AdminLogin.route) { navBackStackEntry ->
        AdminLoginScreen(
            navController = navBackStackEntry.findNavController()
        )
    }

    // Sign Up Screen with user type parameter
    composable(
        route = Routes.SignUp.route,
        arguments = listOf(
            navArgument(RouteArguments.USER_TYPE) {
                type = NavType.StringType
                defaultValue = "Student"
            }
        )
    ) { navBackStackEntry ->
        val userType = navBackStackEntry.arguments?.getString(RouteArguments.USER_TYPE) ?: "Student"
        SignUpScreen(
            navController = navBackStackEntry.findNavController(),
            userType = userType
        )
    }

    // Home Screen
    composable(Routes.Home.route) { navBackStackEntry ->
        HomeScreen(
            navController = navBackStackEntry.findNavController()
        )
    }

    // Admin Screen
    composable(Routes.Admin.route) { navBackStackEntry ->
        AdminScreen(
            navController = navBackStackEntry.findNavController()
        )
    }
}

/**
 * Extension function to find NavController from LocalContext
 */
fun NavGraphEntry.findNavController(): NavController {
    return androidx.navigation.compose.currentBackStackEntryAsState().value?.destination?.let {
        androidx.navigation.compose.NavHostController(it)
    } ?: throw IllegalStateException("NavController not found")
}

// Simplified alias for cleaner imports
typealias NavGraphEntry = androidx.navigation.NavBackStackEntry