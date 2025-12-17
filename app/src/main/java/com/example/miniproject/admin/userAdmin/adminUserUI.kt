package com.example.miniproject.admin.userAdmin

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.miniproject.R
import com.example.miniproject.components.Dashboard
import com.example.miniproject.components.DashboardItemData

@Composable
fun AdminUserScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Define the data for the user management dashboard items.
    val userItems = listOf(
        DashboardItemData(
            title = "Staff",
            imageResId = R.drawable.ic_launcher_background, // Replace with your actual drawable
            backgroundColor = Color(0xFF6A5ACD), // SlateBlue to match screenshot
            destinationRoute = "admin_staff" // Updated to match AppNavigation
        ),
        DashboardItemData(
            title = "Students",
            imageResId = R.drawable.ic_launcher_background, // Replace with your actual drawable
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "admin_students" // Updated to match AppNavigation
        )
    )

    // Reuse the universal Dashboard component to build this screen.
    Dashboard(
        title = "Users",
        items = userItems,
        onItemClick = { item ->
            navController.navigate(item.destinationRoute)
        },
        onBackClick = { navController.popBackStack() },
        modifier = modifier
    )
}
