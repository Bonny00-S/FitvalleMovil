package com.example.fitvalle

data class Request(
    val id: String = "",
    val customerId: String = "",
    val description: String = "",
    val state: String = "pending" // puede ser "pending", "accepted", "rejected"
)
