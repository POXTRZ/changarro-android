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
 * ViewModel para gestionar la lógica del Mercado General (Marketplace).
 */
class MarketplaceViewModel : ViewModel() {

    private val productRepo = ProductoRepository()
    private val transaccionRepo = TransaccionRepository()
    private val usuarioRepo = com.example.actividadmenu.repository.UsuarioRepository()

    private val _listaCompleta = mutableListOf<Producto>()
    
    // LiveData para los favoritos del usuario
    private val _favoritosIds = MutableLiveData<Set<String>>(emptySet())
    val favoritosIds: LiveData<Set<String>> get() = _favoritosIds

    // LiveData que la UI observa para mostrar los productos (ya filtrados)
    private val _productosFiltrados = MutableLiveData<List<Producto>>()
    val productosFiltrados: LiveData<List<Producto>> get() = _productosFiltrados

    // LiveData para manejar mensajes de estado o errores
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private var categoriaActual = "Todos"
    private var textoBusqueda = ""

    init {
        cargarMercado()
    }

    fun syncFavoritos(userId: String) {
        usuarioRepo.escucharUsuario(userId) { usuario ->
            _favoritosIds.value = usuario.favoritos.toSet()
        }
    }

    fun toggleFavorito(userId: String, productoId: String) {
        val actual = _favoritosIds.value ?: emptySet()
        val seraFavorito = !actual.contains(productoId)
        usuarioRepo.toggleFavorito(userId, productoId, seraFavorito) {}
    }

    fun eliminarProducto(productoId: String) {
        productRepo.eliminarProducto(productoId,
            onSuccess = { _error.value = "Producto eliminado" },
            onFailure = { _error.value = "Error al eliminar: ${it.message}" }
        )
    }

    /**
     * Carga todos los productos del mercado en tiempo real.
     */
    fun cargarMercado() {
        productRepo.obtenerTodosLosProductosRealtime(
            onSuccess = { productos ->
                _listaCompleta.clear()
                _listaCompleta.addAll(productos)
                aplicarFiltros()
            },
            onFailure = { e ->
                _error.value = "Error al cargar mercado: ${e.message}"
            }
        )
    }

    fun filtrarPorCategoria(categoria: String) {
        this.categoriaActual = categoria
        aplicarFiltros()
    }

    fun filtrarPorTexto(texto: String) {
        this.textoBusqueda = texto
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val query = textoBusqueda.lowercase(Locale.getDefault())
        val filtrados = _listaCompleta.filter { item ->
            val coincideFiltro = categoriaActual == "Todos" || item.categoria == categoriaActual
            val coincideBusqueda = item.nombre.lowercase().contains(query) || 
                                 item.vendedorNombre.lowercase().contains(query)
            coincideFiltro && coincideBusqueda
        }
        _productosFiltrados.value = filtrados
    }

    /**
     * Lógica de compra: Actualiza el stock y luego registra la transacción.
     */
    fun procesarCompra(producto: Producto, compradorId: String, cantidad: Int = 1) {
        if (producto.stock < cantidad) {
            _error.value = "Stock insuficiente"
            return
        }

        val nuevoStock = producto.stock - cantidad

        productRepo.actualizarStock(
            productoId = producto.id,
            nuevoStock = nuevoStock,
            onSuccess = {
                val transaccion = Transaccion(
                    productoId = producto.id,
                    nombreProducto = producto.nombre,
                    tipo = "VENTA",
                    cantidad = cantidad,
                    montoTotal = producto.precio * cantidad,
                    fecha = System.currentTimeMillis(),
                    vendedorId = producto.vendedorId
                )

                transaccionRepo.registrarTransaccion(
                    transaccion = transaccion,
                    onSuccess = {
                        _error.value = "¡Apartado con éxito!"
                    },
                    onFailure = { e ->
                        _error.value = "Error al registrar transacción: ${e.message}"
                    }
                )
            },
            onFailure = { e ->
                _error.value = "Error al actualizar stock: ${e.message}"
            }
        )
    }

    /**
     * Publica un nuevo producto en el mercado global.
     */
    fun publicarNuevoProducto(
        nombre: String,
        precio: Double,
        stock: Int,
        descripcion: String,
        categoria: String,
        vendedorId: String,
        vendedorNombre: String,
        vendedorCarrera: String = "",
        vendedorSemestre: String = "",
        vendedorWhatsapp: String = ""
    ) {
        val nuevoProducto = Producto(
            nombre = nombre,
            precio = precio,
            stock = stock,
            descripcion = descripcion,
            categoria = categoria,
            vendedorId = vendedorId,
            vendedorNombre = vendedorNombre,
            vendedorCarrera = vendedorCarrera,
            vendedorSemestre = vendedorSemestre,
            vendedorWhatsapp = vendedorWhatsapp
        )

        productRepo.agregarProducto(
            producto = nuevoProducto,
            onSuccess = {
                _error.value = "Producto publicado correctamente"
            },
            onFailure = { e ->
                _error.value = "Error al publicar: ${e.message}"
            }
        )
    }

    fun errorMostrado() {
        _error.value = null
    }
}
