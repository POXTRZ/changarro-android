package com.example.actividadmenu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.actividadmenu.model.Evento
import com.example.actividadmenu.data.repository.EventoRepository

/**
 * ViewModel encargado de la lógica de la pestaña de Eventos / Calendario.
 */
class EventosViewModel : ViewModel() {

    private val repository = EventoRepository()

    // Lista de eventos que la UI observará
    private val _eventos = MutableLiveData<List<Evento>>()
    val eventos: LiveData<List<Evento>> get() = _eventos

    // Mensajes de estado o error para la UI
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    init {
        cargarEventos()
    }

    /**
     * Inicia la escucha en tiempo real de los eventos desde el repositorio.
     */
    private fun cargarEventos() {
        repository.obtenerEventosRealtime(
            onSuccess = { lista ->
                _eventos.value = lista
            },
            onFailure = { e ->
                _error.value = "Error al cargar eventos: ${e.message}"
            }
        )
    }

    /**
     * Crea un nuevo objeto Evento y solicita al repositorio guardarlo en Firestore.
     */
    fun publicarEvento(
        titulo: String,
        descripcion: String,
        lugar: String,
        fecha: String,
        creadorId: String
    ) {
        if (titulo.isBlank() || fecha.isBlank()) {
            _error.value = "El título y la fecha son obligatorios"
            return
        }

        val nuevoEvento = Evento(
            titulo = titulo,
            descripcion = descripcion,
            lugar = lugar,
            fechaDelEvento = fecha,
            creadorId = creadorId
        )

        repository.agregarEvento(
            evento = nuevoEvento,
            onSuccess = {
                // Nota: No es necesario actualizar el LiveData aquí manualmente
                // porque el addSnapshotListener lo hará por nosotros automáticamente.
            },
            onFailure = { e ->
                _error.value = "Error al publicar el evento: ${e.message}"
            }
        )
    }

    /**
     * Limpia el mensaje de error después de ser procesado por la vista.
     */
    fun errorMostrado() {
        _error.value = ""
    }
}
