package com.example.miniproject.admin.facilityAdmin

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.miniproject.facility.FacilityInd

data class CapacityChange(
    val subVenueId: String,
    val subVenueName: String,
    val customMinNum: Int,
    val customMaxNum: Int
)

@Composable
fun SubVenuesScreen(
    navController: NavController,
    mainFacilityId: String?,
    viewModel: SubVenuesViewModel = viewModel()
) {
    var pendingChanges by remember { mutableStateOf<List<CapacityChange>>(emptyList()) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showAddSubVenueDialog by remember { mutableStateOf(false) }

    LaunchedEffect(mainFacilityId) {
        if (mainFacilityId != null) {
            viewModel.fetchSubVenues(mainFacilityId)
        }
    }

    val mainFacility = viewModel.mainFacility
    val subVenues = viewModel.subVenues

    Scaffold(
        floatingActionButton = {
            if (mainFacilityId != null) {
                FloatingActionButton(
                    onClick = { showAddSubVenueDialog = true },
                    containerColor = Color(0xFF6A5ACD),
                    contentColor = Color.White,
                    modifier = Modifier.padding(bottom = 72.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Sub-Venue"
                    )
                }
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text("Go Back")
                }
                Button(
                    onClick = {
                        if (pendingChanges.isNotEmpty()) {
                            showConfirmDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD))
                ) {
                    Text("Done", color = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF0F0F0))
        ) {
            if (mainFacility != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = mainFacility.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sub-Venues (${subVenues.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (subVenues.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No sub-venues found",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(subVenues) { subVenue ->
                                ExpandableSubVenueCard(
                                    subVenue = subVenue,
                                    viewModel = viewModel,
                                    onChangeAdded = { change ->
                                        pendingChanges = pendingChanges.filter { it.subVenueId != change.subVenueId } + change
                                    },
                                    onChangeRemoved = { subVenueId ->
                                        pendingChanges = pendingChanges.filter { it.subVenueId != subVenueId }
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        // Add Sub-Venue Dialog
        if (showAddSubVenueDialog && mainFacilityId != null) {
            AddSubVenueDialog(
                mainFacilityId = mainFacilityId,
                existingSubVenues = subVenues,
                onDismiss = { showAddSubVenueDialog = false },
                onConfirm = { name, customMin, customMax ->
                    viewModel.addSubVenue(
                        mainFacilityId = mainFacilityId,
                        name = name,
                        customMinNum = customMin,
                        customMaxNum = customMax,
                        onSuccess = {
                            showAddSubVenueDialog = false
                            viewModel.fetchSubVenues(mainFacilityId)
                        },
                        onError = { error ->
                            println("Error adding sub-venue: $error")
                        }
                    )
                }
            )
        }

        // Confirmation Dialog
        if (showConfirmDialog) {
            ConfirmChangesDialog(
                changes = pendingChanges,
                onConfirm = {
                    pendingChanges.forEach { change ->
                        viewModel.updateCustomCapacity(
                            facilityIndId = change.subVenueId,
                            customMinNum = change.customMinNum,
                            customMaxNum = change.customMaxNum,
                            onSuccess = {
                                println("Saved: ${change.subVenueName}")
                            },
                            onError = { error ->
                                println("Error saving ${change.subVenueName}: $error")
                            }
                        )
                    }
                    showConfirmDialog = false
                    navController.popBackStack()
                },
                onDismiss = {
                    showConfirmDialog = false
                }
            )
        }
    }
}

@Composable
fun AddSubVenueDialog(
    mainFacilityId: String,
    existingSubVenues: List<FacilityInd>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, customMin: Int, customMax: Int) -> Unit
) {
    var subVenueName by remember { mutableStateOf("") }
    var hasCustomCapacity by remember { mutableStateOf(false) }
    var isSameCapacity by remember { mutableStateOf(true) }
    var minNumText by remember { mutableStateOf("") }
    var maxNumText by remember { mutableStateOf("") }
    var singleValueText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ADD SUB-VENUE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = subVenueName,
                    onValueChange = { subVenueName = it },
                    label = { Text("Sub-Venue Name") },
                    placeholder = { Text("e.g., Court 1") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Create,
                            contentDescription = "Edit",
                            tint = Color.Gray
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = hasCustomCapacity,
                        onCheckedChange = { hasCustomCapacity = it }
                    )
                    Text("Set custom capacity")
                }

                if (hasCustomCapacity) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = isSameCapacity,
                            onClick = { isSameCapacity = true }
                        )
                        Text("Same capacity")
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(
                            selected = !isSameCapacity,
                            onClick = { isSameCapacity = false }
                        )
                        Text("Different")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isSameCapacity) {
                        OutlinedTextField(
                            value = singleValueText,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() } && it.length <= 3) {
                                    singleValueText = it
                                }
                            },
                            label = { Text("Capacity") },
                            placeholder = { Text("50") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = minNumText,
                                onValueChange = {
                                    if (it.all { char -> char.isDigit() } && it.length <= 3) {
                                        minNumText = it
                                    }
                                },
                                label = { Text("Min") },
                                placeholder = { Text("10") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = "to",
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(top = 8.dp)
                            )

                            OutlinedTextField(
                                value = maxNumText,
                                onValueChange = {
                                    if (it.all { char -> char.isDigit() } && it.length <= 3) {
                                        maxNumText = it
                                    }
                                },
                                label = { Text("Max") },
                                placeholder = { Text("50") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val customMin: Int
                        val customMax: Int

                        if (hasCustomCapacity) {
                            if (isSameCapacity) {
                                val value = singleValueText.toIntOrNull() ?: 0
                                customMin = value
                                customMax = value
                            } else {
                                customMin = minNumText.toIntOrNull() ?: 0
                                customMax = maxNumText.toIntOrNull() ?: 0
                            }
                        } else {
                            customMin = 0
                            customMax = 0
                        }

                        onConfirm(subVenueName, customMin, customMax)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    enabled = subVenueName.isNotBlank()
                ) {
                    Text("Add Sub-Venue", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ConfirmChangesDialog(
    changes: List<CapacityChange>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Confirm Changes", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("You have unsaved changes for the following sub-venues:")
                Spacer(modifier = Modifier.height(8.dp))
                changes.forEach { change ->
                    Text(
                        text = "• ${change.subVenueName}: ${if (change.customMinNum == change.customMaxNum) "${change.customMinNum}" else "${change.customMinNum} - ${change.customMaxNum}"}",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Do you want to save these changes?", fontWeight = FontWeight.Medium)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD))
            ) {
                Text("Save & Exit", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ExpandableSubVenueCard(
    subVenue: FacilityInd,
    viewModel: SubVenuesViewModel = viewModel(),
    onChangeAdded: (CapacityChange) -> Unit = {},
    onChangeRemoved: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

    val originalMinNum = subVenue.customMinNum ?: 0
    val originalMaxNum = subVenue.customMaxNum ?: 0
    val hasOriginalCustomCapacity = originalMinNum != 0 || originalMaxNum != 0

    var customCapacity by remember(subVenue.id, hasOriginalCustomCapacity) {
        mutableStateOf(hasOriginalCustomCapacity)
    }
    var isSameCapacity by remember { mutableStateOf(true) }
    var minNumText by remember { mutableStateOf("") }
    var maxNumText by remember { mutableStateOf("") }
    var singleValueText by remember { mutableStateOf("") }

    var hasPendingChanges by remember { mutableStateOf(false) }

    LaunchedEffect(subVenue.id, subVenue.customMinNum, subVenue.customMaxNum) {
        val min = subVenue.customMinNum ?: 0
        val max = subVenue.customMaxNum ?: 0

        if (min != 0 || max != 0) {
            customCapacity = true

            if (min == max) {
                isSameCapacity = true
                singleValueText = min.toString()
                minNumText = ""
                maxNumText = ""
            } else {
                isSameCapacity = false
                minNumText = min.toString()
                maxNumText = max.toString()
                singleValueText = ""
            }
        } else {
            customCapacity = false
            minNumText = ""
            maxNumText = ""
            singleValueText = ""
        }
        hasPendingChanges = false
    }

    fun checkForChanges() {
        val currentMin: Int
        val currentMax: Int

        if (customCapacity) {
            if (isSameCapacity) {
                val value = singleValueText.toIntOrNull() ?: 0
                currentMin = value
                currentMax = value
            } else {
                currentMin = minNumText.toIntOrNull() ?: 0
                currentMax = maxNumText.toIntOrNull() ?: 0
            }
        } else {
            currentMin = 0
            currentMax = 0
        }

        val changed = currentMin != originalMinNum || currentMax != originalMaxNum
        hasPendingChanges = changed

        if (changed) {
            onChangeAdded(
                CapacityChange(
                    subVenueId = subVenue.id,
                    subVenueName = subVenue.name ?: "Unnamed",
                    customMinNum = currentMin,
                    customMaxNum = currentMax
                )
            )
        } else {
            onChangeRemoved(subVenue.id)
        }
    }

    fun onValueChange(currentValue: String, newValue: String, onTextChanged: (String) -> Unit) {
        if (newValue.length <= 2 && newValue.all { it.isDigit() }) {
            onTextChanged(newValue)
            checkForChanges()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = subVenue.name ?: "Unnamed", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        if (hasPendingChanges) {
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(
                                text = "•",
                                fontSize = 18.sp,
                                color = Color(0xFFFF6B6B),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(text = "ID: ${subVenue.id}", fontSize = 12.sp, color = Color.Gray)
                    if (hasOriginalCustomCapacity) {
                        val min = subVenue.customMinNum ?: 0
                        val max = subVenue.customMaxNum ?: 0
                        Text(
                            text = "Custom Capacity: ${if (min == max) "$min" else "$min - $max"}",
                            fontSize = 11.sp,
                            color = Color(0xFF6A5ACD),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        modifier = Modifier.rotate(rotationAngle)
                    )
                }
            }

            if (expanded) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Custom Capacity", fontWeight = FontWeight.Bold)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Checkbox(
                            checked = customCapacity,
                            onCheckedChange = { checked ->
                                customCapacity = checked
                                if (!checked) {
                                    minNumText = ""
                                    maxNumText = ""
                                    singleValueText = ""
                                }
                                checkForChanges()
                            }
                        )
                        Text("Modify custom capacity")
                    }

                    if (customCapacity) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 32.dp)
                        ) {
                            RadioButton(
                                selected = isSameCapacity,
                                onClick = {
                                    isSameCapacity = true
                                    if (maxNumText.isNotEmpty() && singleValueText.isEmpty()) {
                                        singleValueText = maxNumText
                                    }
                                    checkForChanges()
                                }
                            )
                            Text("Same")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 32.dp)
                        ) {
                            RadioButton(
                                selected = !isSameCapacity,
                                onClick = {
                                    isSameCapacity = false
                                    if (singleValueText.isNotEmpty() && minNumText.isEmpty()) {
                                        minNumText = singleValueText
                                        maxNumText = singleValueText
                                    }
                                    checkForChanges()
                                }
                            )
                            Text("Different")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isSameCapacity) {
                            OutlinedTextField(
                                value = singleValueText,
                                onValueChange = {
                                    onValueChange(singleValueText, it) { updatedValue ->
                                        singleValueText = updatedValue
                                    }
                                },
                                label = { Text("Capacity") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 32.dp)
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(start = 32.dp)
                            ) {
                                OutlinedTextField(
                                    value = minNumText,
                                    onValueChange = {
                                        onValueChange(minNumText, it) { updatedValue ->
                                            minNumText = updatedValue
                                            val newMin = updatedValue.toIntOrNull() ?: 0
                                            val maxNum = maxNumText.toIntOrNull() ?: newMin
                                            if (newMin > maxNum) {
                                                maxNumText = newMin.toString()
                                            }
                                        }
                                    },
                                    label = { Text("Min") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )

                                Text("to")

                                OutlinedTextField(
                                    value = maxNumText,
                                    onValueChange = {
                                        onValueChange(maxNumText, it) { updatedValue ->
                                            maxNumText = updatedValue
                                            val newMax = updatedValue.toIntOrNull() ?: 0
                                            val minNum = minNumText.toIntOrNull() ?: 0
                                            if (newMax < minNum) {
                                                minNumText = newMax.toString()
                                            }
                                        }
                                    },
                                    label = { Text("Max") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Conditional Close", fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = true, onClick = { /*TODO*/ })
                        Text("All day")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = false, onClick = { /*TODO*/ })
                        Text("Partial timeslot")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("Remarks") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}