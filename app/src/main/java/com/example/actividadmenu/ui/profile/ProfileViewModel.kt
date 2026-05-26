package com.example.actividadmenu.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.actividadmenu.Producto
import com.example.actividadmenu.data.repository.ProductoRepository
import com.example.actividadmenu.data.repository.TransaccionRepository
import com.example.actividadmenu.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class ProfileViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val productoRepo = ProductoRepository()
    private val transaccionRepo = TransaccionRepository()

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

    private fun cargarFavoritos(ids: List<String>) {
        if (ids.isEmpty()) {
            _misFavoritos.value = emptyList()
            return
        }
        // Firestore limit: 'in' query supports up to 10 ids.
        // For a university app, this is fine for now.
        db.collection("productos").whereIn("id", ids.take(10)).get()
            .addOnSuccessListener { snapshot ->
                val lista = snapshot.documents.mapNotNull { it.toObject(Producto::class.java) }
                _misFavoritos.value = lista
            }
    }

    fun cargarDatosPerfil(userId: String) {
        // Cargar datos del usuario desde Firestore
        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { doc ->
                val usuario = doc.toObject(Usuario::class.java)
                usuario?.let { 
                    _perfilUsuario.value = it 
                    cargarFavoritos(it.favoritos)
                }
            }
        
        // Obtener estadísticas de ventas de hoy
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

        // Obtener productos para monitorear alertas de stock bajo
        productoRepo.obtenerMisProductos(
            userId = userId,
            onSuccess = { productos ->
                val alertas = productos.filter { it.stock < 5 }
                _alertasStock.value = alertas
            },
            onFailure = {
                _alertasStock.value = emptyList()
            }
        )
    }
}