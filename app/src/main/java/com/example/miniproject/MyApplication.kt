package com.example.miniproject

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // This line initializes Firebase for the entire app, right at the start.
        FirebaseApp.initializeApp(this)
    }
}
