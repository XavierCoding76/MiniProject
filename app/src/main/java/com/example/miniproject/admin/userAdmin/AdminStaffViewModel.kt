package com.example.miniproject.admin.userAdmin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.components.SearchResultItemData
import com.example.miniproject.data.SearchHistoryRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

@OptIn(FlowPreview::class)
class AdminStaffViewModel(application: Application) : AndroidViewModel(application) {
    private val historyRepository = SearchHistoryRepository(application)
    private val firestore = FirebaseFirestore.getInstance()
    private val historyKey = "staff_search_history"

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory = _searchHistory.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResultItemData>?>(null)
    val searchResults = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var isManualSearch = false

    init {
        _searchHistory.value = historyRepository.getHistory(historyKey)

        viewModelScope.launch {
            searchText
                .debounce(500)
                .collect { query ->
                    if (!isManualSearch && query.isNotBlank()) {
                        performSearch(query)
                    }
                    isManualSearch = false
                }
        }
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
        isManualSearch = false
    }

    fun onSearch() {
        val query = _searchText.value.trim()
        if (query.isNotBlank()) {
            isManualSearch = true
            addSearchToHistory(query)
            performSearch(query)
        } else {
            _searchResults.value = null
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val exactMatchResults = searchExactMatch(query)

                if (exactMatchResults.isNotEmpty()) {
                    _searchResults.value = exactMatchResults
                } else {
                    searchWithBroadMatch(query)
                }

                _isLoading.value = false

            } catch (e: Exception) {
                _error.value = "Search failed: ${e.message}"
                _isLoading.value = false
                _searchResults.value = emptyList()
            }
        }
    }

    // In searchExactMatch function, change to search by displayId field:
    private suspend fun searchExactMatch(query: String): List<SearchResultItemData> {
        return try {
            val querySnapshot = firestore.collection("user")
                .whereEqualTo("displayId", query)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { document ->
                val displayId = document.getString("displayId") ?: return@mapNotNull null
                val name = document.getString("name") ?: "Unknown"
                val email = document.getString("email") ?: ""

                SearchResultItemData(
                    id = document.id,  // This is now the Auth UID
                    title = "$name ($displayId)",
                    subtitle = email
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun searchWithBroadMatch(query: String) {
        try {
            val querySnapshot = firestore.collection("user")
                .get()
                .await()

            val results = querySnapshot.documents.mapNotNull { document ->
                val displayId = document.getString("displayId") ?: "No ID"
                val username = document.getString("name") ?: ""
                val fullName = document.getString("fullName") ?: ""
                val email = document.getString("email") ?: "No Email"
                val role = document.getString("role") ?: "User"

                val nameToShow = when {
                    fullName.isNotBlank() -> fullName
                    username.isNotBlank() -> username
                    email != "No Email" -> email.substringBefore("@")
                    else -> "User"
                }

                val isStaff = role.contains("staff", ignoreCase = true) ||
                        displayId.startsWith("S", ignoreCase = true) ||
                        email.contains("@staff.", ignoreCase = true)

                if (!isStaff) {
                    return@mapNotNull null
                }

                val searchQuery = query.lowercase(Locale.getDefault())
                val matches = displayId.lowercase(Locale.getDefault()).contains(searchQuery) ||
                        username.lowercase(Locale.getDefault()).contains(searchQuery) ||
                        fullName.lowercase(Locale.getDefault()).contains(searchQuery) ||
                        email.lowercase(Locale.getDefault()).contains(searchQuery) ||
                        role.lowercase(Locale.getDefault()).contains(searchQuery)

                if (matches) {
                    SearchResultItemData(
                        id = document.id,
                        title = "$nameToShow ($displayId)",
                        subtitle = if (email != "No Email") "$email • $role" else role
                    )
                } else null
            }.sortedBy {
                try {
                    val idText = it.title.substringAfter("(").substringBefore(")")
                    idText.replace("S", "", ignoreCase = true).toIntOrNull() ?: Int.MAX_VALUE
                } catch (e: Exception) {
                    Int.MAX_VALUE
                }
            }

            _searchResults.value = results
        } catch (e: Exception) {
            _searchResults.value = emptyList()
        }
    }

    suspend fun generateStaffDisplayId(): String {
        return try {
            val users = firestore.collection("user")
                .get()
                .await()

            val staffIds = users.documents.mapNotNull { document ->
                val displayId = document.getString("displayId") ?: ""
                val role = document.getString("role") ?: ""

                if (role.contains("staff", ignoreCase = true) ||
                    displayId.startsWith("S", ignoreCase = true)) {
                    displayId
                } else {
                    null
                }
            }

            var maxId = 0
            staffIds.forEach { id ->
                try {
                    val numericPart = id.replace(Regex("[^0-9]"), "")
                    if (numericPart.isNotEmpty()) {
                        val num = numericPart.toInt()
                        if (num > maxId) {
                            maxId = num
                        }
                    }
                } catch (e: Exception) {
                    // Skip if not numeric
                }
            }

            val newId = maxId + 1
            "S$newId"

        } catch (e: Exception) {
            "S1001"
        }
    }

    fun addUser(
        name: String,
        email: String,
        password: String,
        displayId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val formattedDisplayId = if (!displayId.startsWith("S", ignoreCase = true)) {
                    "S${displayId.uppercase(Locale.getDefault())}"
                } else {
                    displayId.uppercase(Locale.getDefault())
                }

                val existingIdQuery = firestore.collection("user")
                    .whereEqualTo("displayId", formattedDisplayId)
                    .get()
                    .await()

                if (!existingIdQuery.isEmpty) {
                    onError("Staff with ID $formattedDisplayId already exists")
                    return@launch
                }

                val existingEmailQuery = firestore.collection("user")
                    .whereEqualTo("email", email.lowercase(Locale.getDefault()))
                    .get()
                    .await()

                if (!existingEmailQuery.isEmpty) {
                    onError("Staff with email $email already exists")
                    return@launch
                }

                val staffData = hashMapOf(
                    "displayId" to formattedDisplayId,
                    "name" to name,
                    "email" to email.lowercase(Locale.getDefault()),
                    "role" to "staff",
                )

                firestore.collection("user")
                    .add(staffData)
                    .await()

                onSuccess()

            } catch (e: Exception) {
                onError("Failed to add staff: ${e.message}")
            }
        }
    }

    fun onClearHistoryItem(item: String) {
        historyRepository.clearHistoryItem(historyKey, item)
        _searchHistory.value = historyRepository.getHistory(historyKey)
    }

    fun addSearchToHistory(searchTerm: String) {
        if (searchTerm.isNotBlank()) {
            historyRepository.addToHistory(historyKey, searchTerm)
            _searchHistory.value = historyRepository.getHistory(historyKey)
        }
    }

    fun clearAllHistory() {
        historyRepository.clearAllHistory(historyKey)
        _searchHistory.value = emptyList()
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("user")
                    .document(userId)
                    .delete()
                    .await()

                _searchResults.value = _searchResults.value?.filter { it.id != userId }

            } catch (e: Exception) {
                _error.value = "Failed to delete user: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSearchResults() {
        _searchResults.value = null
    }
}
