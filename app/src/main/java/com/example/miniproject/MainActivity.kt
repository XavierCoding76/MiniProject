package com.example.miniproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.miniproject.ui.theme.MiniProjectTheme
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = FirebaseFirestore.getInstance()

        // Test write
        val testData = hashMapOf(
            "message" to "Hello Firestore!",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("test")
            .add(testData)
            .addOnSuccessListener {
                Log.d("FIRESTORE", "Write successful!")
            }
            .addOnFailureListener { e ->
                Log.w("FIRESTORE", "Error writing document", e)
            }

        setContent {
            // your compose app content
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MiniProjectTheme {
        Greeting("Android")
    }
}