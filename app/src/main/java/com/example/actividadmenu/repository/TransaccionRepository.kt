package com.example.actividadmenu.repository

import com.example.actividadmenu.Transaccion
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Calendar

/**
 * Repositorio para la gestión de transacciones y estadísticas financieras.
 */
class TransaccionRepository {

    private val db = FirebaseFirestore.getInstance()
    private val transaccionesCollection = db.collection("transacciones")

    /**
     * Obtiene todas las transacciones de un usuario en tiempo real.
     */
    fun obtenerMisTransaccionesRealtime(userId: String, onSuccess: (List<Transaccion>) -> Unit, onFailure: (Exception) -> Unit) {
        transaccionesCollection
            .whereEqualTo("vendedorId", userId)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }
                val lista = snapshot?.documents?.mapNotNull { it.toObject(Transaccion::class.java) } ?: emptyList()
                onSuccess(lista)
            }
    }

    /**
     * Obtiene las estadísticas de ventas realizadas el día de hoy en tiempo real.
     */
    fun obtenerMisVentasHoy(
        userId: String,
        onSuccess: (Double, Int) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val inicioHoy = calendar.timeInMillis

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
                    val cantidadVentas = snapshot.size()

                    for (doc in snapshot.documents) {
                        montoTotal += doc.getDouble("montoTotal") ?: 0.0
                    }

                    onSuccess(montoTotal, cantidadVentas)
                }
            }
    }

    /**
     * Registra una nueva transacción en Firestore.
     */
    fun registrarTransaccion(
        transaccion: Transaccion,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val nuevoDoc = transaccionesCollection.document()
        val transaccionConId = transaccion.copy(id = nuevoDoc.id)

        nuevoDoc.set(transaccionConId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}
