package com.example.miniproject.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.miniproject.R
import com.example.miniproject.auth.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController? = null) {
    val authViewModel: AuthViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var selectedUserType by remember { mutableStateOf("Student") }
    var showLoginForm by remember { mutableStateOf(false) }

    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val userData by authViewModel.userData.collectAsState()

    // Define colors for each user type
    val studentColor = Color(0xFF26296F)
    val staffColor = Color(0xFF262B98)
    val adminColor = Color(0xFF000000)

    // Navigate to home when user is authenticated
    LaunchedEffect(userData) {
        if (userData != null && !isLoading) {
            navController?.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // Login Portal Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Login Portal",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = studentColor
            )
        }

        // Uniserve Logo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.uniserve),
                contentDescription = "Uniserve Logo",
                modifier = Modifier.size(120.dp)
            )
        }

        // Welcome text under the logo
        Text(
            text = "Welcome to UniServe",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = studentColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Subtitle text
        Text(
            text = "Log in to book your facility",
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        )

        // Error message display
        errorMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Error",
                        tint = Color.Red
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message,
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }
            }
        }

        if (showLoginForm) {
            // Login Form
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Selected user type indicator
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (selectedUserType) {
                            "Student" -> studentColor.copy(alpha = 0.1f)
                            "Staff" -> staffColor.copy(alpha = 0.1f)
                            "Admin" -> adminColor.copy(alpha = 0.1f)
                            else -> studentColor.copy(alpha = 0.1f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    when (selectedUserType) {
                                        "Student" -> studentColor
                                        "Staff" -> staffColor
                                        "Admin" -> adminColor
                                        else -> studentColor
                                    },
                                    shape = MaterialTheme.shapes.small
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(
                                    id = when (selectedUserType) {
                                        "Student" -> R.drawable.student
                                        "Staff" -> R.drawable.staff
                                        "Admin" -> R.drawable.admin
                                        else -> R.drawable.student
                                    }
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "$selectedUserType Login",
                                fontWeight = FontWeight.Bold,
                                color = when (selectedUserType) {
                                    "Student" -> studentColor
                                    "Staff" -> staffColor
                                    "Admin" -> adminColor
                                    else -> studentColor
                                }
                            )
                            Text(
                                text = "Continue with your credentials",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = "Email")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = "Password")
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (showPassword) {
                        PasswordVisualTransformation()
                    } else {
                        PasswordVisualTransformation()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Login Button
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            authViewModel.errorMessage.value = "Please fill in all fields"
                            return@Button
                        }
                        authViewModel.signIn(email, password)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (selectedUserType) {
                            "Student" -> studentColor
                            "Staff" -> staffColor
                            "Admin" -> adminColor
                            else -> studentColor
                        }
                    ),
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Text(
                            text = "Login as $selectedUserType",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Back to user type selection
                TextButton(
                    onClick = { showLoginForm = false }
                ) {
                    Text("← Back to user type selection")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sign up link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Don't have an account? ")
                    Text(
                        text = "Sign Up",
                        color = Color(0xFF26296F),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(
                            enabled = !isLoading,
                            onClick = {
                                navController?.navigate("signup")
                            }
                        )
                    )
                }
            }
        } else {
            // User type selection cards
            val userTypes = listOf(
                UserTypeCardData("Student", studentColor, R.drawable.student,
                    "Access student facilities and bookings"),
                UserTypeCardData("Staff", staffColor, R.drawable.staff,
                    "Book facilities for departmental use"),
                UserTypeCardData("Admin", adminColor, R.drawable.admin,
                    "Manage all facilities and users")
            )

            userTypes.forEach { userType ->
                UserTypeCard(
                    userType = userType,
                    onClick = {
                        selectedUserType = userType.name
                        showLoginForm = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign up option
            Text(
                text = "New to UniServe?",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Create an Account",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF26296F),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController?.navigate("signup")
                    }
                    .padding(vertical = 8.dp)
            )
        }

        // Add extra space at the bottom
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// Keep your existing UserTypeCardData and UserTypeCard composables the same
data class UserTypeCardData(
    val name: String,
    val color: Color,
    val iconRes: Int,
    val description: String
)

@Composable
private fun UserTypeCard(
    userType: UserTypeCardData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = userType.color
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(userType.color, shape = MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = userType.iconRes),
                        contentDescription = "${userType.name} Icon",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Title and description
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = userType.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = userType.color
                    )
                    Text(
                        text = userType.description,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Arrow icon
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Go to ${userType.name} login",
                    tint = userType.color
                )
            }
        }
    }
}