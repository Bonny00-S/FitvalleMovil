package com.example.fitvalle

import java.io.Serializable

data class SessionExercise(
    val sessionId: String = "",
    val exerciseId: String = "",
    var exerciseName: String? = null,
    val sets: Int = 0,
    val reps: Int = 0,
    val weight: Int = 0,
    val speed: Int = 0,
    val duration: Int = 0,
    val completed: Boolean = false
): Serializable
