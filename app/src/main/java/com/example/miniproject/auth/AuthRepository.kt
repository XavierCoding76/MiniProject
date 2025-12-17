package com.example.miniproject.auth

import com.example.miniproject.user.User
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class AuthRepository{

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
                displayId = "U-${firebaseUser.uid.take(6).uppercase()}", // Corrected to displayId
                email = email,
                name = name,
                role = "user"
            )
            usersCollection.document(firebaseUser.uid).set(newUser).await()
        }
        return authResult
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
     * This performs a targeted query on Firestore, which is much more efficient
     * than downloading all users and filtering on the device.
     */
    suspend fun findUserByDisplayId(displayId: String): User? {
        val snapshot = FirebaseFirestore.getInstance()
            .collection("user")
            .whereEqualTo("displayId", displayId)
            .limit(1)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject(User::class.java)
    }


    fun signOut(){
        FirebaseManager.auth.signOut()
    }
}
