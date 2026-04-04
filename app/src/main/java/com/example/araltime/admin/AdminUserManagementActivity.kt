package com.example.araltime.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.araltime.R
import com.example.araltime.firebase.FirebaseHelper
import com.example.araltime.models.User
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FirebaseFirestore

class AdminUserManagementActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var tvPageIndicator: TextView
    private lateinit var btnPrevious: TextView
    private lateinit var btnNext: TextView
    private lateinit var userAdapter: UserAdapter
    private var users: List<User> = emptyList()
    
    // Pagination
    private val pageSize = 10
    private var currentPage = 1
    private var totalPages = 1
    private var lastVisibleUser: User? = null
    private var firstVisibleUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_user_management)

        initViews()
        setupRecyclerView()
        setupPagination()
        loadUsers()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvPageIndicator = findViewById(R.id.tvPageIndicator)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(users, 
            onUserClick = { user ->
                // Handle user click if needed
            },
            onActionClick = { user ->
                toggleUserStatus(user)
            }
        )
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@AdminUserManagementActivity)
            adapter = userAdapter
        }
    }

    private fun toggleUserStatus(user: User) {
        val newStatus = !user.isDisabled
        val actionText = if (newStatus) "disable" else "enable"
        
        AlertDialog.Builder(this)
            .setTitle("$actionText User Account")
            .setMessage("Are you sure you want to $actionText ${user.email}?")
            .setPositiveButton("Yes") { _, _ ->
                updateUserStatus(user, newStatus)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateUserStatus(user: User, isDisabled: Boolean) {
        FirebaseHelper.firestore.collection(FirebaseHelper.USERS_COLLECTION)
            .document(user.uid)
            .update("isDisabled", isDisabled)
            .addOnSuccessListener {
                val status = if (isDisabled) "disabled" else "enabled"
                Toast.makeText(this, "User account $status successfully", Toast.LENGTH_SHORT).show()
                loadCurrentPage() // Refresh current page
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadCurrentPage() {
        if (currentPage == 1) {
            loadFirstPage()
        } else {
            loadNextPage()
        }
    }

    private fun setupPagination() {
        btnPrevious.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                loadPreviousPage()
            }
        }

        btnNext.setOnClickListener {
            if (currentPage < totalPages) {
                currentPage++
                loadNextPage()
            }
        }

        updatePaginationUI()
    }

    private fun loadUsers() {
        currentPage = 1
        loadFirstPage()
    }

    private fun loadFirstPage() {
        FirebaseHelper.firestore.collection(FirebaseHelper.USERS_COLLECTION)
            .orderBy("email")
            .limit(pageSize.toLong())
            .get()
            .addOnSuccessListener { documents ->
                users = documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)
                }
                
                if (users.isNotEmpty()) {
                    lastVisibleUser = users.last()
                }
                
                updateUI()
                checkForMorePages()
            }
            .addOnFailureListener { exception ->
                // Handle error
                recyclerView.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Error loading users: ${exception.message}"
            }
    }

    private fun loadNextPage() {
        lastVisibleUser?.let { lastUser ->
            FirebaseHelper.firestore.collection(FirebaseHelper.USERS_COLLECTION)
                .orderBy("email")
                .startAfter(lastUser.email)
                .limit(pageSize.toLong())
                .get()
                .addOnSuccessListener { documents ->
                    users = documents.mapNotNull { doc ->
                        doc.toObject(User::class.java)
                    }
                    
                    if (users.isNotEmpty()) {
                        firstVisibleUser = users.first()
                        lastVisibleUser = users.last()
                    }
                    
                    updateUI()
                    checkForMorePages()
                }
                .addOnFailureListener { exception ->
                    // Handle error
                    currentPage-- // Revert page number on error
                    updatePaginationUI()
                }
        }
    }

    private fun loadPreviousPage() {
        firstVisibleUser?.let { firstUser ->
            FirebaseHelper.firestore.collection(FirebaseHelper.USERS_COLLECTION)
                .orderBy("email")
                .endBefore(firstUser.email)
                .limitToLast(pageSize.toLong())
                .get()
                .addOnSuccessListener { documents ->
                    users = documents.mapNotNull { doc ->
                        doc.toObject(User::class.java)
                    }
                    
                    if (users.isNotEmpty()) {
                        firstVisibleUser = users.first()
                        lastVisibleUser = users.last()
                    }
                    
                    updateUI()
                    // Always check for more pages when going back
                    checkForMorePages()
                }
                .addOnFailureListener { exception ->
                    // Handle error
                    currentPage++ // Revert page number on error
                    updatePaginationUI()
                }
        }
    }

    private fun checkForMorePages() {
        lastVisibleUser?.let { lastUser ->
            FirebaseHelper.firestore.collection(FirebaseHelper.USERS_COLLECTION)
                .orderBy("email")
                .startAfter(lastUser.email)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    totalPages = if (documents.isEmpty()) {
                        currentPage
                    } else {
                        currentPage + 1 // There might be more pages
                    }
                    updatePaginationUI()
                }
        }
    }

    private fun updateUI() {
        if (users.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
            userAdapter.updateUsers(users)
        }
        updatePaginationUI()
    }

    private fun updatePaginationUI() {
        tvPageIndicator.text = "Page $currentPage of $totalPages"
        
        btnPrevious.isEnabled = currentPage > 1
        btnNext.isEnabled = currentPage < totalPages
        
        // Update button colors based on state
        if (btnPrevious.isEnabled) {
            btnPrevious.setTextColor(getColor(R.color.primary_color))
        } else {
            btnPrevious.setTextColor(getColor(R.color.text_secondary))
        }
        
        if (btnNext.isEnabled) {
            btnNext.setTextColor(getColor(R.color.primary_color))
        } else {
            btnNext.setTextColor(getColor(R.color.text_secondary))
        }
    }

    // Adapter class for users
    private class UserAdapter(
        private var userList: List<User>,
        private val onUserClick: (User) -> Unit,
        private val onActionClick: (User) -> Unit
    ) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

        fun updateUsers(newUsers: List<User>) {
            userList = newUsers
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_admin_user_table, parent, false)
            return UserViewHolder(view)
        }

        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
            holder.bind(userList[position], onUserClick, onActionClick)
        }

        override fun getItemCount(): Int = userList.size

        class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
            private val tvPoints: TextView = itemView.findViewById(R.id.tvPoints)
            private val tvPremiumStatus: TextView = itemView.findViewById(R.id.tvPremiumStatus)
            private val btnAction: MaterialButton = itemView.findViewById(R.id.btnAction)

            fun bind(user: User, onUserClick: (User) -> Unit, onActionClick: (User) -> Unit) {
                tvEmail.text = user.email
                tvPoints.text = "${user.points} points"
                
                if (user.isPremium) {
                    tvPremiumStatus.text = "✨ Premium"
                    tvPremiumStatus.setTextColor(itemView.context.getColor(R.color.accent_color))
                } else {
                    tvPremiumStatus.text = "Free"
                    tvPremiumStatus.setTextColor(itemView.context.getColor(R.color.text_secondary))
                }
                
                // Update action button based on user status
                if (user.isDisabled) {
                    btnAction.text = "Enable"
                    btnAction.setBackgroundColor(itemView.context.getColor(R.color.primary_color))
                } else {
                    btnAction.text = "Disable"
                    btnAction.setBackgroundColor(itemView.context.getColor(android.R.color.holo_red_dark))
                }
                
                itemView.setOnClickListener {
                    onUserClick(user)
                }
                
                btnAction.setOnClickListener {
                    onActionClick(user)
                }
            }
        }
    }
}
