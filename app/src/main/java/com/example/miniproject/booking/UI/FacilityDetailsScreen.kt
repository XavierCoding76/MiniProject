package com.example.miniproject.booking.UI

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun FacilityDetailsScreen(
    facilityId: String,
    onBookNow: () -> Unit
) {
    Text("Facility Details Screen for $facilityId")
}