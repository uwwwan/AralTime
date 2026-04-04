package com.example.araltime.models

data class User(
    val uid: String = "",
    val name: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val points: Long = 0,
    val isPremium: Boolean = false,
    val profileImage: String = "",
    val isAdmin: Boolean = false,
    val isDisabled: Boolean = false
)

data class StudySession(
    val sessionId: String = "",
    val userId: String = "",
    val subject: String = "",
    val activity: String = "",
    val topic: String = "",
    val duration: Long = 0, // in seconds
    val mood: String = "",
    val timestamp: Long = 0,
    val notes: String = ""
)

data class Subject(
    val subjectId: String = "",
    val userId: String = "",
    val name: String = "",
    val color: String = "#326c49",
    val isPremium: Boolean = false
)

data class Goal(
    val goalId: String = "",
    val userId: String = "",
    val title: String = "",
    val target: Int = 0, // target in minutes
    val progress: Int = 0, // current progress in minutes
    val isCompleted: Boolean = false
)

data class Reminder(
    val reminderId: String = "",
    val userId: String = "",
    val title: String = "",
    val date: String = "",
    val time: String = ""
)

data class OnboardingItem(
    val title: String = "",
    val description: String = "",
    val imageRes: Int = 0
)
