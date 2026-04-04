package com.example.araltime.admin

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.araltime.R
import com.example.araltime.firebase.FirebaseHelper
import com.example.araltime.models.StudySession
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.firestore.FirebaseFirestore

class AdminAnalyticsActivity : AppCompatActivity() {

    private lateinit var pieChartSubjects: PieChart
    private lateinit var pieChartMoods: PieChart
    private lateinit var tvMonthlyProfit: TextView
    private lateinit var tvTotalRevenue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_analytics)

        initViews()
        loadAnalytics()
    }

    private fun initViews() {
        pieChartSubjects = findViewById(R.id.pieChartSubjects)
        pieChartMoods = findViewById(R.id.pieChartMoods)
        tvMonthlyProfit = findViewById(R.id.tvMonthlyProfit)
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue)
    }

    private fun loadAnalytics() {
        loadSubjectAnalytics()
        loadMoodAnalytics()
        loadProfitAnalytics()
    }

    private fun loadSubjectAnalytics() {
        FirebaseHelper.firestore.collection(FirebaseHelper.SESSIONS_COLLECTION)
            .get()
            .addOnSuccessListener { documents ->
                val subjectMap = mutableMapOf<String, Int>()
                
                documents.forEach { document ->
                    val session = document.toObject(StudySession::class.java)
                    if (session != null) {
                        val subject = session.subject
                        val duration = (session.duration / 60).toInt() // Convert to minutes
                        subjectMap[subject] = subjectMap.getOrDefault(subject, 0) + duration
                    }
                }

                setupSubjectPieChart(subjectMap)
            }
            .addOnFailureListener { exception ->
                // Handle error
            }
    }

    private fun loadMoodAnalytics() {
        FirebaseHelper.firestore.collection(FirebaseHelper.SESSIONS_COLLECTION)
            .get()
            .addOnSuccessListener { documents ->
                val moodMap = mutableMapOf<String, Int>()
                
                documents.forEach { document ->
                    val session = document.toObject(StudySession::class.java)
                    if (session != null) {
                        val mood = session.mood
                        moodMap[mood] = moodMap.getOrDefault(mood, 0) + 1
                    }
                }

                setupMoodPieChart(moodMap)
            }
            .addOnFailureListener { exception ->
                // Handle error
            }
    }

    private fun loadProfitAnalytics() {
        FirebaseHelper.firestore.collection(FirebaseHelper.USERS_COLLECTION)
            .whereEqualTo("isPremium", true)
            .get()
            .addOnSuccessListener { documents ->
                val premiumUsers = documents.size()
                val monthlyProfit = premiumUsers * 9.99 // Assuming $9.99 per month
                val totalRevenue = premiumUsers * 9.99 * 12 // Annual revenue
                
                tvMonthlyProfit.text = String.format("$%.2f", monthlyProfit)
                tvTotalRevenue.text = String.format("$%.2f", totalRevenue)
            }
            .addOnFailureListener { exception ->
                tvMonthlyProfit.text = "Error"
                tvTotalRevenue.text = "Error"
            }
    }

    private fun setupSubjectPieChart(subjectMap: Map<String, Int>) {
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()
        
        subjectMap.forEach { (subject, minutes) ->
            entries.add(PieEntry(minutes.toFloat(), subject))
            colors.add(getColorForSubject(subject))
        }

        val dataSet = PieDataSet(entries, "Study Subjects").apply {
            setColors(colors)
            setDrawValues(true)
            setValueTextSize(12f)
            setValueTextColor(resources.getColor(R.color.white, null))
        }

        pieChartSubjects.data = PieData(dataSet)
        pieChartSubjects.description.isEnabled = false
        pieChartSubjects.legend.isEnabled = false
        pieChartSubjects.animateY(1000)
    }

    private fun setupMoodPieChart(moodMap: Map<String, Int>) {
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()
        
        moodMap.forEach { (mood, count) ->
            entries.add(PieEntry(count.toFloat(), mood))
            colors.add(getColorForMood(mood))
        }

        val dataSet = PieDataSet(entries, "Mood Distribution").apply {
            setColors(colors)
            setDrawValues(true)
            setValueTextSize(12f)
            setValueTextColor(resources.getColor(R.color.white, null))
        }

        pieChartMoods.data = PieData(dataSet)
        pieChartMoods.description.isEnabled = false
        pieChartMoods.legend.isEnabled = false
        pieChartMoods.animateY(1000)
    }

    private fun getColorForSubject(subject: String): Int {
        val colors = listOf(
            R.color.primary_color,
            R.color.accent_color,
            R.color.primary_light,
            R.color.text_secondary,
            android.R.color.holo_blue_dark,
            android.R.color.holo_orange_dark,
            android.R.color.holo_green_dark,
            android.R.color.holo_red_dark
        )
        return colors[subject.hashCode() % colors.size]
    }

    private fun getColorForMood(mood: String): Int {
        return when (mood) {
            "😊" -> resources.getColor(R.color.accent_color, null)
            "😐" -> resources.getColor(R.color.text_secondary, null)
            "😔" -> resources.getColor(android.R.color.holo_blue_dark, null)
            "😤" -> resources.getColor(android.R.color.holo_orange_dark, null)
            "🤔" -> resources.getColor(android.R.color.holo_green_dark, null)
            else -> resources.getColor(R.color.primary_color, null)
        }
    }
}
