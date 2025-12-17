package com.example.miniproject.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A generic data class to hold the information for a single item in a search result list.
 * @param id The unique ID of the item, used for edit/delete actions.
 * @param title The main text to display for the item (e.g., "Booking ID: B22").
 */
data class SearchResultItemData(
    val id: String,
    val title: String
)

/**
 * A reusable composable that displays a list of search results or a "No records found" message.
 * @param results The list of items to display. If null, nothing is shown. If empty, the "No records" message is shown.
 * @param onEditItem Callback triggered when the edit icon is clicked, providing the item's ID.
 * @param onDeleteItem Callback triggered when the delete icon is clicked, providing the item's ID.
 */
@Composable
fun SearchResultList(
    results: List<SearchResultItemData>?,
    onEditItem: (String) -> Unit,
    onDeleteItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            results == null -> {
                // Initial state, before a search is performed. Show nothing.
            }
            results.isEmpty() -> {
                // Search was performed, but no results were found.
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No records found!", color = Color.Gray)
                }
            }
            else -> {
                // Search was successful, display the list of results.
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(results) { item ->
                        SearchResultItem(
                            item = item,
                            onEditClick = { onEditItem(item.id) },
                            onDeleteClick = { onDeleteItem(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    item: SearchResultItemData,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)), // Light gray background
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = item.title)
            Spacer(modifier = Modifier.weight(1f))
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Build, contentDescription = "Edit")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}
