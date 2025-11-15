package com.example.fitvalle

data class Session(
    val id: String = "",
    val registerDate: String = "",
    val routineId: String = "",
    val sessionExercises: Map<String, SessionExercise>? = null
)