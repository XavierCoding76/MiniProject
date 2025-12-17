package com.example.miniproject.admin.userAdmin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserInformationState(
    val user: UserInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteDialog: Boolean = false
)

data class EditUserState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showEditDialog: Boolean = false,
    val showPasswordSection: Boolean = false
)

data class UserInfo(
    val id: String,
    val displayId: String,
    val name: String, // CHANGED from username to name
    val email: String,
    val role: String = "User",
    val isActive: Boolean = true,
    val requirePasswordChange: Boolean = false,
    val createdAt: String? = null
)

class UserInformationViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _userState = MutableStateFlow(UserInformationState())
    val userState = _userState.asStateFlow()

    private val _editState = MutableStateFlow(EditUserState())
    val editState = _editState.asStateFlow()

    // In loadUser function, change to search by displayId or email
    fun loadUser(userId: String, userType: String) {
        viewModelScope.launch {
            try {
                _userState.value = _userState.value.copy(isLoading = true, error = null)

                // Try to get user by displayId (since userId might be displayId)
                val querySnapshot = firestore.collection("user")
                    .whereEqualTo("displayId", userId)
                    .limit(1)
                    .get()
                    .await()

                if (querySnapshot.documents.isNotEmpty()) {
                    val document = querySnapshot.documents[0]
                    val data = document.data ?: emptyMap()

                    val user = UserInfo(
                        id = document.id,  // This is the Auth UID
                        displayId = data["displayId"] as? String ?: "No ID",
                        name = data["name"] as? String ?: "No Name",
                        email = data["email"] as? String ?: "No Email",
                        role = data["role"] as? String ?: userType,
                        isActive = data["isActive"] as? Boolean ?: true,
                        requirePasswordChange = data["requirePasswordChange"] as? Boolean ?: false,
                        createdAt = data["createdAt"] as? String
                    )

                    _userState.value = _userState.value.copy(
                        user = user,
                        isLoading = false
                    )
                } else {
                    // Try to get by document ID (Auth UID)
                    val document = firestore.collection("user")
                        .document(userId)
                        .get()
                        .await()

                    if (document.exists()) {
                        val data = document.data ?: emptyMap()

                        val user = UserInfo(
                            id = document.id,
                            displayId = data["displayId"] as? String ?: "No ID",
                            name = data["name"] as? String ?: "No Name",
                            email = data["email"] as? String ?: "No Email",
                            role = data["role"] as? String ?: userType,
                            isActive = data["isActive"] as? Boolean ?: true,
                            requirePasswordChange = data["requirePasswordChange"] as? Boolean ?: false,
                            createdAt = data["createdAt"] as? String
                        )

                        _userState.value = _userState.value.copy(
                            user = user,
                            isLoading = false
                        )
                    } else {
                        _userState.value = _userState.value.copy(
                            error = "User not found",
                            isLoading = false
                        )
                    }
                }

            } catch (e: Exception) {
                _userState.value = _userState.value.copy(
                    error = "Failed to load user: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private suspend fun searchUserByDisplayId(displayId: String, userType: String) {
        try {
            println("🔍 DEBUG: Searching by displayId: $displayId")

            // First try exact match
            var querySnapshot = firestore.collection("user")
                .whereEqualTo("displayId", displayId)
                .limit(1)
                .get()
                .await()

            // If not found, try with "S" prefix
            if (querySnapshot.isEmpty && !displayId.startsWith("S", ignoreCase = true)) {
                val displayIdWithS = "S$displayId"
                println("🔍 DEBUG: Trying with S prefix: $displayIdWithS")

                querySnapshot = firestore.collection("user")
                    .whereEqualTo("displayId", displayIdWithS.uppercase())
                    .limit(1)
                    .get()
                    .await()
            }

            if (querySnapshot.documents.isNotEmpty()) {
                val document = querySnapshot.documents[0]
                val data = document.data ?: emptyMap()

                println("✅ DEBUG: Found user by displayId!")
                println("✅ DEBUG: Document data: $data")

                val user = UserInfo(
                    id = document.id,
                    displayId = data["displayId"] as? String ?: "No ID",
                    name = data["name"] as? String ?: "No Name", // CHANGED
                    email = data["email"] as? String ?: "No Email",
                    role = data["role"] as? String ?: userType,
                    isActive = data["isActive"] as? Boolean ?: true,
                    requirePasswordChange = data["requirePasswordChange"] as? Boolean ?: false,
                    createdAt = data["createdAt"] as? String
                )

                println("✅ DEBUG: User loaded by displayId: ${user.name} (${user.displayId})")

                _userState.value = _userState.value.copy(
                    user = user,
                    isLoading = false
                )
            } else {
                println("❌ DEBUG: No user found with displayId: $displayId")
                _userState.value = _userState.value.copy(
                    error = "User not found with ID: $displayId",
                    isLoading = false
                )
            }

        } catch (e: Exception) {
            println("❌ DEBUG: Error searching by displayId: ${e.message}")
            _userState.value = _userState.value.copy(
                error = "Failed to search user: ${e.message}",
                isLoading = false
            )
        }
    }

    // Edit Dialog Functions
    fun showEditDialog(user: UserInfo) {
        _editState.value = _editState.value.copy(
            showEditDialog = true,
            name = user.name,
            email = user.email,
            showPasswordSection = false,
            password = "",
            confirmPassword = "",
            error = null
        )
    }

    fun hideEditDialog() {
        _editState.value = _editState.value.copy(showEditDialog = false)
    }

    fun updateName(name: String) {
        _editState.value = _editState.value.copy(name = name)
    }

    fun updateEmail(email: String) {
        _editState.value = _editState.value.copy(email = email)
    }

    fun updatePassword(password: String) {
        _editState.value = _editState.value.copy(password = password)
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _editState.value = _editState.value.copy(confirmPassword = confirmPassword)
    }

    fun togglePasswordSection() {
        _editState.value = _editState.value.copy(
            showPasswordSection = !_editState.value.showPasswordSection,
            password = "",
            confirmPassword = ""
        )
    }

    fun updateUser(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _editState.value = _editState.value.copy(isLoading = true, error = null)

                // Validate inputs
                if (_editState.value.name.isBlank()) {
                    throw IllegalArgumentException("Name cannot be empty")
                }

                if (_editState.value.email.isBlank()) {
                    throw IllegalArgumentException("Email cannot be empty")
                }

                if (_editState.value.showPasswordSection) {
                    if (_editState.value.password.isBlank()) {
                        throw IllegalArgumentException("Password cannot be empty")
                    }
                    if (_editState.value.password != _editState.value.confirmPassword) {
                        throw IllegalArgumentException("Passwords do not match")
                    }
                    if (_editState.value.password.length < 6) {
                        throw IllegalArgumentException("Password must be at least 6 characters")
                    }
                }

                // Prepare all updates in a single map
                val updates = mutableMapOf<String, Any>(
                    "name" to _editState.value.name,
                    "email" to _editState.value.email
                )

                // If password needs to be updated, add the flag
                if (_editState.value.showPasswordSection && _editState.value.password.isNotBlank()) {
                    updates["requirePasswordChange"] = true
                }

                // Update Firestore with all changes at once
                firestore.collection("user")
                    .document(userId)
                    .update(updates)
                    .await()

                // IMPORTANT: Update the local state immediately for instant UI feedback
                val currentUser = _userState.value.user
                if (currentUser != null) {
                    _userState.value = _userState.value.copy(
                        user = currentUser.copy(
                            name = _editState.value.name,
                            email = _editState.value.email,
                            requirePasswordChange = if (_editState.value.showPasswordSection) {
                                true
                            } else {
                                currentUser.requirePasswordChange
                            }
                        )
                    )
                }

                _editState.value = _editState.value.copy(isLoading = false)
                hideEditDialog()
                onSuccess()

            } catch (e: Exception) {
                _editState.value = _editState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Update failed"
                )
                onError(e.message ?: "Failed to update user")
            }
        }
    }
    // Delete Dialog Functions
    fun showDeleteConfirmation() {
        _userState.value = _userState.value.copy(showDeleteDialog = true)
    }

    fun hideDeleteConfirmation() {
        _userState.value = _userState.value.copy(showDeleteDialog = false)
    }

    fun deleteUser(userId: String, userType: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                firestore.collection("user")
                    .document(userId)
                    .delete()
                    .await()

                onSuccess()

            } catch (e: Exception) {
                onError("Failed to delete user: ${e.message}")
            }
        }
    }
}