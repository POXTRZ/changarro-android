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

        val rvProductos = view.findViewById<RecyclerView>(R.id.rvProductosCV)
        val etSearch = view.findViewById<EditText>(R.id.etSearchCV)
        val layoutEmpty = view.findViewById<LinearLayout>(R.id.layoutEmptyCV)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupCategorias)
        val fabVender = view.findViewById<FloatingActionButton>(R.id.fabVender)

        adapter = ProductoAdapter(emptyList(),
            onClick = { producto -> mostrarDialogoDetalle(producto) },
            onLongClick = { /* Opcional */ }
        )
        rvProductos.layoutManager = LinearLayoutManager(context)
        rvProductos.adapter = adapter

        // Observar productos del ViewModel
        viewModel.productosFiltrados.observe(viewLifecycleOwner) { productos ->
            adapter.updateLista(productos)
            layoutEmpty.visibility = if (productos.isEmpty()) View.VISIBLE else View.GONE
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
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Publicar Producto")

        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val etNombre = EditText(context).apply { hint = "Nombre del producto" }
        val etPrecio = EditText(context).apply { 
            hint = "Precio" 
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val etStock = EditText(context).apply { 
            hint = "Stock inicial"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val etDesc = EditText(context).apply { hint = "Descripción (donde entregas...)" }
        
        val spCategoria = Spinner(context)
        val categorias = arrayOf("Comida", "Dulces", "Apuntes", "Otros")
        spCategoria.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categorias)

        layout.addView(etNombre)
        layout.addView(etPrecio)
        layout.addView(etStock)
        layout.addView(etDesc)
        layout.addView(spCategoria)
        builder.setView(layout)

        builder.setPositiveButton("Publicar") { _, _ ->
            val nombre = etNombre.text.toString()
            val precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0
            val stock = etStock.text.toString().toIntOrNull() ?: 0
            val desc = etDesc.text.toString()
            val cat = spCategoria.selectedItem.toString()
            val user = auth.currentUser

            if (nombre.isNotEmpty() && user != null) {
                val userName = user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Estudiante"
                viewModel.publicarNuevoProducto(nombre, precio, stock, desc, cat, user.uid, userName)
            } else {
                Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun mostrarDialogoDetalle(producto: Producto) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(producto.nombre)
        builder.setMessage("Vendedor: ${producto.vendedorNombre}\nPrecio: $${producto.precio}\nStock: ${producto.stock}\n\n${producto.descripcion}")
        
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
