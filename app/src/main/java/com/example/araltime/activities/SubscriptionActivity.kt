package com.example.araltime.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.araltime.R
import com.example.araltime.firebase.FirebaseHelper
import com.example.araltime.models.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

class SubscriptionActivity : AppCompatActivity() {

    private lateinit var tvBasePrice: MaterialTextView
    private lateinit var tvUserPoints: MaterialTextView
    private lateinit var tvDiscount: MaterialTextView
    private lateinit var tvFinalPrice: MaterialTextView
    private lateinit var btnSubscribe: MaterialButton
    private lateinit var tvPremiumFeatures: MaterialTextView

    private val basePrice = 59.0
    private var currentUser: User? = null
    private var discountPercent = 0
    private var finalPrice = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription)

        initViews()
        loadUserData()
        setupClickListeners()
    }

    private fun initViews() {
        tvBasePrice = findViewById(R.id.tvBasePrice)
        tvUserPoints = findViewById(R.id.tvUserPoints)
        tvDiscount = findViewById(R.id.tvDiscount)
        tvFinalPrice = findViewById(R.id.tvFinalPrice)
        btnSubscribe = findViewById(R.id.btnSubscribe)
        tvPremiumFeatures = findViewById(R.id.tvPremiumFeatures)

        tvBasePrice.text = "Base Price: ₱$basePrice"
        
        // Set premium features text
        val features = "✨ Premium Features:\n\n" +
                "• Unlock all themes\n" +
                "• Custom mood icons\n" +
                "• Subject badge colors\n" +
                "• Advanced analytics\n" +
                "• Priority support\n" +
                "• No ads experience"
        tvPremiumFeatures.text = features
    }

    private fun loadUserData() {
        val userId = FirebaseHelper.getCurrentUserId()
        if (userId.isEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        FirebaseHelper.firestore.collection(FirebaseHelper.USERS_COLLECTION)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    currentUser = document.toObject(User::class.java)
                    updatePricingDisplay()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading user data: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updatePricingDisplay() {
        currentUser?.let { user ->
            // Calculate discount using FirebaseHelper
            discountPercent = FirebaseHelper.calculateDiscountPercent(user.points)
            finalPrice = FirebaseHelper.calculateFinalPrice(user.points)

            // Update UI
            tvUserPoints.text = "Your Points: ${user.points}"
            tvDiscount.text = "Discount: $discountPercent%"
            
            if (finalPrice == 0.0) {
                tvFinalPrice.text = "Final Price: FREE!"
                btnSubscribe.text = "Unlock Premium"
            } else {
                tvFinalPrice.text = "Final Price: ₱${String.format("%.2f", finalPrice)}"
                btnSubscribe.text = "Subscribe Now"
            }

            // If already premium, update button
            if (user.isPremium) {
                btnSubscribe.text = "Already Premium"
                btnSubscribe.isEnabled = false
                btnSubscribe.backgroundTintList = getColorStateList(android.R.color.darker_gray)
            }
        }
    }

    private fun setupClickListeners() {
        btnSubscribe.setOnClickListener {
            if (currentUser?.isPremium == true) {
                Toast.makeText(this, "You already have premium access!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simulate payment process
            simulatePayment()
        }
    }

    private fun simulatePayment() {
        val userId = FirebaseHelper.getCurrentUserId()
        if (userId.isEmpty()) return

        // Show loading state
        btnSubscribe.isEnabled = false
        btnSubscribe.text = "Processing..."

        // Simulate payment delay
        btnSubscribe.postDelayed({
            // Update user premium status using FirebaseHelper
            FirebaseHelper.updatePremiumStatus(
                userId = userId,
                isPremium = true,
                premiumSource = "paid",
                discountPercent = discountPercent,
                finalPrice = finalPrice
            ) { success ->
                if (success) {
                    Toast.makeText(
                        this,
                        if (finalPrice == 0.0) "Premium unlocked successfully! 🎉"
                        else "Subscription successful! 🎉",
                        Toast.LENGTH_LONG
                    ).show()
                    
                    // Navigate back to profile
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Failed to process subscription", Toast.LENGTH_LONG).show()
                    // Reset button
                    btnSubscribe.isEnabled = true
                    updatePricingDisplay()
                }
            }
        }, 1500) // Simulate 1.5 second payment processing
    }
}
