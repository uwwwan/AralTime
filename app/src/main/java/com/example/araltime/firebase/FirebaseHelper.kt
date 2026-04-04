package com.example.araltime.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class FirebaseHelper {
    companion object {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        
        const val USERS_COLLECTION = "users"
        const val SESSIONS_COLLECTION = "sessions"
        const val SUBJECTS_COLLECTION = "subjects"
        const val GOALS_COLLECTION = "goals"
        const val REMINDERS_COLLECTION = "reminders"
        
        // Point system constants (DEMO MODE - ACCELERATED)
        const val POINTS_PER_MINUTE = 30
        const val POINTS_FOR_DISCOUNT = 100
        const val BASE_SUBSCRIPTION_PRICE = 59.0
        const val MAX_DISCOUNT_PERCENT = 100
        
        fun getCurrentUserId(): String {
            return auth.currentUser?.uid ?: ""
        }
        
        fun isUserLoggedIn(): Boolean {
            return auth.currentUser != null
        }
        
        /**
         * Calculate discount percentage based on user points
         * DEMO MODE: 100 points = 1% discount
         */
        fun calculateDiscountPercent(points: Long): Int {
            return ((points / POINTS_FOR_DISCOUNT).toInt()).coerceAtMost(MAX_DISCOUNT_PERCENT)
        }
        
        /**
         * Calculate final subscription price after discount
         */
        fun calculateFinalPrice(points: Long): Double {
            val discountPercent = calculateDiscountPercent(points)
            val finalPrice = BASE_SUBSCRIPTION_PRICE - (BASE_SUBSCRIPTION_PRICE * discountPercent / 100)
            return if (finalPrice < 0) 0.0 else finalPrice
        }
        
        /**
         * Add points to user account after study session
         * DEMO MODE: 1 minute = 30 points
         */
        fun addPointsToUser(userId: String, durationMinutes: Long, onComplete: (Boolean) -> Unit) {
            val pointsToAdd = durationMinutes * POINTS_PER_MINUTE
            
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val currentPoints = document.getLong("points") ?: 0L
                        val newPoints = currentPoints + pointsToAdd
                        
                        firestore.collection(USERS_COLLECTION)
                            .document(userId)
                            .update("points", newPoints)
                            .addOnSuccessListener {
                                onComplete(true)
                            }
                            .addOnFailureListener {
                                onComplete(false)
                            }
                    } else {
                        onComplete(false)
                    }
                }
                .addOnFailureListener {
                    onComplete(false)
                }
        }
        
        /**
         * Update user premium status after successful subscription
         */
        fun updatePremiumStatus(
            userId: String,
            isPremium: Boolean,
            premiumSource: String = "paid",
            discountPercent: Int = 0,
            finalPrice: Double = 0.0,
            onComplete: (Boolean) -> Unit
        ) {
            val updates = mapOf(
                "isPremium" to isPremium,
                "premiumSource" to premiumSource,
                "discountPercent" to discountPercent,
                "finalPrice" to finalPrice
            )
            
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .update(updates)
                .addOnSuccessListener {
                    onComplete(true)
                }
                .addOnFailureListener {
                    onComplete(false)
                }
        }
    }
}
