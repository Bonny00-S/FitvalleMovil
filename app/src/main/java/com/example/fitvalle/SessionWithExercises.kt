package com.example.fitvalle

import com.example.fitvalle.Exercise

data class SessionWithExercises(
    val sessionId: String = "",
    val exercises: List<Exercise> = emptyList(),
    val completed: Boolean = false
)