package com.example.actividadmenu.model

/**
 * Representa un mensaje individual dentro de una conversación.
 */
data class Mensaje(
    val id: String = "",
    val senderId: String = "",
    val texto: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
