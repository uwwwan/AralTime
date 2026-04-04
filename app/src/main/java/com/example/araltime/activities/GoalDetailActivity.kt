package com.example.araltime.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.araltime.R
import com.example.araltime.firebase.FirebaseHelper
import com.example.araltime.models.Goal
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import java.util.*

class GoalDetailActivity : AppCompatActivity() {

    private lateinit var etTitle: TextInputEditText
    private lateinit var etTarget: TextInputEditText
    private lateinit var tvProgress: MaterialTextView
    private lateinit var btnSave: MaterialButton
    private lateinit var btnAchieve: MaterialButton
    private lateinit var btnDelete: MaterialButton

    private var goalId: String? = null
    private var currentGoal: Goal? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal_detail)

        goalId = intent.getStringExtra("goalId")
        
        initViews()
        setupClickListeners()
        
        if (goalId != null) {
            loadGoal()
        }
    }

    private fun initViews() {
        etTitle = findViewById(R.id.etTitle)
        etTarget = findViewById(R.id.etTarget)
        tvProgress = findViewById(R.id.tvProgress)
        btnSave = findViewById(R.id.btnSave)
        btnAchieve = findViewById(R.id.btnAchieve)
        btnDelete = findViewById(R.id.btnDelete)
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            saveGoal()
        }

        btnAchieve.setOnClickListener {
            achieveGoal()
        }

        btnDelete.setOnClickListener {
            deleteGoal()
        }
    }

    private fun loadGoal() {
        goalId?.let { id ->
            FirebaseHelper.firestore.collection(FirebaseHelper.GOALS_COLLECTION)
                .document(id)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        currentGoal = document.toObject(Goal::class.java)
                        populateFields()
                    }
                }
        }
    }

    private fun populateFields() {
        currentGoal?.let { goal ->
            etTitle.setText(goal.title)
            etTarget.setText(goal.target.toString())
            tvProgress.text = "Current progress: ${goal.progress} minutes"
            
            if (goal.isCompleted) {
                btnAchieve.isEnabled = false
                btnAchieve.text = "✅ Already Completed"
            }
        }
    }

    private fun saveGoal() {
        val title = etTitle.text.toString().trim()
        val targetText = etTarget.text.toString().trim()

        if (title.isEmpty()) {
            etTitle.error = "Title is required"
            return
        }

        if (targetText.isEmpty()) {
            etTarget.error = "Target is required"
            return
        }

        val target = targetText.toIntOrNull() ?: 0
        if (target <= 0) {
            etTarget.error = "Target must be greater than 0"
            return
        }

        val goal = if (currentGoal != null) {
            currentGoal!!.copy(
                title = title,
                target = target
            )
        } else {
            Goal(
                goalId = UUID.randomUUID().toString(),
                userId = FirebaseHelper.getCurrentUserId(),
                title = title,
                target = target,
                progress = 0,
                isCompleted = false
            )
        }

        FirebaseHelper.firestore.collection(FirebaseHelper.GOALS_COLLECTION)
            .document(goal.goalId)
            .set(goal)
            .addOnSuccessListener {
                Toast.makeText(this, "Goal saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to save goal: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun achieveGoal() {
        currentGoal?.let { goal ->
            if (goal.isCompleted) return

            val updatedGoal = goal.copy(
                isCompleted = true,
                progress = goal.target
            )

            FirebaseHelper.firestore.collection(FirebaseHelper.GOALS_COLLECTION)
                .document(goal.goalId)
                .update(
                    "isCompleted", true,
                    "progress", goal.target
                )
                .addOnSuccessListener {
                    // Award points for achieving goal (100 points)
                    updateUserPoints(100)
                    Toast.makeText(this, "Goal achieved! +100 points", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to achieve goal: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun deleteGoal() {
        currentGoal?.let { goal ->
            FirebaseHelper.firestore.collection(FirebaseHelper.GOALS_COLLECTION)
                .document(goal.goalId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Goal deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to delete goal: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun updateUserPoints(pointsToAdd: Int) {
        val userId = FirebaseHelper.getCurrentUserId()
        FirebaseHelper.firestore.collection(FirebaseHelper.USERS_COLLECTION)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val currentPoints = document.getLong("points")?.toInt() ?: 0
                    val newPoints = currentPoints + pointsToAdd
                    
                    FirebaseHelper.firestore.collection(FirebaseHelper.USERS_COLLECTION)
                        .document(userId)
                        .update("points", newPoints)
                }
            }
    }
}
