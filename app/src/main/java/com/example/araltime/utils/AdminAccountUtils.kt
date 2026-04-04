package com.example.araltime.utils

import com.example.araltime.firebase.FirebaseHelper
import com.example.araltime.models.User
import com.google.firebase.auth.FirebaseAuth

object AdminAccountUtils {
    
    fun createAdminAccount() {
        val auth = FirebaseHelper.auth
        val adminEmail = "admin01@araltime.com"
        val adminPassword = "AdminAcc@001"
        val adminName = "admin@01"
        
        // Create admin account in Firebase Auth
        auth.createUserWithEmailAndPassword(adminEmail, adminPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid ?: return@addOnCompleteListener
                    
                    // Create admin user document in Firestore
                    val adminUser = User(
                        uid = userId,
                        name = adminName,
                        email = adminEmail,
                        points = 0,
                        isPremium = true,
                        profileImage = "",
                        isAdmin = true
                    )
                    
                    FirebaseHelper.firestore.collection(FirebaseHelper.USERS_COLLECTION)
                        .document(userId)
                        .set(adminUser)
                        .addOnSuccessListener {
                            // Admin account created successfully
                        }
                        .addOnFailureListener { exception ->
                            // Handle error
                        }
                }
            }
            .addOnFailureListener { exception ->
                // Account might already exist, just update Firestore
                // You can handle this case as needed
            }
    }
}
