package com.example.miniproject.admin.facilityAdmin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.miniproject.R
import com.example.miniproject.components.Dashboard
import com.example.miniproject.components.DashboardItemData
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun ArenaTarumtScreen(navController: NavController, viewModel: FacilityListViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        viewModel.fetchFacilitiesStartingWith("A")
    }

    val facilities by viewModel.facilities

    val sportFacilities = DashboardItemData(
        title = "Sports Facilities",
        imageResId = R.drawable.ic_launcher_background, // Replace with your actual image
        backgroundColor = Color(0xFF6A5ACD),
        destinationRoute = "arena_tarumt_sport"
    )

    val otherFacilities by remember(facilities) {
        derivedStateOf {
            facilities.filter { !it.id.startsWith("AS") }.map { facility ->
                val encodedName = URLEncoder.encode(facility.name, StandardCharsets.UTF_8.toString())
                DashboardItemData(
                    title = facility.name,
                    imageResId = R.drawable.ic_launcher_background, // Replace with your actual image
                    backgroundColor = Color(0xFF6A5ACD),
                    destinationRoute = "facility_detail/$encodedName"
                )
            }
        }
    }

    val items = listOf(sportFacilities) + otherFacilities

    Dashboard(
        title = "Arena",
        items = items,
        onItemClick = { item ->
            navController.navigate(item.destinationRoute)
        },
        onBackClick = { navController.popBackStack() }
    )
}
