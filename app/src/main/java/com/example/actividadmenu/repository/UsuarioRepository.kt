package com.example.actividadmenu.repository

import com.example.actividadmenu.model.Usuario
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class UsuarioRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usuariosCollection = db.collection("usuarios")

    fun obtenerUsuario(uid: String, onSuccess: (Usuario) -> Unit, onFailure: (Exception) -> Unit) {
        usuariosCollection.document(uid).get()
            .addOnSuccessListener { doc ->
                val usuario = doc.toObject(Usuario::class.java)
                if (usuario != null) onSuccess(usuario)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun toggleFavorito(userId: String, productoId: String, isFavorito: Boolean, onSuccess: () -> Unit) {
        val operacion = if (isFavorito) {
            FieldValue.arrayUnion(productoId)
        } else {
            FieldValue.arrayRemove(productoId)
        }

        usuariosCollection.document(userId).update("favoritos", operacion)
            .addOnSuccessListener { onSuccess() }
    }

    fun escucharUsuario(uid: String, onUpdate: (Usuario) -> Unit) {
        usuariosCollection.document(uid).addSnapshotListener { snapshot, _ ->
            snapshot?.toObject(Usuario::class.java)?.let { onUpdate(it) }
        }
    }
}
