package com.example.actividadmenu.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.actividadmenu.Producto
import com.example.actividadmenu.Transaccion
import com.example.actividadmenu.repository.ProductoRepository
import com.example.actividadmenu.repository.TransaccionRepository
import java.util.Locale

/**
 * ViewModel para gestionar el Inventario Personal del vendedor.
 */
class MisProductosViewModel : ViewModel() {

    private val productRepo = ProductoRepository()
    private val transaccionRepo = TransaccionRepository()

    private val _listaCompleta = mutableListOf<Producto>()
    private val _productosFiltrados = MutableLiveData<List<Producto>>()
    val productosFiltrados: LiveData<List<Producto>> get() = _productosFiltrados

    private val _status = MutableLiveData<String?>()
    val status: LiveData<String?> get() = _status

    private var textoBusqueda = ""

    /**
     * Carga los productos que pertenecen al usuario logueado en tiempo real.
     */
    fun cargarMisProductos(userId: String) {
        productRepo.obtenerMisProductos(
            userId = userId,
            onSuccess = { productos ->
                _listaCompleta.clear()
                _listaCompleta.addAll(productos)
                aplicarFiltros()
            },
            onFailure = { _status.value = "Error al cargar inventario: ${it.message}" }
        )
    }

    fun filtrarPorTexto(texto: String) {
        textoBusqueda = texto
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val query = textoBusqueda.lowercase(Locale.getDefault())
        val filtrados = if (query.isEmpty()) {
            _listaCompleta.toList()
        } else {
            _listaCompleta.filter { item ->
                item.nombre.lowercase().contains(query) || 
                item.descripcion.lowercase().contains(query)
            }
        }
        _productosFiltrados.value = filtrados
    }

    fun publicarNuevoProducto(producto: Producto) {
        productRepo.agregarProducto(
            producto = producto,
            onSuccess = { _status.value = "Producto publicado con éxito" },
            onFailure = { _status.value = "Error al publicar: ${it.message}" }
        )
    }

    fun editarProducto(productoId: String, datos: Map<String, Any>) {
        productRepo.actualizarProducto(
            productoId = productoId,
            datos = datos,
            onSuccess = { _status.value = "Producto actualizado" },
            onFailure = { _status.value = "Error al editar: ${it.message}" }
        )
    }

    fun eliminarProducto(productoId: String) {
        productRepo.eliminarProducto(
            productoId = productoId,
            onSuccess = { _status.value = "Producto eliminado" },
            onFailure = { _status.value = "Error al eliminar: ${it.message}" }
        )
    }

    /**
     * Registra una transacción manual y actualiza el stock.
     */
    fun registrarMovimiento(producto: Producto, tipo: String, cantidad: Int, userId: String) {
        val nuevoStock = if (tipo == "VENTA") producto.stock - cantidad else producto.stock + cantidad
        
        if (nuevoStock < 0) {
            _status.value = "Stock insuficiente para la venta"
            return
        }

        productRepo.actualizarStock(
            productoId = producto.id,
            nuevoStock = nuevoStock,
            onSuccess = {
                val transaccion = Transaccion(
                    productoId = producto.id,
                    nombreProducto = producto.nombre,
                    tipo = tipo,
                    cantidad = cantidad,
                    montoTotal = cantidad * producto.precio,
                    vendedorId = userId
                )

                transaccionRepo.registrarTransaccion(
                    transaccion = transaccion,
                    onSuccess = { _status.value = "Movimiento registrado" },
                    onFailure = { _status.value = "Error al registrar transacción: ${it.message}" }
                )
            },
            onFailure = { _status.value = "Error al actualizar stock: ${it.message}" }
        )
    }

    fun statusMostrado() {
        _status.value = null
    }
}
