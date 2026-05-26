package com.example.actividadmenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.actividadmenu.viewmodel.AvisoViewModel
import com.google.firebase.auth.FirebaseAuth

class MuroFragment : Fragment() {

    private lateinit var viewModel: AvisoViewModel
    private lateinit var adapter: AvisoAdapter
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_muro, container, false)

        // Inicializar ViewModel
        viewModel = ViewModelProvider(this)[AvisoViewModel::class.java]

        val rvAvisos = view.findViewById<RecyclerView>(R.id.rvAvisos)
        val btnNuevoAviso = view.findViewById<View>(R.id.btnNuevoAviso)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        // Configurar RecyclerView
        adapter = AvisoAdapter(emptyList())
        rvAvisos.layoutManager = LinearLayoutManager(context)
        rvAvisos.adapter = adapter

        // Observar datos del ViewModel
        viewModel.listaAvisos.observe(viewLifecycleOwner) { avisos ->
            adapter.updateLista(avisos)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.errorMostrado()
            }
        }

        btnNuevoAviso.setOnClickListener {
            mostrarDialogoNuevoAviso()
        }

        return view
    }

    private fun mostrarDialogoNuevoAviso() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Publicar Aviso")

        val input = EditText(requireContext())
        input.hint = "¿Qué quieres decir?"
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

        builder.setPositiveButton("Publicar") { _, _ ->
            val contenido = input.text.toString()
            val user = auth.currentUser
            if (user != null) {
                val userId = user.uid
                val userName = user.email?.substringBefore("@") ?: "Estudiante"
                viewModel.crearAviso(contenido, userId, userName)
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }
}
