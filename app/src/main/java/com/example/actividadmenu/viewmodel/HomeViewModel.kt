package com.example.actividadmenu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.actividadmenu.model.Aviso
import com.example.actividadmenu.repository.AvisoRepository

/**
 * ViewModel encargado de la lógica del Muro de Avisos (Home).
 * Sigue estrictamente el patrón MVVM, exponiendo estados a través de LiveData.
 */
class HomeViewModel : ViewModel() {

    private val repository = AvisoRepository()

    private val _avisos = MutableLiveData<List<Aviso>>()
    val avisos: LiveData<List<Aviso>> get() = _avisos

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    init {
        cargarAvisos()
    }

    fun cargarAvisos() {
        repository.obtenerAvisosRealtime(
            onSuccess = { listaDescargada ->
                _avisos.value = listaDescargada
            },
            onFailure = { exception ->
                _error.value = exception.message ?: "Error desconocido al conectar con Firestore"
            }
        )
    }

    /**
     * Publica un nuevo aviso a través del repositorio.
     */
    fun publicarAviso(aviso: Aviso, onComplete: () -> Unit) {
        repository.publicarAviso(
            aviso,
            onSuccess = {
                onComplete()
            },
            onFailure = { exception ->
                _error.value = "Error al publicar: ${exception.message}"
            }
        )
    }
}
