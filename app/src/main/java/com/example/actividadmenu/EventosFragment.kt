package com.example.actividadmenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.actividadmenu.databinding.FragmentEventosBinding
import com.example.actividadmenu.viewmodel.EventosViewModel
import com.google.firebase.auth.FirebaseAuth

class EventosFragment : Fragment() {

    private var _binding: FragmentEventosBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: EventosViewModel
    private lateinit var adapter: EventoAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEventosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this)[EventosViewModel::class.java]

        setupRecyclerView()
        setupCalendar()

        viewModel.eventos.observe(viewLifecycleOwner) { lista ->
            adapter.updateLista(lista)
        }

        binding.fabAgregarEvento.setOnClickListener { mostrarDialogoAgregar() }
    }

    private fun setupRecyclerView() {
        adapter = EventoAdapter(emptyList())
        binding.rvEventos.layoutManager = LinearLayoutManager(context)
        binding.rvEventos.adapter = adapter
    }

    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            viewModel.filtrarPorFecha(dayOfMonth, month + 1, year)
            Toast.makeText(context, "Filtrando eventos: $dayOfMonth/${month + 1}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoAgregar() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Publicar Nuevo Evento")

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 10)
        }

        val etTitulo = EditText(context).apply { hint = "Título del evento" }
        val etDesc = EditText(context).apply { hint = "Descripción" }
        val etLugar = EditText(context).apply { hint = "Lugar (Ej: Auditorio)" }
        val etFecha = EditText(context).apply { hint = "Fecha legible (Ej: 25 Oct)" }

        layout.addView(etTitulo)
        layout.addView(etDesc)
        layout.addView(etLugar)
        layout.addView(etFecha)
        builder.setView(layout)

        builder.setPositiveButton("Guardar") { _, _ ->
            val titulo = etTitulo.text.toString()
            val desc = etDesc.text.toString()
            val lugar = etLugar.text.toString()
            val fecha = etFecha.text.toString()
            val userId = auth.currentUser?.uid ?: ""

            if (titulo.isNotEmpty() && fecha.isNotEmpty()) {
                viewModel.publicarEvento(titulo, desc, lugar, fecha, userId)
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
