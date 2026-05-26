package com.example.actividadmenu

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val descripcion: String = "",
    val stock: Int = 0,
    val imagenUrl: String = "",
    val vendedorId: String = "",
    val vendedorNombre: String = "",
    val vendedorWhatsapp: String = "",
    val categoria: String = "Otros"
)