package com.example.araltime.activities

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.araltime.R
import com.example.araltime.firebase.FirebaseHelper
import com.example.araltime.models.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream
import java.util.*

class SignupActivity : AppCompatActivity() {

    private lateinit var ivProfile: ShapeableImageView
    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etUsername: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnSignup: MaterialButton
    private lateinit var tvLogin: TextView

    private lateinit var auth: FirebaseAuth
    private var selectedImageUri: Uri? = null
    private var selectedImageBitmap: Bitmap? = null

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val GALLERY_REQUEST_CODE = 101
        private const val CAMERA_PERMISSION_REQUEST = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        auth = FirebaseHelper.auth
        ivProfile = findViewById(R.id.ivProfile)
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSignup = findViewById(R.id.btnSignup)
        tvLogin = findViewById(R.id.tvLogin)
    }

    private fun setupClickListeners() {
        ivProfile.setOnClickListener {
            showImagePickerDialog()
        }

        btnSignup.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (validateInput(firstName, lastName, username, email, phone, password, confirmPassword)) {
                signupUser(firstName, lastName, username, email, phone, password)
            }
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
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
                        selectedImageBitmap = it
                    }
                }
                GALLERY_REQUEST_CODE -> {
                    selectedImageUri = data?.data
                    selectedImageUri?.let { ivProfile.setImageURI(it) }
                }
            }
        }
    }

    private fun validateInput(
        firstName: String, lastName: String, username: String,
        email: String, phone: String, password: String, confirmPassword: String
    ): Boolean {
        if (firstName.isEmpty()) { etFirstName.error = "First name is required"; return false }
        if (lastName.isEmpty()) { etLastName.error = "Last name is required"; return false }
        if (username.isEmpty()) { etUsername.error = "Username is required"; return false }
        if (email.isEmpty()) { etEmail.error = "Email is required"; return false }
        if (phone.isEmpty()) { etPhone.error = "Phone number is required"; return false }
        if (password.isEmpty()) { etPassword.error = "Password is required"; return false }
        if (password.length < 6) { etPassword.error = "Password must be at least 6 characters"; return false }
        if (confirmPassword.isEmpty()) { etConfirmPassword.error = "Please confirm password"; return false }
        if (password != confirmPassword) { etConfirmPassword.error = "Passwords do not match"; return false }

        return true
    }

    private fun signupUser(
        firstName: String, lastName: String, username: String,
        email: String, phone: String, password: String
    ) {
        btnSignup.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                btnSignup.isEnabled = true
                if (task.isSuccessful) {
                    val profileImageBase64 = selectedImageBitmap?.let { bitmap ->
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos)
                        Base64.getEncoder().encodeToString(baos.toByteArray())
                    } ?: ""

                    val user = User(
                        uid = FirebaseHelper.getCurrentUserId(),
                        name = "$firstName $lastName", // For backward compatibility
                        firstName = firstName,
                        lastName = lastName,
                        username = username,
                        email = email,
                        phone = phone,
                        points = 0,
                        isPremium = false,
                        profileImage = profileImageBase64
                    )

                    FirebaseHelper.firestore.collection(FirebaseHelper.USERS_COLLECTION)
                        .document(user.uid)
                        .set(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to save user data", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}