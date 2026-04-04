package com.example.araltime.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.araltime.R
import com.example.araltime.firebase.FirebaseHelper
import com.example.araltime.models.Subject
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import java.util.*

class AddSubjectActivity : AppCompatActivity() {

    private lateinit var etSubjectName: TextInputEditText
    private lateinit var etSubjectColor: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var tvTitle: MaterialTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_subject)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        etSubjectName = findViewById(R.id.etSubjectName)
        etSubjectColor = findViewById(R.id.etSubjectColor)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        tvTitle = findViewById(R.id.tvTitle)

        // Set default color
        etSubjectColor.setText("#326c49")
        
        // Check if editing existing subject
        val subjectId = intent.getStringExtra("subjectId")
        if (subjectId != null) {
            tvTitle.text = "Edit Subject"
            loadSubjectData(subjectId)
        }
    }

    private fun loadSubjectData(subjectId: String) {
        FirebaseHelper.firestore.collection(FirebaseHelper.SUBJECTS_COLLECTION)
            .document(subjectId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val subject = document.toObject(Subject::class.java)
                    subject?.let {
                        etSubjectName.setText(it.name)
                        etSubjectColor.setText(it.color)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading subject: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            val name = etSubjectName.text.toString().trim()
            val color = etSubjectColor.text.toString().trim()

            if (validateInput(name, color)) {
                saveSubject(name, color)
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(name: String, color: String): Boolean {
        if (name.isEmpty()) {
            etSubjectName.error = "Subject name is required"
            return false
        }

        if (color.isEmpty()) {
            etSubjectColor.error = "Color is required"
            return false
        }

        // Simple color validation (hex format)
        if (!color.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$".toRegex())) {
            etSubjectColor.error = "Invalid color format (use #RRGGBB or #RGB)"
            return false
        }

        return true
    }

    private fun saveSubject(name: String, color: String) {
        val userId = FirebaseHelper.getCurrentUserId()
        if (userId.isEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val subjectId = intent.getStringExtra("subjectId") ?: UUID.randomUUID().toString()
        val subject = Subject(
            subjectId = subjectId,
            userId = userId,
            name = name,
            color = color,
            isPremium = false // Can be made configurable later
        )

        FirebaseHelper.firestore.collection(FirebaseHelper.SUBJECTS_COLLECTION)
            .document(subjectId)
            .set(subject)
            .addOnSuccessListener {
                Toast.makeText(this, "Subject saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to save subject: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
}
