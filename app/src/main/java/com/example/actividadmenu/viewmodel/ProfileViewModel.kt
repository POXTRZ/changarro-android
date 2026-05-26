package com.example.actividadmenu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.actividadmenu.Producto
import com.example.actividadmenu.repository.ProductoRepository
import com.example.actividadmenu.repository.TransaccionRepository
import java.util.Locale

/**
 * ViewModel para gestionar el Dashboard del Perfil del usuario.
 * Procesa datos financieros y de inventario para la UI.
 */
class ProfileViewModel : ViewModel() {

    private val productoRepo = ProductoRepository()
    private val transaccionRepo = TransaccionRepository()

    // LiveData para la UI
    private val _ventasHoy = MutableLiveData<String>()
    val ventasHoy: LiveData<String> get() = _ventasHoy

    private val _totalProductosVendidos = MutableLiveData<String>()
    val totalProductosVendidos: LiveData<String> get() = _totalProductosVendidos

    private val _alertasStock = MutableLiveData<List<Producto>>()
    val alertasStock: LiveData<List<Producto>> get() = _alertasStock

    /**
     * Carga y procesa los datos del perfil en tiempo real.
     * @param userId ID del usuario autenticado.
     */
    fun cargarDatosPerfil(userId: String) {
        
        // 1. Obtener estadísticas de ventas de hoy
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

        // 2. Obtener productos para monitorear alertas de stock
        productoRepo.obtenerMisProductos(
            userId = userId,
            onSuccess = { productos ->
                // Filtramos productos con stock crítico (< 5)
                val criticos = productos.filter { it.stock < 5 }
                _alertasStock.value = criticos
            },
            onFailure = {
                _alertasStock.value = emptyList()
            }
        )
    }
}
