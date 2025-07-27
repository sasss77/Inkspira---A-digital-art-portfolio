package com.example.inkspira_adigitalartportfolio

import android.app.Application
import android.util.Log
import com.cloudinary.android.MediaManager
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            // ✅ CRITICAL: Initialize Firebase FIRST and safely
            initializeFirebase()

            // ✅ SAFE: Initialize Cloudinary after Firebase
            initializeCloudinary()

            Log.d("MyApplication", "All services initialized successfully")

        } catch (e: Exception) {
            Log.e("MyApplication", "Initialization error: ${e.message}", e)
            // Don't crash - let app continue with degraded functionality
        }
    }

    private fun initializeFirebase() {
        try {
            // Initialize Firebase App if not already initialized
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }

            // ✅ CRITICAL: Enable offline support BEFORE any database operations
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)

            // ✅ IMPORTANT: Set database cache size to prevent memory issues
            FirebaseDatabase.getInstance().reference.keepSynced(false)

            Log.d("MyApplication", "Firebase initialized successfully")

        } catch (e: Exception) {
            Log.e("MyApplication", "Firebase initialization error: ${e.message}", e)
            throw e // Re-throw to prevent app from starting with broken Firebase
        }
    }

    private fun initializeCloudinary() {
        try {
            val config = HashMap<String, String>()

            // ✅ TODO: Replace these with your actual Cloudinary credentials
            config["cloud_name"] = "dtlckbilm"  // Replace with your cloud name
            config["api_key"] = "826431393446114"        // Replace with your API key
            config["api_secret"] = "f1KbTXyKGGC3-Iuq8c2zNiQNc5g"  // Replace with your API secret

            MediaManager.init(this, config)

            Log.d("MyApplication", "Cloudinary initialized successfully")

        } catch (e: Exception) {
            Log.e("MyApplication", "Cloudinary initialization error: ${e.message}", e)
            // Don't throw - Cloudinary failure shouldn't prevent app start
        }
    }
}
