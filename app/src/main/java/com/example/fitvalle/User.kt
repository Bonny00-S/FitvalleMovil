package com.example.fitvalle

data class User(
    val id: Int = 0,                      // smallint → manejaremos int en Kotlin
    val name: String = "",
    val email: String = "",
    val password: String = "",            // se guardará cifrado
    val role: String = "client",
    val userId: Int? = null,              // opcional
    val state: Int = 1,                   // 1 activo por defecto
    val registerDate: String = "",

    // 🔹 Nuevos campos para el perfil de entrenador:
    val description: String = "",       // breve biografía
    val photoUrl: String = "",          // URL completa o parcial
    val specialty: String = "",         // tipo de entrenamiento
    val students: Int = 0,              // número total de alumnos
    val activeStudents: Int = 0         // alumnos activos
)
