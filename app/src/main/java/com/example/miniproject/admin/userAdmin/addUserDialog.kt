package com.example.miniproject.admin.userAdmin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AddUserDialog(
    userType: String, // "student" or "staff"
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onAddUser: (name: String, email: String, password: String, displayId: String) -> Unit,
    isLoading: Boolean = false,
    error: String? = null,
    generateDisplayId: suspend (String) -> String // New parameter to generate display ID
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var displayId by remember { mutableStateOf("") }
    var isGeneratingId by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // When dialog opens, generate display ID
    LaunchedEffect(showDialog) {
        if (showDialog) {
            isGeneratingId = true
            displayId = generateDisplayId(userType)
            isGeneratingId = false
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { if (!isLoading && !isGeneratingId) onDismiss() },
            title = { Text("Add New ${userType.replaceFirstChar { it.uppercase() }}") },
            text = {
                Column {
                    if (error != null) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Display ID field (read-only)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = displayId,
                            onValueChange = { /* Read-only */ },
                            label = { Text("Display ID (Auto-generated)") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Badge,
                                    contentDescription = "Display ID",
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (isGeneratingId) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            enabled = false,
                            readOnly = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Refresh button
                        IconButton(
                            onClick = {
                                if (!isGeneratingId && !isLoading) {
                                    scope.launch {
                                        isGeneratingId = true
                                        displayId = generateDisplayId(userType)
                                        isGeneratingId = false
                                    }
                                }
                            },
                            enabled = !isGeneratingId && !isLoading
                        ) {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = "Generate New ID",
                                tint = if (isGeneratingId || isLoading) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = name.isBlank()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = email.isBlank()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        isError = password.isNotBlank() && password.length < 6
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Confirm Password field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Confirm Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        isError = confirmPassword.isNotBlank() && password != confirmPassword
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Validate inputs
                        if (displayId.isBlank()) {
                            return@Button
                        }
                        if (name.isBlank()) {
                            return@Button
                        }
                        if (email.isBlank()) {
                            return@Button
                        }
                        if (password.isBlank()) {
                            return@Button
                        }
                        if (password != confirmPassword) {
                            return@Button
                        }
                        if (password.length < 6) {
                            return@Button
                        }

                        onAddUser(name, email, password, displayId)
                    },
                    enabled = !isLoading && !isGeneratingId &&
                            displayId.isNotBlank() &&
                            name.isNotBlank() &&
                            email.isNotBlank() &&
                            password.isNotBlank() &&
                            password == confirmPassword &&
                            password.length >= 6
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Add ${userType.replaceFirstChar { it.uppercase() }}")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onDismiss() },
                    enabled = !isLoading && !isGeneratingId
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}