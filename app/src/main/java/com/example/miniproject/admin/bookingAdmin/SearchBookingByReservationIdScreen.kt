package com.example.miniproject.admin.bookingAdmin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBookingByReservationIdScreen(
    navController: NavController,
    viewModel: SearchBookingByReservationIdViewModel = viewModel()
) {
    val searchText by viewModel.searchText.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val searchResult by viewModel.searchResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

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
            SearchBarCard(
                viewModel = viewModel,
                searchText = searchText,
                searchHistory = searchHistory,
                onSearch = { viewModel.searchReservation(searchText) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error message
            error?.let { errorMsg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = "Error",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            errorMsg,
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Content
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF483D8B))
                    }
                }
                searchResult != null -> {
                    SearchResultSection(searchResult!!, navController, viewModel)
                }
                searchText.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = "Search",
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Enter a reservation ID to search",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        CenterAlignedTopAppBar(
            title = {
                Text(
                    "Search by Reservation ID",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier.padding(top = 40.dp)
        )
    }
}

@Composable
private fun SearchBarCard(
    viewModel: SearchBookingByReservationIdViewModel,
    searchText: String,
    searchHistory: List<String>,
    onSearch: () -> Unit
) {
    var showHistory by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                "Enter Reservation ID",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF483D8B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    viewModel.onSearchTextChange(it)
                    showHistory = it.isEmpty() && searchHistory.isNotEmpty()
                },
                label = { Text("Reservation ID") },
                placeholder = { Text("Enter reservation ID...") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchTextChange("") }) {
                            Icon(
                                Icons.Filled.Clear,
                                contentDescription = "Clear",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF483D8B),
                    focusedLabelColor = Color(0xFF483D8B),
                    focusedLeadingIconColor = Color(0xFF483D8B)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSearch,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF483D8B)
                ),
                enabled = searchText.isNotBlank()
            ) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search", fontSize = 16.sp)
            }

            // Search History
            if (searchHistory.isNotEmpty() && showHistory) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Searches",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    TextButton(
                        onClick = { viewModel.clearAllHistory() }
                    ) {
                        Text("Clear All", fontSize = 12.sp, color = Color(0xFF483D8B))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                searchHistory.take(5).forEach { historyItem ->
                    Surface(
                        onClick = {
                            viewModel.onSearchTextChange(historyItem)
                            viewModel.searchReservation(historyItem)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Filled.History,
                                    contentDescription = "History",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    historyItem,
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                            }
                            IconButton(
                                onClick = { viewModel.onClearHistoryItem(historyItem) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Remove",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultSection(
    reservationDetail: ReservationDetail,
    navController: NavController,
    viewModel: SearchBookingByReservationIdViewModel
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Reservation Found",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF483D8B)
        )

        ReservationDetailCard(
            reservation = reservationDetail.reservation,
            facilityName = reservationDetail.facilityName,
            userDisplayID = reservationDetail.userDisplayID,
            onEditClick = {
                navController.navigate("addEditReservation/${reservationDetail.reservation.id}")
            },
            onDeleteClick = {
                showDeleteDialog = true
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Delete Reservation",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to delete this reservation? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteReservation(reservationDetail.reservation.id)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5252)
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ReservationDetailCard(
    reservation: com.example.miniproject.reservation.Reservation,
    facilityName: String,
    userDisplayID: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // ✅ Check if reservation time has passed
    val currentTime = System.currentTimeMillis()
    val reservationTime = reservation.bookedTime?.toDate()?.time ?: 0L
    val hasExpired = currentTime > reservationTime

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Reservation ID",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = reservation.id,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF483D8B)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ✅ Show expired badge
                    if (hasExpired) {
                        Surface(
                            color = Color(0xFFFF5252),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "ARCHIVED",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Surface(
                        color = when (reservation.bookedHours) {
                            0.5 -> Color(0xFF03A9F4)  // Light Blue
                            1.0 -> Color(0xFF2196F3)  // Blue
                            1.5 -> Color(0xFF00BCD4)  // Cyan
                            2.0 -> Color(0xFF4CAF50)  // Green
                            2.5 -> Color(0xFF8BC34A)  // Light Green
                            3.0 -> Color(0xFFFF9800)  // Orange
                            else -> Color(0xFF6A5ACD)
                        },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "${reservation.bookedHours}h",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            ReservationInfoRow(
                icon = Icons.Filled.Person,
                label = "User Display ID",
                value = userDisplayID
            )

            Spacer(modifier = Modifier.height(12.dp))

            ReservationInfoRow(
                icon = Icons.Filled.LocationOn,
                label = "Facility",
                value = facilityName
            )

            Spacer(modifier = Modifier.height(12.dp))

            ReservationInfoRow(
                icon = Icons.Filled.Badge,
                label = "Facility ID",
                value = reservation.facilityID
            )

            Spacer(modifier = Modifier.height(12.dp))

            ReservationInfoRow(
                icon = Icons.Filled.CalendarToday,
                label = "Booked Time",
                value = formatTimestamp(reservation.bookedTime)
            )

            // ✅ Only show buttons if reservation hasn't expired
            if (!hasExpired) {
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF483D8B)
                        )
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit", fontSize = 14.sp)
                    }

                    Button(
                        onClick = onDeleteClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5252)
                        )
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete", fontSize = 14.sp, color = Color.White)
                    }
                }
            } else {
                // ✅ Show message for expired reservations
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = "Info",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "This reservation has been archived and cannot be modified.",
                            fontSize = 13.sp,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun ReservationInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF483D8B),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                fontSize = 13.sp,
                color = Color.Gray
            )
            Text(
                text = value.ifEmpty { "Not specified" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatTimestamp(timestamp: com.google.firebase.Timestamp?): String {
    return if (timestamp != null) {
        try {
            val date = timestamp.toDate()
            val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
            dateFormat.format(date)
        } catch (e: Exception) {
            "Date unavailable"
        }
    } else {
        "Date unavailable"
    }
}