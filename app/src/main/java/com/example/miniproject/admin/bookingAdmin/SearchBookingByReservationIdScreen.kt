package com.example.miniproject.admin.bookingAdmin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.miniproject.components.SearchScreen

@Composable
fun SearchBookingByReservationIdScreen(navController: NavController, viewModel: SearchBookingByReservationIdViewModel = viewModel()) {
    val searchText by viewModel.searchText.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()

    SearchScreen(
        title = "Reservation ID",
        searchPlaceholder = "Enter by reservation id...",
        searchText = searchText,
        onSearchTextChange = viewModel::onSearchTextChange,
        searchHistory = searchHistory,
        onClearHistoryItem = viewModel::onClearHistoryItem,
        onClearAllHistory = viewModel::clearAllHistory,
        onSearch = { viewModel.addSearchToHistory(searchText) },
        onBackClick = { navController.popBackStack() },
        content = { /* TODO: Display search results here */ }
    )
}
