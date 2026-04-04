package com.example.araltime.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.araltime.R
import com.example.araltime.activities.GoalDetailActivity
import com.example.araltime.activities.LogSessionActivity
import com.example.araltime.activities.ReminderDetailActivity
import com.example.araltime.activities.StudySessionActivity
import com.example.araltime.adapters.GoalsAdapter
import com.example.araltime.adapters.StudyTipsAdapter
import com.example.araltime.firebase.FirebaseHelper
import com.example.araltime.models.Goal
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvTodayStudyTime: TextView
    private lateinit var tvPoints: TextView
    private lateinit var tvRecentActivity: TextView
    private lateinit var tvGoalsCount: TextView
    private lateinit var viewPagerTips: ViewPager2
    private lateinit var recyclerViewGoals: RecyclerView
    private lateinit var btnStartSession: MaterialButton
    private lateinit var btnAddSubject: MaterialButton
    private lateinit var btnSetReminder: MaterialButton
    private lateinit var btnSetGoals: MaterialButton

    private lateinit var goalsAdapter: GoalsAdapter
    private lateinit var tipsAdapter: StudyTipsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupStudyTips()
        setupQuickActions()
        setupGoalsRecyclerView()
        loadHomeData()
    }

    private fun initViews(view: View) {
        tvGreeting = view.findViewById(R.id.tvGreeting)
        tvTodayStudyTime = view.findViewById(R.id.tvTodayStudyTime)
        tvPoints = view.findViewById(R.id.tvPoints)
        tvRecentActivity = view.findViewById(R.id.tvRecentActivity)
        tvGoalsCount = view.findViewById(R.id.tvGoalsCount)
        viewPagerTips = view.findViewById(R.id.viewPagerTips)
        recyclerViewGoals = view.findViewById(R.id.recyclerViewGoals)
        btnStartSession = view.findViewById(R.id.btnStartSession)
        btnAddSubject = view.findViewById(R.id.btnAddSubject)
        btnSetReminder = view.findViewById(R.id.btnSetReminder)
        btnSetGoals = view.findViewById(R.id.btnSetGoals)
    }

    private fun setupStudyTips() {
        val studyTips = listOf(
            "📚 Take breaks every 25 minutes using the Pomodoro Technique",
            "🌱 Study in short, focused sessions for better retention",
            "💡 Use active recall instead of passive reading",
            "🎯 Set specific, achievable goals for each study session",
            "🧘‍♀️ Practice mindfulness before studying to improve focus",
            "📝 Take handwritten notes for better memory retention",
            "🌙 Review material before bed for better consolidation",
            "💧 Stay hydrated - your brain needs water to function optimally"
        )

        tipsAdapter = StudyTipsAdapter()
        viewPagerTips.adapter = tipsAdapter
    }

    private fun setupQuickActions() {
        btnStartSession.setOnClickListener {
            startActivity(Intent(requireContext(), StudySessionActivity::class.java))
        }

        btnAddSubject.setOnClickListener {
            startActivity(Intent(requireContext(), AddSubjectActivity::class.java))
        }

        btnSetReminder.setOnClickListener {
            startActivity(Intent(requireContext(), ReminderDetailActivity::class.java))
        }

        btnSetGoals.setOnClickListener {
            startActivity(Intent(requireContext(), GoalDetailActivity::class.java))
        }
    }

    private fun setupGoalsRecyclerView() {
        goalsAdapter = GoalsAdapter(emptyList()) { goal ->
            val intent = Intent(requireContext(), GoalDetailActivity::class.java)
            intent.putExtra("goalId", goal.goalId)
            startActivity(intent)
        }
        recyclerViewGoals.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = goalsAdapter
        }
    }

    private fun loadHomeData() {
        loadUserData()
        loadTodayStudyTime()
        loadRecentActivity()
        loadGoals()
    }

    private fun loadUserData() {
        val userId = FirebaseHelper.getCurrentUserId()
        if (userId.isEmpty()) return

        FirebaseHelper.firestore.collection(FirebaseHelper.USERS_COLLECTION)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name")
                    val points = document.getLong("points") ?: 0
                    
                    tvGreeting.text = "Hello, $name! 👋"
                    tvPoints.text = "$points points"
                }
            }
    }

    private fun loadTodayStudyTime() {
        val userId = FirebaseHelper.getCurrentUserId()
        if (userId.isEmpty()) return

        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())

        FirebaseHelper.firestore.collection(FirebaseHelper.SESSIONS_COLLECTION)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                var totalSeconds = 0
                documents.forEach { document ->
                    val timestamp = document.getLong("timestamp") ?: 0
                    val sessionDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        .format(java.util.Date(timestamp))
                    
                    if (sessionDate == today) {
                        totalSeconds += (document.getLong("duration") ?: 0).toInt()
                    }
                }

                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60
                val timeText = if (hours > 0) {
                    "${hours}h ${minutes}m"
                } else {
                    "${minutes}m"
                }
                tvTodayStudyTime.text = timeText
            }
    }

    private fun loadRecentActivity() {
        val userId = FirebaseHelper.getCurrentUserId()
        if (userId.isEmpty()) return

        FirebaseHelper.firestore.collection(FirebaseHelper.SESSIONS_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty()) {
                    tvRecentActivity.text = "No recent activity"
                } else {
                    val session = documents.first().toObject(com.example.araltime.models.StudySession::class.java)
                    tvRecentActivity.text = "Last studied: ${session.subject}"
                }
            }
    }

    private fun loadGoals() {
        val userId = FirebaseHelper.getCurrentUserId()
        if (userId.isEmpty()) return

        FirebaseHelper.firestore.collection(FirebaseHelper.GOALS_COLLECTION)
            .whereEqualTo("userId", userId)
            .limit(3)
            .get()
            .addOnSuccessListener { documents ->
                val goals = documents.mapNotNull { doc ->
                    doc.toObject(Goal::class.java)
                }
                
                tvGoalsCount.text = "${goals.size} active goals"
                goalsAdapter.updateGoals(goals)
            }
    }
}
