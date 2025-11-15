package com.example.fitvalle

data class Template(
    val id: String = "",
    val userId: String = "",
    val name: String = "",                    // 🔹 nombre de la plantilla
    val exercises: List<String> = emptyList(), // 🔹 lista de ejercicios (por nombre o ID)
    val createdAt: String = ""                // 🔹 fecha de creación
)
