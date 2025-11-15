package com.example.fitvalle

data class Workout(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val exercises: List<String> = emptyList(),
    val createdAt: String = ""
)