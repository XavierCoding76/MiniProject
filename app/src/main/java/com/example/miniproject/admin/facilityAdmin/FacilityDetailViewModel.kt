package com.example.miniproject.admin.facilityAdmin

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FacilityDetailViewModel : ViewModel() {

    var facilityId by mutableStateOf<String?>(null)
    var facility by mutableStateOf<Map<String, Any>?>(null)
    var facilityExists by mutableStateOf(true)
    var hasEquipment by mutableStateOf<Boolean?>(null)
    var equipmentList by mutableStateOf<List<Map<String, Any>>>(emptyList())
    var hasSubVenues by mutableStateOf(false)

    fun fetchFacilityByName(name: String) {
        println("Searching for facility with name: '$name'")
        viewModelScope.launch {
            val db = Firebase.firestore
            try {
                val querySnapshot = db.collection("facility")
                    .whereEqualTo("name", name)
                    .limit(1)
                    .get()
                    .await()

                if (querySnapshot.isEmpty) {
                    facilityExists = false
                } else {
                    val doc = querySnapshot.documents[0]
                    facilityId = doc.id
                    facility = doc.data
                    facilityExists = true
                    fetchEquipment(doc.id)
                    checkForSubVenues(doc.id)
                }
            } catch (e: Exception) {
                facilityExists = false
                println("Error fetching facility: ${e.message}")
            }
        }
    }

    private fun fetchEquipment(facilityId: String) {
        viewModelScope.launch {
            try {
                // Fetch all equipment where document ID starts with facilityId
                val equipmentQuery = Firebase.firestore.collection("equipment")
                    .whereGreaterThanOrEqualTo(FieldPath.documentId(), "${facilityId}E")
                    .whereLessThan(FieldPath.documentId(), "${facilityId}F")
                    .get()
                    .await()
                equipmentList = equipmentQuery.documents.map { doc ->
                    val data = doc.data!!.plus("id" to doc.id)
                    println("DEBUG - Fetched equipment: ${doc.id}")
                    println("DEBUG - Raw data: ${doc.data}")
                    println("DEBUG - Price type: ${doc.data?.get("price")?.javaClass}")
                    println("DEBUG - Quantity type: ${doc.data?.get("quantity")?.javaClass}")
                    println("DEBUG - Mapped data: $data")
                    data
                }
                hasEquipment = equipmentList.isNotEmpty()
                println("DEBUG - Fetched ${equipmentList.size} equipment items for facility $facilityId")
            } catch (e: Exception) {
                println("Error fetching equipment: ${e.message}")
                hasEquipment = false
            }
        }
    }

    private fun checkForSubVenues(facilityId: String) {
        viewModelScope.launch {
            try {
                val subVenuesQuery = Firebase.firestore.collection("facility")
                    .whereGreaterThan(FieldPath.documentId(), facilityId + "_")
                    .whereLessThan(FieldPath.documentId(), facilityId + "_\uf8ff")
                    .limit(1)
                    .get()
                    .await()
                hasSubVenues = !subVenuesQuery.isEmpty
            } catch (e: Exception) {
                println("Error checking for sub-venues: ${e.message}")
                hasSubVenues = false
            }
        }
    }

    fun saveEquipmentChanges(newEquipmentData: List<Map<String, Any>>, onComplete: () -> Unit) {
        val facilityId = this.facilityId ?: return
        viewModelScope.launch {
            try {
                val db = Firebase.firestore
                val batch = db.batch()
                val originalIds = equipmentList.mapNotNull { it["id"] as? String }.toSet()

                newEquipmentData.forEach { item ->
                    val itemId = item["id"] as? String

                    if (itemId != null && originalIds.contains(itemId)) {
                        // Update existing item
                        val updateData = mapOf(
                            "name" to (item["name"] ?: ""),
                            "price" to (item["price"] ?: "0.00"),
                            "quantity" to (item["quantity"] ?: "0")
                        )
                        batch.update(db.collection("equipment").document(itemId), updateData)
                    } else if (itemId != null) {
                        // Add new item with custom ID
                        val equipmentData = mapOf(
                            "facilityID" to facilityId,
                            "name" to (item["name"] ?: ""),
                            "price" to (item["price"] ?: "0.00"),
                            "quantity" to (item["quantity"] ?: "0")
                        )
                        batch.set(db.collection("equipment").document(itemId), equipmentData)
                    }
                }

                // Delete removed items
                val newIds = newEquipmentData.mapNotNull { it["id"] as? String }.toSet()
                val toDelete = originalIds - newIds
                toDelete.forEach {
                    batch.delete(db.collection("equipment").document(it))
                }

                batch.commit().await()

                // Refresh equipment list after saving
                fetchEquipment(facilityId)
            } catch (e: Exception) {
                println("Error saving equipment changes: ${e.message}")
            } finally {
                onComplete()
            }
        }
    }

    fun deleteFacility(onComplete: () -> Unit) {
        val facilityId = this.facilityId ?: return

        viewModelScope.launch {
            try {
                val db = Firebase.firestore
                val batch = db.batch()

                // Delete all equipment associated with this facility
                val equipmentQuery = db.collection("equipment")
                    .whereGreaterThanOrEqualTo(FieldPath.documentId(), "${facilityId}E")
                    .whereLessThan(FieldPath.documentId(), "${facilityId}F")
                    .get()
                    .await()

                equipmentQuery.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }

                // Delete all sub-venues (facilities with IDs starting with facilityId_)
                val subVenuesQuery = db.collection("facility")
                    .whereGreaterThan(FieldPath.documentId(), facilityId + "_")
                    .whereLessThan(FieldPath.documentId(), facilityId + "_\uf8ff")
                    .get()
                    .await()

                subVenuesQuery.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }

                // Delete the facility itself
                batch.delete(db.collection("facility").document(facilityId))

                // ADD THIS: Delete all facilityind entries that start with the facility ID
                // This will delete documents with IDs like C1_1, C1_2, etc.
                val facilityIndQuery = db.collection("facilityind")
                    .whereGreaterThanOrEqualTo(FieldPath.documentId(), facilityId)
                    .whereLessThan(FieldPath.documentId(), facilityId + "\uf8ff")
                    .get()
                    .await()

                facilityIndQuery.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }

                batch.commit().await()
                println("Successfully deleted facility $facilityId and all associated data")
            } catch (e: Exception) {
                println("Error deleting facility: ${e.message}")
            } finally {
                onComplete()
            }
        }
    }
    fun updateVenueDetails(newData: Map<String, Any>, onComplete: () -> Unit) {
        if (facilityId == null) {
            onComplete()
            return
        }
        viewModelScope.launch {
            try {
                Firebase.firestore.collection("facility").document(facilityId!!)
                    .update(newData)
                    .await()
            } catch (e: Exception) {
                println("Error updating facility details: ${e.message}")
            } finally {
                onComplete()
            }
        }
    }

    // Add facility with duplicate name check
    fun addFacility(buildingType: String, facilityData: Map<String, Any>, onComplete: (Boolean, String) -> Unit) {
        val facilityName = facilityData["name"] as? String ?: ""
        if (facilityName.isEmpty()) {
            Log.e("FacilityDetailViewModel", "Facility name is empty")
            onComplete(false, "Facility name is required")
            return
        }

        viewModelScope.launch {
            try {
                // First, check if facility name already exists
                val nameExists = checkFacilityNameExists(facilityName)

                if (nameExists) {
                    Log.e("FacilityDetailViewModel", "Facility name already exists: $facilityName")
                    onComplete(false, "A facility with this name already exists. Please choose a different name.")
                } else {
                    // Proceed with adding the facility
                    createFacility(buildingType, facilityData, facilityName, onComplete)
                }
            } catch (e: Exception) {
                Log.e("FacilityDetailViewModel", "Error in addFacility", e)
                onComplete(false, "An error occurred. Please try again.")
            }
        }
    }

    private suspend fun checkFacilityNameExists(facilityName: String): Boolean {
        return try {
            val db = Firebase.firestore
            val querySnapshot = db.collection("facility")
                .whereEqualTo("name", facilityName)
                .get()
                .await()

            // If any documents are found, the name exists
            !querySnapshot.isEmpty
        } catch (e: Exception) {
            Log.e("FacilityDetailViewModel", "Error checking facility name", e)
            // On error, assume it doesn't exist to allow the operation to proceed
            false
        }
    }

    private suspend fun createFacility(
        buildingType: String,
        facilityData: Map<String, Any>,
        facilityName: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        try {
            val db = Firebase.firestore
            val prefix = buildingType

            // Get the next available ID for facilities with this prefix
            val querySnapshot = db.collection("facility")
                .whereGreaterThanOrEqualTo(FieldPath.documentId(), prefix)
                .whereLessThan(FieldPath.documentId(), "$prefix\uf8ff")
                .get()
                .await()

            // Extract existing IDs and find the next available number
            val existingIds = querySnapshot.documents.mapNotNull { doc ->
                val id = doc.id
                // Extract number part after the prefix (e.g., "C1" -> 1, "CC10" -> 10)
                id.removePrefix(prefix).toIntOrNull()
            }.toSet()

            // Find smallest missing number starting from 1
            var nextNum = 1
            while (existingIds.contains(nextNum)) {
                nextNum++
            }

            val newFacilityId = "$prefix$nextNum"

            // Prepare the facility document
            val minNum = when (val min = facilityData["minNum"]) {
                is String -> min.toIntOrNull() ?: 0
                is Number -> min.toInt()
                else -> 0
            }

            val maxNum = when (val max = facilityData["maxNum"]) {
                is String -> max.toIntOrNull() ?: 0
                is Number -> max.toInt()
                else -> 0
            }

            val facilityDocument = hashMapOf(
                "name" to facilityName,
                "description" to (facilityData["description"] as? String ?: ""),
                "location" to (facilityData["location"] as? String ?: ""),
                "minNum" to minNum,
                "maxNum" to maxNum,
                "startTime" to (facilityData["startTime"] as? String ?: "0800"),
                "endTime" to (facilityData["endTime"] as? String ?: "2200")
            )

            // Add to Firestore using the ID as the document ID
            db.collection("facility")
                .document(newFacilityId)
                .set(facilityDocument)
                .await()

            Log.d("FacilityDetailViewModel", "Facility added successfully: $newFacilityId")
            onComplete(true, "Facility added successfully!")

        } catch (e: Exception) {
            Log.e("FacilityDetailViewModel", "Error creating facility", e)
            onComplete(false, "Failed to add facility. Please try again.")
        }
    }

    // Validate facility data before adding
    fun validateFacilityData(facilityData: Map<String, Any>): Pair<Boolean, String> {
        val name = facilityData["name"] as? String
        val description = facilityData["description"] as? String
        val location = facilityData["location"] as? String
        val minNum = (facilityData["minNum"] as? String)?.toIntOrNull()
        val maxNum = (facilityData["maxNum"] as? String)?.toIntOrNull()

        return when {
            name.isNullOrBlank() -> false to "Facility name is required"
            description.isNullOrBlank() -> false to "Description is required"
            location.isNullOrBlank() -> false to "Location is required"
            minNum == null || minNum < 0 -> false to "Valid minimum capacity is required"
            maxNum == null || maxNum < 0 -> false to "Valid maximum capacity is required"
            minNum > maxNum -> false to "Minimum capacity cannot exceed maximum capacity"
            else -> true to "Valid"
        }
    }
}