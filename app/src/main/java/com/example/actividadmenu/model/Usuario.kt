package com.example.actividadmenu.model

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val whatsapp: String = "",
    val carrera: String = "",
    val semestre: String = "",
    val fotoUrl: String = "",
    val favoritos: List<String> = emptyList()
)