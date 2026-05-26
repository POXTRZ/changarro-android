package com.example.actividadmenu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.actividadmenu.model.Chat
import com.example.actividadmenu.repository.ChatRepository

/**
 * ViewModel encargado de gestionar la lista de conversaciones activas (bandeja de entrada).
 */
class BandejaViewModel : ViewModel() {

    private val repository = ChatRepository()

    // LiveData que contiene la lista de chats para la UI
    private val _listaChats = MutableLiveData<List<Chat>>()
    val listaChats: LiveData<List<Chat>> get() = _listaChats

    /**
     * Carga y escucha en tiempo real las conversaciones donde participa el usuario.
     * @param miId ID del usuario logueado.
     */
    fun cargarMisChats(miId: String) {
        repository.obtenerMisChatsRealtime(miId) { chats ->
            _listaChats.value = chats
        }
    }
}
