package com.example.actividadmenu

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.actividadmenu.repository.ChatRepository
import com.example.actividadmenu.viewmodel.MarketplaceViewModel
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class CompraVentaFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: MarketplaceViewModel
    private lateinit var adapter: ProductoAdapter
    private var miCarrera = ""
    private var miSemestre = ""
    private var miWhatsapp = ""
    
    private var listaTotal = listOf<Producto>()
    private var categoriaActual = "Todos"
    private var textoBusqueda = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_compraventa, container, false)

        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this)[MarketplaceViewModel::class.java]

        auth.currentUser?.let { user ->
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("usuarios").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    miCarrera = doc.getString("carrera") ?: ""
                    miSemestre = doc.getString("semestre") ?: ""
                    miWhatsapp = doc.getString("whatsapp") ?: ""
                }
        }

        val rvProductos = view.findViewById<RecyclerView>(R.id.rvProductosCV)
        val etSearch = view.findViewById<EditText>(R.id.etSearchCV)
        val layoutEmpty = view.findViewById<LinearLayout>(R.id.layoutEmptyCV)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupCategorias)
        val fabVender = view.findViewById<com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton>(R.id.fabVender)

        adapter = ProductoAdapter(emptyList(),
            onClick = { producto -> mostrarDialogoDetalle(producto) },
            onLongClick = { /* Opcional */ },
            onFavoriteClick = { producto ->
                val user = auth.currentUser
                if (user != null) {
                    viewModel.toggleFavorito(user.uid, producto.id)
                }
            },
            onEliminarClick = { producto ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar producto")
                    .setMessage("¿Deseas quitar este producto del mercado?")
                    .setPositiveButton("Eliminar") { _, _ -> viewModel.eliminarProducto(producto.id) }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        rvProductos.layoutManager = LinearLayoutManager(context)
        rvProductos.adapter = adapter

        // Sincronizar favoritos
        auth.currentUser?.let { viewModel.syncFavoritos(it.uid) }

        // Observar productos del ViewModel
        viewModel.productosFiltrados.observe(viewLifecycleOwner) { productos ->
            adapter.updateLista(productos)
            layoutEmpty.visibility = if (productos.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observar favoritos
        viewModel.favoritosIds.observe(viewLifecycleOwner) { favoritos ->
            adapter.updateFavoritos(favoritos)
        }

        // Observar errores o mensajes de estado
        viewModel.error.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.errorMostrado()
            }
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filtrarPorTexto(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val chipId = checkedIds.firstOrNull()
            val categoria = when (chipId) {
                R.id.chipComida -> "Comida"
                R.id.chipDulces -> "Dulces"
                R.id.chipApuntes -> "Apuntes"
                R.id.chipLibros -> "Libros"
                R.id.chipElectronicos -> "Electrónicos"
                R.id.chipAccesorios -> "Accesorios"
                else -> "Todos"
            }
            viewModel.filtrarPorCategoria(categoria)
        }

        fabVender.setOnClickListener {
            mostrarDialogoPublicar()
        }

        return view
    }

    private fun mostrarDialogoPublicar() {
        val bottomSheet = PublicarProductoBottomSheet { nombre, precio, stock, desc, cat ->
            val user = auth.currentUser
            if (user != null) {
                val userName = user.displayName ?: user.email?.substringBefore("@") ?: "Estudiante"
                viewModel.publicarNuevoProducto(nombre, precio, stock, desc, cat, user.uid, userName, miCarrera, miSemestre, miWhatsapp)
            }
        }
        bottomSheet.show(parentFragmentManager, "PublicarProductoBottomSheet")
    }

    private fun mostrarDialogoDetalle(producto: Producto) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(producto.nombre)
        
        val infoVendedor = StringBuilder("Vendedor: ${producto.vendedorNombre}")
        if (producto.vendedorCarrera.isNotEmpty()) infoVendedor.append("\nCarrera: ${producto.vendedorCarrera}")
        if (producto.vendedorSemestre.isNotEmpty()) infoVendedor.append("\nSemestre: ${producto.vendedorSemestre}")
        
        builder.setMessage("${infoVendedor}\nPrecio: $${producto.precio}\nStock: ${producto.stock}\n\n${producto.descripcion}")
        
        builder.setPositiveButton("Comprar/Apartar") { _, _ ->
            val user = auth.currentUser
            if (user != null) {
                viewModel.procesarCompra(producto, user.uid)
            }
        }
        
        builder.setNeutralButton("WhatsApp") { _, _ ->
            contactarVendedor(producto)
        }

        // INTEGRACIÓN CHAT NATIVO
        builder.setNegativeButton("Chat en App") { _, _ ->
            val user = auth.currentUser
            if (user != null) {
                if (user.uid != producto.vendedorId) {
                    val repo = ChatRepository()
                    val miNombre = user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Usuario"
                    
                    repo.crearUObtenerChat(user.uid, miNombre, producto.vendedorId, producto.vendedorNombre) { chatId ->
                        val intent = Intent(requireContext(), ChatActivity::class.java)
                        intent.putExtra("chatId", chatId)
                        intent.putExtra("otroNombre", producto.vendedorNombre)
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(context, "No puedes chatear contigo mismo", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        builder.show()
    }

    private fun contactarVendedor(producto: Producto) {
        val tel = producto.vendedorWhatsapp
        if (tel.isEmpty()) {
            Toast.makeText(context, "El vendedor no proporcionó WhatsApp", Toast.LENGTH_SHORT).show()
            return
        }
        
        val url = "https://api.whatsapp.com/send?phone=$tel&text=Hola ${producto.vendedorNombre}, me interesa tu producto ${producto.nombre} de Changarro"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
}
