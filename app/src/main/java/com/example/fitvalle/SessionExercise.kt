package com.example.fitvalle

data class SessionExercise(
    val sessionId: String = "",
    val exerciseId: String? = "",
    val reps: Int? = 0,
    val sets: Int? = 0,
    var exerciseName: String? = null,
    val completed: Boolean = false
)
