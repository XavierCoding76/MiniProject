package com.example.miniproject.admin.facilityAdmin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.miniproject.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFacilityScreen(
    navController: NavController,
    buildingType: String,
    viewModel: FacilityDetailViewModel = viewModel()
) {
    var facilityName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var minCapacity by remember { mutableStateOf("") }
    var maxCapacity by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("0800") }
    var endTime by remember { mutableStateOf("2200") }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var editingTimeField by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Track which fields have been touched (focused)
    var facilityNameTouched by remember { mutableStateOf(false) }
    var descriptionTouched by remember { mutableStateOf(false) }
    var locationTouched by remember { mutableStateOf(false) }
    var minCapacityTouched by remember { mutableStateOf(false) }
    var maxCapacityTouched by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val buildingTitle = when (buildingType) {
        "C" -> "Clubhouse"
        "S" -> "Sports Complex"
        "CC" -> "CITC"
        "AL" -> "Arena TARUMT"
        "AS" -> "Arena TARUMT"
        "L" -> "Library"
        else -> "Facility"
    }

    if (showTimePickerDialog) {
        TimePickerDialog(
            initialTime = if (editingTimeField == "startTime") startTime else endTime,
            onDismiss = { showTimePickerDialog = false },
            onSave = { newTime ->
                if (editingTimeField == "startTime") {
                    startTime = newTime
                } else {
                    endTime = newTime
                }
                showTimePickerDialog = false
            }
        )
    }

    if (showConfirmDialog) {
        ConfirmAddFacilityDialog(
            facilityData = mapOf(
                "name" to facilityName,
                "description" to description,
                "location" to location,
                "minNum" to minCapacity,
                "maxNum" to maxCapacity,
                "startTime" to startTime,
                "endTime" to endTime
            ),
            onDismiss = { showConfirmDialog = false },
            onConfirm = {
                isLoading = true
                showConfirmDialog = false

                viewModel.addFacility(
                    buildingType = buildingType,
                    facilityData = mapOf(
                        "name" to facilityName,
                        "description" to description,
                        "location" to location,
                        "minNum" to minCapacity,
                        "maxNum" to maxCapacity,
                        "startTime" to startTime,
                        "endTime" to endTime
                    )
                ) { success, message ->
                    isLoading = false
                    if (success) {
                        navController.popBackStack()
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = message,
                                duration = SnackbarDuration.Long
                            )
                        }
                    }
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Add New $buildingTitle Facility") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6A5ACD),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color.White)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Facility Information",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Facility Name
                OutlinedTextField(
                    value = facilityName,
                    onValueChange = { facilityName = it },
                    label = { Text("Facility Name") },
                    placeholder = { Text("e.g., Basketball Court") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    isError = facilityNameTouched && facilityName.isBlank(),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Create,
                            contentDescription = "Edit",
                            tint = Color.Gray
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (facilityNameTouched && facilityName.isBlank()) Color.Red else Color(0xFF6A5ACD),
                        unfocusedBorderColor = if (facilityNameTouched && facilityName.isBlank()) Color.Red else Color.Gray
                    ),
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        .also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect { interaction ->
                                    if (interaction is androidx.compose.foundation.interaction.FocusInteraction.Unfocus) {
                                        facilityNameTouched = true
                                    }
                                }
                            }
                        }
                )
                if (facilityNameTouched && facilityName.isBlank()) {
                    Text(
                        text = "Facility name is required",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Describe the facility...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    enabled = !isLoading,
                    isError = descriptionTouched && description.isBlank(),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Create,
                            contentDescription = "Edit",
                            tint = Color.Gray
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (descriptionTouched && description.isBlank()) Color.Red else Color(0xFF6A5ACD),
                        unfocusedBorderColor = if (descriptionTouched && description.isBlank()) Color.Red else Color.Gray
                    ),
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        .also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect { interaction ->
                                    if (interaction is androidx.compose.foundation.interaction.FocusInteraction.Unfocus) {
                                        descriptionTouched = true
                                    }
                                }
                            }
                        }
                )
                if (descriptionTouched && description.isBlank()) {
                    Text(
                        text = "Description is required",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    placeholder = { Text("e.g., Building A, Level 2") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    isError = locationTouched && location.isBlank(),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Create,
                            contentDescription = "Edit",
                            tint = Color.Gray
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (locationTouched && location.isBlank()) Color.Red else Color(0xFF6A5ACD),
                        unfocusedBorderColor = if (locationTouched && location.isBlank()) Color.Red else Color.Gray
                    ),
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        .also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect { interaction ->
                                    if (interaction is androidx.compose.foundation.interaction.FocusInteraction.Unfocus) {
                                        locationTouched = true
                                    }
                                }
                            }
                        }
                )
                if (locationTouched && location.isBlank()) {
                    Text(
                        text = "Location is required",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Capacity",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Capacity Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = minCapacity,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() } && it.length <= 3) {
                                    minCapacity = it
                                }
                            },
                            label = { Text("Min") },
                            placeholder = { Text("10") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            isError = minCapacityTouched && minCapacity.isBlank(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (minCapacityTouched && minCapacity.isBlank()) Color.Red else Color(0xFF6A5ACD),
                                unfocusedBorderColor = if (minCapacityTouched && minCapacity.isBlank()) Color.Red else Color.Gray
                            ),
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                .also { interactionSource ->
                                    LaunchedEffect(interactionSource) {
                                        interactionSource.interactions.collect { interaction ->
                                            if (interaction is androidx.compose.foundation.interaction.FocusInteraction.Unfocus) {
                                                minCapacityTouched = true
                                            }
                                        }
                                    }
                                }
                        )
                        if (minCapacityTouched && minCapacity.isBlank()) {
                            Text(
                                text = "Required",
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "to",
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(top = 8.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = maxCapacity,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() } && it.length <= 3) {
                                    maxCapacity = it
                                }
                            },
                            label = { Text("Max") },
                            placeholder = { Text("50") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            isError = maxCapacityTouched && maxCapacity.isBlank(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (maxCapacityTouched && maxCapacity.isBlank()) Color.Red else Color(0xFF6A5ACD),
                                unfocusedBorderColor = if (maxCapacityTouched && maxCapacity.isBlank()) Color.Red else Color.Gray
                            ),
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                .also { interactionSource ->
                                    LaunchedEffect(interactionSource) {
                                        interactionSource.interactions.collect { interaction ->
                                            if (interaction is androidx.compose.foundation.interaction.FocusInteraction.Unfocus) {
                                                maxCapacityTouched = true
                                            }
                                        }
                                    }
                                }
                        )
                        if (maxCapacityTouched && maxCapacity.isBlank()) {
                            Text(
                                text = "Required",
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Operating Hours",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Start Time
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Start Time: ${formatTime(startTime)}")
                        IconButton(
                            onClick = {
                                editingTimeField = "startTime"
                                showTimePickerDialog = true
                            },
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Filled.Create, contentDescription = "Edit")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // End Time
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("End Time: ${formatTime(endTime)}")
                        IconButton(
                            onClick = {
                                editingTimeField = "endTime"
                                showTimePickerDialog = true
                            },
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Filled.Create, contentDescription = "Edit")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = { showConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD)),
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading &&
                                facilityName.isNotBlank() &&
                                description.isNotBlank() &&
                                location.isNotBlank() &&
                                minCapacity.isNotBlank() &&
                                maxCapacity.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Add Facility", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmAddFacilityDialog(
    facilityData: Map<String, String>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("CONFIRM NEW FACILITY", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Please review the facility details:", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))

                    facilityData.forEach { (key, value) ->
                        val displayKey = when (key) {
                            "name" -> "Name"
                            "description" -> "Description"
                            "location" -> "Location"
                            "minNum" -> "Min Capacity"
                            "maxNum" -> "Max Capacity"
                            "startTime" -> "Start Time"
                            "endTime" -> "End Time"
                            else -> key
                        }

                        val displayValue = if (key == "startTime" || key == "endTime") {
                            formatTime(value)
                        } else {
                            value
                        }

                        Text(
                            text = "$displayKey:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = displayValue,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD))
                    ) {
                        Text("Confirm", color = Color.White)
                    }
                }
            }
        }
    }
}

private fun formatTime(time: String?): String {
    if (time == null || time.length != 4) return "N/A"
    return "${time.substring(0, 2)}:${time.substring(2, 4)}"
}