package com.example.araltime.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.araltime.R
import com.google.android.material.button.MaterialButton

class OnboardingActivity : AppCompatActivity() {

    private lateinit var btnSignUp: MaterialButton
    private lateinit var btnLogin: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        btnSignUp = findViewById(R.id.btn_sign_up)
        btnLogin = findViewById(R.id.btn_login)
    }

    private fun setupClickListeners() {
        btnSignUp.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
        
        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
