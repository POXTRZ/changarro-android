package com.example.actividadmenu.data.repository

import com.example.actividadmenu.model.Evento
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Repositorio para la gestión de eventos universitarios en Firestore.
 */
class EventoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val eventosCollection = db.collection("eventos")

    /**
     * Obtiene los eventos en tiempo real, ordenados por fecha de publicación.
     */
    fun obtenerEventosRealtime(
        onSuccess: (List<Evento>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        eventosCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val listaEventos = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Evento::class.java)?.copy(id = doc.id)
                    }
                    onSuccess(listaEventos)
                }
            }
    }

    /**
     * Registra un nuevo evento en la base de datos.
     */
    fun agregarEvento(
        evento: Evento,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val nuevoDoc = eventosCollection.document()
        val eventoConId = evento.copy(id = nuevoDoc.id)

        nuevoDoc.set(eventoConId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}
