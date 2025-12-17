package com.example.miniproject.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.R
import com.example.miniproject.components.Dashboard
import com.example.miniproject.components.DashboardItemData

@Composable
fun AdminScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Define the data for your dashboard items here.
    val items = listOf(
        DashboardItemData(
            title = "Facilities",
            imageResId = R.drawable.ic_launcher_background,
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "facilities_management"
        ),
        DashboardItemData(
            title = "Users",
            imageResId = R.drawable.ic_launcher_background,
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "user_management"
        ),
        DashboardItemData(
            title = "Bookings",
            imageResId = R.drawable.ic_launcher_background,
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "booking_management"
        ),
        DashboardItemData(
            title = "Report",
            imageResId = R.drawable.ic_launcher_background,
            backgroundColor = Color(0xFF6A5ACD),
            destinationRoute = "report_generation"
        )
    )

    Dashboard(
        title = "Admin Panel",
        items = items,
        onItemClick = { item ->
            navController.navigate(item.destinationRoute)
        },
        modifier = modifier,
        bottomContent = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { showLogoutDialog = true }, 
                    modifier = Modifier
                        .width(200.dp) 
                        .height(40.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(text = "Log Out", color = Color.White)
                }
            }
        }
    )

    // Confirmation Dialog with styled buttons
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "Confirm Logout") },
            text = { Text(text = "Are you sure you want to log out?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        // TODO: Call ViewModel to handle actual logout logic
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Yes", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("No", color = Color.Gray) // Simple, no-border text button
                }
            }
        )
    }
}
