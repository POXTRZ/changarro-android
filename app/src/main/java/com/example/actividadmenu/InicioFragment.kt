package com.example.actividadmenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inicio, container, false)

        // 1. Instanciar el ViewModel
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // 2. Referencias de UI
        val rvAvisos = view.findViewById<RecyclerView>(R.id.rvAvisos)
        val fabNuevoAviso = view.findViewById<FloatingActionButton>(R.id.fabNuevoAviso)

        // 3. Configurar RecyclerView
        adapter = AvisoAdapter(emptyList())
        rvAvisos.layoutManager = LinearLayoutManager(context)
        rvAvisos.adapter = adapter

        // 4. Observar el LiveData del ViewModel
        viewModel.avisos.observe(viewLifecycleOwner) { listaAvisos ->
            // Actualizamos el adaptador cuando llegan nuevos datos
            adapter.updateLista(listaAvisos)
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
                        autorNombre = user.email?.substringBefore("@") ?: "Estudiante",
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
