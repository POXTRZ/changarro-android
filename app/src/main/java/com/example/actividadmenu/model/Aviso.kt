package com.example.actividadmenu.model

/**
 * Representa un aviso/publicación en el muro de la comunidad estudiantil.
 * Los valores por defecto son obligatorios para la deserialización de Firebase.
 */
data class Aviso(
    val id: String = "",
    val autorId: String = "",
    val autorNombre: String = "",
    val contenido: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
