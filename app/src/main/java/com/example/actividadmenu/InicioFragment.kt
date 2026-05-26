package com.example.actividadmenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.actividadmenu.model.Aviso
import com.example.actividadmenu.viewmodel.HomeViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

/**
 * Fragmento que representa el Muro de Avisos.
 * Utiliza HomeViewModel para observar los datos en tiempo real.
 */
class InicioFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: AvisoAdapter
    private lateinit var destacadosAdapter: ProductoMiniAdapter
    private val auth = FirebaseAuth.getInstance()
    private var miWhatsApp = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inicio, container, false)

        // 1. Instanciar el ViewModel
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // Fetch WhatsApp
        auth.currentUser?.let { user ->
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("usuarios").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    miWhatsApp = doc.getString("whatsapp") ?: ""
                }
        }

        // 2. Referencias de UI
        val rvAvisos = view.findViewById<RecyclerView>(R.id.rvAvisos)
        val rvDestacados = view.findViewById<RecyclerView>(R.id.rvDestacados)
        val etSearch = view.findViewById<android.widget.EditText>(R.id.etSearchMuro)
        val fabNuevoAviso = view.findViewById<FloatingActionButton>(R.id.fabNuevoAviso)

        // Búsqueda
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filtrarAvisos(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // 3. Configurar RecyclerView Avisos
        adapter = AvisoAdapter(emptyList()) { aviso ->
            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar publicación")
                .setMessage("¿Estás seguro de que quieres eliminar este aviso?")
                .setPositiveButton("Eliminar") { d, w -> viewModel.eliminarAviso(aviso) }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        rvAvisos.layoutManager = LinearLayoutManager(context)
        rvAvisos.adapter = adapter

        // 3.1 Configurar RecyclerView Destacados
        destacadosAdapter = ProductoMiniAdapter(emptyList()) { producto ->
            // Abrir detalle si es necesario o ir al mercado
            Toast.makeText(context, "Miremos este producto en el Mercado", Toast.LENGTH_SHORT).show()
        }
        rvDestacados.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvDestacados.adapter = destacadosAdapter

        // 4. Observar el LiveData del ViewModel
        viewModel.avisos.observe(viewLifecycleOwner) { listaAvisos ->
            adapter.updateLista(listaAvisos)
        }

        viewModel.productosDestacados.observe(viewLifecycleOwner) { lista ->
            destacadosAdapter.update(lista)
        }

        // 5. Observar errores
        viewModel.error.observe(viewLifecycleOwner) { mensaje ->
            mensaje?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }

        // 6. Configurar el FAB para publicar avisos
        fabNuevoAviso.setOnClickListener {
            val bottomSheet = PublicarAvisoBottomSheet { texto ->
                val user = auth.currentUser
                if (user != null) {
                    val nuevoAviso = Aviso(
                        autorId = user.uid,
                        autorNombre = user.displayName ?: user.email?.substringBefore("@") ?: "Estudiante",
                        autorWhatsapp = miWhatsApp,
                        contenido = texto,
                        timestamp = System.currentTimeMillis()
                    )
                    viewModel.publicarAviso(nuevoAviso) {
                        Toast.makeText(context, "¡Aviso publicado!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            bottomSheet.show(parentFragmentManager, "PublicarAvisoBottomSheet")
        }

        return view
    }
}
