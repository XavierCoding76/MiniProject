// Navigation helper functions

package com.example.miniproject.navigation

import androidx.navigation.NavController

/**
 * Extension functions for navigation to make it type-safe and easier to use
 */
fun NavController.navigateToLogin() {
    navigate(Routes.Login.route) {
        // Clear back stack when navigating to login
        popUpTo(Routes.Login.route) {
            inclusive = true
        }
    }
}

fun NavController.navigateToStudentLogin() {
    navigate(Routes.StudentLogin.route)
}

fun NavController.navigateToStaffLogin() {
    navigate(Routes.StaffLogin.route)
}

fun NavController.navigateToAdminLogin() {
    navigate(Routes.AdminLogin.route)
}

fun NavController.navigateToSignUp(userType: String = "Student") {
    navigate(Routes.SignUp.createRoute(userType))
}

fun NavController.navigateToHome() {
    navigate(Routes.Home.route) {
        // Clear back stack when navigating to home
        popUpTo(Routes.Login.route) {
            inclusive = true
        }
    }
}

fun NavController.navigateToAdmin() {
    navigate(Routes.Admin.route)
}

/**
 * Navigate back to previous screen
 */
fun NavController.navigateBack() {
    popBackStack()
}

/**
 * Navigate back to a specific destination
 */
fun NavController.navigateBackTo(route: String) {
    popBackStack(route, inclusive = false)
}

/**
 * Clear navigation stack and go to destination
 */
fun NavController.navigateAndClearStack(route: String) {
    navigate(route) {
        popUpTo(0)
    }
}