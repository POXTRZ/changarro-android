package com.example.actividadmenu.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

/**
 * Repositorio para la gestión de transacciones y estadísticas financieras.
 */
class TransaccionRepository {

    private val db = FirebaseFirestore.getInstance()
    private val transaccionesCollection = db.collection("transacciones")

    /**
     * Obtiene las estadísticas de ventas realizadas el día de hoy en tiempo real.
     * @param userId ID del vendedor.
     * @param onSuccess Callback que devuelve (montoTotalAcumulado, cantidadDeTransacciones).
     */
    fun obtenerMisVentasHoy(
        userId: String,
        onSuccess: (Double, Int) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Calculamos el inicio del día de hoy (00:00:00)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val inicioHoy = calendar.timeInMillis

        // Nota: Si se requiere filtrar también por el fin del día, se puede agregar. 
        // Firestore maneja bien el "mayor o igual que inicioHoy".
        transaccionesCollection
            .whereEqualTo("vendedorId", userId)
            .whereEqualTo("tipo", "VENTA")
            .whereGreaterThanOrEqualTo("fecha", inicioHoy)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    var montoTotal = 0.0
                    val cantidadTransacciones = snapshot.size()

                    for (doc in snapshot.documents) {
                        montoTotal += doc.getDouble("montoTotal") ?: 0.0
                    }

                    onSuccess(montoTotal, cantidadTransacciones)
                }
            }
    }
}
