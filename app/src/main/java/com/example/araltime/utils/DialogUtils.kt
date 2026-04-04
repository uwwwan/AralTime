package com.example.araltime.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog

object DialogUtils {
    
    fun showLogoutConfirmation(context: Context, onConfirm: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirm Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Yes") { _, _ ->
            onConfirm()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.setCancelable(true)
        builder.show()
    }
    
    fun showDeleteConfirmation(context: Context, itemType: String, onConfirm: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirm Delete")
        builder.setMessage("Are you sure you want to delete this $itemType? This action cannot be undone.")
        builder.setPositiveButton("Delete") { _, _ ->
            onConfirm()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.setCancelable(true)
        builder.show()
    }
}
