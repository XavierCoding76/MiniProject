package com.example.miniproject.admin.facilityAdmin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
fun SportComplexScreen(
    navController: NavController,
    viewModel: FacilityListViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.fetchFacilitiesStartingWith("S")
    }

    val facilities by viewModel.facilities

    val items = facilities.map {
        val encodedName = URLEncoder.encode(it.name, StandardCharsets.UTF_8.toString())
        DashboardItemData(
            title = it.name,
            imageResId = R.drawable.ic_launcher_background,
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "facility_detail/$encodedName"
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_facility/S") },
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
            Dashboard(
                title = "Sport Complex",
                items = items,
                onItemClick = { item ->
                    navController.navigate(item.destinationRoute)
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}