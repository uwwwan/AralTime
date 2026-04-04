package com.example.araltime.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.araltime.R
import com.example.araltime.models.StudySession
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(private var sessions: List<StudySession>) : 
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view, dateFormat)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(sessions[position])
    }

    override fun getItemCount(): Int = sessions.size

    fun updateSessions(newSessions: List<StudySession>) {
        sessions = newSessions
        notifyDataSetChanged()
    }

    class HistoryViewHolder(itemView: View, private val dateFormat: SimpleDateFormat) : RecyclerView.ViewHolder(itemView) {
        private val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val tvMood: TextView = itemView.findViewById(R.id.tvMood)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvActivity: TextView = itemView.findViewById(R.id.tvActivity)

        fun bind(session: StudySession) {
            tvSubject.text = session.subject
            tvActivity.text = session.activity
            tvMood.text = session.mood
            
            // Format duration
            val totalSeconds = session.duration
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val durationText = if (hours > 0) {
                "${hours}h ${minutes}m"
            } else {
                "${minutes}m"
            }
            tvDuration.text = durationText
            
            // Format date
            val date = Date(session.timestamp)
            tvDate.text = dateFormat.format(date)
        }
    }
}
