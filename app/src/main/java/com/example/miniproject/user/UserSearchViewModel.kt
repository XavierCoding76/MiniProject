package com.example.miniproject.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.components.SearchResultItemData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserSearchViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _searchResult = MutableStateFlow<List<SearchResultItemData>?>(null)
    val searchResult: StateFlow<List<SearchResultItemData>?> = _searchResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /**
     * Search for a user by displayId across both students and staff collections
     */
    fun searchUserByDisplayId(displayId: String) {
        viewModelScope.launch {
            if (displayId.isBlank()) {
                _searchResult.value = null
                return@launch
            }

            try {
                _isLoading.value = true
                _error.value = null

                val results = mutableListOf<SearchResultItemData>()

                // Search in students collection
                val studentQuery = firestore.collection("students")
                    .whereEqualTo("displayId", displayId)
                    .get()
                    .await()

                studentQuery.documents.forEach { doc ->
                    val username = doc.getString("username") ?: "Unknown"
                    val email = doc.getString("email") ?: "No email"
                    val displayIdValue = doc.getString("displayId") ?: ""

                    results.add(
                        SearchResultItemData(
                            id = doc.id,
                            title = "$username ($displayIdValue)",
                            subtitle = email
                        )
                    )
                }

                // Search in staff collection (if displayId starts with 'S' or is uppercase)
                val staffQuery = firestore.collection("staff")
                    .whereEqualTo("displayId", displayId.uppercase())
                    .get()
                    .await()

                staffQuery.documents.forEach { doc ->
                    val username = doc.getString("username") ?: "Unknown"
                    val email = doc.getString("email") ?: "No email"
                    val displayIdValue = doc.getString("displayId") ?: ""

                    results.add(
                        SearchResultItemData(
                            id = doc.id,
                            title = "$username ($displayIdValue)",
                            subtitle = "$email • Staff"
                        )
                    )
                }

                _searchResult.value = results
                _isLoading.value = false

            } catch (e: Exception) {
                _error.value = "Search failed: ${e.message}"
                _isLoading.value = false
                _searchResult.value = emptyList()
            }
        }
    }

    /**
     * Search with partial matching - searches users whose displayId starts with the query
     */
    fun searchUsersByPrefix(prefix: String) {
        viewModelScope.launch {
            if (prefix.isBlank()) {
                _searchResult.value = null
                return@launch
            }

            try {
                _isLoading.value = true
                _error.value = null

                val results = mutableListOf<SearchResultItemData>()

                // Get all students and filter by prefix
                val studentsSnapshot = firestore.collection("students").get().await()
                studentsSnapshot.documents.forEach { doc ->
                    val displayId = doc.getString("displayId") ?: ""
                    if (displayId.startsWith(prefix, ignoreCase = true)) {
                        val username = doc.getString("username") ?: "Unknown"
                        val email = doc.getString("email") ?: "No email"

                        results.add(
                            SearchResultItemData(
                                id = doc.id,
                                title = "$username ($displayId)",
                                subtitle = email
                            )
                        )
                    }
                }

                // Get all staff and filter by prefix
                val staffSnapshot = firestore.collection("staff").get().await()
                staffSnapshot.documents.forEach { doc ->
                    val displayId = doc.getString("displayId") ?: ""
                    if (displayId.startsWith(prefix, ignoreCase = true)) {
                        val username = doc.getString("username") ?: "Unknown"
                        val email = doc.getString("email") ?: "No email"

                        results.add(
                            SearchResultItemData(
                                id = doc.id,
                                title = "$username ($displayId)",
                                subtitle = "$email • Staff"
                            )
                        )
                    }
                }

                // Sort results by display ID
                _searchResult.value = results.sortedBy {
                    it.title.substringAfter("(").substringBefore(")").toIntOrNull() ?: Int.MAX_VALUE
                }
                _isLoading.value = false

            } catch (e: Exception) {
                _error.value = "Search failed: ${e.message}"
                _isLoading.value = false
                _searchResult.value = emptyList()
            }
        }
    }

    /**
     * Search by email
     */
    fun searchUserByEmail(email: String) {
        viewModelScope.launch {
            if (email.isBlank()) {
                _searchResult.value = null
                return@launch
            }

            try {
                _isLoading.value = true
                _error.value = null

                val results = mutableListOf<SearchResultItemData>()

                // Search students by email
                val studentQuery = firestore.collection("students")
                    .whereEqualTo("email", email.lowercase())
                    .get()
                    .await()

                studentQuery.documents.forEach { doc ->
                    val username = doc.getString("username") ?: "Unknown"
                    val emailValue = doc.getString("email") ?: "No email"
                    val displayId = doc.getString("displayId") ?: ""

                    results.add(
                        SearchResultItemData(
                            id = doc.id,
                            title = "$username ($displayId)",
                            subtitle = emailValue
                        )
                    )
                }

                // Search staff by email
                val staffQuery = firestore.collection("staff")
                    .whereEqualTo("email", email.lowercase())
                    .get()
                    .await()

                staffQuery.documents.forEach { doc ->
                    val username = doc.getString("username") ?: "Unknown"
                    val emailValue = doc.getString("email") ?: "No email"
                    val displayId = doc.getString("displayId") ?: ""

                    results.add(
                        SearchResultItemData(
                            id = doc.id,
                            title = "$username ($displayId)",
                            subtitle = "$emailValue • Staff"
                        )
                    )
                }

                _searchResult.value = results
                _isLoading.value = false

            } catch (e: Exception) {
                _error.value = "Search failed: ${e.message}"
                _isLoading.value = false
                _searchResult.value = emptyList()
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}