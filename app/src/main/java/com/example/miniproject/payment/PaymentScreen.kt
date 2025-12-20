package com.example.miniproject.payment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import java.util.*

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.goyumapp.payment.PayPalRepository
import com.example.miniproject.R
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PaymentScreen(
    userId: String = "",  // ADD THIS
    facilityIndId: String = "",
    equipmentData: String = "",
    startTime: Long = 0L,
    bookedHours: Double = 1.0,
    navController: NavController,
    viewModel: PaymentViewModel = viewModel()
) {
    // Debug incoming values
    println("🎫🎫🎫 PaymentScreen RECEIVED:")
    println("   userId: '$userId'")
    println("   userId.isEmpty(): ${userId.isEmpty()}")
    println("   facilityIndId: '$facilityIndId'")
    println("   equipmentData: '$equipmentData'")

    // Parse equipment data into list
    val equipmentList = if (equipmentData.isNotEmpty()) {
        equipmentData.split(",").filter { it.isNotEmpty() }
    } else {
        emptyList()
    }

    println("   Parsed equipment list: $equipmentList")

    // Convert milliseconds to Firebase Timestamp
    val startTimestamp = if (startTime > 0) {
        Timestamp(startTime / 1000, 0)
    } else {
        null
    }

    val endTimestamp = if (startTimestamp != null && bookedHours > 0) {
        val endTimeMillis = startTime + (bookedHours * 3600 * 1000).toLong()
        Timestamp(endTimeMillis / 1000, 0)
    } else {
        null
    }

    // Payment method selection state
    var isPayPalSelected by remember { mutableStateOf(false) }

    // PayPal processing states
    var isProcessingPayment by remember { mutableStateOf(false) }
    var paymentError by remember { mutableStateOf<String?>(null) }

    // PayPal repository
    val paypalRepository = remember { PayPalRepository() }
    val scope = rememberCoroutineScope()

    // Load payment data
    LaunchedEffect(facilityIndId, equipmentData, startTime, bookedHours) {
        if (facilityIndId.isNotEmpty()) {
            println("🔄 Calling viewModel.loadPaymentData()")
            viewModel.loadPaymentData(
                userId = userId,
                facilityIndId = facilityIndId,
                equipmentData = equipmentList,
                startTime = startTimestamp,
                bookedHours = bookedHours
            )
        } else {
            println("⏸️ No facility ID provided, skipping load")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF483D8B), Color(0xFF6A5ACD))
                )
            )
    ) {
        if (viewModel.isLoading) {
            // Loading state
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading booking details...", color = Color.White)
            }
        } else if (viewModel.errorMessage != null) {
            // Error state
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Error Loading Data",
                    color = Color.Red,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = viewModel.errorMessage ?: "Unknown error",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.loadPaymentData(
                            facilityIndId = facilityIndId,
                            equipmentData = equipmentList,
                            startTime = startTimestamp,
                            bookedHours = bookedHours,
                            userId = userId

                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Retry", color = Color(0xFF6A5ACD))
                }
            }
        } else if (facilityIndId.isEmpty()) {
            // No facility ID provided state
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No Facility Selected",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please select a facility to proceed with booking",
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Go Back", color = Color(0xFF6A5ACD))
                }
            }
        } else {
            // Success state - Show booking summary
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Title
                Text(
                    text = "Booking Summary",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Content area with scroll
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp)
                    ) {
                        // 1. Booking Summary Card
                        BookingSummaryCard(viewModel)
                        Spacer(modifier = Modifier.height(24.dp))

                        // 2. Cost Details Card
                        CostDetailsCard(viewModel)
                        Spacer(modifier = Modifier.height(24.dp))

                        // 3. Total Amount
                        val totalAmount = viewModel.calculateTotal()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Total:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF333333)
                            )
                            Text(
                                text = String.format("RM %.2f", totalAmount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(0xFF6A5ACD)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 4. Payment Method Selection
                        PaymentMethod(
                            isSelected = isPayPalSelected,
                            onSelect = { isPayPalSelected = it }
                        )

                        // Show payment error if any
                        if (paymentError != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "❌",
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Payment Failed",
                                            fontSize = 14.sp,
                                            color = Color(0xFFC62828),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = paymentError ?: "Unknown error",
                                            fontSize = 12.sp,
                                            color = Color(0xFFC62828)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(100.dp)) // Space for anchored button
                    }
                }

                // 5. Anchored Bottom Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = {
                            if (isPayPalSelected && !isProcessingPayment) {
                                val total = viewModel.calculateTotal()
                                paymentError = null // Reset error
                                isProcessingPayment = true

                                scope.launch {
                                    try {
                                        println("💳 Creating PayPal order for RM $total")

                                        // Step 1: Create PayPal order via Firebase Cloud Function
                                        val orderResult = paypalRepository.createOrder(
                                            amount = String.format("%.2f", total),
                                            currency = "MYR",
                                            description = "Facility Booking - ${viewModel.getFacilityIndName()}"
                                        )

                                        if (orderResult.isSuccess) {
                                            val order = orderResult.getOrNull()!!
                                            println("✅ Order created: ${order.orderId}")
                                            println("🔗 Approval URL: ${order.approvalUrl}")

                                            // Step 2: Navigate to PayPal WebView screen
                                            // Encode the approval URL to handle special characters
                                            val encodedUrl = java.net.URLEncoder.encode(order.approvalUrl, "UTF-8")

                                            val endTimeMillis = startTime + (bookedHours * 3600 * 1000).toLong()
                                            navController.navigate(
                                                "payment_paypal/${order.orderId}/$encodedUrl/$userId/$facilityIndId/$equipmentData/$startTime/$bookedHours"
                                            )

                                            isProcessingPayment = false
                                        } else {
                                            val error = orderResult.exceptionOrNull()
                                            println("❌ Order creation failed: ${error?.message}")
                                            paymentError = error?.message ?: "Failed to create order"
                                            isProcessingPayment = false
                                        }
                                    } catch (e: Exception) {
                                        println("❌ Error: ${e.message}")
                                        paymentError = e.message ?: "An error occurred"
                                        isProcessingPayment = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPayPalSelected && !isProcessingPayment)
                                Color(0xFF6A5ACD)
                            else
                                Color(0xFFCCCCCC)
                        ),
                        enabled = isPayPalSelected && !isProcessingPayment
                    ) {
                        if (isProcessingPayment) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Processing...",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Text(
                                "Proceed to PayPal",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingSummaryCard(viewModel: PaymentViewModel) {
    // Format dates and times
    val startDate = viewModel.startTime?.toDate()
    val endDate = viewModel.endTime?.toDate()

    val formattedDate = startDate?.let {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(it)
    } ?: "Date not specified"

    val formattedTime = if (startDate != null && endDate != null) {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        "${timeFormat.format(startDate)} - ${timeFormat.format(endDate)}"
    } else "Time not specified"

    // Card title
    Text(
        "Booking Details",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = Color(0xFF333333)
    )
    Spacer(modifier = Modifier.height(12.dp))

    // Main booking card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Room name (from child facility)
            Text(
                viewModel.getFacilityIndName(),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF2D3748)
            )

            // Facility type (from parent facility)
            Text(
                viewModel.getFacilityName(),
                fontSize = 14.sp,
                color = Color(0xFF4A5568)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Date
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "📅 ",
                    fontSize = 16.sp
                )
                Text(
                    formattedDate,
                    fontSize = 14.sp,
                    color = Color(0xFF4A5568)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Time
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🕒 ",
                    fontSize = 16.sp
                )
                Text(
                    formattedTime,
                    fontSize = 14.sp,
                    color = Color(0xFF4A5568)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Capacity information
            val capacity = viewModel.getFacilityCapacity()
            if (capacity.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "👥 ",
                        fontSize = 16.sp
                    )
                    Text(
                        text = capacity,
                        fontSize = 13.sp,
                        color = Color(0xFF718096)
                    )
                }
            }

            // Location (if available)
            val location = viewModel.getFacilityLocation()
            if (location.isNotEmpty() && location != "Location not specified") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📍 ",
                        fontSize = 16.sp
                    )
                    Text(
                        text = location,
                        fontSize = 13.sp,
                        color = Color(0xFF718096)
                    )
                }
            }

            // Description (if available)
            val description = viewModel.getFacilityDescription()
            if (description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color(0xFFA0AEC0),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }

    // Show confirmation message
    Spacer(modifier = Modifier.height(12.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "✅",
                fontSize = 20.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                text = "Your booking is ready for confirmation",
                fontSize = 14.sp,
                color = Color(0xFF1565C0),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun CostDetailsCard(viewModel: PaymentViewModel) {
    Text(
        "Cost Breakdown",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = Color(0xFF333333)
    )
    Spacer(modifier = Modifier.height(12.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 1. Facility Booking Fee
            val facilityPrice = viewModel.getFacilityPrice()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Facility Booking",
                        fontSize = 16.sp,
                        color = Color(0xFF2D3748)
                    )
                    Text(
                        "Booking fee for the selected venue",
                        fontSize = 12.sp,
                        color = Color(0xFF718096)
                    )
                }
                Text(
                    String.format("RM %.2f", facilityPrice),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2D3748)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Equipment Rentals (if any)
            if (viewModel.equipmentItems.isNotEmpty()) {
                Text(
                    "Equipment Rentals",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4A5568)
                )
                Spacer(modifier = Modifier.height(8.dp))

                viewModel.equipmentItems.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                item.name,
                                fontSize = 14.sp,
                                color = Color(0xFF2D3748)
                            )
                            Text(
                                "Quantity: ${item.purchaseQuantity} × RM ${"%.2f".format(item.unitPrice)} each",
                                fontSize = 12.sp,
                                color = Color(0xFF718096)
                            )
                        }
                        Text(
                            String.format("RM %.2f", item.unitPrice * item.purchaseQuantity),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D3748)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                // No equipment selected
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Equipment Rentals",
                        fontSize = 14.sp,
                        color = Color(0xFF4A5568)
                    )
                    Text(
                        "No equipment selected",
                        fontSize = 13.sp,
                        color = Color(0xFFA0AEC0),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentMethod(
    isSelected: Boolean,
    onSelect: (Boolean) -> Unit
) {
    Column {
        Text(
            "Payment Method",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF333333)
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Selectable PayPal Card
        Card(
            modifier = Modifier
                .size(width = 100.dp, height = 60.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onSelect(!isSelected) }
                .border(
                    width = if (isSelected) 3.dp else 0.dp,
                    color = if (isSelected) Color(0xFF6A5ACD) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.paypal_logo),
                    contentDescription = "PayPal Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        // Selection indicator text
        if (isSelected) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "✓ PayPal selected",
                fontSize = 14.sp,
                color = Color(0xFF6A5ACD),
                fontWeight = FontWeight.Medium
            )
        }
    }
}// Add these to the bottom of your PaymentScreen.kt file

/**
 * Free Booking Confirmation Screen (no PayPal)
 */
// Add these to the bottom of your PaymentScreen.kt file

/**
 * Free Booking Confirmation Screen (no PayPal)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreeBookingScreen(
    userId: String,
    facilityIndId: String,
    equipmentData: String,
    startTime: Long,
    bookedHours: Double,
    navController: NavController
) {
    val paymentViewModel: PaymentViewModel = viewModel()
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Initialize ViewModel with booking data
    LaunchedEffect(Unit) {
        val startTimestamp = Timestamp(startTime / 1000, ((startTime % 1000) * 1000000).toInt())
        val endTimestamp = if (bookedHours > 0) {
            val endTimeMillis = startTime + (bookedHours * 3600 * 1000).toLong()
            Timestamp(endTimeMillis / 1000, 0)
        } else {
            null
        }
        val equipmentList = if (equipmentData.isNotEmpty() && equipmentData != "NONE") {
            equipmentData.split(",").filter { it.isNotEmpty() }
        } else {
            emptyList()
        }

        paymentViewModel.loadPaymentData(
            userId = userId,
            facilityIndId = facilityIndId,
            equipmentData = equipmentList,
            startTime = startTimestamp,
            bookedHours = bookedHours
        )

        // Wait for data to load, then validate
        kotlinx.coroutines.delay(1000)
        isLoading = false


        // Validate availability
        val validation = validateBookingAvailability(
            facilityIndId = facilityIndId,
            startTime = startTimestamp,
            endTime = endTimestamp
        )

        if (!validation.isValid) {
            validationError = validation.errorMessage
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF483D8B), Color(0xFF6A5ACD))
                )
            )
    ) {
        if (isLoading || isProcessing) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isProcessing) "Creating your booking..." else "Loading booking details...",
                    color = Color.White
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Title
                Text(
                    text = "Free Booking",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Content area with scroll
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp)
                    ) {
                        // No payment required card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE3F2FD)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "✅",
                                    fontSize = 24.sp,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Column {
                                    Text(
                                        text = "No Payment Required",
                                        fontSize = 16.sp,
                                        color = Color(0xFF1565C0),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "This is a free booking - RM 0.00",
                                        fontSize = 14.sp,
                                        color = Color(0xFF1976D2)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Booking Summary
                        BookingSummaryCard(paymentViewModel)
                        Spacer(modifier = Modifier.height(24.dp))

                        // Validation Error
                        if (validationError != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFEBEE)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "❌",
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Booking Not Available",
                                            fontSize = 14.sp,
                                            color = Color(0xFFC62828),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = validationError!!,
                                            fontSize = 12.sp,
                                            color = Color(0xFFC62828)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // General Error Message
                        if (errorMessage != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFEBEE)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "⚠️",
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    Text(
                                        text = errorMessage!!,
                                        fontSize = 12.sp,
                                        color = Color(0xFFC62828)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Spacer(modifier = Modifier.height(100.dp)) // Space for button
                    }
                }

                // Anchored Bottom Button - ALWAYS PURPLE, NO PAYPAL SELECTION
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = {
                            if (validationError == null && !isProcessing) {
                                isProcessing = true
                                errorMessage = null

                                scope.launch {
                                    paymentViewModel.createFreeBooking(
                                        onSuccess = { reservationId ->
                                            android.util.Log.d("FreeBooking", "✅ Booking created: $reservationId")
                                            navController.navigate("booking_success") {
                                                popUpTo("home") { inclusive = true }
                                            }
                                        },
                                        onError = { error ->
                                            android.util.Log.e("FreeBooking", "❌ Error: $error")
                                            errorMessage = error
                                            isProcessing = false
                                        }, userId
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (validationError == null && !isProcessing)
                                Color(0xFF6A5ACD)  // Always purple when valid
                            else
                                Color(0xFFCCCCCC)  // Gray when disabled
                        ),
                        enabled = validationError == null && !isProcessing
                    ) {
                        if (isProcessing) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Creating Booking...",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Text(
                                "Confirm Free Booking",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Validation result data class
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

/**
 * Validate booking availability
 */
suspend fun validateBookingAvailability(
    facilityIndId: String,
    startTime: Timestamp,
    endTime: Timestamp?
): ValidationResult {
    if (endTime == null) {
        return ValidationResult(false, "End time is required")
    }
    return try {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        // 1. Extract parent facility ID from facilityIndId (e.g., S3_1 -> S3)
        val facilityId = if (facilityIndId.contains("_")) {
            facilityIndId.split("_")[0]
        } else {
            facilityIndId
        }

        // 2. Get facility operating hours
        val facilityDoc = db.collection("facility")
            .document(facilityId)
            .get()
            .await()

        if (!facilityDoc.exists()) {
            return ValidationResult(false, "Facility not found")
        }

        val facilityStartTime = facilityDoc.getString("startTime") ?: "0000"
        val facilityEndTime = facilityDoc.getString("endTime") ?: "2359"

        // 3. Check if booking time is within facility operating hours
        val bookingStartHHMM = formatTimestampToHHMM(startTime)
        val bookingEndHHMM = formatTimestampToHHMM(endTime)

        if (bookingStartHHMM < facilityStartTime || bookingEndHHMM > facilityEndTime) {
            return ValidationResult(
                false,
                "Booking time ($bookingStartHHMM - $bookingEndHHMM) is outside facility operating hours ($facilityStartTime - $facilityEndTime)"
            )
        }

        // 4. Check for overlapping bookings
        val reservations = db.collection("reservation")
            .whereEqualTo("facilityID", facilityIndId)
            .get()
            .await()

        for (reservation in reservations.documents) {
            val bookedStartTime = reservation.getTimestamp("bookedTime")
            val bookedEndTime = reservation.getTimestamp("endTime")

            if (bookedStartTime != null && bookedEndTime != null) {
                // Check if requested time overlaps with existing booking
                if (timesOverlap(startTime, endTime, bookedStartTime, bookedEndTime)) {
                    val bookedStart = formatTimestampToHHMM(bookedStartTime)
                    val bookedEnd = formatTimestampToHHMM(bookedEndTime)
                    return ValidationResult(
                        false,
                        "This time slot is already booked ($bookedStart - $bookedEnd)"
                    )
                }
            }
        }

        // All checks passed
        ValidationResult(true)

    } catch (e: Exception) {
        android.util.Log.e("Validation", "Error: ${e.message}")
        ValidationResult(false, "Error validating booking: ${e.message}")
    }
}

/**
 * Format Timestamp to HHMM string
 */
fun formatTimestampToHHMM(timestamp: Timestamp): String {
    val date = timestamp.toDate()
    val format = SimpleDateFormat("HHmm", Locale.getDefault())
    return format.format(date)
}

/**
 * Check if two time ranges overlap
 */
fun timesOverlap(
    start1: Timestamp,
    end1: Timestamp,
    start2: Timestamp,
    end2: Timestamp
): Boolean {
    val start1Ms = start1.toDate().time
    val end1Ms = end1.toDate().time
    val start2Ms = start2.toDate().time
    val end2Ms = end2.toDate().time

    return start1Ms < end2Ms && end1Ms > start2Ms
}

