package com.example.actividadmenu.model

/**
 * Representa un evento o actividad en el calendario universitario.
 */
data class Evento(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val lugar: String = "",
    val fechaDelEvento: String = "", // Cambiado a String para coincidir con la firma del ViewModel
    val timestamp: Long = System.currentTimeMillis(),
    val creadorId: String = ""
)
