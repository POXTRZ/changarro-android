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
    private val productoRepo = com.example.actividadmenu.repository.ProductoRepository()

    private val _listaCompletaAvisos = mutableListOf<Aviso>()
    private val _avisos = MutableLiveData<List<Aviso>>()
    val avisos: LiveData<List<Aviso>> get() = _avisos

    private val _productosDestacados = MutableLiveData<List<com.example.actividadmenu.Producto>>()
    val productosDestacados: LiveData<List<com.example.actividadmenu.Producto>> get() = _productosDestacados

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    init {
        cargarAvisos()
        cargarDestacados()
    }

    fun filtrarAvisos(query: String) {
        if (query.isEmpty()) {
            _avisos.value = _listaCompletaAvisos
        } else {
            _avisos.value = _listaCompletaAvisos.filter { 
                it.contenido.contains(query, ignoreCase = true) || 
                it.autorNombre.contains(query, ignoreCase = true)
            }
        }
    }

    fun cargarDestacados() {
        productoRepo.obtenerTodosLosProductosRealtime(
            onSuccess = { productos ->
                _productosDestacados.value = productos.take(6)
            },
            onFailure = { _error.value = it.message }
        )
    }

    fun cargarAvisos() {
        repository.obtenerAvisosRealtime(
            onSuccess = { listaDescargada ->
                _listaCompletaAvisos.clear()
                _listaCompletaAvisos.addAll(listaDescargada)
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

    fun eliminarAviso(aviso: Aviso) {
        repository.eliminarAviso(
            aviso.id,
            onSuccess = { /* La UI se actualiza sola por el listener */ },
            onFailure = { _error.value = it.message }
        )
    }
}
