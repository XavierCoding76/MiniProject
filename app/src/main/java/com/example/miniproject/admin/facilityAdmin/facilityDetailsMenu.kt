package com.example.miniproject.admin.facilityAdmin

import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.material3.Checkbox
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText

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

    var showEditDialog by remember(facilityName) { mutableStateOf(false) }
    var showConfirmDialog by remember(facilityName) { mutableStateOf(false) }
    var showDeleteDialog by remember(facilityName) { mutableStateOf(false) }
    var editingField by remember(facilityName) { mutableStateOf("") }
    var localChanges by remember(facilityName) { mutableStateOf<Map<String, Any>>(emptyMap()) }
    var isEquipmentView by remember(facilityName) { mutableStateOf(false) }
    var showTimePickerDialog by remember(facilityName) { mutableStateOf(false) }
    var showCapacityDialog by remember(facilityName) { mutableStateOf(false) }
    var editingTimeField by remember(facilityName) { mutableStateOf("") }


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

    if (showDeleteDialog) {
        DeleteFacilityDialog(
            facilityName = localChanges["name"] as? String ?: "",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                viewModel.deleteFacility {
                    navController.popBackStack()
                }
                showDeleteDialog = false
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
            if (localChanges.isEmpty()) {
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
                    painter = painterResource(id = R.drawable.ic_launcher_background),
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
                                IconButton(onClick = { navController.navigate("sub_venues/${viewModel.facilityId}") }) {
                                    Icon(Icons.Filled.Add, contentDescription = "Sub Venues")
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { navController.popBackStack() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Go Back", fontSize = 14.sp)
                            }
                            Button(
                                onClick = { showConfirmDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Modify", fontSize = 14.sp, color = Color.White)
                            }
                            androidx.compose.material3.FloatingActionButton(
                                onClick = { showDeleteDialog = true },
                                containerColor = Color(0xFFF44336),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.White
                                )
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
fun DeleteFacilityDialog(
    facilityName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("DELETE FACILITY", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFF44336))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Are you sure you want to delete this facility?",
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    facilityName,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "This action cannot be undone. All associated equipment and sub-venues will also be deleted.",
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = Color.Gray
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
                        Text("Cancel")
                    }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) {
                        Text("Yes, Delete", color = Color.White)
                    }
                }
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
    var showAddEquipmentDialog by remember { mutableStateOf(false) }
    var showEditEquipmentDialog by remember { mutableStateOf(false) }
    var showConfirmChangesDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var editingEquipment by remember { mutableStateOf<Map<String, Any>?>(null) }
    var editingEquipmentIndex by remember { mutableStateOf(-1) }
    var deletingEquipmentIndex by remember { mutableStateOf(-1) }

    // Sync local list with ViewModel's list when it changes
    LaunchedEffect(viewModel.equipmentList) {
        localEquipmentList = viewModel.equipmentList
    }

    // Function to find the next available ID (fills gaps)
    fun getNextAvailableId(): String {
        val facilityId = viewModel.facilityId ?: ""
        val existingIds = localEquipmentList.mapNotNull {
            val id = it["id"] as? String
            // Extract the number part after "E"
            id?.removePrefix("${facilityId}E")?.toIntOrNull()
        }.toSet()

        // Find the smallest missing number starting from 1
        var nextNum = 1
        while (existingIds.contains(nextNum)) {
            nextNum++
        }

        return "${facilityId}E${nextNum}"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f)
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Equipment",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (localEquipmentList.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("No equipment found.", color = Color.Gray, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(localEquipmentList.size) { index ->
                        val equipment = localEquipmentList[index]

                        // Convert price and quantity to strings, handling both String and Number types
                        val priceStr = when (val p = equipment["price"]) {
                            is Number -> String.format("%.2f", p.toDouble())
                            is String -> p
                            else -> "0.00"
                        }

                        val quantityStr = when (val q = equipment["quantity"]) {
                            is Number -> q.toString()
                            is String -> q
                            else -> "0"
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = equipment["name"] as? String ?: "",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "RM $priceStr",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "Qty: $quantityStr",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                                IconButton(onClick = {
                                    editingEquipment = equipment
                                    editingEquipmentIndex = index
                                    showEditEquipmentDialog = true
                                }) {
                                    Icon(Icons.Filled.Create, contentDescription = "Edit")
                                }
                                IconButton(onClick = {
                                    deletingEquipmentIndex = index
                                    showDeleteConfirmDialog = true
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFF44336))
                                }
                            }
                        }
                    }
                }
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
                    onClick = { showConfirmChangesDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD))
                ) {
                    Text("Save", color = Color.White)
                }
            }
        }

        // Floating Action Button
        androidx.compose.material3.FloatingActionButton(
            onClick = { showAddEquipmentDialog = true },
            containerColor = Color(0xFF6A5ACD),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 72.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Equipment",
                tint = Color.White
            )
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && deletingEquipmentIndex >= 0) {
        val equipmentToDelete = localEquipmentList[deletingEquipmentIndex]
        Dialog(onDismissRequest = {
            showDeleteConfirmDialog = false
            deletingEquipmentIndex = -1
        }) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("DELETE EQUIPMENT", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Are you sure you want to delete this equipment?",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        equipmentToDelete["name"] as? String ?: "",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Button(
                            onClick = {
                                showDeleteConfirmDialog = false
                                deletingEquipmentIndex = -1
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                localEquipmentList = localEquipmentList.toMutableList().apply {
                                    removeAt(deletingEquipmentIndex)
                                }
                                showDeleteConfirmDialog = false
                                deletingEquipmentIndex = -1
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                        ) {
                            Text("Yes", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Confirm Changes Dialog
    if (showConfirmChangesDialog) {
        ConfirmEquipmentChangesDialog(
            originalEquipment = viewModel.equipmentList,
            modifiedEquipment = localEquipmentList,
            onDismiss = { showConfirmChangesDialog = false },
            onConfirm = {
                viewModel.saveEquipmentChanges(localEquipmentList) {
                    onBack()
                }
                showConfirmChangesDialog = false
            }
        )
    }

    // Add Equipment Dialog - MODIFIED TO FILL GAPS
    if (showAddEquipmentDialog) {
        AddEquipmentDialog(
            title = "ADD EQUIPMENT",
            initialName = "",
            initialPrice = "",
            initialQuantity = "",
            onDismiss = { showAddEquipmentDialog = false },
            onConfirm = { name, price, quantity ->
                val newEquipmentId = getNextAvailableId()

                localEquipmentList = localEquipmentList + mapOf(
                    "id" to newEquipmentId,
                    "name" to name,
                    "price" to price,
                    "quantity" to quantity
                )
                showAddEquipmentDialog = false
            }
        )
    }

    // Edit Equipment Dialog - FIXED TO PRESERVE ID
    if (showEditEquipmentDialog && editingEquipment != null) {
        // Convert Number types to Strings for editing
        val initialPrice = when (val p = editingEquipment!!["price"]) {
            is Number -> String.format("%.2f", p.toDouble())
            is String -> p
            else -> ""
        }

        val initialQuantity = when (val q = editingEquipment!!["quantity"]) {
            is Number -> q.toString()
            is String -> q
            else -> ""
        }

        AddEquipmentDialog(
            title = "EDIT EQUIPMENT",
            initialName = editingEquipment!!["name"] as? String ?: "",
            initialPrice = initialPrice,
            initialQuantity = initialQuantity,
            onDismiss = {
                showEditEquipmentDialog = false
                editingEquipment = null
                editingEquipmentIndex = -1
            },
            onConfirm = { name, price, quantity ->
                localEquipmentList = localEquipmentList.toMutableList().apply {
                    val originalItem = this[editingEquipmentIndex]
                    this[editingEquipmentIndex] = buildMap {
                        put("name", name)
                        put("price", price)
                        put("quantity", quantity)
                        // Preserve the original ID if it exists
                        originalItem["id"]?.let { put("id", it) }
                    }
                }
                showEditEquipmentDialog = false
                editingEquipment = null
                editingEquipmentIndex = -1
            }
        )
    }
}
@Composable
fun ConfirmEquipmentChangesDialog(
    originalEquipment: List<Map<String, Any>>,
    modifiedEquipment: List<Map<String, Any>>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val originalIds = originalEquipment.mapNotNull { it["id"] as? String }.toSet()
    val modifiedIds = modifiedEquipment.mapNotNull { it["id"] as? String }.toSet()

    val addedItems = modifiedEquipment.filter { item ->
        val id = item["id"] as? String
        id != null && !originalIds.contains(id)
    }

    val deletedItems = originalEquipment.filter { item ->
        val id = item["id"] as? String
        id != null && !modifiedIds.contains(id)
    }

    val modifiedItems = modifiedEquipment.filter { modItem ->
        val id = modItem["id"] as? String
        if (id != null && originalIds.contains(id)) {
            val originalItem = originalEquipment.find { it["id"] == id }
            originalItem != null && (
                    originalItem["name"] != modItem["name"] ||
                            originalItem["price"].toString() != modItem["price"].toString() ||
                            originalItem["quantity"].toString() != modItem["quantity"].toString()
                    )
        } else false
    }

    val hasChanges = addedItems.isNotEmpty() || deletedItems.isNotEmpty() || modifiedItems.isNotEmpty()

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("CONFIRM CHANGES", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))

                if (!hasChanges) {
                    Text("No changes were made.", textAlign = TextAlign.Center)
                } else {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Added items
                        if (addedItems.isNotEmpty()) {
                            Text(
                                "Added Equipment:",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            addedItems.forEach { item ->
                                Text(
                                    text = "  + ${item["name"]} (RM ${item["price"]}, Qty: ${item["quantity"]})",
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Modified items
                        if (modifiedItems.isNotEmpty()) {
                            Text(
                                "Modified Equipment:",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2196F3)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            modifiedItems.forEach { modItem ->
                                val id = modItem["id"] as? String
                                val originalItem = originalEquipment.find { it["id"] == id }
                                if (originalItem != null) {
                                    Text(
                                        text = "  • ${modItem["name"]}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (originalItem["name"] != modItem["name"]) {
                                        Text(
                                            text = "    Name: ${originalItem["name"]} → ${modItem["name"]}",
                                            fontSize = 13.sp
                                        )
                                    }
                                    if (originalItem["price"].toString() != modItem["price"].toString()) {
                                        Text(
                                            text = "    Price: RM ${originalItem["price"]} → RM ${modItem["price"]}",
                                            fontSize = 13.sp
                                        )
                                    }
                                    if (originalItem["quantity"].toString() != modItem["quantity"].toString()) {
                                        Text(
                                            text = "    Qty: ${originalItem["quantity"]} → ${modItem["quantity"]}",
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Deleted items
                        if (deletedItems.isNotEmpty()) {
                            Text(
                                "Deleted Equipment:",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF44336)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            deletedItems.forEach { item ->
                                Text(
                                    text = "  - ${item["name"]} (RM ${item["price"]}, Qty: ${item["quantity"]})",
                                    fontSize = 14.sp
                                )
                            }
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
                        enabled = hasChanges
                    ) {
                        Text("Confirm", color = Color.White)
                    }
                }
            }
        }
    }
}
@Composable
fun AddEquipmentDialog(
    title: String,
    initialName: String,
    initialPrice: String,
    initialQuantity: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var equipmentName by remember { mutableStateOf(initialName) }

    // Store raw digits only - internal state
    var priceDigits by remember {
        mutableStateOf(
            if (initialPrice.isNotEmpty()) {
                val priceValue = initialPrice.toDoubleOrNull() ?: 0.0
                (priceValue * 100).toLong().toString()
            } else "0"
        )
    }

    var quantity by remember { mutableStateOf(initialQuantity) }

    // Format for display
    fun formatPriceDisplay(digits: String): String {
        val value = digits.toLongOrNull() ?: 0
        return String.format("%.2f", value / 100.0)
    }

    // Visual transformation for price
    val priceVisualTransformation = androidx.compose.ui.text.input.VisualTransformation { text ->
        val digits = text.text
        val value = digits.toLongOrNull() ?: 0
        val formatted = String.format("%.2f", value / 100.0)

        TransformedText(
            text = androidx.compose.ui.text.AnnotatedString(formatted),
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int = formatted.length
                override fun transformedToOriginal(offset: Int): Int = digits.length
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        title,
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

                // Equipment Name Field
                OutlinedTextField(
                    value = equipmentName,
                    onValueChange = { equipmentName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Name") },
                    placeholder = { Text("Badminton Racket") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Create,
                            contentDescription = "Edit",
                            tint = Color.Gray
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Price Field with RM prefix - Stack style input with visual transformation
                OutlinedTextField(
                    value = priceDigits,
                    onValueChange = { newValue ->
                        // Only accept digits
                        val filtered = newValue.filter { it.isDigit() }

                        when {
                            // Deleting - remove last digit
                            filtered.length < priceDigits.length -> {
                                priceDigits = if (priceDigits.length > 1) {
                                    priceDigits.dropLast(1)
                                } else {
                                    "0"
                                }
                            }
                            // Adding - append new digit to the end
                            filtered.length > priceDigits.length -> {
                                val newDigit = filtered.last()
                                val newDigits = if (priceDigits == "0") {
                                    newDigit.toString()
                                } else {
                                    priceDigits + newDigit
                                }
                                // Limit to 6 digits (max 9999.99)
                                if (newDigits.length <= 6) {
                                    priceDigits = newDigits
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Price") },
                    placeholder = { Text("0.00") },
                    prefix = { Text("RM ") },
                    visualTransformation = priceVisualTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Create,
                            contentDescription = "Edit",
                            tint = Color.Gray
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quantity Field (Numbers only)
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                            quantity = newValue
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Quantity") },
                    placeholder = { Text("50") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Create,
                            contentDescription = "Edit",
                            tint = Color.Gray
                        )
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val finalPrice = formatPriceDisplay(priceDigits)
                        if (equipmentName.isNotBlank() && finalPrice.isNotBlank() && quantity.isNotBlank()) {
                            onConfirm(equipmentName, finalPrice, quantity)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5ACD)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    enabled = equipmentName.isNotBlank() && priceDigits.isNotBlank() && quantity.isNotBlank()
                ) {
                    Text("Confirm", color = Color.White)
                }
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
                    modifier = Modifier.height(150.dp),
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