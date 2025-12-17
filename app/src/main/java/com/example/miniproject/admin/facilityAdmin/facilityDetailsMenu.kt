package com.example.miniproject.admin.facilityAdmin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

private fun formatTime(time: String?): String {
    if (time == null || time.length != 4) return "N/A"
    return "${time.substring(0, 2)}:${time.substring(2, 4)}"
}

@Composable
fun FacilityDetailScreen(
    navController: NavController,
    facilityName: String?,
    viewModel: FacilityDetailViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // By keying the state to facilityName, it will reset whenever the facilityName changes.
    var showEditDialog by remember(facilityName) { mutableStateOf(false) }
    var showConfirmDialog by remember(facilityName) { mutableStateOf(false) }
    var editingField by remember(facilityName) { mutableStateOf("") }
    var localChanges by remember(facilityName) { mutableStateOf<Map<String, Any>>(emptyMap()) }
    var isEquipmentView by remember(facilityName) { mutableStateOf(false) }
    var showTimePickerDialog by remember(facilityName) { mutableStateOf(false) }
    var showCapacityDialog by remember(facilityName) { mutableStateOf(false) }
    var editingTimeField by remember(facilityName) { mutableStateOf("") } // "startTime" or "endTime"


    fun openEditDialog(field: String) {
        editingField = field
        showEditDialog = true
    }

    if (showEditDialog) {
        val currentEditingValue = localChanges[editingField] as? String
            ?: viewModel.facility?.get(editingField) as? String ?: ""

        ModificationDialog(
            initialValue = currentEditingValue,
            onDismiss = { showEditDialog = false },
            onSave = { newValue ->
                localChanges = localChanges + (editingField to newValue)
                showEditDialog = false
            }
        )
    }

    if (showTimePickerDialog) {
        val initialTime = localChanges[editingTimeField] as? String ?: "0000"
        TimePickerDialog(
            initialTime = initialTime,
            onDismiss = { showTimePickerDialog = false },
            onSave = { newTime ->
                localChanges = localChanges + (editingTimeField to newTime)
                showTimePickerDialog = false
            }
        )
    }

    if (showCapacityDialog) {
        val minNum = (localChanges["minNum"] as? Number)?.toInt() ?: 0
        val maxNum = (localChanges["maxNum"] as? Number)?.toInt() ?: 0
        CapacityModificationDialog(
            initialMin = minNum,
            initialMax = maxNum,
            onDismiss = { showCapacityDialog = false },
            onSave = { newMin, newMax ->
                localChanges = localChanges + mapOf("minNum" to newMin, "maxNum" to newMax)
                showCapacityDialog = false
            }
        )
    }

    if (showConfirmDialog) {
        ConfirmChangesDialog(
            originalData = viewModel.facility ?: emptyMap(),
            modifiedData = localChanges,
            onDismiss = { showConfirmDialog = false },
            onConfirm = {
                viewModel.updateVenueDetails(newData = localChanges, onComplete = { navController.popBackStack() })
                showConfirmDialog = false
            }
        )
    }

    LaunchedEffect(facilityName) {
        if (facilityName != null) {
            val decodedName = URLDecoder.decode(facilityName, StandardCharsets.UTF_8.toString())
            viewModel.fetchFacilityByName(decodedName)
        }
    }

    LaunchedEffect(viewModel.facility) {
        viewModel.facility?.let {
            if (localChanges.isEmpty()) { // Only set initial data once
                localChanges = it
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            if (localChanges.isNotEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background), // Replace with your image
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isEquipmentView) {
                    EquipmentScreen(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        viewModel = viewModel,
                        onBack = { isEquipmentView = false }
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .fillMaxHeight(0.7f)
                            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = localChanges["name"] as? String ?: "",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Row {
                                IconButton(onClick = { openEditDialog("name") }) {
                                    Icon(Icons.Filled.Create, contentDescription = "Edit")
                                }
                                IconButton(onClick = { isEquipmentView = true }) {
                                    Icon(Icons.Filled.Build, contentDescription = "Settings")
                                }
                                IconButton(onClick = { /* TODO: Add functionality */ }) {
                                    Icon(Icons.Filled.Add, contentDescription = "Add")
                                }
                                if (viewModel.hasSubVenues) {
                                    IconButton(onClick = { navController.navigate("sub_venues/${viewModel.facilityId}") }) {
                                        Icon(Icons.Filled.Add, contentDescription = "Add")
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                            Spacer(modifier = Modifier.height(16.dp))
                            DetailCard(
                                text = localChanges["description"] as? String ?: "",
                                showEditIcon = true,
                                onEditClick = { openEditDialog("description") }
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            val minNum = (localChanges["minNum"] as? Number)?.toInt() ?: 0
                            val maxNum = (localChanges["maxNum"] as? Number)?.toInt() ?: 0
                            val capacityText = if (minNum == maxNum) {
                                "Capacity: $maxNum pax"
                            } else {
                                "Capacity: $minNum - $maxNum pax"
                            }

                            DetailCard(
                                text = capacityText,
                                showEditIcon = true,
                                onEditClick = { showCapacityDialog = true }
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            DetailCard(
                                text = "Location: ${localChanges["location"] as? String ?: ""}",
                                showEditIcon = true,
                                onEditClick = { openEditDialog("location") }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            DetailCard(
                                text = "Start Time: ${formatTime(localChanges["startTime"] as? String)}",
                                showEditIcon = true,
                                onEditClick = {
                                    editingTimeField = "startTime"
                                    showTimePickerDialog = true
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            DetailCard(
                                text = "End Time: ${formatTime(localChanges["endTime"] as? String)}",
                                showEditIcon = true,
                                onEditClick = {
                                    editingTimeField = "endTime"
                                    showTimePickerDialog = true
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { navController.popBackStack() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                            ) {
                                Text("Go Back")
                            }
                            Button(
                                onClick = { showConfirmDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD))
                            ) {
                                Text("Modify Venue", color = Color.White)
                            }
                        }
                    }
                }

            } else if (!viewModel.facilityExists) {
                // Handled by LaunchedEffect showing snackbar
            } else {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun EquipmentScreen(
    viewModel: FacilityDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var localEquipmentList by remember { mutableStateOf(viewModel.equipmentList) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f)
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text("Equipment", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(16.dp))
        if (localEquipmentList.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("No equipment found.", color = Color.Gray, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(localEquipmentList) { equipment ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(equipment["name"] as? String ?: "", modifier = Modifier.weight(1f))
                        IconButton(onClick = { /* TODO: Edit equipment name */ }) {
                            Icon(Icons.Filled.Create, contentDescription = "Edit")
                        }
                        IconButton(onClick = { localEquipmentList = localEquipmentList - equipment }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
        IconButton(onClick = { localEquipmentList = localEquipmentList + mapOf("name" to "New Equipment") }) {
            Icon(Icons.Filled.Add, contentDescription = "Add Equipment")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            ) {
                Text("Go Back")
            }
            Button(
                onClick = { viewModel.saveEquipmentChanges(localEquipmentList, onBack) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD))
            ) {
                Text("Save", color = Color.White)
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val initialHour = initialTime.substring(0, 2)
    val initialMinute = initialTime.substring(2, 4)

    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }

    val hours = (0..23).map { it.toString().padStart(2, '0') }
    val minutes = listOf("00", "30")

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("SELECT TIME", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.height(150.dp), // Constrain height for the pickers
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(hours) { hour ->
                            Text(
                                text = hour,
                                color = if (selectedHour == hour) Color.Black else Color.Gray,
                                fontWeight = if (selectedHour == hour) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .clickable { selectedHour = hour }
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }

                    Text(
                        text = ":",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(minutes) { minute ->
                            Text(
                                text = minute,
                                color = if (selectedMinute == minute) Color.Black else Color.Gray,
                                fontWeight = if (selectedMinute == minute) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .clickable { selectedMinute = minute }
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onSave("$selectedHour$selectedMinute") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD))
                    ) {
                        Text("Save", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun CapacityModificationDialog(
    initialMin: Int,
    initialMax: Int,
    onDismiss: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    var isSame by remember { mutableStateOf(initialMin == initialMax) }
    var minNumText by remember { mutableStateOf(initialMin.toString()) }
    var maxNumText by remember { mutableStateOf(initialMax.toString()) }
    var singleValueText by remember { mutableStateOf(if (initialMin == initialMax) initialMin.toString() else "") }

    fun onValueChange(currentValue: String, newValue: String, onTextChanged: (String) -> Unit) {
        if (newValue.length <= 2 && newValue.all { it.isDigit() }) {
            onTextChanged(newValue)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("EDIT CAPACITY", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isSame = !isSame }
                ) {
                    Checkbox(checked = isSame, onCheckedChange = { isSame = it })
                    Text("Min and Max are the same")
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (isSame) {
                    OutlinedTextField(
                        value = singleValueText,
                        onValueChange = {
                            onValueChange(singleValueText, it) { updatedValue ->
                                singleValueText = updatedValue
                            }
                        },
                        label = { Text("Capacity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (isSame) {
                                val singleValue = singleValueText.toIntOrNull() ?: 0
                                onSave(singleValue, singleValue)
                            } else {
                                onSave(minNumText.toIntOrNull() ?: 0, maxNumText.toIntOrNull() ?: 0)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD))
                    ) {
                        Text("Save", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailCard(text: String, showEditIcon: Boolean, onEditClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (showEditIcon) Arrangement.SpaceBetween else Arrangement.Start
        ) {
            Text(text = text, modifier = Modifier.weight(1f))
            if (showEditIcon) {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Filled.Create, contentDescription = "Edit")
                }
            }
        }
    }
}

@Composable
fun ModificationDialog(
    initialValue: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("MODIFICATION", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                    ) {
                        Text("Go Back")
                    }
                    Button(
                        onClick = { onSave(text) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD))
                    ) {
                        Text("Save", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ConfirmChangesDialog(
    originalData: Map<String, Any>,
    modifiedData: Map<String, Any>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val changes = remember {
        modifiedData.filter { (key, value) -> originalData[key] != value }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("CONFIRM CHANGES", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))

                if (changes.isEmpty()) {
                    Text("No changes were made.", textAlign = TextAlign.Center)
                } else {
                    Column(horizontalAlignment = Alignment.Start) {
                        changes.forEach { (key, value) ->
                            val originalValue = originalData[key]?.toString() ?: ""
                            val displayValue = if (key == "startTime" || key == "endTime") {
                                formatTime(value as? String)
                            } else {
                                value
                            }
                            val displayOriginal = if (key == "startTime" || key == "endTime") {
                                formatTime(originalValue)
                            } else {
                                originalValue
                            }
                            Text(
                                text = "- ${key.capitalize()}: \"$displayOriginal\" → \"$displayValue\"",
                                textAlign = TextAlign.Start
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD)),
                        enabled = changes.isNotEmpty()
                    ) {
                        Text("Confirm", color = Color.White)
                    }
                }
            }
        }
    }
}