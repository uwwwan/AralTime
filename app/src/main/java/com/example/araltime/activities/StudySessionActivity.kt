package com.example.araltime.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.araltime.R
import com.example.araltime.firebase.FirebaseHelper
import com.google.android.material.button.MaterialButton

class StudySessionActivity : AppCompatActivity() {

    private lateinit var tvTimer: TextView
    private lateinit var btnStart: MaterialButton
    private lateinit var btnPause: MaterialButton
    private lateinit var btnEnd: MaterialButton

    private var isTimerRunning = false
    private var isPaused = false
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            if (isTimerRunning && !isPaused) {
                elapsedTime = System.currentTimeMillis() - startTime
                updateTimerDisplay()
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_session)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        tvTimer = findViewById(R.id.tvTimer)
        btnStart = findViewById(R.id.btnStart)
        btnPause = findViewById(R.id.btnPause)
        btnEnd = findViewById(R.id.btnEnd)
    }

    private fun setupClickListeners() {
        btnStart.setOnClickListener {
            if (!isTimerRunning) {
                startTimer()
            } else {
                resetTimer()
            }
        }

        btnPause.setOnClickListener {
            if (isTimerRunning) {
                if (isPaused) {
                    resumeTimer()
                } else {
                    pauseTimer()
                }
            }
        }

        btnEnd.setOnClickListener {
            stopTimer()
        }
    }

    private fun startTimer() {
        isTimerRunning = true
        isPaused = false
        startTime = System.currentTimeMillis()
        handler.post(runnable)
        
        btnStart.text = "Reset"
        btnPause.text = "Pause"
        btnPause.isEnabled = true
        btnEnd.isEnabled = true
    }

    private fun pauseTimer() {
        isPaused = true
        btnPause.text = "Resume"
    }

    private fun resumeTimer() {
        isPaused = false
        startTime = System.currentTimeMillis() - elapsedTime
        handler.post(runnable)
        btnPause.text = "Pause"
    }

    private fun stopTimer() {
        isTimerRunning = false
        isPaused = false
        handler.removeCallbacks(runnable)
        
        btnStart.text = "Start"
        btnPause.text = "Pause"
        btnPause.isEnabled = false
        btnEnd.isEnabled = false
        
        // Calculate duration in minutes and add points
        val durationMinutes = elapsedTime / 60000 // Convert milliseconds to minutes
        if (durationMinutes > 0) {
            val userId = FirebaseHelper.getCurrentUserId()
            if (userId.isNotEmpty()) {
                FirebaseHelper.addPointsToUser(userId, durationMinutes) { success ->
                    if (success) {
                        Toast.makeText(
                            this,
                            "Session completed! +${durationMinutes * FirebaseHelper.POINTS_PER_MINUTE} points earned! 🎉",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(this, "Session completed but failed to add points", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        
        // Navigate to log session if there's elapsed time
        if (elapsedTime > 0) {
            val intent = Intent(this, LogSessionActivity::class.java)
            intent.putExtra("duration", elapsedTime)
            startActivity(intent)
            finish()
        }
    }

    private fun resetTimer() {
        stopTimer()
        elapsedTime = 0
        updateTimerDisplay()
    }

    private fun updateTimerDisplay() {
        val totalSeconds = elapsedTime / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        tvTimer.text = timeString
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}
