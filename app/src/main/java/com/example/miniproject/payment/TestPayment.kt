package com.example.miniproject.payment

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Facility Booking",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Test Payment",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Test data: S1_1, Equipment: S1E1-2_S1E2-5",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "Time: 1734700800000 to 1734708000000",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = {
                        // Navigate to payment screen with test data
                        navController.navigate("payment/1/S1_1/S1E1-2_S1E2-5/1734700800000/1.5")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Go to Payment Summary")
                }
            }
        }

        // Free Reservation Test Case
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Test Free Reservation (RM 0)",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Facility with no price + no equipment",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "Test data: S3_1, No equipment",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "Time: 1734700800000 to 1734708000000",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = {
                        // Navigate to payment screen with free facility (no equipment)
                        // Format: payment/{facilityIndId}/{equipmentData}/{startTime}/{endTime}
                        // Use empty string "" for no equipment
                        navController.navigate("payment/1/S3_1/NONE/1734663600000/1.5")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Test Free Booking (No Payment)")
                }
            }
        }

        // Other Test Cases
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Other Test Cases",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
            )

            OutlinedButton(
                onClick = {
                    navController.navigate("payment/1/S2_1/S2E1-3/1734700800000/0.5")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Case 2 (Paid)")
            }

            OutlinedButton(
                onClick = {
                    navController.navigate("payment/S2/S2_1/S2E1-1/1734700800000/2.5")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Test Case 3 (Paid)")
            }
        }
    }
}