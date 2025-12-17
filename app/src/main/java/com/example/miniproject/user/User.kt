package com.example.miniproject.user

data class User(

    @DocumentId
    val id: String = "", // The document ID, same as Firebase Auth UID
    val displayId: String = "", // Corrected to camelCase for consistency
    val email: String = "",
    val name: String = "",
    val userType: String = "student", // Default user type
    val displayId: String
)