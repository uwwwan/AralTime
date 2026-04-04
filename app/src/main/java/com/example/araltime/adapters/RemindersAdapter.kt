package com.example.araltime.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.araltime.R
import com.example.araltime.models.Reminder

class RemindersAdapter(private var reminders: List<Reminder>, private val onReminderClick: (Reminder) -> Unit) : 
    RecyclerView.Adapter<RemindersAdapter.RemindersViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemindersViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return RemindersViewHolder(view)
    }

    override fun onBindViewHolder(holder: RemindersViewHolder, position: Int) {
        holder.bind(reminders[position], onReminderClick)
    }

    override fun getItemCount(): Int = reminders.size

    fun updateReminders(newReminders: List<Reminder>) {
        reminders = newReminders
        notifyDataSetChanged()
    }

    class RemindersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)

        fun bind(reminder: Reminder, onReminderClick: (Reminder) -> Unit) {
            tvTitle.text = reminder.title
            tvDateTime.text = "${reminder.date} at ${reminder.time}"
            
            itemView.setOnClickListener {
                onReminderClick(reminder)
            }
        }
    }
}
