package com.example.fitvalle

data class Exercise(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val typeID: String = "",
    val muscleID: String = "",
    val registerDate: String = "",
    //
    val series: Int = 4,
    val repetitions: String = "10-12",
    val restTime: Int = 60,
    val instructions: List<String> = listOf()
)
