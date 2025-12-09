package com.example.miniproject.booking.UI

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BookingHistoryItem(
    val facilityName: String,
    val date: String,      // formatted
    val time: String       // formatted
)

@Composable
fun BookingHistoryScreen(
    items: List<BookingHistoryItem>,
    onNewBooking: () -> Unit,
    onProfileClick: () -> Unit,
    onBottomHome: () -> Unit,
    onBottomSettings: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewBooking,
                containerColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Booking", tint = Color.White)
            }
        },
        bottomBar = {
            BottomNavBar(
                onHome = onBottomHome,
                onSettings = onBottomSettings,
                onProfile = onProfileClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HeaderSection(onProfileClick)

            Spacer(modifier = Modifier.height(16.dp))

            if (items.isEmpty()) {
                // No booking placeholder
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No Booking Yet.", fontSize = 16.sp)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                    items(items) { booking ->
                        BookingHistoryCard(booking)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(onProfileClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6A4DFF),
                        Color(0xFF9E80FF)
                    )
                )
            )
    ) {
        IconButton(
            onClick = onProfileClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Profile",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        Text(
            text = "Welcome Back!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun BookingHistoryCard(item: BookingHistoryItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEDEAFF), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Text(item.facilityName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(item.date, fontSize = 14.sp)
        Text(item.time, fontSize = 14.sp)
    }
}

@Composable
fun BottomNavBar(
    onHome: () -> Unit,
    onSettings: () -> Unit,
    onProfile: () -> Unit
) {
    NavigationBar(containerColor = Color(0xFFFFFFFF)) {
        NavigationBarItem(
            selected = false,
            onClick = onSettings,
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") }
        )
        NavigationBarItem(
            selected = true, // current screen
            onClick = onHome,
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onProfile,
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") }
        )
    }
}