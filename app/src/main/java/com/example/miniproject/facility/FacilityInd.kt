package com.example.miniproject.facility
//
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.Date


data class FacilityInd(
    @DocumentId
    val id: String = "",

    val name: String = "",

    // 3. Correct syntax for default value assignment.
    val customMaxNum: Int = 0,

    val customMinNum: Int = 0,
    )
