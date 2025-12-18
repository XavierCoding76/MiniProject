package com.example.miniproject.user

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val id: String = "", // The document ID, same as Firebase Auth UID
    val displayId: String = "", // Corrected to camelCase for consistency
    val email: String = "",
    val name: String = "",
    val role: String = "student", // Default user type
)