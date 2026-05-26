package com.example.actividadmenu.repository

import com.example.actividadmenu.Producto
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Repositorio unificado para la gestión de productos.
 */
class ProductoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val productosCollection = db.collection("productos")

    // --- MERCADO ---
    fun obtenerTodosLosProductosRealtime(
        onSuccess: (List<Producto>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        productosCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                onFailure(error)
                return@addSnapshotListener
            }
            val lista = snapshot?.documents?.mapNotNull { it.toObject(Producto::class.java)?.copy(id = it.id) } ?: emptyList()
            onSuccess(lista)
        }
    }

    // --- INVENTARIO / MIS PRODUCTOS ---
    fun obtenerMisProductos(
        userId: String,
        onSuccess: (List<Producto>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        productosCollection.whereEqualTo("vendedorId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }
                val lista = snapshot?.documents?.mapNotNull { it.toObject(Producto::class.java)?.copy(id = it.id) } ?: emptyList()
                onSuccess(lista)
            }
    }

    // --- OPERACIONES CRUD ---
    fun agregarProducto(
        producto: Producto,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val nuevoDoc = productosCollection.document()
        val productoConId = producto.copy(id = nuevoDoc.id)
        nuevoDoc.set(productoConId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun actualizarProducto(
        productoId: String,
        datos: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        productosCollection.document(productoId)
            .update(datos)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun eliminarProducto(
        productoId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        productosCollection.document(productoId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun actualizarStock(
        productoId: String,
        nuevoStock: Int,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        productosCollection.document(productoId)
            .update("stock", nuevoStock)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
