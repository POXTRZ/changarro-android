package com.example.actividadmenu.data.repository

import com.example.actividadmenu.Producto
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Repositorio para la gestión de productos en Firestore.
 */
class ProductoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val productosCollection = db.collection("productos")

    /**
     * Obtiene en tiempo real los productos que pertenecen al usuario actual.
     * @param userId ID del vendedor (usuario actual).
     */
    fun obtenerMisProductos(
        userId: String,
        onSuccess: (List<Producto>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        productosCollection
            .whereEqualTo("vendedorId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val productos = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Producto::class.java)?.copy(id = doc.id)
                    }
                    onSuccess(productos)
                }
            }
    }
}
