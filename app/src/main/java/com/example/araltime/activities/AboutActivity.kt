package com.example.araltime.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.araltime.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

class AboutActivity : AppCompatActivity() {

    private lateinit var tvAppName: MaterialTextView
    private lateinit var tvVersion: MaterialTextView
    private lateinit var tvDescription: MaterialTextView
    private lateinit var tvDeveloper: MaterialTextView
    private lateinit var tvContact: MaterialTextView
    private lateinit var btnRateApp: MaterialButton
    private lateinit var btnShareApp: MaterialButton
    private lateinit var btnBack: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        tvAppName = findViewById(R.id.tvAppName)
        tvVersion = findViewById(R.id.tvVersion)
        tvDescription = findViewById(R.id.tvDescription)
        tvDeveloper = findViewById(R.id.tvDeveloper)
        tvContact = findViewById(R.id.tvContact)
        btnRateApp = findViewById(R.id.btnRateApp)
        btnShareApp = findViewById(R.id.btnShareApp)
        btnBack = findViewById(R.id.btnBack)

        // Set app info
        tvAppName.text = "AralTime"
        tvVersion.text = "Version 1.0.0"
        tvDescription.text = "Your smart study companion that helps you track, manage, and optimize your learning journey. With AralTime, you can monitor your study sessions, set goals, manage reminders, and unlock premium features to enhance your learning experience."
        tvDeveloper.text = "Developed with ❤️ for students"
        tvContact.text = "Contact: support@araltime.app"
    }

    private fun setupClickListeners() {
        btnRateApp.setOnClickListener {
            // Open app store (placeholder)
            openUrl("https://play.google.com/store")
        }

        btnShareApp.setOnClickListener {
            shareApp()
        }

        btnBack.setOnClickListener {
            finish()
        }

        tvContact.setOnClickListener {
            // Open email client
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@araltime.app")
                putExtra(Intent.EXTRA_SUBJECT, "AralTime App Support")
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out AralTime - Your smart study companion! 📚✨\n\nDownload now and start tracking your study sessions like a pro!")
            putExtra(Intent.EXTRA_SUBJECT, "AralTime - Smart Study Companion")
        }
        startActivity(Intent.createChooser(shareIntent, "Share AralTime"))
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}
