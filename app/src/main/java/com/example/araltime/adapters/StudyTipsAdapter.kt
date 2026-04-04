package com.example.araltime.adapters

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.araltime.R

class StudyTipsAdapter : RecyclerView.Adapter<StudyTipsAdapter.TipsViewHolder>() {
    
    private var viewPager2: ViewPager2? = null
    private val handler = Handler(Looper.getMainLooper())
    private val autoSwipeRunnable = object : Runnable {
        override fun run() {
            viewPager2?.let { viewPager ->
                val currentItem = viewPager.currentItem
                val nextItem = if (currentItem == tips.size - 1) 0 else currentItem + 1
                viewPager.setCurrentItem(nextItem, true)
            }
            handler.postDelayed(this, 5000) // 5 seconds
        }
    }

    // Hardcoded study tips
    private val tips = listOf(
        "📚 Take breaks every 25 minutes using the Pomodoro Technique",
        "🌱 Study in short, focused sessions for better retention", 
        "💡 Use active recall instead of passive reading",
        "🎯 Set specific, achievable goals for each study session",
        "🧘‍♀️ Practice mindfulness before studying to improve focus",
        "📝 Take handwritten notes for better memory retention",
        "🌙 Review material before bed for better consolidation",
        "💧 Stay hydrated - your brain needs water to function optimally"
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_study_tip, parent, false)
        return TipsViewHolder(view)
    }

    override fun onBindViewHolder(holder: TipsViewHolder, position: Int) {
        holder.bind(tips[position])
    }

    override fun getItemCount(): Int = tips.size

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        if (recyclerView is ViewPager2) {
            viewPager2 = recyclerView
            startAutoSwipe()
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        stopAutoSwipe()
    }

    private fun startAutoSwipe() {
        handler.postDelayed(autoSwipeRunnable, 5000)
    }

    private fun stopAutoSwipe() {
        handler.removeCallbacks(autoSwipeRunnable)
    }

    class TipsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTip: TextView = itemView.findViewById(R.id.tvTip)

        fun bind(tip: String) {
            tvTip.text = tip
        }
    }
}
