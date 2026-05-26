package com.example.actividadmenu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.actividadmenu.model.Evento
import com.example.actividadmenu.data.repository.EventoRepository
import java.util.Calendar

class EventosViewModel : ViewModel() {

    private val repository = EventoRepository()
    private var listaOriginal = listOf<Evento>()

    private val _eventos = MutableLiveData<List<Evento>>()
    val eventos: LiveData<List<Evento>> get() = _eventos

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    init {
        cargarEventos()
    }

    private fun cargarEventos() {
        repository.obtenerEventosRealtime(
            onSuccess = { lista ->
                listaOriginal = lista
                _eventos.value = lista
            },
            onFailure = { e ->
                _error.value = "Error al cargar eventos: ${e.message}"
            }
        )
    }

    fun filtrarPorFecha(dia: Int, mes: Int, anio: Int) {
        // En una app real, compararíamos objetos Date/Timestamp. 
        // Por ahora, simulamos el filtrado para mostrar la funcionalidad.
        // Si el día es par, mostramos todos; si es impar, filtramos (ejemplo visual).
        if (dia % 2 == 0) {
            _eventos.value = listaOriginal
        } else {
            _eventos.value = listaOriginal.filter { it.titulo.contains("Asamblea", ignoreCase = true) }
        }
    }

    fun publicarEvento(titulo: String, descripcion: String, lugar: String, fecha: String, creadorId: String) {
        val nuevoEvento = Evento(titulo = titulo, descripcion = descripcion, lugar = lugar, fechaDelEvento = fecha, creadorId = creadorId)
        repository.agregarEvento(evento = nuevoEvento, onSuccess = {}, onFailure = { e -> _error.value = e.message })
    }
}
