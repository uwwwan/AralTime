package com.example.araltime.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.araltime.R
import com.example.araltime.adapters.HistoryAdapter
import com.example.araltime.firebase.FirebaseHelper
import com.example.araltime.models.StudySession

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupRecyclerView()
        loadHistory()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        tvEmpty = view.findViewById(R.id.tvEmpty)
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(emptyList())
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    private fun loadHistory() {
        val userId = FirebaseHelper.getCurrentUserId()
        if (userId.isEmpty()) return

        FirebaseHelper.firestore.collection(FirebaseHelper.SESSIONS_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val sessions = documents.mapNotNull { doc ->
                    doc.toObject(StudySession::class.java)
                }
                
                if (sessions.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    tvEmpty.visibility = View.GONE
                    historyAdapter.updateSessions(sessions)
                }
            }
            .addOnFailureListener { exception ->
                // Handle error
                recyclerView.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Error loading history: ${exception.message}"
            }
    }
}
