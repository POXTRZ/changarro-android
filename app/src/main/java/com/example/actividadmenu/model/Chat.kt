package com.example.actividadmenu.model

/**
 * Representa una conversación entre dos usuarios.
 */
data class Chat(
    val id: String = "",
    val usuarios: List<String> = emptyList(), // IDs de los dos participantes
    val nombresUsuarios: Map<String, String> = emptyMap(), // Map de {userId -> userName}
    val ultimoMensaje: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
