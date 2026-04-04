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
import com.example.araltime.activities.GoalDetailActivity
import com.example.araltime.adapters.GoalsAdapter
import com.example.araltime.firebase.FirebaseHelper
import com.example.araltime.models.Goal
import com.google.android.material.floatingactionbutton.FloatingActionButton

class GoalsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var fabAddGoal: FloatingActionButton
    private lateinit var goalsAdapter: GoalsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_goals, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupRecyclerView()
        setupClickListeners()
        loadGoals()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        fabAddGoal = view.findViewById(R.id.fabAddGoal)
    }

    private fun setupRecyclerView() {
        goalsAdapter = GoalsAdapter(emptyList()) { goal ->
            // Navigate to Goal Detail Activity
            val intent = Intent(requireContext(), GoalDetailActivity::class.java)
            intent.putExtra("goalId", goal.goalId)
            startActivity(intent)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = goalsAdapter
        }
    }

    private fun setupClickListeners() {
        fabAddGoal.setOnClickListener {
            // Navigate to Create Goal Activity
            val intent = Intent(requireContext(), GoalDetailActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadGoals() {
        val userId = FirebaseHelper.getCurrentUserId()
        if (userId.isEmpty()) return

        FirebaseHelper.firestore.collection(FirebaseHelper.GOALS_COLLECTION)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val goals = documents.mapNotNull { doc ->
                    doc.toObject(Goal::class.java)
                }
                
                if (goals.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    tvEmpty.visibility = View.GONE
                    goalsAdapter.updateGoals(goals)
                }
            }
            .addOnFailureListener { exception ->
                // Handle error
                recyclerView.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Error loading goals: ${exception.message}"
            }
    }

    override fun onResume() {
        super.onResume()
        loadGoals() // Refresh goals when fragment resumes
    }
}
