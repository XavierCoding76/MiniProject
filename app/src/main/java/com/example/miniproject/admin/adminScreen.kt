package com.example.miniproject.admin

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.miniproject.R
import com.example.miniproject.components.Dashboard
import com.example.miniproject.components.DashboardItemData

@Composable
fun AdminScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Define the data for your dashboard items here.
    // IMPORTANT: Replace R.drawable.ic_launcher_background, etc., with your actual image resources.
    val items = listOf(
        DashboardItemData(
            title = "Facilities",
            imageResId = R.drawable.ic_launcher_background, // Replace with your actual drawable
            backgroundColor = Color(0xFF6A5ACD), // SlateBlue to match screenshot
            destinationRoute = "facilities_management"
        ),
        DashboardItemData(
            title = "Users",
            imageResId = R.drawable.ic_launcher_background, // Replace with your actual drawable
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "user_management"
        ),
        DashboardItemData(
            title = "Bookings",
            imageResId = R.drawable.ic_launcher_background, // Replace with your actual drawable
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "booking_management"
        ),
        DashboardItemData(
            title = "Report",
            imageResId = R.drawable.ic_launcher_background, // Replace with your actual drawable
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "report_generation"
        )
    )

    // Use the new, all-in-one Dashboard component.
    // It handles the background, header, layout, and items.
    Dashboard(
        title = "Admin Panel",
        items = items,
        onItemClick = { item ->
            navController.navigate(item.destinationRoute)
        },
        onBackClick = { navController.popBackStack() },
        modifier = modifier
    )
}
