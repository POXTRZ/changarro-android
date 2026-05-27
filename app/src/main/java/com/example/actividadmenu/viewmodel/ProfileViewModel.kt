package com.example.actividadmenu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.actividadmenu.Producto
import com.example.actividadmenu.model.Usuario
import com.example.actividadmenu.repository.ProductoRepository
import com.example.actividadmenu.repository.TransaccionRepository
import com.example.actividadmenu.repository.UsuarioRepository
import java.util.Locale

class ProfileViewModel : ViewModel() {

    private val productoRepo = ProductoRepository()
    private val transaccionRepo = TransaccionRepository()
    private val usuarioRepo = UsuarioRepository()

    // LiveData para los datos del usuario (Nombre, Carrera, etc)
    private val _perfilUsuario = MutableLiveData<Usuario>()
    val perfilUsuario: LiveData<Usuario> get() = _perfilUsuario

    private val _ventasHoy = MutableLiveData<String>()
    val ventasHoy: LiveData<String> get() = _ventasHoy

    private val _totalProductosVendidos = MutableLiveData<String>()
    val totalProductosVendidos: LiveData<String> get() = _totalProductosVendidos

    private val _alertasStock = MutableLiveData<List<Producto>>()
    val alertasStock: LiveData<List<Producto>> get() = _alertasStock

    private val _misFavoritos = MutableLiveData<List<Producto>>()
    val misFavoritos: LiveData<List<Producto>> get() = _misFavoritos

    fun cargarDatosPerfil(userId: String) {
        // 1. Cargar datos básicos del perfil
        usuarioRepo.obtenerUsuario(userId, 
            onSuccess = { usuario ->
                _perfilUsuario.value = usuario
            },
            onFailure = {
                // Fallback si no hay datos en Firestore
                _perfilUsuario.value = Usuario(nombre = "Estudiante")
            }
        )
        
        // 2. Obtener estadísticas de ventas de hoy
        transaccionRepo.obtenerMisVentasHoy(
            userId = userId,
            onSuccess = { monto, cantidad ->
                _ventasHoy.value = String.format(Locale.US, "$%.2f", monto)
                _totalProductosVendidos.value = cantidad.toString()
            },
            onFailure = {
                _ventasHoy.value = "$0.00"
                _totalProductosVendidos.value = "0"
            }
        )

        // 3. Monitorear alertas de stock
        productoRepo.obtenerMisProductos(
            userId = userId,
            onSuccess = { productos ->
                val criticos = productos.filter { it.stock < 5 }
                _alertasStock.value = criticos
            },
            onFailure = { _alertasStock.value = emptyList() }
        )

        // 4. Cargar Favoritos real (opcional pero Senior)
        usuarioRepo.escucharUsuario(userId) { user ->
            productoRepo.obtenerTodosLosProductosRealtime({ todos ->
                val favs = todos.filter { user.favoritos.contains(it.id) }
                _misFavoritos.value = favs
            }, {})
        }
    }
}
