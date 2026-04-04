package com.example.araltime.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.araltime.R
import com.example.araltime.models.Goal

class GoalsAdapter(private var goals: List<Goal>, private val onGoalClick: (Goal) -> Unit) : 
    RecyclerView.Adapter<GoalsAdapter.GoalsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal, parent, false)
        return GoalsViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalsViewHolder, position: Int) {
        holder.bind(goals[position], onGoalClick)
    }

    override fun getItemCount(): Int = goals.size

    fun updateGoals(newGoals: List<Goal>) {
        goals = newGoals
        notifyDataSetChanged()
    }

    class GoalsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvProgress: TextView = itemView.findViewById(R.id.tvProgress)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(goal: Goal, onGoalClick: (Goal) -> Unit) {
            tvTitle.text = goal.title
            
            val progressPercent = if (goal.target > 0) {
                (goal.progress.toFloat() / goal.target * 100).toInt()
            } else 0
            
            tvProgress.text = "${goal.progress} / ${goal.target} minutes"
            progressBar.progress = progressPercent
            
            if (goal.isCompleted) {
                tvStatus.text = "✅ Completed"
                tvStatus.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
            } else {
                tvStatus.text = "In Progress"
                tvStatus.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
            }
            
            itemView.setOnClickListener {
                onGoalClick(goal)
            }
        }
    }
}
