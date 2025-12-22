package com.example.miniproject.admin.bookingAdmin

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.miniproject.reservation.Reservation
import com.example.miniproject.payment.Payment
import com.example.miniproject.payment.PaymentDetail
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBookingByDateScreen(
    navController: NavController,
    viewModel: SearchBookingByDateViewModel = viewModel()
) {
    val context = LocalContext.current
    val searchText by viewModel.searchText.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

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
            DatePickerCard(viewModel, context)

            Spacer(modifier = Modifier.height(24.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF483D8B))
                    }
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
                                Icons.Filled.CalendarToday,
                                contentDescription = "Calendar",
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Select a date to view bookings",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                searchResults.isNullOrEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Filled.EventBusy,
                                contentDescription = "No bookings",
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No bookings found for this date",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                else -> {
                    SearchResultsSection(searchResults!!, navController, viewModel)
                }
            }
        }

        CenterAlignedTopAppBar(
            title = {
                Text(
                    "Bookings by Date",
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
private fun DatePickerCard(
    viewModel: SearchBookingByDateViewModel,
    context: android.content.Context
) {
    val searchText by viewModel.searchText.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth(),
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
                "Select Date",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF483D8B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val calendar = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val selectedCalendar = Calendar.getInstance()
                            selectedCalendar.set(year, month, dayOfMonth)
                            viewModel.setSelectedDate(selectedCalendar.timeInMillis)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF483D8B)
                )
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Select Date",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Pick a Date", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (searchText.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF483D8B).copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Selected Date",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Selected: $searchText",
                            fontSize = 15.sp,
                            color = Color(0xFF483D8B),
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = { viewModel.clearSearch() }
                        ) {
                            Text("Clear", color = Color(0xFF483D8B))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultsSection(
    searchResults: List<ReservationWithFacility>,
    navController: NavController,
    viewModel: SearchBookingByDateViewModel
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var reservationToDelete by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Bookings Found: ${searchResults.size}",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF483D8B)
            )

            if (searchResults.isNotEmpty()) {
                Surface(
                    color = Color(0xFF483D8B).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "${searchResults.sumOf { it.reservation.bookedHours }} total hours",
                        fontSize = 14.sp,
                        color = Color(0xFF483D8B),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(searchResults) { reservationWithFacility ->
                ReservationCard(
                    reservation = reservationWithFacility.reservation,
                    facilityName = reservationWithFacility.facilityName,
                    userDisplayID = reservationWithFacility.userDisplayID,
                    onEditClick = {
                        navController.navigate("addEditReservation/${reservationWithFacility.reservation.id}")
                    },
                    onDeleteClick = {
                        reservationToDelete = reservationWithFacility.reservation.id
                        showDeleteDialog = true
                    }
                )
            }
        }
    }

    if (showDeleteDialog && reservationToDelete != null) {
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
                        reservationToDelete?.let { id ->
                            viewModel.deleteReservation(id)
                        }
                        showDeleteDialog = false
                        reservationToDelete = null
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
private fun ReservationCard(
    reservation: Reservation,
    facilityName: String,
    userDisplayID: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val currentTime = System.currentTimeMillis()
    val reservationTime = reservation.bookedTime?.toDate()?.time ?: 0L
    val hasExpired = currentTime > reservationTime

    var showPaymentDialog by remember { mutableStateOf(false) }
    var paymentData by remember { mutableStateOf<Pair<Payment, List<PaymentDetail>>?>(null) }
    var hasPayment by remember { mutableStateOf(false) }

    // Check if reservation has payment
    LaunchedEffect(reservation.id) {
        FirebaseFirestore.getInstance()
            .collection("payment")
            .whereEqualTo("reservationID", reservation.id)
            .get()
            .addOnSuccessListener { paymentSnapshot ->
                if (!paymentSnapshot.isEmpty) {
                    hasPayment = true
                    val payment = paymentSnapshot.documents.first().toObject(Payment::class.java)
                    payment?.let { p ->
                        FirebaseFirestore.getInstance()
                            .collection("paymentDetails")
                            .whereEqualTo("paymentId", p.id)
                            .get()
                            .addOnSuccessListener { detailsSnapshot ->
                                val details = detailsSnapshot.documents.mapNotNull {
                                    it.toObject(PaymentDetail::class.java)
                                }
                                paymentData = Pair(p, details)
                            }
                    }
                } else {
                    hasPayment = false
                }
            }
    }

    // Check if reservation is deletable (not expired AND no payment)
    val isDeletable = !hasExpired && !hasPayment
    val isEditable = !hasExpired

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
                        text = if (reservation.id.length > 8) "${reservation.id.take(6)}..." else reservation.id,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF483D8B)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (paymentData != null) {
                        IconButton(
                            onClick = { showPaymentDialog = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Filled.AttachMoney,
                                contentDescription = "View Payment",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    if (hasExpired) {
                        Surface(
                            color = Color(0xFFFF5252),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "EXPIRED",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Surface(
                        color = when (reservation.bookedHours) {
                            0.5 -> Color(0xFF03A9F4)
                            1.0 -> Color(0xFF2196F3)
                            1.5 -> Color(0xFF00BCD4)
                            2.0 -> Color(0xFF4CAF50)
                            2.5 -> Color(0xFF8BC34A)
                            3.0 -> Color(0xFFFF9800)
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
                icon = Icons.Filled.CalendarToday,
                label = "Booked Time",
                value = formatTimestamp(reservation.bookedTime)
            )

            if (!hasExpired) {
                // Show action buttons for future reservations
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Edit button - always available for future reservations
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

                    // Delete button - only available if no payment
                    Button(
                        onClick = onDeleteClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5252)
                        ),
                        enabled = isDeletable
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

                // Show info message if delete is disabled due to payment
                if (hasPayment) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = "Protected",
                                tint = Color(0xFF1976D2),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "This reservation has payment and cannot be deleted",
                                fontSize = 13.sp,
                                color = Color(0xFF1976D2)
                            )
                        }
                    }
                }
            } else {
                // Show archived message for expired reservations
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
                            "This reservation has been archived and cannot be modified",
                            fontSize = 13.sp,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            }
        }
    }

    if (showPaymentDialog && paymentData != null) {
        SimplifiedPaymentReceiptDialog(
            payment = paymentData!!.first,
            onDismiss = { showPaymentDialog = false }
        )
    }
}

@Composable
private fun SimplifiedPaymentReceiptDialog(
    payment: Payment,
    onDismiss: () -> Unit
) {
    BackHandler(enabled = true) {
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Receipt,
                    contentDescription = "Receipt",
                    tint = Color(0xFF483D8B),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Payment Receipt", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Payment Header
                Surface(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Payment ID: ${payment.id}", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Date: ${formatTimestamp(payment.date)}", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Reservation ID: ${payment.reservationID}", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Total Amount (Highlighted)
                Surface(
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Total Amount",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                String.format("RM %.2f", payment.totalAmount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = Color(0xFF4CAF50)
                            )
                        }

                        Icon(
                            Icons.Filled.AttachMoney,
                            contentDescription = "Payment",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF483D8B))
            }
        }
    )
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