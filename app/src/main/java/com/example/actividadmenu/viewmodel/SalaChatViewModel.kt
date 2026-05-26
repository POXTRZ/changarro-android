package com.example.actividadmenu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.actividadmenu.model.Mensaje
import com.example.actividadmenu.repository.ChatRepository

/**
 * ViewModel encargado de la lógica dentro de una sala de chat específica.
 */
class SalaChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    // LiveData que contiene los mensajes de la conversación actual
    private val _mensajes = MutableLiveData<List<Mensaje>>()
    val mensajes: LiveData<List<Mensaje>> get() = _mensajes

    /**
     * Inicia la escucha en tiempo real de los mensajes de una sala.
     * @param chatId ID del documento del chat en Firestore.
     */
    fun inicializarSala(chatId: String) {
        repository.obtenerMensajesRealtime(chatId) { lista ->
            _mensajes.value = lista
        }
    }

    /**
     * Envía un nuevo mensaje de texto a la sala.
     * @param chatId ID del chat.
     * @param senderId ID del usuario que envía el mensaje.
     * @param texto Contenido del mensaje.
     */
    fun enviarMensajeTexto(chatId: String, senderId: String, texto: String) {
        if (texto.isBlank()) return

        val nuevoMensaje = Mensaje(
            senderId = senderId,
            texto = texto,
            timestamp = System.currentTimeMillis()
        )

        repository.enviarMensaje(chatId, nuevoMensaje) {
            // El éxito se refleja automáticamente mediante el SnapshotListener
            // que actualiza el LiveData de mensajes.
        }
    }
}
