package com.example.miniproject.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data class to hold the information for each dashboard item
data class DashboardItemData(
    val title: String,
    @DrawableRes val imageResId: Int,
    val backgroundColor: Color,
    val destinationRoute: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(
    title: String,
    items: List<DashboardItemData>,
    onItemClick: (DashboardItemData) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Outermost Box with the purple gradient background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF483D8B), // DarkSlateBlue
                        Color(0xFF6A5ACD)  // SlateBlue
                    )
                )
            )
    ) {
        // White content area with rounded top corners that contains the items
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 140.dp) // Pushed the white card down further
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
                .padding(16.dp), // General padding for the content inside the white card
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically), // Increased spacing
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items.forEach { item ->
                DashboardItem(
                    item = item,
                    onClick = { onItemClick(item) }
                )
            }
        }

        // The TopAppBar now lives inside the universal Dashboard component
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp // Larger font size as requested
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            ),
            // This modifier pushes the entire app bar down from the top of the screen
            modifier = Modifier.padding(top = 40.dp)
        )
    }
}

@Composable
fun DashboardItem(
    item: DashboardItemData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            Image(
                painter = painterResource(id = item.imageResId),
                contentDescription = null, // Decorative
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth(0.4f) // Image takes up 40% of the width
                    .fillMaxHeight()
                    .align(Alignment.CenterStart)
            )

            // Gradient Overlay and Text
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                item.backgroundColor.copy(alpha = 0.5f),
                                item.backgroundColor
                            ),
                            startX = 150f // Start gradient after the image
                        )
                    )
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterEnd) // Align text to the end (right)
                        .padding(end = 24.dp) // Add padding from the end
                )
            }
        }
    }
}
