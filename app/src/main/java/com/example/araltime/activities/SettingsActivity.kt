package com.example.araltime.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Switch
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import com.example.araltime.R
import com.example.araltime.firebase.FirebaseHelper
import com.example.araltime.utils.DialogUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView

class SettingsActivity : AppCompatActivity() {

    private lateinit var tvTitle: MaterialTextView
    private lateinit var cardNotifications: MaterialCardView
    private lateinit var cardTheme: MaterialCardView
    private lateinit var cardLanguage: MaterialCardView
    private lateinit var cardPrivacy: MaterialCardView
    private lateinit var cardAbout: MaterialCardView
    private lateinit var cardTerms: MaterialCardView
    private lateinit var cardHelp: MaterialCardView
    private lateinit var switchNotifications: Switch
    private lateinit var switchDarkMode: Switch
    private lateinit var btnLogout: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        tvTitle = findViewById(R.id.tvTitle)
        cardNotifications = findViewById(R.id.cardNotifications)
        cardTheme = findViewById(R.id.cardTheme)
        cardLanguage = findViewById(R.id.cardLanguage)
        cardPrivacy = findViewById(R.id.cardPrivacy)
        cardAbout = findViewById(R.id.cardAbout)
        cardTerms = findViewById(R.id.cardTerms)
        cardHelp = findViewById(R.id.cardHelp)
        switchNotifications = findViewById(R.id.switchNotifications)
        switchDarkMode = findViewById(R.id.switchDarkMode)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun setupClickListeners() {
        cardNotifications.setOnClickListener {
            // Toggle notification switch
            switchNotifications.isChecked = !switchNotifications.isChecked
        }

        cardTheme.setOnClickListener {
            // Toggle dark mode switch
            switchDarkMode.isChecked = !switchDarkMode.isChecked
        }

        cardLanguage.setOnClickListener {
            // Open language settings (placeholder)
            showToast("Language settings coming soon!")
        }

        cardPrivacy.setOnClickListener {
            // Open privacy settings (placeholder)
            showToast("Privacy settings coming soon!")
        }

        cardAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        cardTerms.setOnClickListener {
            startActivity(Intent(this, TermsAndConditionsActivity::class.java))
        }

        cardHelp.setOnClickListener {
            // Open help/support (placeholder)
            showToast("Help & Support coming soon!")
        }

        btnLogout.setOnClickListener {
            logoutUser()
        }

        // Switch listeners
        switchNotifications.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            // Handle notification preference
            showToast("Notifications ${if (isChecked) "enabled" else "disabled"}")
        }

        switchDarkMode.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            // Handle dark mode preference (placeholder)
            showToast("Dark mode ${if (isChecked) "enabled" else "disabled"}")
        }
    }

    private fun logoutUser() {
        DialogUtils.showLogoutConfirmation(this) {
            FirebaseHelper.auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
