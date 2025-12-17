
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
fun AdminStaffScreen(
    navController: NavController,
    viewModel: AdminStaffViewModel = viewModel()
) {
    val searchText by viewModel.searchText.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddUserDialog by remember { mutableStateOf(false) }

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
                Icon(Icons.Default.Add, contentDescription = "Add Staff", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            SearchScreen(
                title = "Staff",
                searchPlaceholder = "Enter staff display ID (e.g., S1, S2, S3)...",
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
                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF6A5ACD))
                            }
                        }
                        searchResults == null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Enter a staff display ID to search",
                                    color = Color.Gray
                                )
                            }
                        }
                        searchResults?.isEmpty() == true && searchText.isNotBlank() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No staff found with ID: $searchText",
                                    color = Color.Gray
                                )
                            }
                        }
                        else -> {
                            SearchResultList(
                                results = searchResults,
                                onEditItem = { userId ->
                                    navController.navigate("user_information/$userId/staff")
                                },
                                onDeleteItem = { userId ->
                                    viewModel.deleteUser(userId)
                                }
                            )
                        }
                    }
                }
            )

            AddUserDialog(
                userType = "staff",
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
                                snackbarHostState.showSnackbar("Staff added successfully")
                                showAddUserDialog = false
                                viewModel.clearSearchResults()
                            }
                        },
                        onError = { errorMessage ->
                            scope.launch {
                                snackbarHostState.showSnackbar("Failed to add staff: $errorMessage")
                            }
                        }
                    )
                },
                isLoading = false,
                error = null,
                generateDisplayId = { userType ->
                    viewModel.generateStaffDisplayId()
                }
            )
        }
    }
}