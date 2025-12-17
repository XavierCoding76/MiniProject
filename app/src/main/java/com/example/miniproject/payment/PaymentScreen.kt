package com.example.miniproject.payment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.miniproject.R
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PaymentScreen(
    paymentId: String = "P1", // Using P1 as default
    viewModel: PaymentViewModel = viewModel()
) {
    LaunchedEffect(paymentId) {
        viewModel.loadPaymentData(paymentId)
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
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp)
            ) {
                Text(
                    text = "Summary",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(Color.White)
                        .padding(24.dp)
                ) {
                    BookingSummaryCard(viewModel)
                    Spacer(modifier = Modifier.height(24.dp))
                    PaymentDetailsCard(viewModel)
                    Spacer(modifier = Modifier.height(24.dp))

                    val totalAmount = viewModel.payment?.get("totalAmount") as? Double ?: 0.0
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total:", fontWeight = FontWeight.Bold)
                        Text(String.format("RM %.2f", totalAmount), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    PaymentMethod()

                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { /* TODO */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD))
                    ) {
                        Text("Proceed", color = Color.White, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun BookingSummaryCard(viewModel: PaymentViewModel) {
    val facility = viewModel.facility
    val reservation = viewModel.reservation

    val startDate = (reservation?.get("startTime") as? Timestamp)?.toDate()
    val endDate = (reservation?.get("endTime") as? Timestamp)?.toDate()
    val formattedDate = startDate?.let { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(it) } ?: ""
    val formattedTime = if (startDate != null && endDate != null) {
        "${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(startDate)} - ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(endDate)}"
    } else ""

    Text("Booking Summary", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(facility?.get("name") as? String ?: "", fontWeight = FontWeight.Bold)
                Text(formattedDate)
                Text(formattedTime)
            }
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background), // Replace with facility image
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun PaymentDetailsCard(viewModel: PaymentViewModel) {
    Text("Payment Summary", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Venue Reservation (assuming it's free or its cost is included elsewhere)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Venue Reservation")
                Text("RM 0.00")
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Equipment Rentals
            viewModel.paymentDetails.forEach { detail ->
                val equipment = viewModel.equipmentDetails[detail["equipmentId"] as String]
                val name = equipment?.get("name") as? String ?: "Unknown Item"
                val price = equipment?.get("price") as? Double ?: 0.0
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(name)
                    Text(String.format("RM %.2f", price))
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun PaymentMethod() {
    Text("Payment Method", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = { /* TODO: Handle PayPal selection */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0)) // Light gray
    ) {
        Image(
            painter = painterResource(id = R.drawable.paypal_logo), // Add paypal_logo to your drawables
            contentDescription = "PayPal",
            modifier = Modifier.height(24.dp)
        )
    }
}
