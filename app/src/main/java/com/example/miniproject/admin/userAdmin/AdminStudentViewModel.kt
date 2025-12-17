package com.example.miniproject.admin.userAdmin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.components.SearchResultItemData
import com.example.miniproject.data.SearchHistoryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

@OptIn(FlowPreview::class)
class AdminStudentViewModel(application: Application) : AndroidViewModel(application) {
    private val historyRepository = SearchHistoryRepository(application)
    private val firestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = Firebase.auth
    private val historyKey = "student_search_history"

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
        val query = _searchText.value
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
                    searchWithPrefix(query)
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

    private suspend fun searchWithPrefix(query: String) {
        try {
            val querySnapshot = firestore.collection("user")
                .get()
                .await()

            val results = querySnapshot.documents.mapNotNull { document ->
                val displayId = document.getString("displayId") ?: return@mapNotNull null
                val name = document.getString("name") ?: "Unknown"
                val email = document.getString("email") ?: ""
                val role = document.getString("role") ?: "user"

                val isStudent = role.contains("student", ignoreCase = true) ||
                        !displayId.startsWith("S", ignoreCase = true)

                if (!isStudent) {
                    return@mapNotNull null
                }

                if (displayId.startsWith(query, ignoreCase = true) ||
                    displayId.contains(query, ignoreCase = true) ||
                    name.contains(query, ignoreCase = true) ||
                    email.contains(query, ignoreCase = true)) {
                    SearchResultItemData(
                        id = document.id,
                        title = "$name ($displayId)",
                        subtitle = email
                    )
                } else null
            }.sortedBy {
                try {
                    it.title.substringAfter("(").substringBefore(")").replace("S", "", ignoreCase = true).toIntOrNull() ?: Int.MAX_VALUE
                } catch (e: Exception) {
                    Int.MAX_VALUE
                }
            }

            _searchResults.value = results
        } catch (e: Exception) {
            _searchResults.value = emptyList()
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
                // First delete from Authentication
                auth.currentUser?.let {
                    // Note: You need admin privileges to delete other users
                    // For now, we'll just delete from Firestore
                }

                // Delete from Firestore
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

    suspend fun generateStudentDisplayId(): String {
        return try {
            val users = firestore.collection("user")
                .get()
                .await()

            val studentIds = users.documents.mapNotNull { document ->
                val displayId = document.getString("displayId") ?: ""
                val role = document.getString("role") ?: ""

                if (role.contains("student", ignoreCase = true) ||
                    !displayId.startsWith("S", ignoreCase = true)) {
                    displayId
                } else {
                    null
                }
            }

            var maxId = 0
            studentIds.forEach { id ->
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
            newId.toString()

        } catch (e: Exception) {
            "1001"
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
                val formattedDisplayId = displayId.uppercase(Locale.getDefault())

                // Check if display ID already exists in Firestore
                val existingIdQuery = firestore.collection("user")
                    .whereEqualTo("displayId", formattedDisplayId)
                    .get()
                    .await()

                if (!existingIdQuery.isEmpty) {
                    onError("Student with ID $formattedDisplayId already exists")
                    return@launch
                }

                // Check if email already exists in Firestore
                val existingEmailQuery = firestore.collection("user")
                    .whereEqualTo("email", email.lowercase(Locale.getDefault()))
                    .get()
                    .await()

                if (!existingEmailQuery.isEmpty) {
                    onError("Student with email $email already exists")
                    return@launch
                }

                // 1. Create user in Firebase Authentication with displayId as UID
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user

                if (user == null) {
                    onError("Failed to create authentication user")
                    return@launch
                }

                // 2. Update user profile with name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()

                user.updateProfile(profileUpdates).await()

                // 3. Store additional user data in Firestore using the same ID
                val studentData = hashMapOf(
                    "displayId" to formattedDisplayId,
                    "name" to name,
                    "email" to email.lowercase(Locale.getDefault()),
                    "role" to "student",
                    "authUid" to user.uid  // Store the Auth UID for reference
                )

                // Use the Auth UID as the Firestore document ID for consistency
                firestore.collection("user")
                    .document(user.uid)
                    .set(studentData)
                    .await()

                onSuccess()
            } catch (e: Exception) {
                onError("Failed to add student: ${e.message}")
            }
        }
    }
}