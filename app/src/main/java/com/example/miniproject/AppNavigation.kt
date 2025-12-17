package com.example.miniproject

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.miniproject.admin.AdminScreen
import com.example.miniproject.admin.bookingAdmin.AdminBookingScreen
import com.example.miniproject.admin.bookingAdmin.SearchBookingByDateScreen
import com.example.miniproject.admin.bookingAdmin.SearchBookingByFacilityScreen
import com.example.miniproject.admin.bookingAdmin.SearchBookingByReservationIdScreen
import com.example.miniproject.admin.bookingAdmin.SearchBookingByUserScreen
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
import com.example.miniproject.payment.PaymentScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "admin", // Temporarily set to show the new screen
        modifier = modifier // Apply the modifier passed from the Scaffold
    ) {
        composable("admin") {
            AdminScreen(navController = navController)
        }

        composable("payment_screen") {
            PaymentScreen()
        }

        composable("user_management") { 
            AdminUserScreen(navController = navController)
        }

        composable("student_list") {
            AdminStudentScreen(navController = navController)
        }

        composable("staff_list") {
            AdminStaffScreen(navController = navController)
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
            // TODO: Replace with the actual Equipment Modification screen
            val facilityId = it.arguments?.getString("facilityId")
            Text("Equipment Modification Screen for facilityId: $facilityId")
        }

        composable("report_generation") {
            // TODO: Replace with the actual Report Generation screen
            Text("Report Generation Screen")
        }
    }
}
