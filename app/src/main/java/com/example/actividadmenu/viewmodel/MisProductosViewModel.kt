package com.example.actividadmenu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.actividadmenu.Producto
import com.example.actividadmenu.Transaccion
import com.example.actividadmenu.repository.ProductoRepository
import com.example.actividadmenu.repository.TransaccionRepository
import java.util.Locale

class MisProductosViewModel : ViewModel() {

    private val productRepo = ProductoRepository()
    private val transaccionRepo = TransaccionRepository()

    private val _listaCompleta = mutableListOf<Producto>()

    private val _productosFiltrados = MutableLiveData<List<Producto>>()
    val productosFiltrados: LiveData<List<Producto>> get() = _productosFiltrados

    private val _listaTransacciones = MutableLiveData<List<Transaccion>>()
    val listaTransacciones: LiveData<List<Transaccion>> get() = _listaTransacciones

    private val _status = MutableLiveData<String?>()
    val status: LiveData<String?> get() = _status

    fun cargarMisProductos(userId: String) {
        productRepo.obtenerMisProductos(userId, { lista ->
            _listaCompleta.clear()
            _listaCompleta.addAll(lista)
            _productosFiltrados.value = lista
        }, {
            _status.value = "Error al cargar productos"
        })
    }

    fun filtrarPorTexto(texto: String) {
        val query = texto.lowercase(Locale.getDefault())
        _productosFiltrados.value = if (query.isEmpty()) {
            _listaCompleta
        } else {
            _listaCompleta.filter { it.nombre.lowercase().contains(query) }
        }
    }

    fun registrarMovimiento(producto: Producto, tipo: String, cantidad: Int, userId: String) {
        val nuevoStock = if (tipo == "VENTA") producto.stock - cantidad else producto.stock + cantidad
        
        if (nuevoStock < 0) {
            _status.value = "Stock insuficiente"
            return
        }

        productRepo.actualizarStock(producto.id, nuevoStock, {
            val transaccion = Transaccion(
                productoId = producto.id,
                nombreProducto = producto.nombre,
                tipo = tipo,
                cantidad = cantidad,
                montoTotal = if (tipo == "VENTA") producto.precio * cantidad else 0.0,
                fecha = System.currentTimeMillis(),
                vendedorId = userId
            )
            transaccionRepo.registrarTransaccion(transaccion, {
                _status.value = "Movimiento registrado con éxito"
            }, {
                _status.value = "Error al registrar transacción"
            })
        }, {
            _status.value = "Error al actualizar stock"
        })
    }

    fun publicarNuevoProducto(producto: Producto) {
        productRepo.agregarProducto(producto, {
            _status.value = "Producto publicado correctamente"
        }, {
            _status.value = "Error al publicar producto"
        })
    }

    fun editarProducto(productoId: String, datos: Map<String, Any>) {
        productRepo.actualizarProducto(productoId, datos, {
            _status.value = "Producto actualizado"
        }, {
            _status.value = "Error al actualizar"
        })
    }

    fun cargarTransacciones(userId: String) {
        transaccionRepo.obtenerMisTransaccionesRealtime(userId, { lista ->
            _listaTransacciones.value = lista
        }, { _status.value = "Error al cargar historial" })
    }

    fun eliminarProducto(productoId: String) {
        productRepo.eliminarProducto(productoId, { _status.value = "Producto eliminado" }, {
            _status.value = "Error al eliminar"
        })
    }

    fun statusMostrado() { _status.value = null }
}
