package com.example.actividadmenu.repository

import com.example.actividadmenu.model.Chat
import com.example.actividadmenu.model.Mensaje
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Repositorio para gestionar la mensajería nativa entre usuarios.
 * Maneja la creación de salas de chat y el envío/recepción de mensajes en tiempo real.
 */
class ChatRepository {

    private val db = FirebaseFirestore.getInstance()
    private val chatsCollection = db.collection("chats")

    /**
     * Busca un chat existente entre dos usuarios o crea uno nuevo si no existe.
     * @param miId ID del usuario actual.
     * @param miNombre Nombre del usuario actual.
     * @param otroId ID del destinatario.
     * @param otroNombre Nombre del destinatario.
     * @param onSuccess Callback que devuelve el ID del chat.
     */
    fun crearUObtenerChat(
        miId: String,
        miNombre: String,
        otroId: String,
        otroNombre: String,
        onSuccess: (String) -> Unit
    ) {
        // Buscamos si ya existe un chat que contenga a ambos usuarios
        chatsCollection
            .whereArrayContains("usuarios", miId)
            .get()
            .addOnSuccessListener { snapshot ->
                val chatExistente = snapshot.documents.find { doc ->
                    val usuarios = doc.get("usuarios") as? List<*>
                    usuarios?.contains(otroId) == true
                }

                if (chatExistente != null) {
                    onSuccess(chatExistente.id)
                } else {
                    // Si no existe, creamos uno nuevo
                    val nuevoChatDoc = chatsCollection.document()
                    val nuevoChat = Chat(
                        id = nuevoChatDoc.id,
                        usuarios = listOf(miId, otroId),
                        nombresUsuarios = mapOf(miId to miNombre, otroId to otroNombre),
                        ultimoMensaje = "Iniciaste una conversación",
                        timestamp = System.currentTimeMillis()
                    )
                    nuevoChatDoc.set(nuevoChat).addOnSuccessListener {
                        onSuccess(nuevoChatDoc.id)
                    }
                }
            }
    }

    /**
     * Escucha los chats activos del usuario en tiempo real.
     */
    fun obtenerMisChatsRealtime(miId: String, onSuccess: (List<Chat>) -> Unit) {
        chatsCollection
            .whereArrayContains("usuarios", miId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val chats = snapshot?.documents?.mapNotNull { it.toObject(Chat::class.java) } ?: emptyList()
                onSuccess(chats)
            }
    }

    /**
     * Escucha los mensajes de un chat específico en tiempo real.
     */
    fun obtenerMensajesRealtime(chatId: String, onSuccess: (List<Mensaje>) -> Unit) {
        chatsCollection.document(chatId).collection("mensajes")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                val mensajes = snapshot?.documents?.mapNotNull { it.toObject(Mensaje::class.java) } ?: emptyList()
                onSuccess(mensajes)
            }
    }

    /**
     * Envía un mensaje y actualiza la información del chat padre.
     */
    fun enviarMensaje(chatId: String, mensaje: Mensaje, onSuccess: () -> Unit) {
        val chatDoc = chatsCollection.document(chatId)
        val mensajeDoc = chatDoc.collection("mensajes").document()
        
        val mensajeConId = mensaje.copy(id = mensajeDoc.id)

        // 1. Subir el mensaje a la subcolección
        mensajeDoc.set(mensajeConId).addOnSuccessListener {
            // 2. Actualizar el chat padre con el último mensaje y nuevo timestamp
            val updates = mapOf(
                "ultimoMensaje" to mensaje.texto,
                "timestamp" to System.currentTimeMillis()
            )
            chatDoc.update(updates).addOnSuccessListener {
                onSuccess()
            }
        }
    }
}
