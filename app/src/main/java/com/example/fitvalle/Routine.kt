package com.example.fitvalle

data class Routine(
    var id: String = "",
    var name: String = "",
    val coachId: String = "",
    val customerId: String = "",
    val registerDate: String = "",
    val state: String = "",
    val sessions: Map<String, Session> = emptyMap(),
    // campo local para mostrar (no se sube a Firebase)
    @Transient
    var coachName: String? = null
)
