package com.example.miniproject.admin.bookingAdmin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.miniproject.components.SearchScreen
import com.example.miniproject.components.SearchResultItemData
import com.example.miniproject.components.SearchResultList

@Composable
fun SearchBookingByUserScreen(navController: NavController, viewModel: SearchBookingByUserViewModel = viewModel()) {
    val searchText by viewModel.searchText.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    SearchScreen(
        title = "Bookings by User",
        searchPlaceholder = "Enter by user...",
        searchText = searchText,
        onSearchTextChange = viewModel::onSearchTextChange,
        searchHistory = searchHistory,
        onClearHistoryItem = viewModel::onClearHistoryItem,
        onClearAllHistory = viewModel::clearAllHistory,
        onSearch = { viewModel.addSearchToHistory(searchText) },
        onBackClick = { navController.popBackStack() },
        content = {
            // Map the specific Reservation list to the generic SearchResultItemData list
            val mappedResults = searchResults?.map {
                SearchResultItemData(
                    id = it.id,
                    title = "Booking ID: ${it.id.take(6).uppercase()}"
                )
            }

            // Use the new reusable SearchResultList component to display the results
            SearchResultList(
                results = mappedResults,
                onEditItem = { /* TODO: Navigate to edit screen */ },
                onDeleteItem = { /* TODO: Call ViewModel to delete */ }
            )
        }
    )
}
