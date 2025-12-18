package com.example.miniproject.user

// UserType.kt

enum class UserType {
    STUDENT,
    STAFF,
    ADMIN
}

object UserTypeManager {
    var currentUserType: UserType? = null
        private set

    fun setUserType(type: UserType) {
        currentUserType = type
    }

    fun clearUserType() {
        currentUserType = null
    }
}