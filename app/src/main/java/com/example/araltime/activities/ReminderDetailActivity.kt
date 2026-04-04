package com.example.araltime.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.araltime.R
import com.example.araltime.firebase.FirebaseHelper
import com.example.araltime.models.Reminder
import com.example.araltime.utils.ReminderBroadcastReceiver
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.*

class ReminderDetailActivity : AppCompatActivity() {

    private lateinit var etTitle: TextInputEditText
    private lateinit var etDate: TextInputEditText
    private lateinit var etTime: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnDelete: MaterialButton

    private var reminderId: String? = null
    private var currentReminder: Reminder? = null
    private var selectedDate: Long = 0
    private var selectedHour: Int = 0
    private var selectedMinute: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_detail)

        reminderId = intent.getStringExtra("reminderId")
        
        initViews()
        setupClickListeners()
        
        if (reminderId != null) {
            loadReminder()
        } else {
            // Set default values for new reminder
            selectedDate = MaterialDatePicker.todayInUtcMilliseconds()
            etDate.setText(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(selectedDate)))
            etTime.setText("12:00 PM")
            selectedHour = 12
            selectedMinute = 0
        }
    }

    private fun initViews() {
        etTitle = findViewById(R.id.etTitle)
        etDate = findViewById(R.id.etDate)
        etTime = findViewById(R.id.etTime)
        btnSave = findViewById(R.id.btnSave)
        btnDelete = findViewById(R.id.btnDelete)
    }

    private fun setupClickListeners() {
        etDate.setOnClickListener {
            showDatePicker()
        }

        etTime.setOnClickListener {
            showTimePicker()
        }

        btnSave.setOnClickListener {
            saveReminder()
        }

        btnDelete.setOnClickListener {
            deleteReminder()
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(selectedDate)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            selectedDate = selection
            etDate.setText(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(selection)))
        }

        datePicker.show(supportFragmentManager, "date_picker")
    }

    private fun showTimePicker() {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(selectedHour)
            .setMinute(selectedMinute)
            .setTitleText("Select Time")
            .build()

        timePicker.addOnPositiveButtonClickListener {
            selectedHour = timePicker.hour
            selectedMinute = timePicker.minute
            
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            calendar.set(Calendar.MINUTE, selectedMinute)
            
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            etTime.setText(timeFormat.format(calendar.time))
        }

        timePicker.show(supportFragmentManager, "time_picker")
    }

    private fun loadReminder() {
        reminderId?.let { id ->
            FirebaseHelper.firestore.collection(FirebaseHelper.REMINDERS_COLLECTION)
                .document(id)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        currentReminder = document.toObject(Reminder::class.java)
                        populateFields()
                    }
                }
        }
    }

    private fun populateFields() {
        currentReminder?.let { reminder ->
            etTitle.setText(reminder.title)
            etDate.setText(reminder.date)
            etTime.setText(reminder.time)
            btnDelete.visibility = android.view.View.VISIBLE
        }
    }

    private fun saveReminder() {
        val title = etTitle.text.toString().trim()
        val date = etDate.text.toString().trim()
        val time = etTime.text.toString().trim()

        if (title.isEmpty()) {
            etTitle.error = "Title is required"
            return
        }

        if (date.isEmpty()) {
            etDate.error = "Date is required"
            return
        }

        if (time.isEmpty()) {
            etTime.error = "Time is required"
            return
        }

        val reminder = if (currentReminder != null) {
            currentReminder!!.copy(
                title = title,
                date = date,
                time = time
            )
        } else {
            Reminder(
                reminderId = UUID.randomUUID().toString(),
                userId = FirebaseHelper.getCurrentUserId(),
                title = title,
                date = date,
                time = time
            )
        }

        FirebaseHelper.firestore.collection(FirebaseHelper.REMINDERS_COLLECTION)
            .document(reminder.reminderId)
            .set(reminder)
            .addOnSuccessListener {
                scheduleNotification(reminder)
                Toast.makeText(this, "Reminder saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to save reminder: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun deleteReminder() {
        currentReminder?.let { reminder ->
            FirebaseHelper.firestore.collection(FirebaseHelper.REMINDERS_COLLECTION)
                .document(reminder.reminderId)
                .delete()
                .addOnSuccessListener {
                    cancelNotification(reminder.reminderId)
                    Toast.makeText(this, "Reminder deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to delete reminder: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun scheduleNotification(reminder: Reminder) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Parse date and time to get timestamp
        val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        val dateTime = "$reminder.date ${reminder.time}"
        
        try {
            val date = dateFormat.parse(dateTime)
            date?.let {
                val triggerTime = it.time
                
                val intent = Intent(this, ReminderBroadcastReceiver::class.java).apply {
                    putExtra("reminderId", reminder.reminderId)
                    putExtra("title", reminder.title)
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    reminder.reminderId.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error scheduling notification: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun cancelNotification(reminderId: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(this, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
    }
}
