package com.example.fitvalle

data class Workout(
    val id: String = "",
    val userId: String = "",
    val templateName: String = "",
    val date: String = "",
    val status: String = "completado",
    val exercises: List<String> = emptyList(),
    val notes: String? = null
)
