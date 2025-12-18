package com.example.miniproject.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.text.style.TextAlign
import com.example.miniproject.user.UserTypeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val primaryColor = Color(0xFF5553DC)
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "UniServe Home",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    TextButton(
                        onClick = { showLogoutDialog = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text("Logout")
                    }
                }
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Welcome text
                    Text(
                        text = "Welcome to UniServe",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Additional info
                    Text(
                        text = "Your facility booking platform",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Main action card
                    Card(
                        modifier = Modifier
                            .width(320.dp)
                            .padding(horizontal = 16.dp),
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Quick Actions",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            // Profile button
                            Button(
                                onClick = {
                                    // Navigate to profile
                                    // navController.navigate("profile")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "View Profile",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Facilities button
                            Button(
                                onClick = {
                                    // Navigate to facilities
                                    // navController.navigate("facilities")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor.copy(alpha = 0.85f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Browse Facilities",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Bookings button
                            Button(
                                onClick = {
                                    // Navigate to bookings
                                    // navController.navigate("bookings")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor.copy(alpha = 0.7f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "My Bookings",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Divider(
                                color = Color.LightGray.copy(alpha = 0.5f),
                                thickness = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Logout button inside card
                            Button(
                                onClick = { showLogoutDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF44336),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Logout",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Stats section
                    Row(
                        modifier = Modifier
                            .width(320.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Stats Card 1
                        Card(
                            modifier = Modifier
                                .width(100.dp)
                                .height(100.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = primaryColor.copy(alpha = 0.1f)
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "5",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                                Text(
                                    "Bookings",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Stats Card 2
                        Card(
                            modifier = Modifier
                                .width(100.dp)
                                .height(100.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = primaryColor.copy(alpha = 0.1f)
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "3",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                                Text(
                                    "Facilities",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Stats Card 3
                        Card(
                            modifier = Modifier
                                .width(100.dp)
                                .height(100.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = primaryColor.copy(alpha = 0.1f)
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "2",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                                Text(
                                    "Upcoming",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Footer text
                    Text(
                        text = "UniServe v1.0.0",
                        fontSize = 12.sp,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                }
            }
        }
    )

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    "Confirm Logout",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            },
            text = {
                Text(
                    "Are you sure you want to logout from your account?",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Perform logout
                        UserTypeManager.clearUserType()
                        navController.navigate("login") {
                            // Clear back stack and navigate to login
                            popUpTo("login") { inclusive = false }
                        }
                        showLogoutDialog = false
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        "Logout",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(
                        "Cancel",
                        fontSize = 16.sp,
                        color = primaryColor
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}