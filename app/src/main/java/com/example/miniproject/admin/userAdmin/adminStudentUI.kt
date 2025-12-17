package com.example.miniproject.admin.userAdmin

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.miniproject.components.SearchResultList
import com.example.miniproject.components.SearchScreen

@Composable
fun AdminStudentScreen(
    navController: NavController,
    viewModel: AdminStudentViewModel = viewModel()
) {
    val searchText by viewModel.searchText.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Navigate to Add Student Screen */ },
                containerColor = Color(0xFF6A5ACD)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Student", tint = Color.White)
            }
        }
    ) { paddingValues ->
        SearchScreen(
            title = "Student",
            searchPlaceholder = "Enter student display ID...", // Updated placeholder
            searchText = searchText,
            onSearchTextChange = viewModel::onSearchTextChange,
            searchHistory = searchHistory,
            onClearHistoryItem = viewModel::onClearHistoryItem,
            onClearAllHistory = viewModel::clearAllHistory,
            onSearch = { 
                // Add the final search term to history when the user clicks the search icon
                viewModel.addSearchToHistory(searchText)
                // The search is already being performed as the user types.
            },
            onBackClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            content = { 
                // Display the search results from the ViewModel.
                SearchResultList(
                    results = searchResults,
                    onEditItem = {
                        // TODO: Navigate to an edit screen for the user with this ID
                    },
                    onDeleteItem = {
                        // TODO: Implement user deletion logic
                    }
                )
            }
        )
    }
}
