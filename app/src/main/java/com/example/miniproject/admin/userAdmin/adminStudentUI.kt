package com.example.miniproject.admin.userAdmin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.miniproject.components.SearchResultList
import com.example.miniproject.components.SearchScreen
import kotlinx.coroutines.launch

@Composable
fun AdminStudentScreen(
    navController: NavController,
    viewModel: AdminStudentViewModel = viewModel()
) {
    val searchText by viewModel.searchText.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // State for Add User Dialog
    var showAddUserDialog by remember { mutableStateOf(false) }

    // Show error in snackbar
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddUserDialog = true
                },
                containerColor = Color(0xFF6A5ACD),
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Student",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            SearchScreen(
                title = "Student Management",
                searchPlaceholder = "Search by ID, name, or email...",
                searchText = searchText,
                onSearchTextChange = viewModel::onSearchTextChange,
                searchHistory = searchHistory,
                onClearHistoryItem = viewModel::onClearHistoryItem,
                onClearAllHistory = viewModel::clearAllHistory,
                onSearch = {
                    viewModel.onSearch()
                },
                onBackClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                content = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        when {
                            isLoading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color(0xFF6A5ACD))
                                }
                            }

                            searchResults == null && searchText.isBlank() -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Enter student ID, name, or email to search",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }

                            searchResults?.isEmpty() == true -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No students found for: $searchText",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }

                            else -> {
                                SearchResultList(
                                    results = searchResults ?: emptyList(),
                                    onEditItem = { userId ->
                                        navController.navigate("user_information/$userId/student")
                                    },
                                    onDeleteItem = { userId ->
                                        viewModel.deleteUser(userId)
                                    }
                                )
                            }
                        }
                    }
                }
            )

            // Add User Dialog
            AddUserDialog(
                userType = "student",
                showDialog = showAddUserDialog,
                onDismiss = { showAddUserDialog = false },
                onAddUser = { name, email, password, displayId ->
                    viewModel.addUser(
                        name = name,
                        email = email,
                        password = password,
                        displayId = displayId,
                        onSuccess = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Student added successfully")
                                showAddUserDialog = false
                                viewModel.clearSearchResults()
                            }
                        },
                        onError = { errorMessage ->
                            scope.launch {
                                snackbarHostState.showSnackbar("Failed to add student: $errorMessage")
                            }
                        }
                    )
                },
                isLoading = false,
                error = null,
                generateDisplayId = { userType ->
                    viewModel.generateStudentDisplayId()
                }
            )
        }
    }
}