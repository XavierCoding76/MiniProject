package com.example.miniproject.admin.facilityAdmin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.miniproject.R
import com.example.miniproject.components.Dashboard
import com.example.miniproject.components.DashboardItemData
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun CitcScreen(
    navController: NavController,
    viewModel: FacilityListViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.fetchFacilitiesStartingWith("CC")
    }

    val facilities by viewModel.facilities

    val items = facilities.map {
        val encodedName = URLEncoder.encode(it.name, StandardCharsets.UTF_8.toString())
        DashboardItemData(
            title = it.name,
            imageResId = R.drawable.ic_launcher_background, // Replace with your actual image
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "facility_detail/$encodedName"
        )
    }

    Dashboard(
        title = "CITC",
        items = items,
        onItemClick = { item ->
            navController.navigate(item.destinationRoute)
        },
        onBackClick = { navController.popBackStack() }
    )
}
