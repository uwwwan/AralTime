package com.example.araltime.admin

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.araltime.R
import com.example.araltime.activities.LoginActivity
import com.example.araltime.firebase.FirebaseHelper
import com.example.araltime.utils.DialogUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var tvTotalUsers: TextView
    private lateinit var tvTotalSessions: TextView
    private lateinit var tvPremiumUsers: TextView
    private lateinit var tvAvgWeeklyHours: TextView
    private lateinit var cardUserManagement: MaterialCardView
    private lateinit var cardAnalytics: MaterialCardView
    private lateinit var btnLogout: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        initViews()
        setupClickListeners()
        loadDashboardData()
    }

    private fun initViews() {
        tvTotalUsers = findViewById(R.id.tvTotalUsers)
        tvTotalSessions = findViewById(R.id.tvTotalSessions)
        tvPremiumUsers = findViewById(R.id.tvPremiumUsers)
        tvAvgWeeklyHours = findViewById(R.id.tvAvgWeeklyHours)
        cardUserManagement = findViewById(R.id.cardUserManagement)
        cardAnalytics = findViewById(R.id.cardAnalytics)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun setupClickListeners() {
        cardUserManagement.setOnClickListener {
            startActivity(Intent(this, AdminUserManagementActivity::class.java))
        }

        cardAnalytics.setOnClickListener {
            startActivity(Intent(this, AdminAnalyticsActivity::class.java))
        }

        btnLogout.setOnClickListener {
            DialogUtils.showLogoutConfirmation(this) {
                FirebaseHelper.auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun loadDashboardData() {
        loadTotalUsers()
        loadTotalSessions()
        loadPremiumUsers()
        loadAverageWeeklyHours()
    }

    private fun loadTotalUsers() {
        FirebaseHelper.firestore.collection(FirebaseHelper.USERS_COLLECTION)
            .get()
            .addOnSuccessListener { documents ->
                tvTotalUsers.text = documents.size().toString()
            }
            .addOnFailureListener {
                tvTotalUsers.text = "Error"
            }
    }

    private fun loadTotalSessions() {
        FirebaseHelper.firestore.collection(FirebaseHelper.SESSIONS_COLLECTION)
            .get()
            .addOnSuccessListener { documents ->
                tvTotalSessions.text = documents.size().toString()
            }
            .addOnFailureListener {
                tvTotalSessions.text = "Error"
            }
    }

    private fun loadPremiumUsers() {
        FirebaseHelper.firestore.collection(FirebaseHelper.USERS_COLLECTION)
            .whereEqualTo("isPremium", true)
            .get()
            .addOnSuccessListener { documents ->
                tvPremiumUsers.text = documents.size().toString()
            }
            .addOnFailureListener {
                tvPremiumUsers.text = "Error"
            }
    }

    private fun loadAverageWeeklyHours() {
        FirebaseHelper.firestore.collection(FirebaseHelper.SESSIONS_COLLECTION)
            .get()
            .addOnSuccessListener { documents ->
                val totalMinutes = documents.sumOf { doc ->
                    doc.getLong("duration") ?: 0L
                } / 60 // Convert to minutes
                
                val totalHours = totalMinutes / 60.0
                val avgWeeklyHours = if (!documents.isEmpty) {
                    totalHours / documents.size().toDouble()
                } else 0.0
                
                tvAvgWeeklyHours.text = String.format("%.1f", avgWeeklyHours)
            }
            .addOnFailureListener {
                tvAvgWeeklyHours.text = "Error"
            }
    }
}
