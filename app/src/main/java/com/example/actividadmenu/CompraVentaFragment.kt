package com.example.actividadmenu

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.actividadmenu.databinding.FragmentCompraventaBinding
import com.example.actividadmenu.repository.ChatRepository
import com.example.actividadmenu.viewmodel.MarketplaceViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CompraVentaFragment : Fragment() {

    private var _binding: FragmentCompraventaBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: MarketplaceViewModel
    private lateinit var adapter: ProductoAdapter

    private var miCarrera = ""
    private var miSemestre = ""
    private var miWhatsapp = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCompraventaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this)[MarketplaceViewModel::class.java]

        // Cargar info del usuario actual
        auth.currentUser?.let { user ->
            FirebaseFirestore.getInstance().collection("usuarios").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    miCarrera = doc.getString("carrera") ?: ""
                    miSemestre = doc.getString("semestre") ?: ""
                    miWhatsapp = doc.getString("whatsapp") ?: ""
                }
            viewModel.syncFavoritos(user.uid)
        }

        setupRecyclerView()
        setupFilters()

        // Observar productos
        viewModel.productosFiltrados.observe(viewLifecycleOwner) { productos ->
            adapter.updateLista(productos)
            binding.layoutEmptyCV.visibility = if (productos.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observar favoritos
        viewModel.favoritosIds.observe(viewLifecycleOwner) { favoritos ->
            adapter.updateFavoritos(favoritos)
        }

        binding.fabVender.setOnClickListener { mostrarDialogoPublicar() }
    }

    private fun setupRecyclerView() {
        adapter = ProductoAdapter(
            listaProductos = emptyList(),
            onClick = { producto -> mostrarDialogoDetalle(producto) },
            onLongClick = { /* Senior: Acción de pulsación larga */ },
            onFavoriteClick = { producto ->
                auth.currentUser?.let { viewModel.toggleFavorito(it.uid, producto.id) }
            },
            onEliminarClick = { producto ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar")
                    .setMessage("¿Quitar del mercado?")
                    .setPositiveButton("Sí") { _, _ -> viewModel.eliminarProducto(producto.id) }
                    .setNegativeButton("No", null).show()
            }
        )
        binding.rvProductosCV.layoutManager = LinearLayoutManager(context)
        binding.rvProductosCV.adapter = adapter
    }

    private fun setupFilters() {
        binding.etSearchCV.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filtrarPorTexto(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // FIX SENIOR: Solo usamos los IDs que existen en el XML premium
        binding.chipGroupCategorias.setOnCheckedStateChangeListener { _, checkedIds ->
            val categoria = when (checkedIds.firstOrNull()) {
                R.id.chipComida -> "Comida"
                R.id.chipDulces -> "Dulces"
                R.id.chipElectronicos -> "Electrónicos"
                else -> "Todos"
            }
            viewModel.filtrarPorCategoria(categoria)
        }
    }

    private fun mostrarDialogoPublicar() {
        val bottomSheet = PublicarProductoBottomSheet { nombre, precio, stock, desc, cat, url ->
            val user = auth.currentUser
            if (user != null) {
                val userName = user.displayName ?: "Estudiante"
                // Llamamos a la función gratuita directa
                viewModel.publicarNuevoProducto(
                    nombre = nombre,
                    precio = precio,
                    stock = stock,
                    descripcion = desc,
                    categoria = cat,
                    vendedorId = user.uid,
                    vendedorNombre = userName,
                    vendedorCarrera = miCarrera,
                    vendedorSemestre = miSemestre,
                    vendedorWhatsapp = miWhatsapp,
                    imagenUrl = url
                )
            }
        }
        bottomSheet.show(parentFragmentManager, "PublicarProductoBottomSheet")
    }

    private fun mostrarDialogoDetalle(producto: Producto) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(producto.nombre)
        builder.setMessage("Vendedor: ${producto.vendedorNombre}\nPrecio: $${producto.precio}\n\n${producto.descripcion}")

        builder.setPositiveButton("WhatsApp") { _, _ ->
            val url = "https://api.whatsapp.com/send?phone=${producto.vendedorWhatsapp}&text=Hola, me interesa tu producto: ${producto.nombre}"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        builder.setNegativeButton("Chat App") { _, _ ->
            val user = auth.currentUser
            if (user != null && user.uid != producto.vendedorId) {
                val repo = ChatRepository()
                repo.crearUObtenerChat(user.uid, "Usuario", producto.vendedorId, producto.vendedorNombre) { chatId ->
                    val intent = Intent(requireContext(), ChatActivity::class.java)
                    intent.putExtra("chatId", chatId)
                    intent.putExtra("otroNombre", producto.vendedorNombre)
                    startActivity(intent)
                }
            }
        }
        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}