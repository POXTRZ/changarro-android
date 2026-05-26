package com.example.actividadmenu.repository

import com.example.actividadmenu.model.Aviso
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Repositorio encargado de la gestión de datos para el Muro de Avisos.
 * Actúa como la fuente de verdad conectada a Firebase Firestore.
 */
class AvisoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val avisosCollection = db.collection("avisos")

    /**
     * Guarda un nuevo aviso en Firestore.
     * @param aviso Objeto con la información de la publicación.
     * @param onSuccess Callback para manejo de éxito.
     * @param onFailure Callback para manejo de errores.
     */
    fun publicarAviso(
        aviso: Aviso,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Generamos un nuevo documento para obtener un ID automático
        val nuevoDoc = avisosCollection.document()
        
        // Creamos una copia del aviso con el ID generado por Firestore
        val avisoConId = aviso.copy(id = nuevoDoc.id)

        nuevoDoc.set(avisoConId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    /**
     * Escucha los cambios en la colección de avisos en tiempo real.
     * Ordena los resultados por timestamp de forma descendente.
     * @param onSuccess Devuelve la lista actualizada de avisos.
     * @param onFailure Devuelve la excepción en caso de error de conexión.
     */
    fun obtenerAvisosRealtime(
        onSuccess: (List<Aviso>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        avisosCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Mapeamos los documentos a objetos de tipo Aviso
                    val listaAvisos = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Aviso::class.java)
                    }
                    onSuccess(listaAvisos)
                }
            }
    }

    fun eliminarAviso(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        avisosCollection.document(id).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
