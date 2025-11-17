package com.example.fitvalle

import java.io.Serializable

data class TemplateExercise(
    val exerciseId: String = "",
    val exerciseName: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val weight: Int = 0,
    val speed: Int = 0,
    val duration: Int = 0
) : Serializable
