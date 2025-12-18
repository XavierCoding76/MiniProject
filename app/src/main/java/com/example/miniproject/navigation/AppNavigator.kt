// Central Navigation Controller

package com.example.miniproject.navigation

import androidx.navigation.NavController

/**
 * Central navigation controller that can be injected where needed
 */
class AppNavigator(private val navController: NavController) {

    fun navigateToLogin() = navController.navigateToLogin()
    fun navigateToStudentLogin() = navController.navigateToStudentLogin()
    fun navigateToStaffLogin() = navController.navigateToStaffLogin()
    fun navigateToAdminLogin() = navController.navigateToAdminLogin()
    fun navigateToSignUp(userType: String = "Student") = navController.navigateToSignUp(userType)
    fun navigateToHome() = navController.navigateToHome()
    fun navigateToAdmin() = navController.navigateToAdmin()
    fun navigateBack() = navController.navigateBack()
    fun navigateBackTo(route: String) = navController.navigateBackTo(route)
}