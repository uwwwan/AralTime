package com.example.araltime.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.araltime.R
import com.example.araltime.admin.AdminDashboardActivity
import com.example.araltime.firebase.FirebaseHelper
import com.example.araltime.models.User
import com.google.android.material.navigation.NavigationView
import com.google.android.material.imageview.ShapeableImageView

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var tvTitle: TextView
    private lateinit var ivLogo: ShapeableImageView
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        loadUserData()
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)
        tvTitle = findViewById(R.id.tvTitle)
        ivLogo = findViewById(R.id.ivLogo)
    }

    private fun loadUserData() {
        val userId = FirebaseHelper.getCurrentUserId()
        if (userId.isEmpty()) return

        FirebaseHelper.firestore.collection(FirebaseHelper.USERS_COLLECTION)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    currentUser = document.toObject(User::class.java) ?: return@addOnSuccessListener
                    
                    setupToolbar()
                    setupNavigation()
                }
            }
    }

    private fun setupNavigation() {
        val navController = findNavController(R.id.nav_host_fragment)
        
        if (currentUser.isAdmin) {
            // Admin navigation
            appBarConfiguration = AppBarConfiguration(
                setOf(R.id.nav_admin_dashboard),
                drawerLayout
            )
            setupAdminNavigation(navController)
        } else {
            // User navigation
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_home, R.id.nav_subjects, R.id.nav_history,
                    R.id.nav_goals, R.id.nav_reminders, R.id.nav_overview
                ), drawerLayout
            )
            setupUserNavigation(navController)
        }
        
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    logoutUser()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> {
                    val handled = onNavigationItemSelected(menuItem)
                    if (handled) {
                        drawerLayout.closeDrawer(GravityCompat.START)
                    }
                    handled
                }
            }
        }
    }

    private fun setupUserNavigation(navController: androidx.navigation.NavController) {
        navView.menu.clear()
        navView.inflateMenu(R.menu.user_drawer_menu)
        
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_home -> {
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    tvTitle.visibility = android.view.View.VISIBLE
                    ivLogo.visibility = android.view.View.GONE
                    tvTitle.text = "Hello, ${if (currentUser.firstName.isNotEmpty()) currentUser.firstName else currentUser.name}! 👋"
                }
                else -> {
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    tvTitle.visibility = android.view.View.VISIBLE
                    ivLogo.visibility = android.view.View.GONE
                    tvTitle.text = destination.label?.toString() ?: "AralTime"
                }
            }
        }
    }

    private fun setupAdminNavigation(navController: androidx.navigation.NavController) {
        navView.menu.clear()
        navView.inflateMenu(R.menu.admin_drawer_menu)
        
        // Navigate directly to admin dashboard
        navController.navigate(R.id.nav_admin_dashboard)
        
        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            tvTitle.visibility = android.view.View.GONE
            ivLogo.visibility = android.view.View.VISIBLE
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }
    }

    private fun logoutUser() {
        FirebaseHelper.auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_notifications -> {
                Toast.makeText(this, "Notifications coming soon", Toast.LENGTH_SHORT).show()
                true
            }
            android.R.id.home -> {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    drawerLayout.openDrawer(GravityCompat.START)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun onNavigationItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return when (item.itemId) {
            R.id.nav_home -> {
                navController.navigate(R.id.nav_home)
                true
            }
            R.id.nav_subjects -> {
                navController.navigate(R.id.nav_subjects)
                true
            }
            R.id.nav_history -> {
                navController.navigate(R.id.nav_history)
                true
            }
            R.id.nav_goals -> {
                navController.navigate(R.id.nav_goals)
                true
            }
            R.id.nav_reminders -> {
                navController.navigate(R.id.nav_reminders)
                true
            }
            R.id.nav_overview -> {
                navController.navigate(R.id.nav_overview)
                true
            }
            R.id.nav_admin_dashboard -> {
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                true
            }
            else -> false
        }
    }
}
