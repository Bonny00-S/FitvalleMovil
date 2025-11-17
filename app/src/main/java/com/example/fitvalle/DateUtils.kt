package com.example.fitvalle

/**
 * Formatea una fecha ISO (instante) a formato legible dd/MM/yyyy HH:mm
 */
fun formatDate(isoDate: String): String {
    return try {
        val date = java.time.Instant.parse(isoDate)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(java.time.ZoneId.systemDefault())
        formatter.format(date)
    } catch (_: Exception) {
        isoDate
    }
}
