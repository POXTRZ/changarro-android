package com.example.actividadmenu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.actividadmenu.model.Aviso
import com.example.actividadmenu.repository.AvisoRepository

/**
 * ViewModel encargado de gestionar la lógica del Muro de Avisos.
 * Observa el repositorio y expone el estado a la UI mediante LiveData.
 */
class AvisoViewModel : ViewModel() {

    private val repository = AvisoRepository()

    // Lista de avisos que la UI observará
    private val _listaAvisos = MutableLiveData<List<Aviso>>()
    val listaAvisos: LiveData<List<Aviso>> get() = _listaAvisos

    // Estado de carga (útil para mostrar un ProgressBar)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Mensajes de error para mostrar en la UI
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    init {
        // Iniciamos la escucha en tiempo real al instanciar el ViewModel
        obtenerAvisos()
    }

    /**
     * Conecta con el repositorio para obtener avisos en tiempo real.
     */
    private fun obtenerAvisos() {
        _isLoading.value = true
        repository.obtenerAvisosRealtime(
            onSuccess = { avisos ->
                _listaAvisos.value = avisos
                _isLoading.value = false
            },
            onFailure = { e ->
                _error.value = "Error al cargar avisos: ${e.message}"
                _isLoading.value = false
            }
        )
    }

    /**
     * Publica un nuevo aviso.
     * @param contenido Texto del anuncio.
     * @param userId ID del estudiante (obtenido de Auth).
     * @param userName Nombre del estudiante (obtenido de Auth o Perfil).
     */
    fun crearAviso(contenido: String, userId: String, userName: String) {
        if (contenido.isBlank()) {
            _error.value = "El contenido no puede estar vacío"
            return
        }

        val nuevoAviso = Aviso(
            autorId = userId,
            autorNombre = userName,
            contenido = contenido
        )

        _isLoading.value = true
        repository.publicarAviso(
            nuevoAviso,
            onSuccess = {
                _isLoading.value = false
                _error.value = null // Limpiamos errores previos
            },
            onFailure = { e ->
                _error.value = "Error al publicar: ${e.message}"
                _isLoading.value = false
            }
        )
    }
    
    /**
     * Limpia el mensaje de error después de ser mostrado.
     */
    fun errorMostrado() {
        _error.value = null
    }
}
