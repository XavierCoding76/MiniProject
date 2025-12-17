package com.example.miniproject.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.example.miniproject.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    title: String,
    searchPlaceholder: String,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    searchHistory: List<String>,
    onClearHistoryItem: (String) -> Unit,
    onClearAllHistory: () -> Unit,
    onSearch: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit // Slot for search results
) {
    var showHistory by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF483D8B), Color(0xFF6A5ACD))
                )
            )
    ) {
        // Header
        CenterAlignedTopAppBar(
            title = { Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 32.sp) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
            modifier = Modifier.padding(top = 40.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            // Box to anchor the search bar and the dropdown menu
            Box {
                TextField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    placeholder = { Text(searchPlaceholder) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                showHistory = true
                            }
                        }
                        .clickable { // Explicitly show history on any click
                            showHistory = true
                        },
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
                    trailingIcon = {
                        IconButton(onClick = {
                            onSearch()
                            focusManager.clearFocus()
                        }) {
                            Icon(Icons.Filled.Search, contentDescription = "Perform Search")
                        }
                    }
                )

                DropdownMenu(
                    expanded = showHistory && searchHistory.isNotEmpty(),
                    onDismissRequest = { showHistory = false }, // Just hide the dropdown
                    properties = PopupProperties(focusable = false),
                    modifier = Modifier.fillMaxWidth().background(Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onClearAllHistory) {
                            Text("Clear all", color = Color(0xFF6A5ACD), fontWeight = FontWeight.Bold)
                        }
                    }
                    searchHistory.take(5).forEach {
                        HistoryItem(text = it, onClear = { onClearHistoryItem(it) })
                    }
                }
            }
            
            // Content slot for displaying search results
            Box(modifier = Modifier.padding(top = 16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun HistoryItem(text: String, onClear: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.history),
                    contentDescription = "History Icon",
                    modifier = Modifier.size(24.dp)
                )
                Text(text)
            }
            IconButton(onClick = onClear) {
                Icon(Icons.Filled.Clear, contentDescription = "Clear History Item", tint = Color.Gray)
            }
        }
        HorizontalDivider()
    }
}
