package com.example.actividadmenu

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.actividadmenu.viewmodel.MisProductosViewModel
import com.google.firebase.auth.FirebaseAuth

class ProductoFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: MisProductosViewModel
    private lateinit var adapter: ProductoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_producto, container, false)

        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this)[MisProductosViewModel::class.java]
        
        val rvProductos = view.findViewById<RecyclerView>(R.id.rvProductos)
        val btnAgregar = view.findViewById<TextView>(R.id.btnAgregarProducto)
        val layoutEmpty = view.findViewById<LinearLayout>(R.id.layoutEmpty)
        val etSearch = view.findViewById<EditText>(R.id.etSearch)

        adapter = ProductoAdapter(emptyList(), 
            onClick = { producto -> mostrarMenuAcciones(producto) },
            onLongClick = { producto -> mostrarDialogoEliminar(producto) }
        )
        rvProductos.layoutManager = LinearLayoutManager(context)
        rvProductos.adapter = adapter

        // Observar datos
        viewModel.productosFiltrados.observe(viewLifecycleOwner) { productos ->
            adapter.updateLista(productos)
            layoutEmpty.visibility = if (productos.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.status.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.statusMostrado()
            }
        }

        // Cargar productos del usuario actual
        auth.currentUser?.let { user ->
            viewModel.cargarMisProductos(user.uid)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filtrarPorTexto(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnAgregar.setOnClickListener {
            mostrarDialogoFormulario(null)
        }

        return view
    }

    private fun mostrarMenuAcciones(producto: Producto) {
        val opciones = arrayOf("Vender (-)", "Surtir (+)", "Editar Info")
        AlertDialog.Builder(requireContext())
            .setTitle(producto.nombre)
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> mostrarDialogoTransaccion(producto, "VENTA")
                    1 -> mostrarDialogoTransaccion(producto, "COMPRA")
                    2 -> mostrarDialogoFormulario(producto)
                }
            }
            .show()
    }

    private fun mostrarDialogoTransaccion(producto: Producto, tipo: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(if (tipo == "VENTA") "Registrar Venta" else "Registrar Compra")
        
        val input = EditText(requireContext())
        input.hint = "Cantidad de unidades"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        
        val container = LinearLayout(requireContext())
        container.orientation = LinearLayout.VERTICAL
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(50, 20, 50, 20)
        input.layoutParams = lp
        container.addView(input)
        builder.setView(container)

        builder.setPositiveButton("Confirmar") { _, _ ->
            val cantidad = input.text.toString().toIntOrNull() ?: 0
            if (cantidad > 0) {
                val userId = auth.currentUser?.uid ?: return@setPositiveButton
                viewModel.registrarMovimiento(producto, tipo, cantidad, userId)
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun mostrarDialogoFormulario(producto: Producto?) {
        val esEdicion = producto != null
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(if (esEdicion) "Editar Mi Producto" else "Publicar Producto")

        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 10)

        val etNombre = EditText(context).apply { 
            hint = "Nombre"
            setText(producto?.nombre ?: "")
        }
        val etPrecio = EditText(context).apply { 
            hint = "Precio"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(producto?.precio?.toString() ?: "")
        }
        val etStock = EditText(context).apply { 
            hint = "Cantidad disponible"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(producto?.stock?.toString() ?: "1")
        }
        val etImagenUrl = EditText(context).apply {
            hint = "URL de la imagen (opcional)"
            setText(producto?.imagenUrl ?: "")
        }
        val etDescripcion = EditText(context).apply { 
            hint = "Descripción (Donde entregas, etc.)"
            setText(producto?.descripcion ?: "")
        }

        layout.addView(etNombre)
        layout.addView(etPrecio)
        layout.addView(etStock)
        layout.addView(etImagenUrl)
        layout.addView(etDescripcion)
        builder.setView(layout)

        builder.setPositiveButton(if (esEdicion) "Guardar" else "Publicar") { _, _ ->
            val nombre = etNombre.text.toString()
            val precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0
            val stock = etStock.text.toString().toIntOrNull() ?: 0
            val imagenUrl = etImagenUrl.text.toString()
            val descripcion = etDescripcion.text.toString()
            val currentUser = auth.currentUser

            if (nombre.isNotEmpty() && currentUser != null) {
                if (esEdicion) {
                    val datos = mapOf(
                        "nombre" to nombre,
                        "precio" to precio,
                        "stock" to stock,
                        "imagenUrl" to imagenUrl,
                        "descripcion" to descripcion
                    )
                    viewModel.editarProducto(producto!!.id, datos)
                } else {
                    val nuevoProducto = Producto(
                        id = "", // Firebase generará el ID
                        nombre = nombre,
                        precio = precio,
                        stock = stock,
                        imagenUrl = imagenUrl,
                        descripcion = descripcion,
                        vendedorId = currentUser.uid,
                        vendedorNombre = currentUser.email?.substringBefore("@") ?: "Estudiante"
                    )
                    viewModel.publicarNuevoProducto(nuevoProducto)
                }
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun mostrarDialogoEliminar(producto: Producto) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Publicación")
            .setMessage("¿Quieres quitar '${producto.nombre}' del mercado?")
            .setPositiveButton("Sí, eliminar") { _, _ ->
                viewModel.eliminarProducto(producto.id)
            }
            .setNegativeButton("No", null)
            .show()
    }
}
