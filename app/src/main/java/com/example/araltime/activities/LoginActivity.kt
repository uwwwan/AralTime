package com.example.araltime.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.araltime.R
import com.example.araltime.firebase.FirebaseHelper
import com.example.araltime.models.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvSignup: MaterialTextView
    private lateinit var tvSendTicketRequest: MaterialTextView
    private lateinit var tvForgotPassword: MaterialTextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Check if user is already logged in
        if (FirebaseHelper.isUserLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        auth = FirebaseHelper.auth
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvSignup = findViewById(R.id.tvSignup)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        tvSendTicketRequest = findViewById(R.id.tvSendTicketRequest)
        tvSendTicketRequest.visibility = android.view.View.GONE
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        // Fixed: Click on "Signup" goes to SignupActivity
        tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show()
        }

        tvSendTicketRequest.setOnClickListener {
            sendTicketRequest()
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            return false
        }
        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            return false
        }
        return true
    }

    private fun loginUser(email: String, password: String) {
        btnLogin.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                btnLogin.isEnabled = true
                if (task.isSuccessful) {
                    checkUserStatus(email)
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun checkUserStatus(email: String) {
        FirebaseHelper.firestore.collection(FirebaseHelper.USERS_COLLECTION)
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val user = documents.first().toObject(User::class.java)
                if (user?.isDisabled == true) {
                    showDisabledAccountMessage()
                } else {
                    Toast.makeText(this, "Welcome Back to AralTime!", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error checking user status: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showDisabledAccountMessage() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Account Disabled")
        builder.setMessage("Due to inactivity. The AralTime Administrator has temporarily disabled your account. If you wish to activate your account, Kindly send a ticket request. Thank you!")
        builder.setCancelable(false)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            tvSendTicketRequest.visibility = android.view.View.VISIBLE
        }

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setTextColor(getColor(android.R.color.holo_red_dark))
        }
        dialog.show()
    }

    private fun sendTicketRequest() {
        val email = etEmail.text.toString().trim()
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email first", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Ticket request sent successfully! Admin will be notified.", Toast.LENGTH_LONG).show()
        tvSendTicketRequest.visibility = android.view.View.GONE
    }
}