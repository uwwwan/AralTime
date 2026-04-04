package com.example.araltime.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.araltime.R
import com.example.araltime.firebase.FirebaseHelper
import com.example.araltime.models.StudySession
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import java.util.UUID

class LogSessionActivity : AppCompatActivity() {

    private lateinit var etSubject: TextInputEditText
    private lateinit var etActivity: TextInputEditText
    private lateinit var etTopic: TextInputEditText
    private lateinit var etDuration: TextInputEditText
    private lateinit var etNotes: TextInputEditText
    private lateinit var tvSelectedMood: MaterialTextView
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton

    private var duration: Long = 0
    private var selectedMood: String = ""

    private val moodOptions = listOf("😊", "😐", "😔", "😤", "🤔")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_session)

        duration = intent.getLongExtra("duration", 0)
        
        initViews()
        setupClickListeners()
        setupMoodSelection()
    }

    private fun initViews() {
        etSubject = findViewById(R.id.etSubject)
        etActivity = findViewById(R.id.etActivity)
        etTopic = findViewById(R.id.etTopic)
        etDuration = findViewById(R.id.etDuration)
        etNotes = findViewById(R.id.etNotes)
        tvSelectedMood = findViewById(R.id.tvSelectedMood)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)

        // Auto-fill duration field
        if (duration > 0) {
            val hours = duration / 3600000
            val minutes = (duration % 3600000) / 60000
            val seconds = (duration % 60000) / 1000
            val durationText = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            etDuration.setText(durationText)
        }

        // Set up activity spinner
        setupActivitySpinner()
    }

    private fun setupActivitySpinner() {
        val activities = arrayOf("Reading", "Quiz", "Reviewing", "Noting", "Assignment", "Other")
        // TODO: Implement proper spinner setup
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            if (validateInput()) {
                saveSession()
            }
        }

        btnCancel.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun setupMoodSelection() {
        val moodContainer = findViewById<android.widget.LinearLayout>(R.id.moodContainer)
        
        moodOptions.forEach { mood ->
            val moodView = MaterialTextView(this).apply {
                text = mood
                textSize = 32f
                setPadding(16, 16, 16, 16)
                setOnClickListener {
                    selectMood(mood)
                }
            }
            moodContainer.addView(moodView)
        }
    }

    private fun selectMood(mood: String) {
        selectedMood = mood
        tvSelectedMood.text = "Selected mood: $mood"
        
        // Update visual feedback
        val moodContainer = findViewById<android.widget.LinearLayout>(R.id.moodContainer)
        for (i in 0 until moodContainer.childCount) {
            val child = moodContainer.getChildAt(i) as MaterialTextView
            if (child.text == mood) {
                child.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light, null))
            } else {
                child.setBackgroundColor(resources.getColor(android.R.color.transparent, null))
            }
        }
    }

    private fun validateInput(): Boolean {
        if (etSubject.text.toString().trim().isEmpty()) {
            etSubject.error = "Subject is required"
            return false
        }
        if (etActivity.text.toString().trim().isEmpty()) {
            etActivity.error = "Activity is required"
            return false
        }
        if (selectedMood.isEmpty()) {
            Toast.makeText(this, "Please select your mood", Toast.LENGTH_SHORT).show()
            return false
        }
        
        // Parse duration from input field
        val durationInput = etDuration.text.toString().trim()
        if (durationInput.isNotEmpty()) {
            try {
                val parts = durationInput.split(":")
                val hours = parts.getOrNull(0)?.toLongOrNull() ?: 0
                val minutes = parts.getOrNull(1)?.toLongOrNull() ?: 0
                val seconds = parts.getOrNull(2)?.toLongOrNull() ?: 0
                duration = (hours * 3600 + minutes * 60 + seconds) * 1000
            } catch (e: Exception) {
                etDuration.error = "Invalid duration format"
                return false
            }
        }
        
        return true
    }

    private fun saveSession() {
        val session = StudySession(
            sessionId = UUID.randomUUID().toString(),
            userId = FirebaseHelper.getCurrentUserId(),
            subject = etSubject.text.toString().trim(),
            activity = etActivity.text.toString().trim(),
            topic = etTopic.text.toString().trim(),
            duration = duration / 1000, // Convert to seconds
            mood = selectedMood,
            timestamp = System.currentTimeMillis(),
            notes = etNotes.text.toString().trim()
        )

        FirebaseHelper.firestore.collection(FirebaseHelper.SESSIONS_COLLECTION)
            .document(session.sessionId)
            .set(session)
            .addOnSuccessListener {
                // Update user points (1 minute = 30 points)
                val minutes = duration / 60000
                val pointsEarned = minutes.toInt() * 30
                
                updateUserPoints(pointsEarned)
                
                Toast.makeText(this, "Session saved successfully! +$pointsEarned points", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to save session: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateUserPoints(pointsEarned: Int) {
        val userId = FirebaseHelper.getCurrentUserId()
        FirebaseHelper.firestore.collection(FirebaseHelper.USERS_COLLECTION)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val currentPoints = document.getLong("points")?.toInt() ?: 0
                    val newPoints = currentPoints + pointsEarned
                    
                    FirebaseHelper.firestore.collection(FirebaseHelper.USERS_COLLECTION)
                        .document(userId)
                        .update("points", newPoints)
                }
            }
    }
}
