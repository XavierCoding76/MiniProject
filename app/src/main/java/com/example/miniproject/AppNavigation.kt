package com.example.miniproject

import BookingFailedScreen
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.booking.BookingSuccessScreen
import com.example.miniproject.admin.AdminScreen
import com.example.miniproject.admin.bookingAdmin.AddEditReservationScreen
import com.example.miniproject.admin.bookingAdmin.AdminBookingScreen
import com.example.miniproject.admin.bookingAdmin.SearchBookingByDateScreen
import com.example.miniproject.admin.bookingAdmin.SearchBookingByFacilityScreen
import com.example.miniproject.admin.bookingAdmin.SearchBookingByReservationIdScreen
import com.example.miniproject.admin.bookingAdmin.SearchBookingByUserScreen
import com.example.miniproject.admin.facilityAdmin.AddFacilityScreen
import com.example.miniproject.admin.facilityAdmin.AdminFacilityScreen
import com.example.miniproject.admin.facilityAdmin.ArenaTarumtScreen
import com.example.miniproject.admin.facilityAdmin.ArenaTarumtSportScreen
import com.example.miniproject.admin.facilityAdmin.CitcScreen
import com.example.miniproject.admin.facilityAdmin.ClubhouseScreen
import com.example.miniproject.admin.facilityAdmin.FacilityDetailScreen
import com.example.miniproject.admin.facilityAdmin.LibraryScreen
import com.example.miniproject.admin.facilityAdmin.SportComplexScreen
import com.example.miniproject.admin.facilityAdmin.SubVenuesScreen
import com.example.miniproject.admin.userAdmin.AdminStaffScreen
import com.example.miniproject.admin.userAdmin.AdminStudentScreen
import com.example.miniproject.admin.userAdmin.AdminUserScreen
import com.example.miniproject.admin.userAdmin.UserInformationScreen
import com.example.miniproject.payment.FreeBookingScreen
import com.example.miniproject.payment.HomeScreen
import com.example.miniproject.payment.PayPalWebViewScreen
import com.example.miniproject.payment.PaymentScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatTime(timestamp: Long): String {
    return if (timestamp > 0) {
        val date = Date(timestamp)
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
    } else {
        "Invalid/Zero"
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "admin",
        modifier = modifier
    ) {
        // ==================== HOME ====================
        composable("home") {
            HomeScreen(navController = navController)
        }

        // ==================== PAYMENT ROUTES ====================

        // Main payment route - decides between free booking or paid booking
        composable(
            route = "payment/{userId}/{facilityIndId}/{equipmentData}/{startTime}/{bookedHours}",  // CHANGED
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("facilityIndId") { type = NavType.StringType },
                navArgument("equipmentData") { type = NavType.StringType },
                navArgument("startTime") { type = NavType.LongType },
                navArgument("bookedHours") { type = NavType.FloatType }  // CHANGED
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val facilityIndId = backStackEntry.arguments?.getString("facilityIndId") ?: ""
            val equipmentDataRaw = backStackEntry.arguments?.getString("equipmentData") ?: ""
            val startTime = backStackEntry.arguments?.getLong("startTime") ?: 0L
            val bookedHours = backStackEntry.arguments?.getFloat("bookedHours")?.toDouble() ?: 1.0

            // Convert equipment format
            val equipmentData = equipmentDataRaw
                .replace("_", ",")
                .replace("-", ":")

            // Debug logging
            println("📋 PAYMENT ROUTE:")
            println("   userId: '$userId'")
            println("   facilityIndId: '$facilityIndId'")
            println("   equipmentData: '$equipmentData'")
            println("   startTime: $startTime (${formatTime(startTime)})")
            println("   bookedHours: $bookedHours hours")  // CHANGED

            // Determine if this is a free booking
            val isFreeBooking = equipmentData == "NONE" || equipmentData.isEmpty()

            if (isFreeBooking) {
                println("✅ Showing FreeBookingScreen")
                FreeBookingScreen(
                    userId = userId,
                    facilityIndId = facilityIndId,
                    equipmentData = equipmentData,
                    startTime = startTime,
                    bookedHours = bookedHours,  // CHANGED
                    navController = navController
                )
            } else {
                println("💳 Showing PaymentScreen with PayPal")
                PaymentScreen(
                    userId = userId,
                    facilityIndId = facilityIndId,
                    equipmentData = equipmentData,
                    startTime = startTime,
                    bookedHours = bookedHours,  // CHANGED
                    navController = navController
                )
            }
        }

        // PayPal WebView route (for paid bookings only)
        composable(
            route = "payment_paypal/{orderId}/{approvalUrl}/{userId}/{facilityIndId}/{equipmentData}/{startTime}/{bookedHours}",  // CHANGED
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType },
                navArgument("approvalUrl") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType },
                navArgument("facilityIndId") { type = NavType.StringType },
                navArgument("equipmentData") { type = NavType.StringType },
                navArgument("startTime") { type = NavType.LongType },
                navArgument("bookedHours") { type = NavType.FloatType }  // CHANGED
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            val approvalUrlEncoded = backStackEntry.arguments?.getString("approvalUrl") ?: ""
            val approvalUrl = java.net.URLDecoder.decode(approvalUrlEncoded, "UTF-8")
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val facilityIndId = backStackEntry.arguments?.getString("facilityIndId") ?: ""
            val equipmentData = backStackEntry.arguments?.getString("equipmentData") ?: ""
            val startTime = backStackEntry.arguments?.getLong("startTime") ?: 0L
            val bookedHours = backStackEntry.arguments?.getFloat("bookedHours")?.toDouble() ?: 1.0

            println("💳 PAYPAL WEBVIEW ROUTE:")
            println("   orderId: $orderId")
            println("   approvalUrl: $approvalUrl")
            println("   facilityIndId: $facilityIndId")
            println("   equipmentData: $equipmentData")
            println("   bookedHours: $bookedHours hours")  // CHANGED

            PayPalWebViewScreen(
                orderId = orderId,
                approvalUrl = approvalUrl,
                facilityIndId = facilityIndId,
                equipmentData = equipmentData,
                startTime = startTime,
                bookedHours = bookedHours,  // CHANGED
                navController = navController,
                userId = userId
            )
        }

        // Success screen (after payment or free booking)
        composable("booking_success") {
            BookingSuccessScreen(navController)
        }

        // Failure screen (if PayPal payment fails)
        composable("booking_failed") {
            BookingFailedScreen(navController)
        }

        // ==================== ADMIN ROUTES ====================

        composable("admin") {
            AdminScreen(navController = navController)
        }

        composable("user_management") {
            AdminUserScreen(navController = navController)
        }

        composable("admin_students") {
            AdminStudentScreen(navController = navController)
        }

        composable("admin_staff") {
            AdminStaffScreen(navController = navController)
        }

        composable("student_list") {
            AdminStudentScreen(navController = navController)
        }

        composable("booking_management") {
            AdminBookingScreen(navController = navController)
        }

        composable("search_booking_by_user") {
            SearchBookingByUserScreen(navController = navController)
        }

        composable("search_booking_by_facility") {
            SearchBookingByFacilityScreen(navController = navController)
        }

        composable("search_booking_by_reservation_id") {
            SearchBookingByReservationIdScreen(navController = navController)
        }

        composable("search_booking_by_date") {
            SearchBookingByDateScreen(navController = navController)
        }

        composable("facilities_management") {
            AdminFacilityScreen(navController = navController)
        }

        composable("sport_complex") {
            SportComplexScreen(navController = navController)
        }

        composable("clubhouse") {
            ClubhouseScreen(navController = navController)
        }

        composable("library") {
            LibraryScreen(navController = navController)
        }

        composable("citc") {
            CitcScreen(navController = navController)
        }

        composable("arena_tarumt") {
            ArenaTarumtScreen(navController = navController)
        }

        composable("arena_tarumt_sport") {
            ArenaTarumtSportScreen(navController = navController)
        }

        composable(
            route = "facility_detail/{facilityName}",
            arguments = listOf(navArgument("facilityName") { type = NavType.StringType })
        ) {
            val facilityName = it.arguments?.getString("facilityName")
            FacilityDetailScreen(navController = navController, facilityName = facilityName)
        }

        composable(
            route = "sub_venues/{mainFacilityId}",
            arguments = listOf(navArgument("mainFacilityId") { type = NavType.StringType })
        ) {
            val mainFacilityId = it.arguments?.getString("mainFacilityId")
            SubVenuesScreen(navController = navController, mainFacilityId = mainFacilityId)
        }

        composable(
            route = "equipment_modification/{facilityId}",
            arguments = listOf(navArgument("facilityId") { type = NavType.StringType })
        ) {
            val facilityId = it.arguments?.getString("facilityId")
            Text("Equipment Modification Screen for facilityId: $facilityId")
        }

        composable("report_generation") {
            Text("Report Generation Screen")
        }

        composable(
            route = "add_facility/{buildingType}",
            arguments = listOf(navArgument("buildingType") { type = NavType.StringType })
        ) { backStackEntry ->
            val buildingType = backStackEntry.arguments?.getString("buildingType") ?: "C"
            AddFacilityScreen(
                navController = navController,
                buildingType = buildingType
            )
        }

        composable(
            route = "user_information/{userId}/{userType}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("userType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val userType = backStackEntry.arguments?.getString("userType") ?: "student"

            UserInformationScreen(
                navController = navController,
                userId = userId,
                userType = userType
            )
        }

        composable(
            route = "addEditReservation/{reservationId}",
            arguments = listOf(navArgument("reservationId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val reservationId = backStackEntry.arguments?.getString("reservationId")
            AddEditReservationScreen(
                navController = navController,
                reservationId = reservationId
            )
        }

        composable("addEditReservation") {
            AddEditReservationScreen(navController = navController)
        }
    }
}