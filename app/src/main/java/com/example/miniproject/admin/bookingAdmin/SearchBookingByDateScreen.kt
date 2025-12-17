package com.example.miniproject.admin.bookingAdmin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBookingByDateScreen(
    navController: NavController,
    viewModel: SearchBookingByDateViewModel = viewModel()
) {
    var showCalendar by remember { mutableStateOf(false) }
    val state = rememberDatePickerState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF483D8B), Color(0xFF6A5ACD))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 140.dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            // Box wrapper to reliably capture clicks
            Box {
                TextField(
                    value = viewModel.getFormattedDate(),
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    placeholder = { Text("Enter date...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledIndicatorColor = Color.Transparent,
                        disabledPlaceholderColor = Color.Gray,
                        disabledTrailingIconColor = Color.Black,
                        disabledLeadingIconColor = Color.Black
                    ),
                    trailingIcon = { 
                        Icon(Icons.Default.DateRange, contentDescription = "Date Range Icon") 
                    }
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { showCalendar = true }
                )
            }
        }

        // Header
        CenterAlignedTopAppBar(
            title = { Text("Bookings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 32.sp) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
            modifier = Modifier.padding(top = 40.dp)
        )

        // DatePickerDialog is a popup that overlays the screen
        if (showCalendar) {
            DatePickerDialog(
                onDismissRequest = { showCalendar = false },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.setSelectedDate(state.selectedDateMillis)
                        showCalendar = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCalendar = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = state)
            }
        }
    }
}
