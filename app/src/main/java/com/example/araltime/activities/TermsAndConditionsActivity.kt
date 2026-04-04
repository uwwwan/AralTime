package com.example.araltime.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.araltime.R
import com.google.android.material.button.MaterialButton

class TermsAndConditionsActivity : AppCompatActivity() {

    private lateinit var btnBack: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_and_conditions)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }
    }
}
