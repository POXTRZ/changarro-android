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
import androidx.recyclerview.widget.RecyclerView
import com.example.actividadmenu.viewmodel.EventosViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import android.widget.CalendarView

class EventosFragment : Fragment() {

    private lateinit var viewModel: EventosViewModel
    private lateinit var adapter: EventoAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_eventos, container, false)

        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this)[EventosViewModel::class.java]

        val rvEventos = view.findViewById<RecyclerView>(R.id.rvEventos)
        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        val fabAgregar = view.findViewById<FloatingActionButton>(R.id.fabAgregarEvento)

        adapter = EventoAdapter(emptyList())
        rvEventos.layoutManager = LinearLayoutManager(context)
        rvEventos.adapter = adapter

        // TAREA 2: Filtrado por fecha al tocar el calendario
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            viewModel.filtrarPorFecha(dayOfMonth, month + 1, year)
            Toast.makeText(context, "Eventos para el $dayOfMonth/${month + 1}/$year", Toast.LENGTH_SHORT).show()
        }

        viewModel.eventos.observe(viewLifecycleOwner) { lista ->
            adapter.updateLista(lista)
        }

        fabAgregar.setOnClickListener {
            mostrarDialogoAgregar()
        }

        return view
    }

    private fun mostrarDialogoAgregar() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Publicar Nuevo Evento")

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val etTitulo = EditText(context).apply { hint = "Título" }
        val etDesc = EditText(context).apply { hint = "Descripción" }
        val etLugar = EditText(context).apply { hint = "Lugar" }
        val etFecha = EditText(context).apply { hint = "Fecha (Ej: 25 Oct)" }

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
}
