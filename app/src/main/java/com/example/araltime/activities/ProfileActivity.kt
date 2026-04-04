package com.example.araltime.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.araltime.R
import com.example.araltime.firebase.FirebaseHelper
import com.example.araltime.models.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivProfile: ShapeableImageView
    private lateinit var tvName: MaterialTextView
    private lateinit var tvEmail: MaterialTextView
    private lateinit var tvMemberSince: MaterialTextView
    private lateinit var tvCurrentPoints: MaterialTextView
    private lateinit var progressBar: android.widget.ProgressBar
    private lateinit var tvPremiumStatus: MaterialTextView
    private lateinit var btnTogglePremium: MaterialButton
    private lateinit var btnChangeProfile: MaterialButton
    private lateinit var btnLogout: MaterialButton
    private var currentUser: User? = null
    private var selectedImageUri: Uri? = null

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val GALLERY_REQUEST_CODE = 101
        private const val CAMERA_PERMISSION_REQUEST = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        setupClickListeners()
        loadUserProfile()
    }

    private fun initViews() {
        ivProfile = findViewById(R.id.ivProfile)
        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)
        tvMemberSince = findViewById(R.id.tvMemberSince)
        tvCurrentPoints = findViewById(R.id.tvCurrentPoints)
        progressBar = findViewById(R.id.progressBar)
        tvPremiumStatus = findViewById(R.id.tvPremiumStatus)
        btnTogglePremium = findViewById(R.id.btnTogglePremium)
        btnChangeProfile = findViewById(R.id.btnChangeProfile)
        btnLogout = findViewById(R.id.btnLogout)
        
        // Set click listener for the camera icon indicator area
        ivProfile.setOnClickListener {
            showImagePickerDialog()
        }
    }

    private fun setupClickListeners() {
        btnChangeProfile.setOnClickListener {
            showImagePickerDialog()
        }

        btnTogglePremium.setOnClickListener {
            // Navigate to subscription page
            startActivity(Intent(this, SubscriptionActivity::class.java))
        }

        btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun loadUserProfile() {
        val userId = FirebaseHelper.getCurrentUserId()
        if (userId.isEmpty()) return

        FirebaseHelper.firestore.collection(FirebaseHelper.USERS_COLLECTION)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    currentUser = document.toObject(User::class.java)
                    populateProfile()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading profile: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun populateProfile() {
        currentUser?.let { user ->
            // Handle both old name field and new firstName/lastName fields
            val displayName = if (user.firstName.isNotEmpty() && user.lastName.isNotEmpty()) {
                "${user.firstName} ${user.lastName}"
            } else {
                user.name
            }
            
            tvName.text = displayName
            tvEmail.text = user.email
            
            // Set member since date (you can customize this)
            val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            tvMemberSince.text = "Member since: ${dateFormat.format(Date())}"
            
            tvCurrentPoints.text = "${user.points} points"
            
            // Update progress bar (max 1000 points for demo)
            progressBar.progress = (user.points % 1000).toInt()
            
            if (user.isPremium) {
                tvPremiumStatus.text = "✨ Premium User"
                tvPremiumStatus.setTextColor(getColor(R.color.accent_color))
                btnTogglePremium.text = "Manage Subscription"
                btnTogglePremium.backgroundTintList = getColorStateList(android.R.color.darker_gray)
            } else {
                tvPremiumStatus.text = "Free User"
                tvPremiumStatus.setTextColor(getColor(R.color.text_secondary))
                btnTogglePremium.text = "Unlock Premium"
                btnTogglePremium.backgroundTintList = getColorStateList(R.color.accent_color)
                
                // Show points progress
                val discountPercent = (user.points / 100).toInt().coerceAtMost(100)
                tvPremiumStatus.text = "Free User - $discountPercent% discount available"
            }
            
            // Load profile image
            if (user.profileImage.isNotEmpty()) {
                try {
                    val imageBytes = Base64.getDecoder().decode(user.profileImage)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    ivProfile.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    // Set default image if loading fails
                    ivProfile.setImageResource(R.drawable.ic_profile)
                }
            } else {
                ivProfile.setImageResource(R.drawable.ic_profile)
            }
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Choose Profile Picture")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> checkCameraPermission()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val bitmap = data?.extras?.get("data") as? Bitmap
                    bitmap?.let {
                        ivProfile.setImageBitmap(it)
                        uploadProfileImage(it)
                    }
                }
                GALLERY_REQUEST_CODE -> {
                    selectedImageUri = data?.data
                    selectedImageUri?.let { uri ->
                        ivProfile.setImageURI(uri)
                        uploadProfileImageFromUri(uri)
                    }
                }
            }
        }
    }

    private fun uploadProfileImage(bitmap: Bitmap) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()
        val encodedImage = Base64.getEncoder().encodeToString(imageBytes)
        
        saveProfileImage(encodedImage)
    }

    private fun uploadProfileImageFromUri(uri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            uploadProfileImage(bitmap)
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading image: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveProfileImage(encodedImage: String) {
        val userId = FirebaseHelper.getCurrentUserId()
        FirebaseHelper.firestore.collection(FirebaseHelper.USERS_COLLECTION)
            .document(userId)
            .update("profileImage", encodedImage)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to update profile picture: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun togglePremiumStatus() {
        val userId = FirebaseHelper.getCurrentUserId()
        val newPremiumStatus = !(currentUser?.isPremium ?: false)
        
        FirebaseHelper.firestore.collection(FirebaseHelper.USERS_COLLECTION)
            .document(userId)
            .update("isPremium", newPremiumStatus)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    if (newPremiumStatus) "Premium enabled! ✨" else "Premium disabled",
                    Toast.LENGTH_SHORT
                ).show()
                loadUserProfile() // Refresh profile
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to update premium status: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun logoutUser() {
        FirebaseHelper.auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
