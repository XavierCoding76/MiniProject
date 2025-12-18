package com.example.miniproject.admin.facilityAdmin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
        imageResId = R.drawable.ic_launcher_background,
        backgroundColor = Color(0xFF6A5ACD),
        destinationRoute = "arena_tarumt_sport"
    )

    val otherFacilities by remember(facilities) {
        derivedStateOf {
            facilities.filter { !it.id.startsWith("AS") }.map { facility ->
                val encodedName = URLEncoder.encode(facility.name, StandardCharsets.UTF_8.toString())
                DashboardItemData(
                    title = facility.name,
                    imageResId = R.drawable.ic_launcher_background,
                    backgroundColor = Color(0xFF6A5ACD),
                    destinationRoute = "facility_detail/$encodedName"
                )
            }
        }
    }

    val items = listOf(sportFacilities) + otherFacilities

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_facility/A") },
                containerColor = Color(0xFF6A5ACD),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Facility"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Wrap Dashboard in a scrollable container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Dashboard(
                    title = "Arena",
                    items = items,
                    onItemClick = { item ->
                        navController.navigate(item.destinationRoute)
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}