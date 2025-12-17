package com.example.miniproject.facility

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.Date


data class Facility(
    @DocumentId
    val id: String = "",

    val name: String = "",

    // 3. Correct syntax for default value assignment.
    val maxNum: Int = 0,

    val minNum: Int = 0,

    val startTime: String = "",

    val endTime: String = "",

    // 4. CRITICAL: Add the ownerId to create the relationship with the User.
    val description: String = "",

    val location: String = ""
)
