package com.example.fitvalle

data class User(
    val id: Int = 0,                      // smallint → manejaremos int en Kotlin
    val name: String = "",
    val email: String = "",
    val password: String = "",            // se guardará cifrado
    val role: String = "client",
    val userId: Int? = null,              // opcional
    val state: Int = 1,                   // 1 activo por defecto
    val registerDate: String = ""
)
