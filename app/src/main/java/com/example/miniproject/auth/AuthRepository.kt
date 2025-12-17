package com.example.miniproject.auth

import com.example.miniproject.user.User
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val usersCollection = FirebaseManager.firestore.collection("users")

    suspend fun signIn(email: String, password: String): AuthResult {
        return FirebaseManager.auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signUp(email: String, password: String, name: String): AuthResult {
        val authResult = FirebaseManager.auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user
        if (firebaseUser != null) {
            val newUser = User(
                id = firebaseUser.uid,
                displayId = "U-${firebaseUser.uid.take(6).uppercase()}",
                email = email,
                name = name,
                role = "user"
            )
            usersCollection.document(firebaseUser.uid).set(newUser).await()
        }
        return authResult
    }

    /**
     * Creates a new user (staff or student) by admin.
     * This function creates both Firebase Auth account and Firestore document.
     */
    suspend fun createUser(
        email: String,
        password: String,
        username: String,
        role: String // "staff" or "student"
    ): Result<User> {
        return try {
            // Create Firebase Auth account
            val authResult = FirebaseManager.auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // Generate display ID based on role
                val displayIdPrefix = when (role.lowercase()) {
                    "staff" -> "STF"
                    "student" -> "STU"
                    else -> "USR"
                }

                val newUser = User(
                    id = firebaseUser.uid,
                    displayId = "$displayIdPrefix-${firebaseUser.uid.take(6).uppercase()}",
                    email = email,
                    name = username,
                    role = role.lowercase()
                )

                // Save to Firestore
                usersCollection.document(firebaseUser.uid).set(newUser).await()

                Result.success(newUser)
            } else {
                Result.failure(Exception("Failed to create user account"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves a single user's data by their unique Firebase Auth UID.
     */
    suspend fun getUserData(id: String): User? {
        val documentSnapshot = usersCollection.document(id).get().await()
        return documentSnapshot.toObject<User>()
    }

    /**
     * Finds a single user by their display ID.
     */
    suspend fun findUserByDisplayId(displayId: String): User? {
        val snapshot = usersCollection
            .whereEqualTo("displayId", displayId)
            .limit(1)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject(User::class.java)
    }

    /**
     * Gets all users with a specific role (staff or student).
     */
    suspend fun getUsersByRole(role: String): List<User> {
        val snapshot = usersCollection
            .whereEqualTo("role", role.lowercase())
            .get()
            .await()

        return snapshot.documents.mapNotNull { it.toObject(User::class.java) }
    }

    /**
     * Updates user information in Firestore.
     * Note: This does NOT update Firebase Auth email/password.
     */
    suspend fun updateUser(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            usersCollection.document(userId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates a user's email in both Firebase Auth and Firestore.
     */
    suspend fun updateUserEmail(userId: String, newEmail: String): Result<Unit> {
        return try {
            // Update in Firebase Auth (requires user to be signed in)
            val currentUser = FirebaseManager.auth.currentUser
            if (currentUser?.uid == userId) {
                currentUser.updateEmail(newEmail).await()
            }

            // Update in Firestore
            usersCollection.document(userId).update("email", newEmail).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a user from both Firebase Auth and Firestore.
     * Note: Deleting from Firebase Auth requires the user to be recently authenticated.
     */
    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            // Delete from Firestore first
            usersCollection.document(userId).delete().await()

            // Note: Deleting from Firebase Auth can only be done by the user themselves
            // or through Firebase Admin SDK on the backend
            // For now, we just delete the Firestore document

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Searches users by display ID pattern (for search functionality).
     */
    suspend fun searchUsersByDisplayId(searchQuery: String): List<User> {
        return try {
            // Firestore doesn't support full-text search, so we need to get all users
            // and filter on the client side for partial matches
            val snapshot = usersCollection.get().await()
            val allUsers = snapshot.documents.mapNotNull { it.toObject(User::class.java) }

            allUsers.filter { user ->
                user.displayId.contains(searchQuery, ignoreCase = true)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Searches users by name pattern.
     */
    suspend fun searchUsersByName(searchQuery: String): List<User> {
        return try {
            val snapshot = usersCollection.get().await()
            val allUsers = snapshot.documents.mapNotNull { it.toObject(User::class.java) }

            allUsers.filter { user ->
                user.name.contains(searchQuery, ignoreCase = true)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Gets all users (for admin purposes).
     */
    suspend fun getAllUsers(): List<User> {
        return try {
            val snapshot = usersCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(User::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun signOut() {
        FirebaseManager.auth.signOut()
    }
}