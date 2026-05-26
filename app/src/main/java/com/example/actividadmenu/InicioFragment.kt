package com.example.actividadmenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.actividadmenu.model.Aviso
import com.example.actividadmenu.viewmodel.HomeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Fragmento que representa el Home con diseño tipo Uber/DiDi.
 */
class InicioFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: AvisoAdapter
    private lateinit var destacadosAdapter: ProductoMiniAdapter
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var miWhatsApp = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inicio, container, false)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // 1. Referencias UI
        val rvAvisos = view.findViewById<RecyclerView>(R.id.rvAvisos)
        val rvDestacados = view.findViewById<RecyclerView>(R.id.rvDestacados)
        val etSearch = view.findViewById<EditText>(R.id.etSearchMuro)
        val fabNuevoAviso = view.findViewById<ExtendedFloatingActionButton>(R.id.fabNuevoAviso)
        val tvGreeting = view.findViewById<TextView>(R.id.tvGreeting)

        val btnNavMercado = view.findViewById<View>(R.id.btnNavMercado)
        val btnNavAvisos = view.findViewById<View>(R.id.btnNavAvisos)
        val btnNavEventos = view.findViewById<View>(R.id.btnNavEventos)

        // 2. Fetch Datos de Usuario (Nombre y WhatsApp)
        auth.currentUser?.let { user ->
            db.collection("usuarios").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    val nombre = doc.getString("nombre") ?: user.displayName ?: "Estudiante"
                    tvGreeting.text = "Hola, $nombre"
                    miWhatsApp = doc.getString("whatsapp") ?: ""
                }
        }

        // 3. Configurar Navegación Rápida
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigation)
        
        btnNavMercado.setOnClickListener { bottomNav?.selectedItemId = R.id.nav_mercado }
        btnNavEventos.setOnClickListener { bottomNav?.selectedItemId = R.id.nav_calendario }
        btnNavAvisos.setOnClickListener { 
            // Ya estamos en el muro, pero scrolleamos al inicio de los avisos
            view.findViewById<androidx.core.widget.NestedScrollView>(R.id.nestedScrollInicio)?.smoothScrollTo(0, 800)
        }

        // 4. Configurar Buscador
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filtrarAvisos(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // 5. Configurar Adaptadores
        adapter = AvisoAdapter(emptyList()) { aviso ->
            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar publicación")
                .setMessage("¿Estás seguro de que quieres eliminar este aviso?")
                .setPositiveButton("Eliminar") { _, _ -> viewModel.eliminarAviso(aviso) }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        rvAvisos.layoutManager = LinearLayoutManager(context)
        rvAvisos.adapter = adapter
        rvAvisos.isNestedScrollingEnabled = false // Evita conflictos con el scroll padre

        destacadosAdapter = ProductoMiniAdapter(emptyList()) { producto ->
            Toast.makeText(context, "Detalle: ${producto.nombre}", Toast.LENGTH_SHORT).show()
        }
        rvDestacados.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvDestacados.adapter = destacadosAdapter

        // 6. Observar LiveData
        viewModel.avisos.observe(viewLifecycleOwner) { lista -> adapter.updateLista(lista) }
        viewModel.productosDestacados.observe(viewLifecycleOwner) { lista -> destacadosAdapter.update(lista) }
        viewModel.error.observe(viewLifecycleOwner) { msg -> msg?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() } }

        // 7. Botón de Publicar
        fabNuevoAviso.setOnClickListener {
            val bottomSheet = PublicarAvisoBottomSheet { texto ->
                val user = auth.currentUser
                if (user != null) {
                    val nuevoAviso = Aviso(
                        autorId = user.uid,
                        autorNombre = tvGreeting.text.toString().removePrefix("Hola, "),
                        autorWhatsapp = miWhatsApp,
                        contenido = texto,
                        timestamp = System.currentTimeMillis()
                    )
                    viewModel.publicarAviso(nuevoAviso) {
                        Toast.makeText(context, "¡Publicado!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            bottomSheet.show(parentFragmentManager, "PublicarAvisoBottomSheet")
        }

        return view
    }
}
