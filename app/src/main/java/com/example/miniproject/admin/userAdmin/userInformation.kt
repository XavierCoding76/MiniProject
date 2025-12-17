package com.example.miniproject.admin.userAdmin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.miniproject.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInformationScreen(
    navController: NavController,
    userId: String,
    userType: String, // "staff" or "student"
    viewModel: UserInformationViewModel = viewModel()
) {
    val userState by viewModel.userState.collectAsState()
    val editState by viewModel.editState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Load user data when screen opens
    LaunchedEffect(userId, userType) {
        viewModel.loadUser(userId, userType)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("${userType.replaceFirstChar { it.uppercase() }} Information")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6A5ACD),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            when {
                userState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF6A5ACD)
                    )
                }
                userState.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = userState.error ?: "Unknown error",
                            color = Color.Red,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD))
                        ) {
                            Text("Go Back")
                        }
                    }
                }
                userState.user != null -> {
                    val user = userState.user!!

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .fillMaxHeight(0.85f)
                            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                            .background(Color.White)
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // User Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = user.name,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "ID: ${user.displayId}",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }

                            // Role Badge
                            Surface(
                                color = when (user.role.lowercase()) {
                                    "staff" -> Color(0xFF2196F3)
                                    "admin" -> Color(0xFFFF9800)
                                    else -> Color(0xFF4CAF50)
                                },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = user.role.replaceFirstChar { it.uppercase() },
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // User Information Cards
                        UserInfoCard(
                            icon = Icons.Filled.Person,
                            label = "Name",
                            value = user.name
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        UserInfoCard(
                            icon = Icons.Filled.Email,
                            label = "Email",
                            value = user.email
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        UserInfoCard(
                            icon = Icons.Filled.Badge,
                            label = "Display ID",
                            value = user.displayId
                        )

                        if (user.createdAt != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            UserInfoCard(
                                icon = Icons.Filled.CalendarToday,
                                label = "Created At",
                                value = user.createdAt
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Action Buttons
                        Text(
                            text = "Actions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Edit Button
                        OutlinedButton(
                            onClick = {
                                viewModel.showEditDialog(user)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF6A5ACD)
                            )
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit User Information")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Delete Button
                        Button(
                            onClick = {
                                viewModel.showDeleteConfirmation()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF5252)
                            )
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete User", color = Color.White)
                        }
                    }

                    // Delete Confirmation Dialog
                    if (userState.showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { viewModel.hideDeleteConfirmation() },
                            title = { Text("Delete User?") },
                            text = {
                                Text("Are you sure you want to delete ${user.name}? This action cannot be undone.")
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.deleteUser(
                                            userId = user.id, // Use the actual document ID
                                            userType = userType,
                                            onSuccess = {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("User deleted successfully")
                                                    navController.popBackStack()
                                                }
                                            },
                                            onError = { error ->
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(error)
                                                }
                                            }
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFF5252)
                                    )
                                ) {
                                    Text("Delete", color = Color.White)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { viewModel.hideDeleteConfirmation() }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    // Edit User Dialog
                    if (editState.showEditDialog) {
                        AlertDialog(
                            onDismissRequest = { viewModel.hideEditDialog() },
                            title = { Text("Edit User Information") },
                            text = {
                                Column {
                                    if (editState.error != null) {
                                        Text(
                                            text = editState.error!!,
                                            color = Color.Red,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }

                                    // Name field
                                    OutlinedTextField(
                                        value = editState.name,
                                        onValueChange = { viewModel.updateName(it) },
                                        label = { Text("Name") },
                                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Name") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = editState.name.isBlank()
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Email field
                                    OutlinedTextField(
                                        value = editState.email,
                                        onValueChange = { viewModel.updateEmail(it) },
                                        label = { Text("Email") },
                                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = editState.email.isBlank()
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Password change toggle
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.togglePasswordSection() }
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Checkbox(
                                            checked = editState.showPasswordSection,
                                            onCheckedChange = { viewModel.togglePasswordSection() }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Change Password")
                                    }

                                    // Password fields (conditional)
                                    if (editState.showPasswordSection) {
                                        Spacer(modifier = Modifier.height(16.dp))

                                        OutlinedTextField(
                                            value = editState.password,
                                            onValueChange = { viewModel.updatePassword(it) },
                                            label = { Text("New Password") },
                                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Password") },
                                            singleLine = true,
                                            visualTransformation = PasswordVisualTransformation(),
                                            modifier = Modifier.fillMaxWidth(),
                                            isError = editState.password.isNotBlank() && editState.password.length < 6
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        OutlinedTextField(
                                            value = editState.confirmPassword,
                                            onValueChange = { viewModel.updateConfirmPassword(it) },
                                            label = { Text("Confirm Password") },
                                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Confirm Password") },
                                            singleLine = true,
                                            visualTransformation = PasswordVisualTransformation(),
                                            modifier = Modifier.fillMaxWidth(),
                                            isError = editState.confirmPassword.isNotBlank() &&
                                                    editState.password != editState.confirmPassword
                                        )
                                    }
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.updateUser(
                                            userId = user.id,
                                            onSuccess = {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("User updated successfully")
                                                    // Reload user data
                                                    viewModel.loadUser(userId, userType)
                                                }
                                            },
                                            onError = { error ->
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(error)
                                                }
                                            }
                                        )
                                    },
                                    enabled = !editState.isLoading &&
                                            editState.name.isNotBlank() &&
                                            editState.email.isNotBlank() &&
                                            (!editState.showPasswordSection ||
                                                    (editState.password.isNotBlank() &&
                                                            editState.password == editState.confirmPassword &&
                                                            editState.password.length >= 6))
                                ) {
                                    if (editState.isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                    } else {
                                        Text("Save Changes")
                                    }
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { viewModel.hideEditDialog() },
                                    enabled = !editState.isLoading
                                ) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
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
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF6A5ACD),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}