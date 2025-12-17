package com.example.miniproject.admin.bookingAdmin

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.miniproject.R
import com.example.miniproject.components.Dashboard
import com.example.miniproject.components.DashboardItemData

@Composable
fun AdminBookingScreen(navController: NavController, modifier: Modifier = Modifier) {
    val items = listOf(
        DashboardItemData(
            title = "By User",
            imageResId = R.drawable.ic_launcher_background, // Replace with your actual drawable
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "search_booking_by_user"
        ),
        DashboardItemData(
            title = "By Facility",
            imageResId = R.drawable.ic_launcher_background, // Replace with your actual drawable
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "search_booking_by_facility"
        ),
        DashboardItemData(
            title = "By Reservation ID",
            imageResId = R.drawable.ic_launcher_background, // Replace with your actual drawable
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "search_booking_by_reservation_id"
        ),
        DashboardItemData(
            title = "By Date",
            imageResId = R.drawable.ic_launcher_background, // Replace with your actual drawable
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "search_booking_by_date" // Placeholder for now
        )
    )

    Dashboard(
        title = "Bookings",
        items = items,
        onItemClick = { item -> navController.navigate(item.destinationRoute) },
        onBackClick = { navController.popBackStack() },
        modifier = modifier
    )
}
