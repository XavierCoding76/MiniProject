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
    // IMPORTANT: Replace R.drawable.staff_image, etc., with your actual image resources.
    val userItems = listOf(
        DashboardItemData(
            title = "Staff",
            imageResId = R.drawable.ic_launcher_background, // Replace with your actual drawable
            backgroundColor = Color(0xFF6A5ACD), // SlateBlue to match screenshot
            destinationRoute = "staff_list" // Placeholder route for staff list
        ),
        DashboardItemData(
            title = "Students",
            imageResId = R.drawable.ic_launcher_background, // Replace with your actual drawable
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "student_list" // Placeholder route for student list
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
