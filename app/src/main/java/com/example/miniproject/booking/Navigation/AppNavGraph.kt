package com.example.miniproject.booking.Navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

// Import your screens
import com.example.miniproject.booking.UI.BookingHistoryScreen
import com.example.miniproject.booking.UI.FacilityListScreen
import com.example.miniproject.booking.UI.FacilityDetailsScreen
import com.example.miniproject.booking.UI.BookingFormScreen
import com.example.miniproject.booking.UI.BookingHistoryItem

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.BookingHistory.route
    ) {
        composable(AppRoute.BookingHistory.route) {
            val vm: BookingHistoryViewModel = viewModel()
            val items by vm.items.collectAsState()

            BookingHistoryScreen(
                items = items,
                onNewBooking = { navController.navigate(AppRoute.FacilityList.route) },
                onProfileClick = { /* go to profile */ },
                onBottomHome = { navController.navigate(AppRoute.BookingHistory.route) },
                onBottomSettings = { /* Navigate to settings */ },
            )
        }

        composable(AppRoute.FacilityList.route) {
            FacilityListScreen(
                onViewDetails = { id ->
                    navController.navigate(AppRoute.FacilityDetails.createRoute(id))
                },
                onBookNow = { id ->
                    navController.navigate(AppRoute.BookingForm.createRoute(id))
                }
            )
        }

        composable(AppRoute.FacilityDetails.route) { backStackEntry ->
            val facilityId = backStackEntry.arguments?.getString("facilityId") ?: ""
            FacilityDetailsScreen(
                facilityId = facilityId,
                onBookNow = {
                    navController.navigate(AppRoute.BookingForm.createRoute(facilityId))
                }
            )
        }

        composable(AppRoute.BookingForm.route) { backStackEntry ->
            val facilityId = backStackEntry.arguments?.getString("facilityId") ?: ""
            BookingFormScreen(facilityId = facilityId)
        }
    }
}