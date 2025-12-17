package com.example.miniproject.admin.facilityAdmin

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.miniproject.R
import com.example.miniproject.components.Dashboard
import com.example.miniproject.components.DashboardItemData

@Composable
fun AdminFacilityScreen(navController: NavController) {
    val items = listOf(
        DashboardItemData(
            title = "Sport Complex",
            imageResId = R.drawable.ic_launcher_background, // Replace with your actual image
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "sport_complex"
        ),
        DashboardItemData(
            title = "Clubhouse",
            imageResId = R.drawable.ic_launcher_background, // Replace with your actual image
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "clubhouse"
        ),
        DashboardItemData(
            title = "Library",
            imageResId = R.drawable.ic_launcher_background, // Replace with your actual image
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "library"
        ),
        DashboardItemData(
            title = "Cyber Centre, CITC",
            imageResId = R.drawable.ic_launcher_background, // Replace with your actual image
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "citc"
        ),
        DashboardItemData(
            title = "Arena TARUMT",
            imageResId = R.drawable.ic_launcher_background, // Replace with your actual image
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "arena_tarumt"
        )
    )

    Dashboard(
        title = "Facility",
        items = items,
        onItemClick = { item ->
            navController.navigate(item.destinationRoute)
        },
        onBackClick = { navController.popBackStack() }
    )
}
