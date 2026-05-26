package com.example.actividadmenu.repository

import com.example.actividadmenu.model.Chat
import com.example.actividadmenu.model.Mensaje
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatRepository {
    private val db = FirebaseFirestore.getInstance()
    private val chatsCollection = db.collection("chats")

    fun crearUObtenerChat(miId: String, miNombre: String, otroId: String, otroNombre: String, onSuccess: (String) -> Unit) {
        if (otroId.isEmpty() || miId == otroId) return

        chatsCollection.whereArrayContains("usuarios", miId).get().addOnSuccessListener { snapshot ->
            val chatExistente = snapshot.documents.find { doc ->
                val usuarios = doc.get("usuarios") as? List<*>
                usuarios?.contains(otroId) == true
            }

            if (chatExistente != null) {
                onSuccess(chatExistente.id)
            } else {
                val nuevoChatDoc = chatsCollection.document()
                val nuevoChat = Chat(
                    id = nuevoChatDoc.id,
                    usuarios = listOf(miId, otroId),
                    nombresUsuarios = mapOf(miId to miNombre, otroId to otroNombre),
                    ultimoMensaje = "Iniciaste una conversación",
                    timestamp = System.currentTimeMillis()
                )
                nuevoChatDoc.set(nuevoChat).addOnSuccessListener { onSuccess(nuevoChatDoc.id) }
            }
        }
    }

    fun obtenerMisChatsRealtime(miId: String, onSuccess: (List<Chat>) -> Unit) {
        chatsCollection.whereArrayContains("usuarios", miId)
            .addSnapshotListener { snapshot, _ ->
                val chats = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Chat::class.java)?.copy(id = doc.id)
                }?.sortedByDescending { it.timestamp } ?: emptyList()
                onSuccess(chats)
            }
    }

    fun obtenerMensajesRealtime(chatId: String, onSuccess: (List<Mensaje>) -> Unit) {
        if (chatId.isEmpty()) return
        chatsCollection.document(chatId).collection("mensajes")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                val mensajes = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Mensaje::class.java)
                } ?: emptyList()
                onSuccess(mensajes)
            }
    }

    fun enviarMensaje(chatId: String, mensaje: Mensaje, onSuccess: () -> Unit) {
        if (chatId.isEmpty()) return
        val chatDoc = chatsCollection.document(chatId)
        val mensajeDoc = chatDoc.collection("mensajes").document()
        mensajeDoc.set(mensaje.copy(id = mensajeDoc.id)).addOnSuccessListener {
            chatDoc.update(mapOf("ultimoMensaje" to mensaje.texto, "timestamp" to System.currentTimeMillis()))
                .addOnSuccessListener { onSuccess() }
        }
    }
}
