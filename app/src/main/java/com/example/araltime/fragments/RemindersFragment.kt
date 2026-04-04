package com.example.araltime.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.araltime.R
import com.example.araltime.activities.ReminderDetailActivity
import com.example.araltime.adapters.RemindersAdapter
import com.example.araltime.firebase.FirebaseHelper
import com.example.araltime.models.Reminder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class RemindersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var fabAddReminder: FloatingActionButton
    private lateinit var remindersAdapter: RemindersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reminders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupRecyclerView()
        setupClickListeners()
        loadReminders()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        fabAddReminder = view.findViewById(R.id.fabAddReminder)
    }

    private fun setupRecyclerView() {
        remindersAdapter = RemindersAdapter(emptyList()) { reminder ->
            // Navigate to Reminder Detail Activity
            val intent = Intent(requireContext(), ReminderDetailActivity::class.java)
            intent.putExtra("reminderId", reminder.reminderId)
            startActivity(intent)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = remindersAdapter
        }
    }

    private fun setupClickListeners() {
        fabAddReminder.setOnClickListener {
            // Navigate to Create Reminder Activity
            val intent = Intent(requireContext(), ReminderDetailActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadReminders() {
        val userId = FirebaseHelper.getCurrentUserId()
        if (userId.isEmpty()) return

        FirebaseHelper.firestore.collection(FirebaseHelper.REMINDERS_COLLECTION)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val reminders = documents.mapNotNull { doc ->
                    doc.toObject(Reminder::class.java)
                }
                
                if (reminders.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    tvEmpty.visibility = View.GONE
                    remindersAdapter.updateReminders(reminders)
                }
            }
            .addOnFailureListener { exception ->
                // Handle error
                recyclerView.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Error loading reminders: ${exception.message}"
            }
    }

    override fun onResume() {
        super.onResume()
        loadReminders() // Refresh reminders when fragment resumes
    }
}
