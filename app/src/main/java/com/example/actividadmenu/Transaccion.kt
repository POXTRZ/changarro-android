package com.example.actividadmenu

/**
 * Representa una transacción de compra o venta en el sistema.
 * Se añadió vendedorId para permitir el filtrado en el Dashboard Financiero.
 */
data class Transaccion(
    val id: String = "",
    val productoId: String = "",
    val nombreProducto: String = "",
    val tipo: String = "", // "VENTA" o "COMPRA"
    val cantidad: Int = 0,
    val montoTotal: Double = 0.0,
    val fecha: Long = System.currentTimeMillis(),
    val vendedorId: String = "" // ID del usuario que realiza la venta
)
