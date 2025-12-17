// Define all navigation routes
package com.example.miniproject.navigation

/**
 * Sealed class defining all possible destinations in the app
 */
sealed class Routes(val route: String) {
    object Login : Routes("login")
    object StudentLogin : Routes("studentLogin")
    object StaffLogin : Routes("staffLogin")
    object AdminLogin : Routes("adminLogin")
    object SignUp : Routes("signup/{userType}") {
        fun createRoute(userType: String = "Student") = "signup/$userType"
    }
    object Home : Routes("home")
    object Admin : Routes("admin")

    // Add more routes here as your app grows
    // object Profile : Routes("profile")
    // object Bookings : Routes("bookings")
    // object FacilityDetails : Routes("facility/{id}")
}

/**
 * Arguments for routes that require parameters
 */
object RouteArguments {
    const val USER_TYPE = "userType"
    const val FACILITY_ID = "id"
}


