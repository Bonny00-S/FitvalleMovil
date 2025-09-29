package com.example.fitvalle

data class Customer(
    val id: String = "",
    val weight: String = "",
    val height: String = "",
    val birthdate: String = "",
    val goalWeight: String = "",   // ✅ Agregado
    val registerDate: String = ""
)
